package com.bhola.livevideochat4;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bhola.livevideochat4.Models.ChatItem_ModelClass;
import com.bhola.livevideochat4.Models.GiftItemModel;
import com.bhola.livevideochat4.Models.Model_Profile;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Profile extends AppCompatActivity {

    ChatItem_ModelClass modelClass;
    AlertDialog block_user_dialog = null;
    AlertDialog report_user_dialog = null;
    AlertDialog report_userSucessfully_dialog = null;
    GridLayout gridLayout;
    Model_Profile model_profile;
    public static TextView send;
    boolean isOnline;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (MyApplication.Ads_State.equals("active")) {
//            showAds();
        }

        setContentView(R.layout.activity_profile_girl);

//        fullscreenMode();

        getGirlProfile_DB();
        actionbar();
        lottieGift();


    }

    private void selectBot() {

        TextView selectBotBtn = findViewById(R.id.selectBotBtn);
        TextView likeBtn = findViewById(R.id.likeBtn);

        if (model_profile.getCensored() == 1) {
            selectBotBtn.setVisibility(View.GONE);
        } else {
            likeBtn.setVisibility(View.GONE);

        }
        selectedbotBtn(selectBotBtn);
        likeButton(likeBtn);

    }

    private void selectedbotBtn(TextView selectBotBtn) {
        if (model_profile.getSelectedBot() == 0) {
            selectBotBtn.setBackgroundColor(getResources().getColor(R.color.themeColor));
            selectBotBtn.setText("Not Selected");
        } else {
            selectBotBtn.setBackgroundColor(getResources().getColor(R.color.green)); // Assumes you have a green color defined in your resources
            selectBotBtn.setText("Selected Bot");

        }

        selectBotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (model_profile.getSelectedBot() == 0) {
                    String res = new DatabaseHelper(Profile.this, MyApplication.DB_NAME, MyApplication.DB_VERSION, "GirlsProfile").selectedBot(model_profile.getUsername(), 1);

                    selectBotBtn.setBackgroundColor(getResources().getColor(R.color.green));
                    selectBotBtn.setText("Selected Bot");
                    model_profile.setSelectedBot(1);

                } else {
                    String res = new DatabaseHelper(Profile.this, MyApplication.DB_NAME, MyApplication.DB_VERSION, "GirlsProfile").selectedBot(model_profile.getUsername(), 0);

                    selectBotBtn.setBackgroundColor(getResources().getColor(R.color.themeColor)); // Assumes you have a green color defined in your resources
                    selectBotBtn.setText("Not Selected");

                    model_profile.setSelectedBot(0);

                }

            }
        });
    }

    private void likeButton(TextView likeBtn) {
        if (model_profile.getLike() == 0) {
            likeBtn.setBackgroundColor(getResources().getColor(R.color.themeColor));
            likeBtn.setText("Not liked");
        } else {
            likeBtn.setBackgroundColor(getResources().getColor(R.color.green)); // Assumes you have a green color defined in your resources
            likeBtn.setText("liked Bot");

        }

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (model_profile.getLike() == 0) {
                    String res = new DatabaseHelper(Profile.this, MyApplication.DB_NAME, MyApplication.DB_VERSION, "GirlsProfile").updateLike(model_profile.getUsername(), 1);

                    likeBtn.setBackgroundColor(getResources().getColor(R.color.green));
                    likeBtn.setText("liked Bot");
                    model_profile.setLike(1);

                } else {
                    String res = new DatabaseHelper(Profile.this, MyApplication.DB_NAME, MyApplication.DB_VERSION, "GirlsProfile").updateLike(model_profile.getUsername(), 0);

                    likeBtn.setBackgroundColor(getResources().getColor(R.color.themeColor)); // Assumes you have a green color defined in your resources
                    likeBtn.setText("Not liked");

                    model_profile.setLike(0);

                }

            }
        });
    }


    private void lottieGift() {
        LottieAnimationView lottiegift = findViewById(R.id.lottiegift);
        lottiegift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openBottomSheetDialog();
            }
        });
    }

    private void openBottomSheetDialog() {
        BottomSheetDialog bottomSheetDialog;

        bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottomsheetdialog_gifts, null);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();

        send = view.findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rechargeDialog(view.getContext());

            }
        });
        TextView coinCount = view.findViewById(R.id.coin);
        coinCount.setText(String.valueOf(MyApplication.coins));
        TextView topup = view.findViewById(R.id.topup);
        topup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Profile.this, VipMembership.class));
            }
        });
        TextView problem = findViewById(R.id.problem);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        String[] items = {"Rose", "Penghua", "TeddyBear", "Ring", "CrystalShoes", "LaserBall", "Crown", "Ferrari", "Motorcycle", "Yacht", "Bieshu", "Castle"};

        List<GiftItemModel> itemList = new ArrayList<>();

        for (int i = 0; i < items.length; i++) {
            String item = items[i];
            int coin = 99 + (i * 100); // Calculate the "coin" value based on the index

            GiftItemModel giftItemModel = new GiftItemModel(item, coin, false);
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("gift", item);
            itemMap.put("coin", coin);

            itemList.add(giftItemModel);
        }

        GiftItemAdapter giftItemAdapter = new GiftItemAdapter(Profile.this, itemList);
        GridLayoutManager layoutManager = new GridLayoutManager(Profile.this, 4);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(giftItemAdapter);

    }

    private void bindDetails() {
        ImageView profileImage = findViewById(R.id.profileImage);
        Picasso.get().load(model_profile.getProfilePhoto()).into(profileImage);

        TextView id = findViewById(R.id.id);
        id.setText(convertUsernameto_number(model_profile.getUsername()));
        TextView profileName = findViewById(R.id.profileName);
        profileName.setText(model_profile.getName());

        TextView age = findViewById(R.id.age);
        age.setText(model_profile.getAge());

        TextView country = findViewById(R.id.country);
        country.setText(model_profile.getFrom());


        CardView voiceCall = findViewById(R.id.voiceCall);
        voiceCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rechargeDialog(view.getContext());
            }
        });

        LottieAnimationView videoCall = findViewById(R.id.videoCall);
        videoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rechargeDialog(view.getContext());

            }
        });

        CardView chat = findViewById(R.id.chat);
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = view.getContext().getSharedPreferences("UserInfo", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("userName", model_profile.getUsername());

                editor.apply(); // Apply the changes to SharedPreferences

                Intent intent = new Intent(Profile.this, ChatScreen_User.class);
                intent.putExtra("online", isOnline);
                startActivity(intent);
            }
        });

        TextView Languages = findViewById(R.id.Languages);
        Languages.setText(model_profile.getLanguages());
        LinearLayout Languageslayout = findViewById(R.id.Languageslayout);
        if (model_profile.getLanguages().length() == 0) {
            Languageslayout.setVisibility(View.GONE);
        }

        TextView InterestedIn = findViewById(R.id.InterestedIn);
        InterestedIn.setText(model_profile.getInterested());
        LinearLayout InterestedInlayout = findViewById(R.id.InterestedInlayout);
        if (model_profile.getInterested().length() == 0) {
            InterestedInlayout.setVisibility(View.GONE);
        }

        TextView BodyType = findViewById(R.id.BodyType);
        BodyType.setText(model_profile.getBodyType());
        LinearLayout BodyTypelayout = findViewById(R.id.BodyTypelayout);
        if (model_profile.getBodyType().length() == 0) {
            BodyTypelayout.setVisibility(View.GONE);
        }
        TextView Specifics = findViewById(R.id.Specifics);
        Specifics.setText(model_profile.getSpecifics());
        LinearLayout Specificslayout = findViewById(R.id.Specificslayout);
        if (model_profile.getSpecifics().length() == 0 || MyApplication.App_updating.equals("active") || !MyApplication.userLoggedIAs.equals("Google")) {
            Specificslayout.setVisibility(View.GONE);
        }

        TextView Ethnicity = findViewById(R.id.Ethnicity);
        Ethnicity.setText(model_profile.getEthnicity());
        LinearLayout Ethnicitylayout = findViewById(R.id.Ethnicitylayout);
        if (model_profile.getEthnicity().length() == 0) {
            Ethnicitylayout.setVisibility(View.GONE);
        }


        TextView Hair = findViewById(R.id.Hair);
        Hair.setText(model_profile.getHair());
        Ethnicity.setText(model_profile.getEthnicity());
        LinearLayout Hairlayout = findViewById(R.id.Hairlayout);
        if (model_profile.getHair().length() == 0) {
            Hairlayout.setVisibility(View.GONE);
        }

        TextView EyeColor = findViewById(R.id.EyeColor);
        EyeColor.setText(model_profile.getEyeColor());
        LinearLayout EyeColorlayout = findViewById(R.id.EyeColorlayout);
        if (model_profile.getEyeColor().length() == 0) {
            EyeColorlayout.setVisibility(View.GONE);
        }

        TextView Subculture = findViewById(R.id.Subculture);
        Subculture.setText(model_profile.getSubculture());
        LinearLayout Subculturelayout = findViewById(R.id.Subculturelayout);
        if (model_profile.getSubculture().length() == 0) {
            Subculturelayout.setVisibility(View.GONE);
        }

        LinearLayout onlineLayout = findViewById(R.id.onlineLayout);
         isOnline = getIntent().getBooleanExtra("online", false);
        if (isOnline) {
            onlineLayout.setVisibility(View.VISIBLE);
        }

    }

    private String convertUsernameto_number(String username) {

        long numericValue = 0;

        // Convert letters to their numeric values based on position in the alphabet
        for (char c : username.toCharArray()) {
            if (Character.isLetter(c)) {
                char lowercaseChar = Character.toLowerCase(c);
                int position = lowercaseChar - 'a' + 1;
                numericValue = numericValue * 26 + position;
            }
        }

        // Extract numeric digits and append them to the numeric value
        for (char c : username.toCharArray()) {
            if (Character.isDigit(c)) {
                int digitValue = Character.getNumericValue(c);
                numericValue = numericValue * 10 + digitValue;
            }
        }


        String stringValue = Long.toString(numericValue);
        // Check if the string has more than 6 digits
        if (stringValue.length() > 6) {
            // Truncate the string to 6 digits
            stringValue = stringValue.substring(0, 6);
        }
        return stringValue.replace("-", "");
    }


    private void actionbar() {
        ImageView backArrow = findViewById(R.id.backArrow);
        ImageView warningSign = findViewById(R.id.warningSign);
        ImageView menuDots = findViewById(R.id.menuDots);


        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        warningSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blockUserDialog();
            }
        });

        menuDots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportUserDialog();
            }
        });


    }

    private void blockUserDialog() {

        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Profile.this);
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View promptView = inflater.inflate(R.layout.dialog_block_user, null);
        builder.setView(promptView);
        builder.setCancelable(true);

        TextView confirm = promptView.findViewById(R.id.confirm);
        TextView cancel = promptView.findViewById(R.id.cancel);


        block_user_dialog = builder.create();
        block_user_dialog.show();


        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Profile.this, "User blocked succesfully", Toast.LENGTH_SHORT).show();
                block_user_dialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                block_user_dialog.dismiss();
            }
        });

        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        block_user_dialog.getWindow().setBackgroundDrawable(inset);

    }

    private void reportUserDialog() {

        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Profile.this);
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View promptView = inflater.inflate(R.layout.dialog_report_user, null);
        builder.setView(promptView);
        builder.setCancelable(true);

        TextView report = promptView.findViewById(R.id.reportBtn);
        ImageView cross = promptView.findViewById(R.id.cross);


        report_user_dialog = builder.create();
        report_user_dialog.show();


        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                report_user_dialog.dismiss();
                reportUserSucessfullDialog();
            }
        });

        cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                report_user_dialog.dismiss();
            }
        });


        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        report_user_dialog.getWindow().setBackgroundDrawable(inset);

    }

    private void reportUserSucessfullDialog() {

        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Profile.this);
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View promptView = inflater.inflate(R.layout.dialog_report_user_sucessfull, null);
        builder.setView(promptView);
        builder.setCancelable(true);

        TextView confirm = promptView.findViewById(R.id.confirm);


        report_userSucessfully_dialog = builder.create();
        report_userSucessfully_dialog.show();


        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Profile.this, "User Reported", Toast.LENGTH_SHORT).show();
                report_userSucessfully_dialog.dismiss();
            }
        });


        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        report_userSucessfully_dialog.getWindow().setBackgroundDrawable(inset);

    }

    private void getGirlProfile_DB() {

        String userName = getIntent().getStringExtra("userName");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = new DatabaseHelper(Profile.this, MyApplication.DB_NAME, MyApplication.DB_VERSION, "GirlsProfile").readSingleGirl(userName);
                if (cursor.moveToFirst()) {

                    model_profile = Utils.readCursor(cursor);
                }
                cursor.close();
                ((Activity) Profile.this).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                bindDetails();
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    // this is because when sdk is lower the blur effect on ImageView not working
                                    setImageinGridLayout();
                                }
                                selectBot();

                            }
                        }, 200);

                    }
                });
            }
        }).start();


    }


    private void setImageinGridLayout() {
        if (MyApplication.App_updating.equals("active")) {
            return;
        }
        ArrayList<Map<String, String>> imageList = new ArrayList<>();

        for (int i = 0; i < model_profile.getImages().size(); i++) {
            Map<String, String> stringMap1 = new HashMap<>();
            stringMap1.put("url", model_profile.getImages().get(i).replace("thumb", "full"));
            stringMap1.put("type", "premium");  //premium
            imageList.add(stringMap1);
        }

//        for (int i = 0; i < model_profile.getVideos().size(); i++) {
//            Map<String, String> stringMap1 = new HashMap<>();
//            stringMap1.put("url", model_profile.getVideos().get(i).get("imageUrl"));
//            stringMap1.put("type", "free");  //premium
//            imageList.add(stringMap1);
//        }

        RecyclerView recyclerView = findViewById(R.id.recyclerView); // Replace with your RecyclerView's ID
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));

        // Populate imageList with your image data

        ProfileGirlImageAdapter imageAdapter = new ProfileGirlImageAdapter(this, imageList);
        recyclerView.setAdapter(imageAdapter);

        int originalScreenWidth = getResources().getDisplayMetrics().widthPixels;

        // Decrease the screen width by 15%
        int screenWidth = (int) (originalScreenWidth * 0.85);
//        int cardViewWidth = screenWidth / numColumns;

    }


    private void fullscreenMode() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat windowInsetsCompat = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        windowInsetsCompat.hide(WindowInsetsCompat.Type.statusBars());
        windowInsetsCompat.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
    }

    private void showAds() {
        if (MyApplication.Ad_Network_Name.equals("admob")) {
            ADS_ADMOB.Interstitial_Ad(this);
        } else {
            com.facebook.ads.InterstitialAd facebook_IntertitialAds = null;
            ADS_FACEBOOK.interstitialAd(this, facebook_IntertitialAds, getString(R.string.Facebook_InterstitialAdUnit));
        }
    }

    public static void rechargeDialog(Context context) {

        AlertDialog recharge_dialog = null;

        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View promptView = inflater.inflate(R.layout.dialog_recharge, null);
        builder.setView(promptView);
        builder.setCancelable(true);

        TextView recharge = promptView.findViewById(R.id.recharge);
        TextView cancel = promptView.findViewById(R.id.cancel);


        recharge_dialog = builder.create();
        recharge_dialog.show();


        recharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, VipMembership.class));
            }
        });

        AlertDialog finalRecharge_dialog = recharge_dialog;
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalRecharge_dialog.dismiss();
            }
        });

        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        recharge_dialog.getWindow().setBackgroundDrawable(inset);

    }

}


class ProfileGirlImageAdapter extends RecyclerView.Adapter<ProfileGirlImageAdapter.ImageViewHolder> {
    private final Context context;
    private final List<Map<String, String>> imageList;

    public ProfileGirlImageAdapter(Context context, List<Map<String, String>> imageList) {
        this.context = context;
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_image_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Map<String, String> imageItem = imageList.get(position);
//        holder.bind(imageItem);

        Picasso.get().load(imageItem.get("url")).fit().centerCrop() // Set the width in pixels and let Picasso calculate the height
                .into(holder.imageView);

        int widthInPixels = holder.imageView.getWidth(); // Get the current width
        int heightInPixels = (int) (widthInPixels * 3.5 / 4); // Calculate the height


        if (MyApplication.coins == 0) {

            if (imageItem.get("type").equals("premium")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    holder.imageView.setRenderEffect(RenderEffect.createBlurEffect(40, 40, Shader.TileMode.MIRROR));
                } else {

                }
            }
        } else {
            holder.vipText.setVisibility(View.GONE);
        }

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DisplayMetrics displayMetrics = new DisplayMetrics();
                ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int originalScreenWidth = displayMetrics.widthPixels;
                int screenHeight = displayMetrics.heightPixels;


                // Decrease the screen width by 15%
                int screenWidth = (int) (originalScreenWidth * 0.85);

                FragmentManager fragmentManager = ((Activity) context).getFragmentManager();

                Fragment_LargePhotoViewer fragment = Fragment_LargePhotoViewer.newInstance(context, (ArrayList<Map<String, String>>) imageList, holder.getAbsoluteAdapterPosition(), screenWidth, screenHeight);
                fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment) // Replace with your container ID
                        .addToBackStack(null) // Optional, for back navigation
                        .commit();
            }
        });
    }


    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final ImageView imageView;
        TextView vipText;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            imageView = itemView.findViewById(R.id.imageView);
            vipText = itemView.findViewById(R.id.vipText);
        }

    }
}


