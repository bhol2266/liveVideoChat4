package com.bhola.livevideochat4;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.bhola.livevideochat4.Models.ChatItem_ModelClass;
import com.bhola.livevideochat4.Models.Model_Profile;
import com.bhola.livevideochat4.Models.UserBotMsg;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    public static TextView badge_text;
    public static int unreadMessage_count;
    public static ViewPager2 viewPager2;
    com.facebook.ads.InterstitialAd facebook_IntertitialAds;
    private InAppUpdate inAppUpdate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

//        fullscreenMode();
        checkForupdate();
        try {
            // this is check if user data is available or not , if not start splashscreen
            String userId = String.valueOf(MyApplication.userModel.getUserId());
        } catch (Exception e) {
            startActivity(new Intent(MainActivity.this, SplashScreen.class));
            return;
        }


        if (MyApplication.Ads_State.equals("active")) {
            showAds();
        }


        initializeBottonFragments();
        startIncomingCallService();


    }

    private void startIncomingCallService() {
        if (MyApplication.App_updating.equals("active")) {
            return;
        }
//        Intent intent = new Intent(MainActivity.this, IncomingCallService.class);
//        startService(intent);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                showFragment();
            }
        }, 20000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showFragment();
            }
        }, 90000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showFragment();
            }
        }, 210000);

    }

    private void showFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (!fragmentManager.isDestroyed()) {
            try {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                Fragment_Calling fragment = new Fragment_Calling();

                Model_Profile randomgirl = Utils.getSingleRandomGirlVideoObject(MainActivity.this);

                Bundle args = new Bundle();
                args.putString("name", randomgirl.getUsername());
                args.putString("profile", randomgirl.getProfilePhoto());
                args.putString("username", randomgirl.getUsername());
                fragment.setArguments(args);
                fragmentTransaction.replace(R.id.fragment_container, fragment);
                fragmentTransaction.commit();
            } catch (Exception e) {

            }
        }


    }


    private void initializeBottonFragments() {
        viewPager2 = findViewById(R.id.viewpager);
        viewPager2.setAdapter(new PagerAdapter(MainActivity.this));

        viewPager2.setOffscreenPageLimit(5);
        TabLayout tabLayout = findViewById(R.id.tabLayout);


        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {

                    case 0:
                        tab.setIcon(R.drawable.trending);
                        View view5 = getLayoutInflater().inflate(R.layout.customtab, null);
                        view5.findViewById(R.id.icon).setBackgroundResource(R.drawable.trending);
                        tab.setCustomView(view5);

                        //By default tab 0 will be selected to change the tint of that tab
                        View tabView = tab.getCustomView();
                        ImageView tabIcon = tabView.findViewById(R.id.icon);
                        tabIcon.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.themeColor));

                        break;
                    case 1:
                        tab.setIcon(R.drawable.videocall2);

                        View view1 = getLayoutInflater().inflate(R.layout.customtab, null);
                        view1.findViewById(R.id.icon).setBackgroundResource(R.drawable.videocall2);
                        tab.setCustomView(view1);

                        break;
                    case 2:
                        tab.setIcon(R.drawable.chat);


                        View view2 = getLayoutInflater().inflate(R.layout.customtab, null);
                        view2.findViewById(R.id.icon).setBackgroundResource(R.drawable.chat);
                        tab.setCustomView(view2);
                        unreadMessage_count = getUndreadMessage_Count();

                        badge_text = view2.findViewById(R.id.badge_text);
                        badge_text.setVisibility(View.GONE);

                        if (!Fragment_Messenger.retreive_sharedPreferences(MainActivity.this)) {
                            //logged in first time
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //First time
                                    badge_text.setVisibility(View.VISIBLE);
                                    badge_text.setText("1");
                                    badge_text.setBackgroundResource(R.drawable.badge_background);
//                                    MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.message_received);
//                                    mediaPlayer.start();

                                }
                            }, 20000);

                        } else {
                            if (unreadMessage_count != 0) {
                                badge_text.setVisibility(View.VISIBLE);
                                badge_text.setText(String.valueOf(unreadMessage_count));
                                badge_text.setBackgroundResource(R.drawable.badge_background);

                            } else {
                                badge_text.setVisibility(View.GONE);
                            }
                        }

                        break;


                    case 3:
                        tab.setIcon(R.drawable.info_2);


                        View view3 = getLayoutInflater().inflate(R.layout.customtab, null);
                        view3.findViewById(R.id.icon).setBackgroundResource(R.drawable.info_2);
                        tab.setCustomView(view3);
                        break;


                    default:
                        tab.setIcon(R.drawable.user2);
                        View view4 = getLayoutInflater().inflate(R.layout.customtab, null);
                        view4.findViewById(R.id.icon).setBackgroundResource(R.drawable.user2);
                        tab.setCustomView(view4);
                        break;
                }
            }
        });
        tabLayoutMediator.attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Get the custom view of the selected tab
                View tabView = tab.getCustomView();
                if (tabView != null) {
                    // Find the ImageView in the custom view
                    ImageView tabIcon = tabView.findViewById(R.id.icon);

                    // Set the background tint color for the selected tab
                    tabIcon.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.themeColor));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Get the custom view of the unselected tab
                View tabView = tab.getCustomView();
                if (tabView != null) {
                    // Find the ImageView in the custom view
                    ImageView tabIcon = tabView.findViewById(R.id.icon);

                    // Set the background tint color for the unselected tab
                    tabIcon.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, com.google.android.ads.mediationtestsuite.R.color.gmts_light_gray));
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Tab reselected, no action needed
            }
        });
    }

    private int getUndreadMessage_Count() {

        ArrayList<ChatItem_ModelClass> userListTemp = new ArrayList<>();
        SharedPreferences sharedPreferences = MainActivity.this.getSharedPreferences("messenger_chats", Context.MODE_PRIVATE);

// Retrieve the JSON string from SharedPreferences
        String json = "";
        if (MyApplication.userLoggedIn && MyApplication.userLoggedIAs.equals("Google")) {
            json = sharedPreferences.getString("userListTemp_Google", null);
        } else {
            json = sharedPreferences.getString("userListTemp_Guest", null);
        }

// Convert the JSON string back to ArrayList
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<ChatItem_ModelClass>>() {
        }.getType();


        if (json == null) {
            // Handle case when no ArrayList is saved in SharedPreferences
            return 0;
        } else {
            userListTemp = gson.fromJson(json, type);

            int count = 0;
            for (int i = 0; i < userListTemp.size(); i++) {

                ChatItem_ModelClass modelclass = userListTemp.get(i);

                for (int j = 0; j < modelclass.getUserBotMsg().size(); j++) {
                    UserBotMsg userBotMsg = modelclass.getUserBotMsg().get(j);
                    if (userBotMsg.getSent() == 1 && userBotMsg.getRead() == 0) {
                        count = count + 1;
                    }
                }
                if (modelclass.isContainsQuestion()) {
                    if (modelclass.getQuestionWithAns().getSent() == 1 && modelclass.getQuestionWithAns().getRead() == 0) {
                        count = count + 1;
                    }
                }
            }
            return count;
        }

    }


    @Override
    public void onBackPressed() {
        exit_dialog();
        if (MyApplication.Ads_State.equals("active")) {
            showAds();
        }
    }

    private void checkForupdate() {
        inAppUpdate = new InAppUpdate(MainActivity.this);
        inAppUpdate.checkForAppUpdate();

    }

    private void exit_dialog() {

        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View promptView = inflater.inflate(R.layout.dialog_exit_app, null);
        builder.setView(promptView);
        builder.setCancelable(true);

        TextView exit = promptView.findViewById(R.id.confirm);
        TextView cancel = promptView.findViewById(R.id.cancel);


        AlertDialog exitDialog = builder.create();
        exitDialog.show();


        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (MyApplication.exit_Refer_appNavigation.equals("active") && MyApplication.Login_Times < 2 && MyApplication.Refer_App_url2.length() > 10) {

                    Intent j = new Intent(Intent.ACTION_VIEW);
                    j.setData(Uri.parse(MyApplication.Refer_App_url2));
                    try {
                        startActivity(j);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    finishAffinity();
                    System.exit(0);
                    finish();
                    exitDialog.dismiss();

                } else {

                    finishAffinity();
                    finish();
                    System.exit(0);
                    finish();
                    exitDialog.dismiss();

                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitDialog.cancel();
            }
        });


        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        exitDialog.getWindow().setBackgroundDrawable(inset);

    }


    private void showAds() {
        if (MyApplication.Ad_Network_Name.equals("admob")) {
            if (!SplashScreen.homepageAdShown) {
                ADS_ADMOB.Interstitial_Ad(this);
                SplashScreen.homepageAdShown = true;
            }
        } else {
            if (!SplashScreen.homepageAdShown) {
                ADS_FACEBOOK.interstitialAd(this, facebook_IntertitialAds, getString(R.string.Facebook_InterstitialAdUnit));
                SplashScreen.homepageAdShown = true;
            }
        }
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


}