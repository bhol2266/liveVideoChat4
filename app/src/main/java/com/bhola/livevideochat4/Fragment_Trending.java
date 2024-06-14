package com.bhola.livevideochat4;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bhola.livevideochat4.Models.CountryInfo_Model;
import com.bhola.livevideochat4.Models.Model_Profile;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;


public class Fragment_Trending extends Fragment {

    Context context;
    View view;
    public static DrawerLayout drawerLayout;
    public static String selectedCountry = "All";
    public static CircleImageView flagIcon;
    public static TextView countryName;


    public static GirlsCardAdapter adapter;
    SliderAdapter sliderAdapter;
    public static ArrayList<Model_Profile> girlsList;
    ArrayList<Model_Profile> girlsList_slider;
    ArrayList<Model_Profile> girlsList_nearBy;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    final int NOTIFICATION_REQUEST_CODE = 112;
    Boolean isScrolling = false;
    GridLayoutManager layoutManager;
    public static SwipeRefreshLayout swipeRefreshLayout;
    NearByAdapter nearByAdapter;
    RecyclerView recyclerview_NearBy;
    ArrayList<CountryInfo_Model> countrylist_forRecyclerview;
    CountryRecyclerViewAdapter countryRecyclerViewAdapter;
    String currentSelectedView = "Hot";  //hot fragment or nearbyFragment . we need this because when click hottextview even hot view is already there, it creates error when location permission is denied

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        getUserLocation_Permission();

        view = inflater.inflate(R.layout.fragment_trending, container, false);

        context = getContext();

        TextView HotTextview = view.findViewById(R.id.HotTextview);
        ImageView HotTextview_line = view.findViewById(R.id.HotTextview_line);

        TextView NearbyTextview = view.findViewById(R.id.NearbyTextview);
        ImageView NearbyTextview_line = view.findViewById(R.id.NearbyTextview_line);


        HotTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentSelectedView.equals("Hot")) {
                    return;
                }
                currentSelectedView = "Hot";
                recyclerview_NearBy.setVisibility(View.GONE);
                swipeRefreshLayout.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setEnabled(true);

                float textSizeInDp = 25; // Replace with your desired text size in dp
                float scale = getResources().getDisplayMetrics().density;
                int textSizeInPixels = (int) (textSizeInDp * scale + 0.5f);
                int textSizeInPixels2 = (int) (16 * scale + 0.5f);

// Set the text size in pixels
                NearbyTextview.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeInPixels2);
                NearbyTextview_line.setVisibility(View.INVISIBLE);

                HotTextview.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeInPixels);
                HotTextview_line.setVisibility(View.VISIBLE);

                int lightgray = getResources().getColor(com.google.android.ads.mediationtestsuite.R.color.gmts_light_gray); // Replace with your color resource or a specific color value
                int semiblack = getResources().getColor(R.color.semiblack); // Replace with your color resource or a specific color value

                NearbyTextview.setTextColor(lightgray);
                HotTextview.setTextColor(semiblack);

            }
        });

        NearbyTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MyApplication.App_updating.equals("active")) {
                    return;
                }

                if (MyApplication.currentCountry.equals("")) {
                    return;
                }
                if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                } else {
                    // Permission not granted, request it
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);

                }

                currentSelectedView = "Nearby";

                recyclerview_NearBy.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setVisibility(View.GONE);
                swipeRefreshLayout.setEnabled(false);

                float textSizeInDp = 25; // Replace with your desired text size in dp
                float scale = getResources().getDisplayMetrics().density;
                int textSizeInPixels = (int) (textSizeInDp * scale + 0.5f);
                int textSizeInPixels2 = (int) (16 * scale + 0.5f);

// Set the text size in pixels
                NearbyTextview.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeInPixels);
                NearbyTextview_line.setVisibility(View.VISIBLE);

                HotTextview.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeInPixels2);
                HotTextview_line.setVisibility(View.INVISIBLE);

                int lightgray = getResources().getColor(com.google.android.ads.mediationtestsuite.R.color.gmts_light_gray); // Replace with your color resource or a specific color value
                int semiblack = getResources().getColor(R.color.semiblack); // Replace with your color resource or a specific color value

                NearbyTextview.setTextColor(semiblack);
                HotTextview.setTextColor(lightgray);
            }
        });


        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setRefreshing(true);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (MyApplication.App_updating.equals("active")) {
                    return;
                }
                Fragment_Trending.selectedCountry = "All";
                girlsList.clear();
                loadDatabase();
                updateFlagButton();
            }
        });

        updateFlagIconButton();
        sideLayout_Countries();
        setUpSlider();
        setupRecycerView();


        return view;
    }

    private void updateFlagIconButton() {
        flagIcon = view.findViewById(R.id.flagIcon);
        countryName = view.findViewById(R.id.countryName);

    }

    private void sideLayout_Countries() {

        LinearLayout openDrawer = view.findViewById(R.id.openDrawer);
        drawerLayout = view.findViewById(R.id.drawerLayout);

        openDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MyApplication.App_updating.equals("active")) {
                    return;
                }
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END);
                } else {
                    drawerLayout.openDrawer(GravityCompat.END);
                }
            }
        });
        countrylist_forRecyclerview = new ArrayList<>();
        countrylist_forRecyclerview.addAll(MyApplication.countryList);


        CountryInfo_Model countryInfoModel = new CountryInfo_Model();
        countryInfoModel.setNationality("All");
        countryInfoModel.setFlagUrl("All");
        countryInfoModel.setCountry("All");
        countryInfoModel.setSelected(true);

        countrylist_forRecyclerview.add(0, countryInfoModel);

        countryRecyclerViewAdapter = new CountryRecyclerViewAdapter(context,countrylist_forRecyclerview);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(countryRecyclerViewAdapter);


    }


    private void showNearBy() {
        girlsList_nearBy = new ArrayList<>();


        recyclerview_NearBy = view.findViewById(R.id.recyclerview_NearBy);


        if (MyApplication.currentCountry.length() != 0) {
            loadDatabase_Country_NearBy(MyApplication.currentCountry);
        } else {
            loadDatabase_NearBy();
        }

        nearByAdapter = new NearByAdapter(context, girlsList_nearBy);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerview_NearBy.setLayoutManager(layoutManager);
        recyclerview_NearBy.setAdapter(nearByAdapter);

    }

    private void updateFlagButton() {
        if (Fragment_Trending.selectedCountry.equals("All")) {
            Drawable drawable = context.getResources().getDrawable(R.drawable.earth);
            Fragment_Trending.flagIcon.setImageDrawable(drawable);
            Fragment_Trending.countryName.setText("Region");
        } else {
            loadImageview(Fragment_Trending.flagIcon, Fragment_Trending.selectedCountry, context);
            Fragment_Trending.countryName.setText(Fragment_Trending.selectedCountry);

        }
    }

    private void loadImageview(CircleImageView flagurl, String country, Context context) {
        try {
            // Replace "image.jpg" with the actual filename and path within the assets folder
            InputStream inputStream = context.getAssets().open("countryFlag/" + country.replaceAll(" ", "-") + ".png");

            // Decode the input stream into a Bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // Set the Bitmap to the ImageView
            flagurl.setImageBitmap(bitmap);

            // Close the input stream when done
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private void setUpSlider() {
        girlsList_slider = new ArrayList<>();
        loadDatabase_slider();
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        CustomRecyclerView recyclerView_slider = view.findViewById(R.id.recyclerView_slider);

        recyclerView_slider.setLayoutManager(layoutManager);
        sliderAdapter = new SliderAdapter(context, girlsList_slider);
        recyclerView_slider.setAdapter(sliderAdapter);
        sliderAdapter.notifyDataSetChanged();
    }

    private void loadDatabase_slider() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = new DatabaseHelper(context, MyApplication.DB_NAME, MyApplication.DB_VERSION, "GirlsProfile").readRandomGirls();
                if (cursor.moveToFirst()) {
                    do {
                        girlsList_slider.add(Utils.readCursor(cursor));
                    } while (cursor.moveToNext());

                }
                cursor.close();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (MyApplication.App_updating.equals("active")) {
                            if (girlsList_slider.size() > 4) {
                                girlsList_slider.subList(4, girlsList_slider.size()).clear();
                            }
                        }
                        sliderAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();

    }

    private void setupRecycerView() {
        girlsList = new ArrayList<>();


        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setVisibility(View.VISIBLE);


        adapter = new GirlsCardAdapter(context, girlsList);
        getLocation();


        layoutManager = new GridLayoutManager(context, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);
        NestedScrollView nestedScrollview = view.findViewById(R.id.nestedScrollview);

        nestedScrollview.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                    // Scrolled to the end of the NestedScrollView
                    // You can load more data here
                    if (Fragment_Trending.selectedCountry.equals("All")) {
                        swipeRefreshLayout.setRefreshing(true);
                        loadDatabase();
                    }

                }
            }
        });


    }

    private void loadDatabase() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = new DatabaseHelper(context, MyApplication.DB_NAME, MyApplication.DB_VERSION, "GirlsProfile").readRandomGirls();
                if (cursor.moveToFirst()) {
                    do {
                        girlsList.add(Utils.readCursor(cursor));
                    } while (cursor.moveToNext());

                }
                cursor.close();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                swipeRefreshLayout.setRefreshing(false);

                            }
                        }, 1500);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                if (MyApplication.App_updating.equals("active")) {
                                    if (girlsList.size() > 6) {
                                        girlsList.subList(6, girlsList.size()).clear();
                                    }
                                }

//                                createJSON(); //this json is for creating chat bots

                                adapter.notifyDataSetChanged();
                            }
                        }, 200);

                    }
                });
            }
        }).start();


    }

    private void createJSON() {
        try {
            JSONArray jsonArray = new JSONArray();

            for (Model_Profile model_profile : girlsList) {

                String nationality = "";
                for (CountryInfo_Model countryInfo_model : MyApplication.countryList) {
                    if (model_profile.getFrom().equals(countryInfo_model.getCountry())) {
                        nationality = countryInfo_model.getNationality();
                    }
                }


                JSONObject object1 = new JSONObject();
                object1.put("name", model_profile.getName());
                object1.put("username", model_profile.getUsername());

                JSONArray imagesArray = new JSONArray();
                for (int i = 0; i < model_profile.getImages().size(); i++) {
                    imagesArray.put(MyApplication.databaseURL_images + "VideoChatProfiles/" + nationality + "/" + model_profile.getUsername() + "/" + String.valueOf(i) + ".jpg");
                }
                object1.put("images", imagesArray);
                jsonArray.put(object1);
            }


            FileWriter fileWriter = new FileWriter(context.getFilesDir() + "/myjsonfile.json");
            fileWriter.write(jsonArray.toString());
            fileWriter.close();

        } catch (Exception e) {
            Log.d(MyApplication.TAG, "run: " + e.getMessage());
        }


    }


    private void getLocation() {

        if (MyApplication.App_updating.equals("active")) {
            loadDatabase();
            return;
        }
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            loadDatabase();
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener((Activity) context, location -> {
                    if (location != null) {
                        // Use the location data
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        // Call reverse geocoding to get the city and country
                        getAddressFromLocation(latitude, longitude);
                    } else {
                        loadDatabase();
                    }
                });


    }

    private void getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                MyApplication.currentCity = addresses.get(0).getLocality();
                MyApplication.currentCountry = addresses.get(0).getCountryName();
                // Now you have the city and country information

                updateLocationFireStore();
                showNearBy();

                boolean countryAvailable = false;
                for (int i = 0; i < MyApplication.countryList.size(); i++) {
                    CountryInfo_Model countryInfo_model = MyApplication.countryList.get(i);
                    if (MyApplication.currentCountry.equalsIgnoreCase(countryInfo_model.getCountry().trim())) {
                        countryAvailable = true;
                        loadDatabase_Country(countryInfo_model.getCountry().trim());
                    }
                }
                if (!countryAvailable) {
                    loadDatabase();
                }


                // below code is for moving the current country flag on top in sidelayout countries layout
                int fromIndex = -1;
                for (int i = 0; i < countrylist_forRecyclerview.size(); i++) {
                    CountryInfo_Model countryInfoModel1 = countrylist_forRecyclerview.get(i);
                    if (MyApplication.currentCountry.equals(countryInfoModel1.getCountry())) {
                        fromIndex = i; // Index of the item to move
                    }
                }
                if (fromIndex != -1) {
                    CountryInfo_Model countryInfoModel2 = countrylist_forRecyclerview.remove(fromIndex);
                    countrylist_forRecyclerview.add(1, countryInfoModel2);
                    countryRecyclerViewAdapter.notifyDataSetChanged();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateLocationFireStore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("Users");
        String userId = String.valueOf(MyApplication.userModel.getUserId());
        DocumentReference userDocRef = usersRef.document(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("location", MyApplication.currentCity);

        userDocRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    // The field(s) were successfully updated
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that might occur during the update
                });

    }


    private void loadDatabase_Country(String selectedCountry) {

        girlsList.clear();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = new DatabaseHelper(context, MyApplication.DB_NAME, MyApplication.DB_VERSION, "GirlsProfile").readGirls_Country(selectedCountry);
                if (cursor.moveToFirst()) {
                    do {

                        Model_Profile model_profile = Utils.readCursor(cursor);
                        if (model_profile.getImages().size() != 0) {
                            girlsList.add(0, model_profile);
                        } else {
                            girlsList.add(model_profile);
                        }
                    } while (cursor.moveToNext());

                }
                cursor.close();
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Collections.shuffle(girlsList);
                                if (MyApplication.App_updating.equals("active")) {
                                    if (girlsList.size() > 6) {
                                        girlsList.subList(6, girlsList.size()).clear();
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            }
                        }, 200);

                    }
                });
            }
        }).start();


    }

    private void askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) requireActivity(), new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_REQUEST_CODE);

            }
        }
    }

    private void getUserLocation_Permission() {

        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        } else {
            // Permission not granted, request it
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);

        }
    }


    private void loadDatabase_Country_NearBy(String selectedCountry) {


        new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = new DatabaseHelper(context, MyApplication.DB_NAME, MyApplication.DB_VERSION, "GirlsProfile").readGirls_Country(selectedCountry);
                if (cursor.moveToFirst()) {
                    do {
                        girlsList_nearBy.add(Utils.readCursor(cursor));

                    } while (cursor.moveToNext());

                }
                cursor.close();
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                Collections.shuffle(girlsList_nearBy);
                                if (girlsList_nearBy.size() > 10) {
                                    // Remove elements beyond the first 10
                                    girlsList_nearBy.subList(10, girlsList_nearBy.size()).clear();
                                }

                                nearByAdapter.notifyDataSetChanged();
                            }
                        }, 200);

                    }
                });
            }
        }).start();


    }

    private void loadDatabase_NearBy() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = new DatabaseHelper(context, MyApplication.DB_NAME, MyApplication.DB_VERSION, "GirlsProfile").readRandomGirls();
                if (cursor.moveToFirst()) {
                    do {
                        girlsList_nearBy.add(Utils.readCursor(cursor));
                    } while (cursor.moveToNext());

                }
                cursor.close();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                Collections.shuffle(girlsList_nearBy);
                                if (girlsList_nearBy.size() > 10) {
                                    // Remove elements beyond the first 10
                                    girlsList_nearBy.subList(10, girlsList_nearBy.size()).clear();
                                }
                                nearByAdapter.notifyDataSetChanged();
                            }
                        }, 200);

                    }
                });
            }
        }).start();


    }

    private ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    Log.d(MyApplication.TAG, "requestCode: " + result);
                    if (result) {
                        getLocation();
                        askForNotificationPermission();
                    } else {
                        // PERMISSION NOT GRANTED
                    }
                }
            }
    );

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


class GirlsCardAdapter extends RecyclerView.Adapter<GirlsCardAdapter.GridViewHolder> {

    private final List<Model_Profile> girlsList;
    private final Context context;
    private ObjectAnimator breatheAnimator;


    public GirlsCardAdapter(Context context, ArrayList<Model_Profile> girlsList) {
        this.context = context;
        this.girlsList = girlsList;
    }


    @androidx.annotation.NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.girlcard, parent, false);
        return new GridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull GridViewHolder holder, int position) {
        Model_Profile item = girlsList.get(position);

        holder.name.setText(item.getName());
        Picasso.get()
                .load(item.getProfilePhoto())
                .fit()  // Provide target width and height
                .centerInside()         // Maintain aspect ratio and fit within ImageView bounds
                .into(holder.profile);


        for (CountryInfo_Model countryMap : MyApplication.countryList) {
            if (item.getFrom().equals(countryMap.getCountry())) {
                loadImageview(holder.flag, countryMap.getCountry());
            }
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation breathingAnimation = AnimationUtils.loadAnimation(context, R.anim.breathing_animation);
                holder.hello.startAnimation(breathingAnimation);
            }
        }, getRandomNumber());

        holder.cardView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, Profile.class);
                intent.putExtra("userName", item.getUsername());
                intent.putExtra("online", false);
                context.startActivity(intent);
            }
        });

        holder.hello.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences sharedPreferences = view.getContext().getSharedPreferences("UserInfo", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("userName", item.getUsername());
                editor.apply(); // Apply the changes to SharedPreferences

                Intent intent = new Intent(context, ChatScreen_User.class);
                context.startActivity(intent);

            }
        });

        censoredBtn(item.getCensored(), item.getUsername(), holder.censoredBtn, holder.getAbsoluteAdapterPosition(), item);


    }

    private void loadImageview(ImageView imageView, String country) {
        try {
            // Replace "image.jpg" with the actual filename and path within the assets folder
            InputStream inputStream = context.getAssets().open("countryFlag/" + country.replaceAll(" ", "-") + ".png");

            // Decode the input stream into a Bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // Set the Bitmap to the ImageView
            imageView.setImageBitmap(bitmap);

            // Close the input stream when done
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void censoredBtn(int censored, String username, Button censoredBtn, int absoluteAdapterPosition, Model_Profile item) {
        if (censored == 0) {
            censoredBtn.setBackgroundColor(context.getResources().getColor(R.color.themeColor));
            censoredBtn.setText("Not Censored");
        } else {
            censoredBtn.setBackgroundColor(context.getResources().getColor(R.color.green)); // Assumes you have a green color defined in your resources
            censoredBtn.setText("Censored");

        }

        censoredBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (censored == 0) {
                    String res = new DatabaseHelper(context, MyApplication.DB_NAME, MyApplication.DB_VERSION, "GirlsProfile").updateCensored(username, 1);

                    censoredBtn.setBackgroundColor(context.getResources().getColor(R.color.green));
                    censoredBtn.setText("Censored");
                    for (int i = 0; i < Fragment_Trending.girlsList.size(); i++) {
                        if (Fragment_Trending.girlsList.get(i).getUsername().equals(username)) {
                            Fragment_Trending.girlsList.get(i).setCensored(1);
                        }
                    }
                } else {
                    String res = new DatabaseHelper(context, MyApplication.DB_NAME, MyApplication.DB_VERSION, "GirlsProfile").updateCensored(username, 0);

                    censoredBtn.setBackgroundColor(context.getResources().getColor(R.color.themeColor)); // Assumes you have a green color defined in your resources
                    censoredBtn.setText("Not Censored");

                    for (int i = 0; i < Fragment_Trending.girlsList.size(); i++) {
                        if (Fragment_Trending.girlsList.get(i).getUsername().equals(username)) {
                            Fragment_Trending.girlsList.get(i).setCensored(0);
                        }
                    }

                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Fragment_Trending.adapter.notifyItemChanged(absoluteAdapterPosition);

                    }
                }, 500);

            }
        });
    }


    private long getRandomNumber() {

        Random random = new Random();
        int minValue = 100;
        int maxValue = 700;
        int increment = 100;

        // Calculate the range of possible values
        int range = (maxValue - minValue) / increment + 1;

        // Generate a random index within the range
        int randomIndex = random.nextInt(range);

        int randomNumber = minValue + randomIndex * increment;
        return randomNumber;
    }


    @Override
    public int getItemCount() {
        return girlsList.size();
    }


    public static class GridViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView profile, hello, flag;
        CardView cardView1;
        Button censoredBtn;

        public GridViewHolder(@androidx.annotation.NonNull View itemView) {
            super(itemView);
            profile = itemView.findViewById(R.id.profile);
            name = itemView.findViewById(R.id.name);
            hello = itemView.findViewById(R.id.hello);
            flag = itemView.findViewById(R.id.flag);
            cardView1 = itemView.findViewById(R.id.cardView1);
            censoredBtn = itemView.findViewById(R.id.censoredBtn);


        }
    }

}


class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.viewholder> {

    Context context;
    ArrayList<Model_Profile> girllist;


    public SliderAdapter(Context context, ArrayList<Model_Profile> girllist) {
        this.context = context;
        this.girllist = girllist;
    }

    @androidx.annotation.NonNull
    @Override
    public viewholder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view = layoutInflater.inflate(R.layout.slider_item, parent, false);
        return new viewholder(view);
    }

    @Override
    public void onViewRecycled(viewholder holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull viewholder holder, int position) {
        Model_Profile item = girllist.get(position);
        holder.title.setText(item.getName());
        Picasso.get().load(item.getProfilePhoto().replace("profile","profile_original")).fit().into(holder.thumbnail);

        holder.sliderlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, Profile.class);
                intent.putExtra("userName", item.getUsername());
                intent.putExtra("online", true);
                context.startActivity(intent);
            }
        });

    }


    @Override
    public int getItemCount() {
        return girllist.size();
    }


    public class viewholder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView title;
        LinearLayout sliderlayout;

        public viewholder(@androidx.annotation.NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.imageview);
            title = itemView.findViewById(R.id.categorytextview);
            sliderlayout = itemView.findViewById(R.id.sliderlayout);
        }
    }
}


class NearByAdapter extends RecyclerView.Adapter<NearByAdapter.ViewHolder> {

    ArrayList<Model_Profile> girlsList;
    Context context;

    public NearByAdapter(Context context, ArrayList<Model_Profile> girlsList) {
        this.girlsList = girlsList;
        this.context = context;
    }


    @android.support.annotation.NonNull
    @Override
    public ViewHolder onCreateViewHolder(@android.support.annotation.NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.nearby_girl_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@android.support.annotation.NonNull ViewHolder holder, int position) {
        Model_Profile model_profile = girlsList.get(position);
        Picasso.get().load(model_profile.getProfilePhoto().replace("profile","profile_original")).fit().into(holder.profileImage);

        holder.age.setText(model_profile.getAge().replace("years old", "").trim());
        holder.location.setText(model_profile.getFrom());
        holder.name.setText(model_profile.getName());

        holder.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, Profile.class);
                intent.putExtra("userName", model_profile.getUsername());
                intent.putExtra("online", false);
                context.startActivity(intent);
            }
        });
        holder.message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                showCustomToast();
                SharedPreferences sharedPreferences = view.getContext().getSharedPreferences("UserInfo", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("userName", model_profile.getUsername());
                editor.apply(); // Apply the changes to SharedPreferences

                Intent intent = new Intent(context, ChatScreen_User.class);
                context.startActivity(intent);
            }
        });

        holder.videocall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment_Trending.rechargeDialog(view.getContext());
            }
        });
    }


    private void showCustomToast() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.custom_toast_layout, null);

        TextView secondMessage = layout.findViewById(R.id.secondMessage);
        secondMessage.setVisibility(View.GONE);
        TextView profileName = layout.findViewById(R.id.profileName);
        profileName.setText("nice to meet you");
        // You can customize the text and other properties of the view elements here

        Toast toast = new Toast(context);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 130); // Adjust the margin (bottom) here
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();

    }

    @Override
    public int getItemCount() {
        return girlsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView name, age, location;
        CardView message;
        ImageView videocall;

        public ViewHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            name = itemView.findViewById(R.id.name);
            age = itemView.findViewById(R.id.age);
            location = itemView.findViewById(R.id.location);
            message = itemView.findViewById(R.id.message);
            videocall = itemView.findViewById(R.id.videocall);
        }
    }
}

class CountryRecyclerViewAdapter extends RecyclerView.Adapter<CountryRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private ArrayList<CountryInfo_Model> itemList;

    public CountryRecyclerViewAdapter(Context context, ArrayList<CountryInfo_Model> countryList) {
        this.context = context;
        this.itemList = countryList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.country_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CountryInfo_Model countryInfoModel = itemList.get(position);
        holder.countryName.setText(countryInfoModel.getCountry());

        if (countryInfoModel.getCountry().equals("All")) {
            Drawable drawable = context.getResources().getDrawable(R.drawable.earth);
            holder.flagurl.setImageDrawable(drawable);
        } else {

            loadImageview(holder.flagurl, countryInfoModel.getCountry(), context);

        }

        if (countryInfoModel.isSelected()) {
            holder.counrtyCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.themeColorExtralight));
            holder.countryName.setTextColor(Color.WHITE);
        } else {
            holder.counrtyCard.setCardBackgroundColor(Color.parseColor("#F1FBFF"));
            int colorResource = R.color.semiblack; // Replace with your color resource
            int textColor = ContextCompat.getColor(context, colorResource);
            holder.countryName.setTextColor(textColor);


        }

        holder.counrtyCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (CountryInfo_Model otherItem : itemList) {
                    otherItem.setSelected(false);
                }

                // Select the clicked item
                countryInfoModel.setSelected(true);
                Fragment_Trending.selectedCountry = countryInfoModel.getCountry();
                notifyDataSetChanged(); // Notify adapter to refresh the UI
                loadDatabase_Country(countryInfoModel.getCountry(), holder.counrtyCard);


                if (Fragment_Trending.selectedCountry.equals("All")) {
                    Drawable drawable = context.getResources().getDrawable(R.drawable.earth);
                    Fragment_Trending.flagIcon.setImageDrawable(drawable);
                    Fragment_Trending.countryName.setText("Region");
                } else {
                    loadImageview(Fragment_Trending.flagIcon, Fragment_Trending.selectedCountry, context);
                    Fragment_Trending.countryName.setText(countryInfoModel.getCountry());

                }
            }
        });

    }

    private void loadImageview(CircleImageView flagurl, String country, Context context) {
        try {
            // Replace "image.jpg" with the actual filename and path within the assets folder
            InputStream inputStream = context.getAssets().open("countryFlag/" + country.replaceAll(" ", "-") + ".png");

            // Decode the input stream into a Bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // Set the Bitmap to the ImageView
            flagurl.setImageBitmap(bitmap);

            // Close the input stream when done
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void loadDatabase_Country(String selectedCountry, CardView counrtyCard) {

        Fragment_Trending.girlsList.clear();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = new DatabaseHelper(context, MyApplication.DB_NAME, MyApplication.DB_VERSION, "GirlsProfile").readGirls_Country(selectedCountry);
                if (cursor.moveToFirst()) {
                    do {

                        Model_Profile model_profile = Utils.readCursor(cursor);
                        if (model_profile.getImages().size() != 0) {
                            Fragment_Trending.girlsList.add(0, model_profile);
                        } else {
                            Fragment_Trending.girlsList.add(model_profile);
                        }
                    } while (cursor.moveToNext());

                }
                cursor.close();
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Fragment_Trending.swipeRefreshLayout.setRefreshing(false);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Fragment_Trending.adapter.notifyDataSetChanged();
                                Fragment_Trending.drawerLayout.closeDrawer(GravityCompat.END);

                            }
                        }, 200);

                    }
                });
            }
        }).start();


    }


    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView countryName;
        CircleImageView flagurl;
        CardView counrtyCard;

        public ViewHolder(View itemView) {
            super(itemView);
            countryName = itemView.findViewById(R.id.countryName);
            flagurl = itemView.findViewById(R.id.flagurl);
            counrtyCard = itemView.findViewById(R.id.counrtyCard);
        }
    }
}





