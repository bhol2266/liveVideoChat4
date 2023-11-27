package com.bhola.livevideochat4;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bhola.livevideochat4.Models.GalleryModel;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Profile2 extends AppCompatActivity {

    AlertDialog block_user_dialog = null;
    AlertDialog report_user_dialog = null;
    AlertDialog report_userSucessfully_dialog = null;
    UserModel model_profile;
    public static TextView send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);


        if (MyApplication.Ads_State.equals("active")) {
//            showAds();
        }

        setContentView(R.layout.activity_profile_girl2);

//        fullscreenMode();

        getProfileDetail();
        bindDetails();
        setImageinGridLayout();
        actionbar();


    }

    private void getProfileDetail() {
        String userModelJson = getIntent().getStringExtra("userModelJson");
        model_profile = new Gson().fromJson(userModelJson, UserModel.class); // Using Gson for JSON deserialization

    }




    private void bindDetails() {
        ImageView genderIcon = findViewById(R.id.genderIcon);

        if (model_profile.getSelectedGender().equals("male")) {
            genderIcon.setImageResource(R.drawable.male);
            int tintColor = ContextCompat.getColor(this, R.color.male_icon);
            genderIcon.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN);
        }


        ImageView profileImage = findViewById(R.id.profileImage);

        if (model_profile.getProfilepic().isEmpty()) {
            if (model_profile.getSelectedGender().equals("male")) {
                profileImage.setImageResource(R.drawable.male);
            } else {
                profileImage.setImageResource(R.drawable.female_logo);
            }
        } else {
            Picasso.get().load(model_profile.getProfilepic()).into(profileImage);
        }


        TextView profileName = findViewById(R.id.profileName);
        TextView bioTextview = findViewById(R.id.bioTextview);

        bioTextview.setText(model_profile.getEmail());
        bioTextview.setOnClickListener(view->{
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

            // Create a ClipData object to hold the text
            ClipData clipData = ClipData.newPlainText("label", model_profile.getEmail());

            // Set the ClipData on the clipboard
            clipboardManager.setPrimaryClip(clipData);
            Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();

        });
        profileName.setText(model_profile.getFullname());

        TextView age = findViewById(R.id.age);
        age.setText(String.valueOf(new Utils().calculateAge(model_profile.getBirthday())));

        TextView idTextview = findViewById(R.id.id);
        idTextview.setText(String.valueOf(model_profile.getUserId()));

        TextView country = findViewById(R.id.country);
        country.setText(model_profile.getLocation());
        if (country.length() == 0) {
            LinearLayout locationLayout = findViewById(R.id.locationLayout);
            locationLayout.setVisibility(View.GONE);
        }

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

    }



    private void setImageinGridLayout() {
        if (MyApplication.App_updating.equals("active")) {
            return;
        }
        ArrayList<Map<String, String>> imageList = new ArrayList<>();

        for (int i = 1; i < model_profile.getGalleryImages().size(); i++) {

            GalleryModel galleryModel = model_profile.getGalleryImages().get(i);
            Map<String, String> stringMap1 = new HashMap<>();
            stringMap1.put("url", galleryModel.getDownloadUrl());
            stringMap1.put("type", "premium");  //premium
            imageList.add(stringMap1);

        }


        RecyclerView recyclerView = findViewById(R.id.recyclerView); // Replace with your RecyclerView's ID
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));


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

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
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


