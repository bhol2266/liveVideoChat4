package com.bhola.livevideochat4;


import android.animation.Animator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Date;

public class SplashScreen extends AppCompatActivity {

    Animation topAnim, bottomAnim;
    TextView textView;
    LottieAnimationView lottie_progressbar;

    DatabaseReference url_mref;

    public static boolean homepageAdShown = false;
    boolean animationCompleted = false;
    boolean activityChanged = false;

    com.facebook.ads.InterstitialAd facebook_IntertitialAds;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        fullscreenMode();
        allUrl();


        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);
        textView = findViewById(R.id.textView_splashscreen);
        lottie_progressbar = findViewById(R.id.lottie_progressbar);


//        textView.setAnimation(topAnim);
        lottie_progressbar.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animationCompleted = true;


            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        generateNotification();
        generateFCMToken();


//        clearChats();
    }


    private void clearChats() {
        SharedPreferences sharedPreferences = SplashScreen.this.getSharedPreferences("messenger_chats", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

// Clear the SharedPreferences
        editor.clear();
        editor.apply();

    }


    private void allUrl() {
        if (Utils.isInternetAvailable(SplashScreen.this)) {
            {
                url_mref = FirebaseDatabase.getInstance().getReference().child("LiveVideoChat4");
                url_mref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        MyApplication.Refer_App_url2 = (String) snapshot.child("Refer_App_url2").getValue();
                        MyApplication.exit_Refer_appNavigation = (String) snapshot.child("switch_Exit_Nav").getValue();
                        MyApplication.Ads_State = (String) snapshot.child("Ads").getValue();
                        MyApplication.Ad_Network_Name = (String) snapshot.child("Ad_Network").getValue();
                        MyApplication.App_updating = (String) snapshot.child("App_updating").getValue();
                        MyApplication.Notification_ImageURL = (String) snapshot.child("Notification_ImageURL").getValue();
                        MyApplication.databaseURL_video = (String) snapshot.child("databaseURL_video").getValue();
                        MyApplication.databaseURL_images = (String) snapshot.child("databaseURL_images").getValue();


                        sharedPrefrences();


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SplashScreen.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }


    }

    private void sharedPrefrences() {

        //Reading Login Times and login details
        SharedPreferences sh = getSharedPreferences("UserInfo", MODE_PRIVATE);
        int a = sh.getInt("loginTimes", 0);
        int userId = sh.getInt("userId", 0);
        MyApplication.coins = sh.getInt("coins", 0);
        String loginAs = sh.getString("loginAs", "not set");
        if (!loginAs.equals("not set")) {
            MyApplication.userLoggedIn = true;
            getUserFromFireStore(userId);
            if (loginAs.equals("Google")) {
                MyApplication.userLoggedIAs = "Google";
            } else {
                MyApplication.userLoggedIAs = "Guest";

            }
        } else {
            handler_forIntent();
        }
        MyApplication.Login_Times = a + 1;

        // Updating Login Times data into SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putInt("loginTimes", a + 1);
        myEdit.commit();


    }

    private void getUserFromFireStore(int userId) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("Users");

        DocumentReference userRef = usersRef.document(String.valueOf(userId));

        userRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        MyApplication.userModel = documentSnapshot.toObject(UserModel.class);
                        // Use the user data
                        //update user latest login date
                        Utils utils = new Utils();
                        utils.updateDateonFireStore("date", new Date());
                    } else {

                        MyApplication.userLoggedIn = false;
                        // User document doesn't exist
                    }

                    if (animationCompleted) {
                        handler_forIntent();
                    } else {
                        lottie_progressbar.addAnimatorListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                handler_forIntent();

                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                    Log.d("sadfsadf", "e: " + e.getMessage());

                });


    }


    private void generateFCMToken() {

        if (getIntent() != null && getIntent().hasExtra("KEY1")) {
            if (getIntent().getExtras().getString("KEY1").equals("Notification_Story")) {
                MyApplication.Notification_Intent_Firebase = "active";
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (facebook_IntertitialAds != null) {
            facebook_IntertitialAds.destroy();

        }
    }

    private void generateNotification() {
        FirebaseMessaging.getInstance().subscribeToTopic("all").addOnCompleteListener(task -> {

            if (!task.isSuccessful()) {
                String msg = "Failed";
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void handler_forIntent() {
        activityChanged = true;
        if (!Utils.isInternetAvailable(SplashScreen.this)) {
            createSnackBar();
            return;
        }
        if (MyApplication.userLoggedIn && MyApplication.firebaseUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        } else {

            if (MyApplication.userLoggedIn && MyApplication.userLoggedIAs.equals("Guest")) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(getApplicationContext(), LoginScreen.class);
                startActivity(intent);
            }
        }
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        finish();
    }

    private void createSnackBar() {

        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "No Internet Connection!", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Retry", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SplashScreen.this, MyApplication.class));
                snackbar.dismiss();
            }
        });
        snackbar.show();
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


    protected void onStart() {
        super.onStart();
        MyApplication.firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        SharedPreferences sh = getSharedPreferences("UserInfo", MODE_PRIVATE);
        String loginAs = sh.getString("loginAs", "not set");
        if (MyApplication.firebaseUser != null && loginAs.equals("Google")) {
            MyApplication.authProviderName = MyApplication.firebaseUser.getProviderData().get(MyApplication.firebaseUser.getProviderData().size() - 1).getProviderId();
            MyApplication.userLoggedIn = true;
        }
    }


}