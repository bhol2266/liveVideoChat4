package com.bhola.livevideochat4;


import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import com.bhola.livevideochat4.Models.CountryInfo_Model;
import com.bhola.livevideochat4.Models.Model_Profile;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.reflect.TypeToken;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplashScreen extends AppCompatActivity {

    static String TAG = "TAGA";
    Animation topAnim, bottomAnim;
    TextView textView;
    LottieAnimationView lottie_progressbar;

    public static UserModel userModel;
    public static String Notification_Intent_Firebase = "inactive";
    public static String Ad_Network_Name = "facebook";
    public static String Refer_App_url2 = "https://play.google.com/store/apps/developer?id=UK+DEVELOPERS";
    public static String Ads_State = "inactive";
    public static String App_updating = "active";
    public static String databaseURL_video = "https://bhola2266.ap-south-1.linodeobjects.com//"; //default
    public static String databaseURL_images = "https://bucket2266.blr1.digitaloceanspaces.com/"; //default
    public static ArrayList<CountryInfo_Model> countryList;

    public static String exit_Refer_appNavigation = "inactive";
    public static String Notification_ImageURL = "https://hotdesipics.co/wp-content/uploads/2022/06/Hot-Bangla-Boudi-Ki-Big-Boobs-Nangi-Selfies-_002.jpg";
    DatabaseReference url_mref;
    public static int Login_Times = 0;
    com.facebook.ads.InterstitialAd facebook_IntertitialAds;


    public static boolean homepageAdShown = false;
    boolean animationCompleted = false;
    boolean activityChanged = false;

    public static String terms_service_link = "https://sites.google.com/view/desi-girls-live-video-chat/terms_service";
    public static String privacy_policy_link = "https://sites.google.com/view/desi-girls-live-video-chat/privacypolicy";


    //sqlDB
    public static String DB_NAME = "profiles";
    public static int DB_VERSION = 1;//manual set
    public static int DB_VERSION_INSIDE_TABLE = 1; //manual set

    //Google login
    public static boolean userLoggedIn = false;
    public static int coins = 0;
    public static String userLoggedIAs = "not set";
    public static String authProviderName = "";
    public static String userEmail = "";
    FirebaseUser firebaseUser;
    private FirebaseAnalytics mFirebaseAnalytics;

    //location
    public static String currentCity = "";
    public static String currentCountry = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        fullscreenMode();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        copyDatabase();

        try {
            allUrl();
        } catch (Exception e) {
            e.printStackTrace();
        }

        countryList = loadCountryListFromAsset(this, "countrylist.json");


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

                if (!activityChanged) {
//                    handler_forIntent();
                }


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

//        startTransferProcess();

//        clearChats();
    }


    private void clearChats() {
        SharedPreferences sharedPreferences = SplashScreen.this.getSharedPreferences("messenger_chats", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

// Clear the SharedPreferences
        editor.clear();
        editor.apply();

    }


    private void copyDatabase() {


//      Check For Database is Available in Device or not
        DatabaseHelper databaseHelper = new DatabaseHelper(this, DB_NAME, DB_VERSION, "DB_VERSION");
        try {
            databaseHelper.CheckDatabases();
        } catch (Exception e) {
            e.printStackTrace();

        }

    }


    private void allUrl() {
        if (!isInternetAvailable(SplashScreen.this)) {

            Handler handler2 = new Handler();
            handler2.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Login_Times > 5) {
                        App_updating = "inactive";
                        Ads_State = "active";
                        Ad_Network_Name = "admob";
                    }
                    if (!activityChanged) {
                        handler_forIntent();
                    }
                }
            }, 2000);

            return;
        } else {
            url_mref = FirebaseDatabase.getInstance().getReference().child("LiveVideoChat4");
            url_mref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Refer_App_url2 = (String) snapshot.child("Refer_App_url2").getValue();
                    exit_Refer_appNavigation = (String) snapshot.child("switch_Exit_Nav").getValue();
                    Ads_State = (String) snapshot.child("Ads").getValue();
                    Ad_Network_Name = (String) snapshot.child("Ad_Network").getValue();
                    App_updating = (String) snapshot.child("App_updating").getValue();
                    Notification_ImageURL = (String) snapshot.child("Notification_ImageURL").getValue();
                    databaseURL_video = (String) snapshot.child("databaseURL_video").getValue();
                    databaseURL_images = (String) snapshot.child("databaseURL_images").getValue();


                    sharedPrefrences();


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SplashScreen.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }


    }

    private void sharedPrefrences() {

        //Reading Login Times and login details
        SharedPreferences sh = getSharedPreferences("UserInfo", MODE_PRIVATE);
        int a = sh.getInt("loginTimes", 0);
        int userId = sh.getInt("userId", 0);
        coins = sh.getInt("coins", 0);
        String loginAs = sh.getString("loginAs", "not set");
        if (!loginAs.equals("not set")) {
            userLoggedIn = true;
            getUserFromFireStore(userId);
            if (loginAs.equals("Google")) {
                userLoggedIAs = "Google";
            } else {
                userLoggedIAs = "Guest";

            }
        } else {
            handler_forIntent();
        }
        Login_Times = a + 1;

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
                        userModel = documentSnapshot.toObject(UserModel.class);
                        // Use the user data
                        //update user latest login date
                        Utils utils = new Utils();
                        utils.updateDateonFireStore("date", new Date());
                        if (animationCompleted) {
                            handler_forIntent();
                        }
                    } else {

                        SplashScreen.userLoggedIn = false;
                        if (animationCompleted) {
                            handler_forIntent();
                        }
                        // User document doesn't exist
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                });


    }


    private void generateFCMToken() {

        if (getIntent() != null && getIntent().hasExtra("KEY1")) {
            if (getIntent().getExtras().getString("KEY1").equals("Notification_Story")) {
                Notification_Intent_Firebase = "active";
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
        if (!isInternetAvailable(SplashScreen.this)) {
            createSnackBar();
            return;
        }
        if (SplashScreen.userLoggedIn && firebaseUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        } else {

            if (SplashScreen.userLoggedIn && SplashScreen.userLoggedIAs.equals("Guest")) {
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
                startActivity(new Intent(SplashScreen.this, SplashScreen.class));
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }


    private void startTransferProcess() {
        String[] countries = {
                "African", "American", "Arab", "Argentinian", "Australian", "Belgian", "Brazilian", "Bulgarian",
                "Canadian", "Chilean", "Chinese", "Colombian", "Croatian", "Czech", "Danish", "Dutch", "Ecuadorian",
                "Estonian", "Finnish", "French", "German", "Greek", "Hungarian", "Indian", "Irish", "Israeli", "Italian",
                "Japanese", "Kenyan", "Korean", "Lithuanian", "Malagasy", "Mexican", "Nigerian", "Nordic", "Norwegian",
                "Peruvian", "Polish", "Portuguese", "Romanian", "Russian", "Serbian", "Slovakian", "South African",
                "Spanish", "SriLankan", "Swedish", "Swiss", "Thai", "Turkish", "UK", "Ugandan", "Ukrainian", "Uruguayan",
                "Venezuelan", "Vietnamese", "Zimbabwean"
        };

//        for (String country : countries) {
//            List<String> filenames = new ArrayList<>();
//            filenames = getusers_fromSingleCountry("data/" + country);
//            for (String filename : filenames) {
//                readJSON("data/" + country + "/" + filename, filename);
//            }
//        }


        List<String> filenames = new ArrayList<>();
        filenames = getusers_fromSingleCountry("videoProfiles");
        Log.d("TAGAA", "startTransferProcess: " + filenames);

        for (String filename : filenames) {
            readJSON("videoProfiles/" + filename, filename);
        }


    }

    private List<String> getusers_fromSingleCountry(String path) {
        List<String> filenames = new ArrayList<>();
        AssetManager assetManager = getAssets(); // Get the AssetManager
        try {
            String[] assetFiles = assetManager.list(path); // Replace with the subfolder name you want to list


            for (String fileName : assetFiles) {
                filenames.add(fileName); // Remove the trailing slash
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("TAGAA", "e: " + e.getMessage());
        }
        return filenames;
    }

    private void readJSON(String path, String username) {
        try {
            JSONObject jsonData = new JSONObject(loadJSONFromAsset(path));


            String Username = username.replace(".json", "");
            String Name = jsonData.getString("Name");
            String From = jsonData.getString("From");
            String Languages = jsonData.getString("Languages");
            String Age = jsonData.getString("Age");
            String InterestedIn = jsonData.getString("InterestedIn");
            String BodyType = jsonData.getString("BodyType");
            String Specifics = jsonData.getString("Specifics");
            String Ethnicity = jsonData.getString("Ethnicity");
            String Hair = jsonData.getString("Hair");
            String EyeColor = jsonData.getString("EyeColor");
            String Subculture = jsonData.getString("Subculture");
            String profilePhoto = jsonData.getString("profilePhoto");
            String coverPhoto = jsonData.getString("coverPhoto");

            JSONArray interestArray_json = jsonData.getJSONArray("Interests");
            List<Map<String, String>> interestArraylist = new ArrayList<>();
            for (int i = 0; i < interestArray_json.length(); i++) {
                JSONObject jsonObject = interestArray_json.getJSONObject(i);
                String interest = jsonObject.getString("interest");
                String url = jsonObject.getString("url");

                Map<String, String> map1 = new HashMap<>();
                map1.put("interest", interest);
                map1.put("url", url);
                interestArraylist.add(map1);
            }


            JSONArray imagesArray_json = jsonData.getJSONArray("images");
            List<String> imagesArray = new ArrayList<>();
            for (int i = 0; i < imagesArray_json.length(); i++) {
                imagesArray.add((String) imagesArray_json.get(i));
            }


            JSONArray videosArray_json = jsonData.getJSONArray("videos");
            List<Map<String, String>> videosArraylist = new ArrayList<>();
            for (int i = 0; i < videosArray_json.length(); i++) {
                JSONObject jsonObject = videosArray_json.getJSONObject(i);
                String imageUrl = jsonObject.getString("imageUrl");
                String videoUrl = jsonObject.getString("videoUrl");

                Map<String, String> map1 = new HashMap<>();
                map1.put("imageUrl", imageUrl);
                map1.put("videoUrl", videoUrl);
                if (videoUrl.length() > 50) {
                    videosArraylist.add(map1);
                }
            }

            Model_Profile model_profile = new Model_Profile(Username, Name, From, Languages, Age, InterestedIn, BodyType, Specifics,
                    Ethnicity, Hair, EyeColor, Subculture, profilePhoto, coverPhoto, interestArraylist, imagesArray, videosArraylist, 0, 0, 0);

            String res = new DatabaseHelper(SplashScreen.this, DB_NAME, DB_VERSION, "Profiles").addProfiles(model_profile);
            Log.d(TAG, "onSuccess: " + res + ":   " + SplashScreen.encryption(Username));
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("TAGAA", "JSONException: " + e.getMessage());
        }
    }


    public String loadJSONFromAsset(String path) {
        String json = null;
        try {
            InputStream is = getApplicationContext().getAssets().open(path);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.d("TAGAA", "IOException: " + ex.getMessage());

            return null;
        }

        return json;
    }

    public static String decryption(String encryptedText) {

        int key = 5;
        String decryptedText = "";

        //Decryption
        char[] chars2 = encryptedText.toCharArray();
        for (char c : chars2) {
            c -= key;
            decryptedText = decryptedText + c;
        }
        return decryptedText;
    }

    public static String encryption(String text) {

        int key = 5;
        char[] chars = text.toCharArray();
        String encryptedText = "";
        String decryptedText = "";

        //Encryption
        for (char c : chars) {
            c += key;
            encryptedText = encryptedText + c;
        }

        //Decryption
        char[] chars2 = encryptedText.toCharArray();
        for (char c : chars2) {
            c -= key;
            decryptedText = decryptedText + c;
        }
        return encryptedText;
    }


    boolean isInternetAvailable(Context context) {
        if (context == null) return false;


        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {


            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                        return true;
                    }
                }
            } else {

                try {
                    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                        Log.i("update_statut", "Network is available : true");
                        return true;
                    }
                } catch (Exception e) {
                    Log.i("update_statut", "" + e.getMessage());
                }
            }
        }
        Log.i("update_statut", "Network is available : FALSE ");
        return false;
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
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        SharedPreferences sh = getSharedPreferences("UserInfo", MODE_PRIVATE);
        String loginAs = sh.getString("loginAs", "not set");
        if (firebaseUser != null && loginAs.equals("Google")) {
            authProviderName = firebaseUser.getProviderData().get(firebaseUser.getProviderData().size() - 1).getProviderId();
            userLoggedIn = true;
        }
    }


    private ArrayList<CountryInfo_Model> loadCountryListFromAsset(Context context, String fileName) {
        ArrayList<CountryInfo_Model> countryList = new ArrayList<>();

        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(fileName);

            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            String json = new String(buffer, "UTF-8");

            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                CountryInfo_Model countryInfoModel = new CountryInfo_Model();
                countryInfoModel.setNationality(jsonObject.getString("nationality"));
                countryInfoModel.setFlagUrl(jsonObject.getString("flagUrl"));
                countryInfoModel.setCountry(jsonObject.getString("country"));
                countryInfoModel.setCountryCode(jsonObject.getString("countryCode"));

                countryInfoModel.setSelected(false);

                countryList.add(countryInfoModel);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return countryList;
    }

    public static Model_Profile readCursor(Cursor cursor) {

        // Extract data from the cursor and populate the Model_Profile object
        String Username = SplashScreen.decryption(cursor.getString(0));
        String Name = SplashScreen.decryption(cursor.getString(1));
        String Country = cursor.getString(2);
        String Languages = cursor.getString(3);
        String Age = cursor.getString(4);
        String InterestedIn = cursor.getString(5);
        String BodyType = cursor.getString(6);
        String Specifics = SplashScreen.decryption(cursor.getString(7));
        String Ethnicity = cursor.getString(8);
        String Hair = cursor.getString(9);
        String EyeColor = cursor.getString(10);
        String Subculture = cursor.getString(11);
        String profilePhoto = SplashScreen.decryption(cursor.getString(13));
        String coverPhoto = SplashScreen.decryption(cursor.getString(14));
        int censored = cursor.getInt(17);
        int like = cursor.getInt(18);
        int selectedBot = cursor.getInt(19);

        // Convert JSON strings back to arrays/lists using Gson
        Gson gson = new Gson();


        String interestsJson = SplashScreen.decryption(cursor.getString(12));
        List<Map<String, String>> Interests = gson.fromJson(interestsJson, new TypeToken<List<Map<String, String>>>() {
        }.getType());

        String nationality = "";
        for (CountryInfo_Model countryInfo_model : SplashScreen.countryList) {
            if (Country.equals(countryInfo_model.getCountry())) {
                nationality = countryInfo_model.getNationality();
            }
        }

        String imagesJson = SplashScreen.decryption(cursor.getString(15));
        List<String> images = new ArrayList<>();

        String videosJson = SplashScreen.decryption(cursor.getString(16));
        List<Map<String, String>> videos = new ArrayList<>();
        try {
            JSONArray imagesArray = new JSONArray(imagesJson);
            for (int i = 0; i < imagesArray.length(); i++) {
                images.add(SplashScreen.databaseURL_images + "VideoChatProfiles/" + nationality + "/" + Username + "/" + String.valueOf(i) + ".jpg");
            }

            JSONArray videoArray = new JSONArray(videosJson);
            for (int i = 0; i < videoArray.length(); i++) {
                Map map = new HashMap<>();
                map.put("imageUrl", "");
                map.put("videoUrl", "");
                videos.add(map);
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // Create a new Model_Profile object and populate it
        Model_Profile model_profile = new Model_Profile(Username, Name, Country, Languages, Age, InterestedIn, BodyType, Specifics, Ethnicity, Hair, EyeColor, Subculture, profilePhoto, coverPhoto, Interests, images, videos, censored, like, selectedBot);

        return model_profile;
    }

}