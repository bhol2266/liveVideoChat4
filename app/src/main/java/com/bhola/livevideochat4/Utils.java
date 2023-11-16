package com.bhola.livevideochat4;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ParseException;
import android.net.Uri;
import android.os.StrictMode;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class Utils {

    private ProgressDialog progressDialog;


    public void showLoadingDialog(Context context, String message) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void dismissLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public void updateProfileonFireStore(String key, String value) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("Users");
        String userId = String.valueOf(SplashScreen.userModel.getUserId()); // Replace with the actual user ID
        DocumentReference userDocRef = usersRef.document(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put(key, value);

        userDocRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    // The field(s) were successfully updated
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that might occur during the update
                });

    }

    public void updateDateonFireStore(String key, Date date) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("Users");
        String userId = String.valueOf(SplashScreen.userModel.getUserId()); // Replace with the actual user ID
        DocumentReference userDocRef = usersRef.document(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put(key, date);

        userDocRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    // The field(s) were successfully updated
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that might occur during the update
                });
    }

    public static void replaceFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                String token = task.getResult();
                new Utils().updateProfileonFireStore("fcmToken",token);
            }
        });
    }


    public void downloadProfile_andGetURI(String image_url, Context context) throws IOException {
        //this method is used to download profle pic from google signIN option and get Uri to upload to digital ocean space

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                            .permitAll().build();
                    StrictMode.setThreadPolicy(policy);


                    URL url = new URL(image_url);
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();

                    InputStream inputStream = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    File internalStorage = new File(context.getFilesDir(), "images");

                    if (!internalStorage.exists()) {
                        internalStorage.mkdir();
                    }
                    File file = new File(internalStorage, "profile.jpg");
                    Uri imageURI = Uri.fromFile(file);
                    SplashScreen.userModel.setProfilepic(SplashScreen.databaseURL_images + "RealVideoChat1/profilePic/" + String.valueOf(SplashScreen.userModel.getUserId()) + ".jpg");
                    if (file.exists()) file.delete();

                    FileOutputStream outputStream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();

                } catch (Exception e) {
                    Log.d("SpaceError", "saveProfileDetails: " + e.getMessage());
                }
            }
        }).start();


    }

    public int calculateAge(String birthDateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            // Parse the birthdate string into a Date object
            Date birthDate = sdf.parse(birthDateString);

            // Get the current date
            Calendar currentDate = Calendar.getInstance();
            Date now = currentDate.getTime();

            // Calculate the age

            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(birthDate);
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(currentDate.getTime());

            int age = cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR);

            // Check if the birthdate has occurred this year or not
            if (cal2.get(Calendar.DAY_OF_YEAR) < cal1.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            return age;

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (java.text.ParseException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    public Bitmap applyBlur(Bitmap inputBitmap, int radius,Context context) {
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript renderScript = RenderScript.create(context);

        Allocation tmpIn = Allocation.createFromBitmap(renderScript, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap);

        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        blurScript.setInput(tmpIn);
        blurScript.setRadius(radius);
        blurScript.forEach(tmpOut);

        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }
}
