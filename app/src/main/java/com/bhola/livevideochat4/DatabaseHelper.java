package com.bhola.livevideochat4;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.bhola.livevideochat4.Models.Model_Profile;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "TAGA";
    String DbName;
    String DbPath;
    Context context;
    String Database_tableNo;
    Cursor cursor;

    public DatabaseHelper(@Nullable Context mcontext, String name, int version, String Database_tableNo) {
        super(mcontext, name, null, version);
        this.context = mcontext;
        this.DbName = name;
        this.Database_tableNo = Database_tableNo;
        DbPath = "/data/data/" + "com.bhola.livevideochat4" + "/databases/";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.d("TAGA", "oldVersion: " + oldVersion);
        Log.d("TAGA", "newVersion: " + newVersion);


    }

    public void CheckDatabases() {
        try {
            String path = DbPath + DbName;
            SQLiteDatabase.openDatabase(path, null, 0);
//            db_delete();
            //Database file is Copied here
            checkandUpdateLoginTimes_UpdateDatabaseCheck();
        } catch (Exception e) {
            this.getReadableDatabase();
            Log.d(MyApplication.TAG, "CheckDatabases: " + "First Time Copying " + DbName);
            CopyDatabases();
        }
    }

    public void CopyDatabases() {


        try {
            InputStream mInputStream = context.getAssets().open(DbName);
            String outFilename = DbPath + DbName;
            OutputStream mOutputstream = new FileOutputStream(outFilename);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = mInputStream.read(buffer)) > 0) {
                mOutputstream.write(buffer, 0, length);
            }
            mOutputstream.flush();
            mOutputstream.close();
            mInputStream.close();
            //Database file is Copied here
            checkandUpdateLoginTimes_UpdateDatabaseCheck();
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void checkandUpdateLoginTimes_UpdateDatabaseCheck() {

        //       Check for Database Update

        Cursor cursor1 = new DatabaseHelper(context, MyApplication.DB_NAME, MyApplication.DB_VERSION, "DB_VERSION").read_DB_VERSION();
        while (cursor1.moveToNext()) {
            int DB_VERSION_FROM_DATABASE = cursor1.getInt(1);

            if (DB_VERSION_FROM_DATABASE != MyApplication.DB_VERSION_INSIDE_TABLE) {
                DatabaseHelper databaseHelper2 = new DatabaseHelper(context, MyApplication.DB_NAME, MyApplication.DB_VERSION, "DB_VERSION");
                databaseHelper2.db_delete();
            }

        }
        cursor1.close();

    }


    public void db_delete() {

        File file = new File(DbPath + DbName);
        if (file.exists()) {
            file.delete();
            Log.d("TAGA", "db_delete: " + "Database Deleted " + DbName);

        }
        CopyDatabases();
    }

    public void OpenDatabase() {
        String path = DbPath + DbName;
        SQLiteDatabase.openDatabase(path, null, 0);

    }


    public Cursor readRandomGirls() {
        SQLiteDatabase db = this.getWritableDatabase();

        if (MyApplication.App_updating.equals("inactive") && MyApplication.userLoggedIn && MyApplication.userLoggedIAs.equals("Google")) {

            String query = "SELECT * FROM " + Database_tableNo + " WHERE LENGTH(images) > 50 ORDER BY RANDOM() LIMIT 30";
            Cursor cursor = db.rawQuery(query, null);
            return cursor;


        } else {
            String query = "SELECT * FROM " + Database_tableNo + " WHERE LENGTH(images) > 50 AND censored = 1 ORDER BY RANDOM() LIMIT 30";
            Cursor cursor = db.rawQuery(query, null);
            return cursor;
        }
    }

    public Cursor readSingleGirl(String username) {

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(Database_tableNo, null, "Username=?", new String[]{Utils.encryption(username)}, null, null, null, null);

        return cursor;

    }

    public Cursor readGirls_Country(String countryName) {


        SQLiteDatabase db = this.getWritableDatabase();
        if (MyApplication.App_updating.equals("inactive") && MyApplication.userLoggedIn && MyApplication.userLoggedIAs.equals("Google")) {
            if (countryName.equals("All")) {
                String query = "SELECT * FROM " + Database_tableNo + " WHERE LENGTH(images) > 50 ORDER BY RANDOM() LIMIT 30";
                Cursor cursor = db.rawQuery(query, null);
                return cursor;
            } else {
                Cursor cursor = db.query(Database_tableNo, null, "country=?", new String[]{countryName}, null, null, "RANDOM()", "40");
                return cursor;
            }

        } else {

            if (countryName.equals("All")) {
                String query = "SELECT * FROM " + Database_tableNo + " WHERE LENGTH(images) > 50 AND censored = 1 ORDER BY RANDOM() LIMIT 30";
                Cursor cursor = db.rawQuery(query, null);
                return cursor;

            } else {

                String selection = "country=? AND censored=?"; // Modify the condition as needed
                String[] selectionArgs = new String[]{countryName, String.valueOf(1)}; // Replace with your arguments

                Cursor cursor = db.query(Database_tableNo, null, selection, selectionArgs, null, null, null, null);
                return cursor;
            }
        }


    }


    public String updateCensored(String username, int censoredValue) {
        SQLiteDatabase sQLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("censored", censoredValue);

        float res = sQLiteDatabase.update(Database_tableNo, contentValues, "Username = ?", new String[]{Utils.encryption(username)});
        Log.d("sdafdsaf", "updateCensored: " + DbName);
        Log.d("sdafdsaf", "updateCensored: " + res);
        Log.d("sdafdsaf", "updateCensored: " + Utils.encryption(username));
        if (res == -1)
            return "Failed";
        else
            return "Success";
    }

    public String selectedBot(String username, int selectedBot_Value) {
        SQLiteDatabase sQLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("selectedBot", selectedBot_Value);

        float res = sQLiteDatabase.update(Database_tableNo, contentValues, "Username = ?", new String[]{Utils.encryption(username)});
        if (res == -1)
            return "Failed";
        else
            return "Success";
    }

    public String updateLike(String username, int likeValue) {
        SQLiteDatabase sQLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("like", likeValue);

        float res = sQLiteDatabase.update(Database_tableNo, contentValues, "Username = ?", new String[]{Utils.encryption(username)});
        if (res == -1)
            return "Failed";
        else
            return "Success";
    }

    public String addProfiles(Model_Profile model_profile) {


        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Username", Utils.encryption(model_profile.getUsername()));
        values.put("Name", Utils.encryption(model_profile.getName()));
        values.put("Country", model_profile.getFrom());
        values.put("Languages", model_profile.getLanguages());
        values.put("Age", model_profile.getAge());
        values.put("InterestedIn", model_profile.getInterested());
        values.put("BodyType", model_profile.getBodyType());
        values.put("Specifics", Utils.encryption(model_profile.getSpecifics()));
        values.put("Ethnicity", model_profile.getEthnicity());
        values.put("Hair", model_profile.getHair());
        values.put("EyeColor", model_profile.getEyeColor());
        values.put("Subculture", model_profile.getSubculture());
        values.put("profilePhoto", Utils.encryption(model_profile.getProfilePhoto()));
        values.put("coverPhoto", Utils.encryption(model_profile.getCoverPhoto()));

        Gson gson = new Gson();
        String interestsJson = gson.toJson(model_profile.getInterested());
        values.put("Interests", Utils.encryption(interestsJson));

        String imagesJson = gson.toJson(model_profile.getImages());
        values.put("images", Utils.encryption(imagesJson));

        String videos = gson.toJson(model_profile.getVideos());
        values.put("videos", Utils.encryption(videos));


        float res = db.insert("GirlsProfile", null, values);

        if (res == -1)

            return "Failed";
        else
            return "Sucess";

    }


    public void deleteAllrows() {
        Log.d(TAG, "deleteAllrows: " + Database_tableNo);
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Database_tableNo, null, null);
    }

    public Cursor read_DB_VERSION() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(Database_tableNo, null, null, null, null, null, null, null);
        return cursor;

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.disableWriteAheadLogging();
    }


}
