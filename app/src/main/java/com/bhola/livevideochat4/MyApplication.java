package com.bhola.livevideochat4;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;
import android.util.Log;

import com.bhola.livevideochat4.Models.CountryInfo_Model;
import com.bhola.livevideochat4.Models.Model_Profile;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyApplication extends Application {
    private static Context context;
    static String TAG = "TAGA";

    public static ArrayList<CountryInfo_Model> countryList;

    public static String terms_service_link = "https://sites.google.com/view/desi-girls-live-video-chat/terms_service";
    public static String privacy_policy_link = "https://sites.google.com/view/desi-girls-live-video-chat/privacypolicy";



    public static UserModel userModel;
    public static String Notification_Intent_Firebase = "inactive";
    public static String Ad_Network_Name = "facebook";
    public static String Refer_App_url2 = "https://play.google.com/store/apps/developer?id=UK+DEVELOPERS";
    public static String Ads_State = "inactive";
    public static String App_updating = "active";
    public static String databaseURL_video = "https://bhola2266.ap-south-1.linodeobjects.com//"; //default
    public static String databaseURL_images = "https://bucket2266.blr1.digitaloceanspaces.com/"; //default

    public static String exit_Refer_appNavigation = "inactive";
    public static String Notification_ImageURL = "https://hotdesipics.co/wp-content/uploads/2022/06/Hot-Bangla-Boudi-Ki-Big-Boobs-Nangi-Selfies-_002.jpg";
    public static int Login_Times = 0;


    //sqlDB
    public static String DB_NAME = "profiles";
    public static int DB_VERSION = 1;//manual set
    public static int DB_VERSION_INSIDE_TABLE = 1; //manual set


    //Google login
    public static boolean userLoggedIn = false;
    public static int coins = 0;
    public static String userLoggedIAs = "not set";
    public static String authProviderName = "";
    public static FirebaseUser firebaseUser;
    private FirebaseAnalytics mFirebaseAnalytics;


    //location
    public static String currentCity = "";
    public static String currentCountry = "";


    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);

        copyDatabase();
        countryList = loadCountryListFromAsset(this, "countrylist.json");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                startTransferProcess();

            }
        }, 5000);



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
            Log.d(MyApplication.TAG, "readJSON: "+username);
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


            String res = new DatabaseHelper(context, DB_NAME, DB_VERSION, "Profiles").addProfiles(model_profile);
            Log.d(TAG, "onSuccess: " + res + ":   " + Utils.encryption(Username));
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

    private void startTransferProcess() {


        String[] countries = {
                "Indian"
        };

        for (String country : countries) {
            List<String> filenames = new ArrayList<>();
            filenames = getusers_fromSingleCountry("videoProfiles/" + country);
            for (String filename : filenames) {
                readJSON("videoProfiles/" + country + "/" + filename, filename);
            }
        }


//        List<String> filenames = new ArrayList<>();
//        filenames = getusers_fromSingleCountry("videoProfiles");
//        Log.d("TAGAA", "startTransferProcess: " + filenames);
//
//        for (String filename : filenames) {
//            Log.d(TAG, "startTransferProcess: "+"videoProfiles/" + filename);
////            readJSON("videoProfiles/" + filename, filename);
//        }


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




}
