package com.bhola.livevideochat4;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.bhola.livevideochat4.Models.GalleryModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class Fill_details extends AppCompatActivity {

    String selectedGender = "";
    EditText nickName;
    String Birthday = "";
    Button nextBtn;
    String photoUrl;
    int userId;
    private final int PROFILE_IMAGE_CODE = 222;
    boolean DP_changed = false;
    Uri ChangeDP_URI;
    String loggedAs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_details);

        loggedAs = getIntent().getStringExtra("loggedAs");
        generateUserID();


        nextBtn = findViewById(R.id.nextBtn);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nickName.getText().toString().length() > 0 && selectedGender.length() > 0 && Birthday.length() > 0) {
                    int age = new Utils().calculateAge(Birthday);
                    if (age < 18) {
                        Toast.makeText(Fill_details.this, "Under 18 not allowed", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (DP_changed) {
                        uploadImagetoFirebaseStorage(ChangeDP_URI);
                    } else {

                        saveProfileDetails();
                        Toast.makeText(Fill_details.this, "Logged In!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Fill_details.this, MainActivity.class));
                    }
                }
            }
        });
        nickName = findViewById(R.id.nickName);
        nickName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                btnStatus();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        TextView dateOfBirth = findViewById(R.id.dateOfBirth);
        CardView selectDate = findViewById(R.id.selectDate);
        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContextThemeWrapper themedContext = new ContextThemeWrapper(Fill_details.this, R.style.DatePickerDialogTheme);
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
                                dateOfBirth.setText(Birthday);
                                btnStatus();
                            }
                        },
                        2023, 0, 1  // Year, Month (0-indexed), Day
                );
                datePickerDialog.show();
            }
        });

        changeProfileImage();
        genderSelector();

        receiveIntent();


    }


    private void saveProfileDetails() {
        Intent receivedIntent = getIntent();
        String email = receivedIntent.getStringExtra("email");

        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("email", email);
        editor.putString("photoUrl", photoUrl);
        editor.putString("loginAs", loggedAs);

        editor.putString("nickName", nickName.getText().toString());
        editor.putString("Gender", selectedGender);
        editor.putString("Birthday", Birthday);
        editor.putInt("userId", userId);
        editor.putInt("coins", 0);
        editor.apply();


        UserModel userModel = new UserModel(nickName.getText().toString(), email, photoUrl, loggedAs, selectedGender, Birthday, "", "English", "", "", false, 0, userId, new java.util.Date(), "", new ArrayList<GalleryModel>(), "", false);
        MyApplication.userModel = userModel;


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users")
                .document(String.valueOf(userId))
                .set(userModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle the error if the data upload fails
                        // You can add error handling code here
                    }
                });

        //add fcm token
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                new Utils().updateProfileonFireStore("fcmToken", token);
            }
        });


        try {
            new Utils().downloadProfile_andGetURI(photoUrl, Fill_details.this);
        } catch (IOException e) {
            Log.d("SpaceError", "saveProfileDetails: " + e.getMessage());
        }

    }

    private void generateUserID() {
        Random random = new Random();
        int min = 1000000; // The minimum 7-digit number (1,000,000)
        int max = 9999999; // The maximum 7-digit number (9,999,999)

        int randomInt = random.nextInt((max - min) + 1) + min;

        String collectionPath = "Users";
        String documentId = String.valueOf(randomInt);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Task<DocumentSnapshot> documentSnapshotTask = db.collection(collectionPath).document(documentId).get();

        documentSnapshotTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                if (document.exists()) {
                    generateUserID();
                } else {
                    userId = randomInt;
                }
            } else {
                userId = randomInt;
            }
        });

    }

    private void receiveIntent() {
        Intent receivedIntent = getIntent();
        String loggedAs = receivedIntent.getStringExtra("loggedAs");
        String displayName = receivedIntent.getStringExtra("nickName");

        if (loggedAs.equals("Google")) {
            nickName.setText(displayName);
        }

    }

    private void genderSelector() {
        CardView maleCard = findViewById(R.id.maleCard);
        CardView femaleCard = findViewById(R.id.femaleCard);

        ImageView maleicon = findViewById(R.id.maleicon);
        ImageView femaleIcon = findViewById(R.id.femaleIcon);

        TextView maleText = findViewById(R.id.maleText);
        TextView femaleText = findViewById(R.id.femaleText);

        maleCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedGender.equals("male")) {
                    return;
                }

                selectedGender = "male";
                maleCard.setCardBackgroundColor(ContextCompat.getColor(Fill_details.this, R.color.themeColor));
                femaleCard.setCardBackgroundColor(ContextCompat.getColor(Fill_details.this, R.color.cardView_bg));

                maleText.setTextColor(ContextCompat.getColor(Fill_details.this, R.color.white));
                femaleText.setTextColor(ContextCompat.getColor(Fill_details.this, R.color.semiblack));

                maleicon.setColorFilter(ContextCompat.getColor(Fill_details.this, R.color.white));
                femaleIcon.setColorFilter(ContextCompat.getColor(Fill_details.this, R.color.female_icon));

                btnStatus();
            }
        });

        femaleCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedGender.equals("female")) {
                    return;
                }

                selectedGender = "female";
                femaleCard.setCardBackgroundColor(ContextCompat.getColor(Fill_details.this, R.color.themeColor));
                maleCard.setCardBackgroundColor(ContextCompat.getColor(Fill_details.this, R.color.cardView_bg));

                femaleText.setTextColor(ContextCompat.getColor(Fill_details.this, R.color.white));
                maleText.setTextColor(ContextCompat.getColor(Fill_details.this, R.color.semiblack));

                femaleIcon.setColorFilter(ContextCompat.getColor(Fill_details.this, R.color.white));
                maleicon.setColorFilter(ContextCompat.getColor(Fill_details.this, R.color.male_icon));

                btnStatus();

            }
        });


    }

    private void btnStatus() {
        if (nickName.getText().toString().length() > 0 && selectedGender.length() > 0 && Birthday.length() > 0) {
            nextBtn.setAlpha(1);
        } else {
            nextBtn.setAlpha(0.5F);
        }

    }

    private void changeProfileImage() {
        photoUrl = getIntent().getStringExtra("photoUrl");

        CircleImageView profileImage = findViewById(R.id.profileImage);
        if (photoUrl.length() != 0) {
            if (photoUrl.startsWith("http")) {
                Picasso.get().load(photoUrl).into(profileImage);
            } else {
                profileImage.setImageURI(Uri.parse(photoUrl));
            }
        }

        LinearLayout profileImageLayout = findViewById(R.id.editProfilelayout);
        profileImageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PROFILE_IMAGE_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PROFILE_IMAGE_CODE && resultCode == RESULT_OK && data != null) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1) // Specify the aspect ratio you want
                    .start(this);

        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri croppedImageUri = result.getUri();

                DP_changed = true;
                ChangeDP_URI = croppedImageUri;

                CircleImageView profileImage = findViewById(R.id.profileImage);
                profileImage.setImageURI(croppedImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                // Handle cropping error
            }
        }
    }

    private void uploadImagetoFirebaseStorage(Uri croppedImageUri) {
        Utils utils = new Utils();
        utils.showLoadingDialog(Fill_details.this, "Uploading...");
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        // Get a reference to the location where you want to store the file in Firebase Storage
        StorageReference imageRef = storageReference.child("Users/" + String.valueOf(userId) + "/profile.jpg");


        int orientation = ImageResizer.getImageOrientation(croppedImageUri, Fill_details.this);
        Bitmap bitmap = ImageResizer.imageURItoBitmap(croppedImageUri, Fill_details.this);
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
                        photoUrl = downloadUrl;

                        utils.dismissLoadingDialog();

                        saveProfileDetails();
                        Toast.makeText(Fill_details.this, "Logged In!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Fill_details.this, MainActivity.class));
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }


}