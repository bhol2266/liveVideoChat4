package com.bhola.livevideochat4;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bhola.livevideochat4.Models.GalleryModel;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileEdit extends AppCompatActivity {

    private final int GALLERY_REQUEST_CODE = 111;

    List<GalleryModel> galleryImages;
    private final int PROFILE_IMAGE_CODE = 222;
    private int currentCroppingAction = 0; // Initialize to 0 this is for CropImage resultAcitivty to distinguish between gallery image or profile image
    CircleImageView profileImage;
    public static String photoUrl;
    GalleryImageAdapter galleryImageAdapter;
    String nickName, Gender, Birthday, Bio;
    public static String Language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_edit);

        galleryImages = new ArrayList<>();
        retreive_Userinfo();


        changeProfileImage();
        loadImagesFromGalley();
        ImageView deleteIcon = findViewById(R.id.deleteIcon);

        deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(UserProfileEdit.this, "Long Press image to delete", Toast.LENGTH_SHORT).show();
            }
        });

        profileDetails();

        ImageView backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView languageTextView = findViewById(R.id.language);
        languageTextView.setText(Language);
    }

    private void profileDetails() {
        TextView birthdayTevtview = findViewById(R.id.birthdayTevtview);
        TextView genderTextview = findViewById(R.id.genderTextview);
        genderTextview.setText(Gender);
        birthdayTevtview.setText(Birthday);


        RelativeLayout birthdayLayout = findViewById(R.id.birthdayLayout);
        birthdayLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContextThemeWrapper themedContext = new ContextThemeWrapper(UserProfileEdit.this, R.style.DatePickerDialogTheme);
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        themedContext,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                // Format the month and day with zero padding if needed
                                String formattedMonth = String.format("%02d", month + 1);
                                String formattedDay = String.format("%02d", dayOfMonth);

                                // Handle the selected date
                                Birthday = year + "-" + formattedMonth + "-" + formattedDay;
                                birthdayTevtview.setText(Birthday);
                                new Utils().updateProfileonFireStore("birthday",Birthday);

                                save_userInfo_alldetails();
                            }
                        },
                        2023, 0, 1  // Year, Month (0-indexed), Day
                );
                datePickerDialog.show();
            }
        });

        TextView languageTextview = findViewById(R.id.language);

        languageTextview.setText(Language);

        RelativeLayout languageLayout = findViewById(R.id.languageLayout);
        languageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserProfileEdit.this, Acitivity_LanguageSeletor.class));
            }
        });

        TextView nickNameTextview = findViewById(R.id.nickName);
        nickNameTextview.setText(nickName);

        RelativeLayout nickNameLayout = findViewById(R.id.nickNameLayout);
        nickNameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openEditDialog("nickName");
            }
        });

        RelativeLayout bioLayout = findViewById(R.id.bioLayout);
        bioLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openEditDialog("Bio");
            }
        });


    }

    private void openEditDialog(String type) {

        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(UserProfileEdit.this);
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View promptView = inflater.inflate(R.layout.dialog_nickname_bio_edit, null);
        builder.setView(promptView);
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();

        TextView title = promptView.findViewById(R.id.title);
        if (type.equals("Bio")) {
            title.setText("Edit Bio");
        } else {
            title.setText("Edit Nickname");
        }


        LinearLayout infolayout = promptView.findViewById(R.id.infolayout);
        infolayout.setVisibility(View.INVISIBLE);
        EditText nickNameEdit = promptView.findViewById(R.id.nickName);
        nickNameEdit.setText(nickName);
        nickNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() >= 3) {
                    infolayout.setVisibility(View.INVISIBLE);
                } else {
                    infolayout.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        EditText textArea = promptView.findViewById(R.id.textArea);
        textArea.setText(Bio);


        if (type.equals("Bio")) {
            CardView nickNameCard = promptView.findViewById(R.id.nickNameCard);
            nickNameCard.setVisibility(View.GONE);
            infolayout.setVisibility(View.GONE);

        } else {
            CardView textareaCard = promptView.findViewById(R.id.textareaCard);
            textareaCard.setVisibility(View.GONE);
        }

        TextView saveBtn = promptView.findViewById(R.id.save);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (type.equals("Bio")) {
                    editor.putString("Bio", textArea.getText().toString());
                    Bio = textArea.getText().toString();
                    new Utils().updateProfileonFireStore("bio",Bio);

                } else {
                    if (nickNameEdit.getText().toString().length() < 3) {
                        return;
                    }
                    TextView nickNameTextView = findViewById(R.id.nickName);
                    nickNameTextView.setText(nickNameEdit.getText().toString());
                    editor.putString("nickName", nickNameEdit.getText().toString());
                    nickName = nickNameEdit.getText().toString();
                    new Utils().updateProfileonFireStore("fullname",nickName);

                }
                editor.apply();

                Toast.makeText(UserProfileEdit.this, "Saved", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();


        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        dialog.getWindow().setBackgroundDrawable(inset);

    }

    private void changeProfileImage() {
        profileImage = findViewById(R.id.profileImage);
        if (photoUrl.length() != 0) {
            if (photoUrl.startsWith("http")) {
                Picasso.get().load(photoUrl).into(profileImage);
            } else {
                profileImage.setImageURI(Uri.parse(photoUrl));
            }
        } else {
            if (Gender.equals("female")) {
                profileImage.setImageResource(R.drawable.female_logo);
            }
        }
        LinearLayout profileImageLayout = findViewById(R.id.profileImageLayout);
        profileImageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PROFILE_IMAGE_CODE);
            }
        });
    }

    private void loadImagesFromGalley() {

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(UserProfileEdit.this, 4);
        recyclerView.setLayoutManager(layoutManager);
        galleryImageAdapter = new GalleryImageAdapter(UserProfileEdit.this, galleryImages);
        recyclerView.setAdapter(galleryImageAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1) // Specify the aspect ratio you want
                    .start(this);
            currentCroppingAction = GALLERY_REQUEST_CODE;
        } else if (requestCode == PROFILE_IMAGE_CODE && resultCode == RESULT_OK && data != null) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1) // Specify the aspect ratio you want
                    .start(this);
            currentCroppingAction = PROFILE_IMAGE_CODE;


        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri croppedImageUri = result.getUri();

                // Create Image directory to internal storage if doesn't exist
                File internalStorage = new File(getFilesDir(), "images");
                if (!internalStorage.exists()) {
                    internalStorage.mkdir();
                }
                if (currentCroppingAction == GALLERY_REQUEST_CODE) {

                    File destinationFile = new File(internalStorage, System.currentTimeMillis() + ".jpg");
                    Uri copiedImageUri = Uri.fromFile(destinationFile);

                    try {
                        saveCroppedImage(croppedImageUri.getPath(), destinationFile.getPath());
                    } catch (IOException e) {
                    }

                    try {
                        uploadImagetoFirebaseStorageGallery(copiedImageUri);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }


                } else {

                    File destinationFile = new File(internalStorage, "profile.jpg");
                    Uri copiedImageUri = Uri.fromFile(destinationFile);

                    try {
                        saveCroppedImage(croppedImageUri.getPath(), destinationFile.getPath());
                    } catch (IOException e) {
                        Log.d("onBindViewHolder", "onActivityResult: "+e.getMessage());
                    }
                    //Resize image



                    uploadImagetoFirebaseStorageProfile(copiedImageUri, "Users/" + String.valueOf(MyApplication.userModel.getUserId()) + "/profile.jpg");


                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                // Handle cropping error
            }
        }
    }

    private void uploadImagetoFirebaseStorageGallery(Uri croppedImageUri) throws IOException {


        Utils utils = new Utils();
        utils.showLoadingDialog(UserProfileEdit.this, "Uploading...");
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        long currentTimeMillis = System.currentTimeMillis();
        String currentTimeString = Long.toString(currentTimeMillis);
        String path = "Users/" + String.valueOf(MyApplication.userModel.getUserId()) + "/gallery/" + currentTimeString + ".jpg";
        StorageReference imageRef = storageReference.child(path);


        int orientation = ImageResizer.getImageOrientation(croppedImageUri,UserProfileEdit.this);
        Bitmap bitmap=ImageResizer.imageURItoBitmap(croppedImageUri,UserProfileEdit.this);
        Bitmap rotatedBitmap = ImageResizer.rotateBitmap(bitmap, orientation);

        Bitmap redusedBitmap = ImageResizer.reduceBitmapSize(rotatedBitmap, 400000);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        redusedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();


// Upload the file to Firebase Storage
        imageRef.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> {
                    // File uploaded successfully
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();

                        GalleryModel galleryModel = new GalleryModel(downloadUrl, croppedImageUri.toString(), path, currentTimeString + ".jpg");

                        galleryImages.add(1, galleryModel);
                        galleryImageAdapter.notifyItemInserted(1);

                        updateGalleryonFireStore();

                        save_userInfo_gallery(UserProfileEdit.this, galleryImages);
                        utils.dismissLoadingDialog();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateGalleryonFireStore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("Users");
        String userId = String.valueOf(MyApplication.userModel.getUserId()); // Replace with the actual user ID
        DocumentReference userDocRef = usersRef.document(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("galleryImages", galleryImages);

        userDocRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    // The field(s) were successfully updated
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that might occur during the update
                });

    }

    private void uploadImagetoFirebaseStorageProfile(Uri croppedImageUri, String path) {
        Utils utils = new Utils();
        utils.showLoadingDialog(UserProfileEdit.this, "Uploading...");
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        int orientation = ImageResizer.getImageOrientation(croppedImageUri,UserProfileEdit.this);
        Bitmap bitmap=ImageResizer.imageURItoBitmap(croppedImageUri,UserProfileEdit.this);
        Bitmap rotatedBitmap = ImageResizer.rotateBitmap(bitmap, orientation);

        Bitmap redusedBitmap = ImageResizer.reduceBitmapSize(rotatedBitmap, 400000);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        redusedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();



        StorageReference imageRef = storageReference.child(path);

// Upload the file to Firebase Storage
        imageRef.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> {
                    // File uploaded successfully
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();

                        profileImage.setImageURI(croppedImageUri);
                        photoUrl = downloadUrl;

                        try {
                            new Utils().downloadProfile_andGetURI(photoUrl,UserProfileEdit.this);
                        } catch (IOException e) {
                            Log.d("SpaceError", "saveProfileDetails: "+e.getMessage());
                        }

                        new Utils().updateProfileonFireStore("profilepic",photoUrl);

                        save_userInfo_alldetails();
                        utils.dismissLoadingDialog();

                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }




    private void saveCroppedImage(String sourcePath, String destinationPath) throws IOException {
        File sourceFile = new File(sourcePath);
        File destinationFile = new File(destinationPath);

        FileInputStream inputStream = new FileInputStream(sourceFile);
        FileOutputStream outputStream = new FileOutputStream(destinationFile);
        FileChannel inChannel = inputStream.getChannel();
        FileChannel outChannel = outputStream.getChannel();

        inChannel.transferTo(0, inChannel.size(), outChannel);

        inputStream.close();
        outputStream.close();
    }


    public static void save_userInfo_gallery(Context context, List<GalleryModel> itemList) {

        SharedPreferences sharedPreferences = context.getSharedPreferences("UserInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(itemList);
        editor.putString("galleryImages", json);
        editor.apply();
    }

    public void save_userInfo_alldetails() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("nickName", nickName);
        editor.putString("Gender", Gender);
        editor.putString("photoUrl", photoUrl);
        editor.putString("Birthday", Birthday);
        editor.putString("Bio", Bio);
        editor.apply();
    }

    public void retreive_Userinfo() {

        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        nickName = sharedPreferences.getString("nickName", "not set");
        Gender = sharedPreferences.getString("Gender", "not set");
        Birthday = sharedPreferences.getString("Birthday", "not set");
        Bio = sharedPreferences.getString("Bio", "");
        photoUrl = sharedPreferences.getString("photoUrl", "");
        Language = sharedPreferences.getString("Language", "");


        String json = sharedPreferences.getString("galleryImages", "");
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<GalleryModel>>() {
        }.getType();
        if (json.length() > 0) {
            galleryImages = gson.fromJson(json, type);

        } else {

            galleryImages.add(0, new GalleryModel("","","",""));
        }

    }


    public static void dialog_uploadImage_waring(Context context) {

        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View promptView = inflater.inflate(R.layout.dialog_upload_image_profile, null);
        builder.setView(promptView);
        builder.setCancelable(true);

        TextView upload = promptView.findViewById(R.id.upload);


        AlertDialog uploadUserWaring = builder.create();
        uploadUserWaring.show();


        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                ((Activity) context).startActivityForResult(intent, 111);
                uploadUserWaring.dismiss();

            }
        });


        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        uploadUserWaring.getWindow().setBackgroundDrawable(inset);

        Window window = uploadUserWaring.getWindow();

// Set the gravity of the dialog
        if (window != null) {
            window.setGravity(Gravity.BOTTOM); // Set the gravity to bottom
        }

    }


    private void dialog_upload_successfull() {

        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(UserProfileEdit.this);
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View promptView = inflater.inflate(R.layout.dialog_image_upload_sucess, null);
        builder.setView(promptView);
        builder.setCancelable(true);

        TextView ok = promptView.findViewById(R.id.ok);


        AlertDialog dialog = builder.create();
        dialog.show();

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });


        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        dialog.getWindow().setBackgroundDrawable(inset);

    }


    @Override
    public void onBackPressed() {
        // Get the fragment manager
        FragmentManager fragmentManager = ((Activity) UserProfileEdit.this).getFragmentManager();

        // Check if there are any fragments in the back stack
        if (fragmentManager.getBackStackEntryCount() > 0) {
            // If there are fragments in the back stack, pop the top one (close it)
            fragmentManager.popBackStack();
        } else {
            // If there are no fragments in the back stack, perform the default back button behavior
            super.onBackPressed();
        }
    }

    public void reflectChangesBtn(View view) {
        startActivity(new Intent(UserProfileEdit.this, MyApplication.class));
    }
}

class GalleryImageAdapter extends RecyclerView.Adapter<GalleryImageAdapter.ImageViewHolder> {
    private final Context context;
    private final List<GalleryModel> imageList;

    public GalleryImageAdapter(Context context, List<GalleryModel> imageList) {
        this.context = context;
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_image_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        GalleryModel galleryModel = imageList.get(position);


        if (position != 0) {
            holder.imageview.setImageURI(Uri.parse(galleryModel.getImage_uri()));
            holder.loadImage.setVisibility(View.GONE);
            holder.imageview.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
                    LayoutInflater inflater = LayoutInflater.from(context);
                    View promptView = inflater.inflate(R.layout.dialog_delete_gallery_image, null);
                    builder.setView(promptView);
                    builder.setCancelable(true);

                    TextView cancel = promptView.findViewById(R.id.cancel);
                    TextView confirm = promptView.findViewById(R.id.confirm);


                    AlertDialog dialog = builder.create();
                    dialog.show();

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });


                    confirm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            for (int i = 0; i < imageList.size(); i++) {
                                GalleryModel galleryModel1 = imageList.get(i);
                                if (galleryModel.getImage_uri().equals(galleryModel1.getImage_uri())) {

                                    removeFromFirebaseStorage(galleryModel.getImagePathFirebaseStorage(), i);

                                }
                            }

                            dialog.dismiss();
                        }
                    });


                    ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
                    InsetDrawable inset = new InsetDrawable(back, 20);
                    dialog.getWindow().setBackgroundDrawable(inset);
                    return false;
                }
            });

            holder.imageview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ArrayList<Map<String, String>> imageListt = new ArrayList<>();

                    for (int i = 1; i < imageList.size(); i++) {
                        GalleryModel galleryModel1 = imageList.get(i);

                        Map<String, String> stringMap2 = new HashMap<>();
                        stringMap2.put("url", galleryModel1.getImage_uri());
                        stringMap2.put("type", "free");
                        imageListt.add(stringMap2);
                    }

                    int index = 0;
                    for (int i = 0; i < imageList.size(); i++) {
                        GalleryModel galleryModel1 = imageList.get(i);

                        if (galleryModel.getImage_uri().equals(galleryModel1.getImage_uri())) {
                            index = i;
                        }
                    }


                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int originalScreenWidth = displayMetrics.widthPixels;
                    int screenHeight = displayMetrics.heightPixels;


                    // Decrease the screen width by 15%
                    int screenWidth = (int) (originalScreenWidth * 0.85);
                    Fragment_LargePhotoViewer fragment = Fragment_LargePhotoViewer.newInstance(context, (ArrayList<Map<String, String>>) imageListt, index, screenWidth, screenHeight);

                    FragmentManager fragmentManager = ((Activity) context).getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment) // Replace with your container ID
                            .addToBackStack(null) // Optional, for back navigation
                            .commit();
                }
            });
        } else {
            holder.loadImage.setVisibility(View.VISIBLE);
            holder.loadImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (imageList.size() >= 8) {
                        Toast.makeText(context, "Maximum Limit reached", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    UserProfileEdit.dialog_uploadImage_waring(view.getContext());
                }
            });
        }

    }

    private void removeFromFirebaseStorage(String imagePath, int i) {
        Utils utils = new Utils();
        utils.showLoadingDialog(context, "deleting...");
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        StorageReference imageRef = storageRef.child(imagePath);
        Log.d("imagePath", "imagePath: " + imageRef);
        // Delete the image
        imageRef.delete()
                .addOnSuccessListener(aVoid -> {
                    imageList.remove(i);
                    notifyItemRemoved(i);
                    UserProfileEdit.save_userInfo_gallery(context, imageList);

                    updateOnFirestore(imageList);
                    Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                    utils.dismissLoadingDialog();

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Failed to delete the image
                    // You can add code here to handle the failure case
                });


    }

    private void updateOnFirestore(List<GalleryModel> imageList) {


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("Users");
        String userId = String.valueOf(MyApplication.userModel.getUserId()); // Replace with the actual user ID
        DocumentReference userDocRef = usersRef.document(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("galleryImages", imageList);

        userDocRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    // The field(s) were successfully updated
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that might occur during the update
                });
    }


    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        private final CardView loadImage;
        ShapeableImageView imageview;
        TextView vipText;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            loadImage = itemView.findViewById(R.id.loadImage);
            imageview = itemView.findViewById(R.id.customImageView);
        }

    }


}

