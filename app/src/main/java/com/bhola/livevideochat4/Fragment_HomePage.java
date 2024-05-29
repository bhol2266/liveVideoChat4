package com.bhola.livevideochat4;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Fragment_HomePage extends Fragment {

    private RecyclerView recyclerView1, recyclerView2, recyclerView3;
    private ImageAdapter imageAdapter, imageAdapter2, imageAdapter3;
    LinearSmoothScroller smoothScroller1, smoothScroller2, smoothScroller3;
    private Handler handlerAnimation, blinkhandler, countHandler;
    RelativeLayout btnRelativelayout;
    int randomNumber, current_value;
    TextView onlineCountTextview;
    AlertDialog permissionDialog;
    ActivityResultLauncher<String[]> mPermissionResultLauncher;
    private boolean isCameraPermissionGranted = false;
    private boolean isRecordAudioPermissionGranted = false;

    private String[] PERMISSIONS;
    List<String> imageList_MomingIMages = new ArrayList<>();
    View view;
    Context context;

    public Fragment_HomePage() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_home_page, container, false);
        context = view.getContext();

        context = getContext();
        // Inflate the layout for context fragment
        mPermissionResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                if (result.get(Manifest.permission.CAMERA) != null) {
                    isCameraPermissionGranted = result.get(Manifest.permission.CAMERA);
                }
                if (result.get(Manifest.permission.RECORD_AUDIO) != null) {
                    isRecordAudioPermissionGranted = result.get(Manifest.permission.RECORD_AUDIO);
                }
                if (result.get(Manifest.permission.CAMERA) != null && result.get(Manifest.permission.RECORD_AUDIO) != null && isCameraPermissionGranted && isRecordAudioPermissionGranted) {
                    Intent intent = new Intent(context, BeforeVideoCall.class);
                    intent.putExtra("count", onlineCountTextview.getText().toString());
                    startActivity(intent);
                }
            }
        });


        if (MyApplication.App_updating.equals("inactive")) {
            loadDatabase(); //this will load images for moving images and call method    setimagesScrolling()
        }
        setButtonAnimation(view, context);
        blinkWorldMap(view, context);
        update_onlineCount(view, context);


        PERMISSIONS = new String[]{

                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
        };

        return view;
    }


    private void requestPermission() {

        isCameraPermissionGranted = ContextCompat.checkSelfPermission((Activity) context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        isRecordAudioPermissionGranted = ContextCompat.checkSelfPermission((Activity) context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        List<String> permmisionRequestList = new ArrayList<>();
        if (!isCameraPermissionGranted) {
            permmisionRequestList.add(Manifest.permission.CAMERA);
        }
        if (!isRecordAudioPermissionGranted) {
            permmisionRequestList.add(Manifest.permission.RECORD_AUDIO);
        }

        if (!permmisionRequestList.isEmpty()) {
            mPermissionResultLauncher.launch(permmisionRequestList.toArray(new String[0]));
        } else {
            Intent intent = new Intent(context, BeforeVideoCall.class);
            intent.putExtra("count", onlineCountTextview.getText().toString());
            startActivity(intent);
        }
    }


    private void update_onlineCount(View view, Context context) {
        Random random = new Random();

//get random number between 1000 - 4000
        int min = 300;
        int max = 1300;
        randomNumber = random.nextInt(max - min + 1) + min;
        current_value = randomNumber;


        onlineCountTextview = view.findViewById(R.id.onlineCount);
        onlineCountTextview.setText(String.valueOf(randomNumber));
        incrementValueSlowly(view, context, onlineCountTextview);


        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                int range[] = {5, 10, 15, 20, 25};
                int randomIndex = new Random().nextInt(range.length);
                int randomElement = range[randomIndex];
                int min1 = randomNumber - randomElement;
                int max1 = randomNumber + randomElement;
                randomNumber = random.nextInt(max1 - min1 + 1) + min1;
                incrementValueSlowly(view, context, onlineCountTextview);
            }
        };

        timer.schedule(task, 0, 5000);
    }


    private void incrementValueSlowly(View view, Context context, TextView onlineCountTextview) {

        if (getActivity() != null) {

            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (current_value < randomNumber) {
                                current_value++;
                                onlineCountTextview.setText(String.valueOf(current_value));
                                incrementValueSlowly(view, context, onlineCountTextview);
                            }
                            if (current_value > randomNumber) {
                                current_value--;
                                onlineCountTextview.setText(String.valueOf(current_value));
                                incrementValueSlowly(view, context, onlineCountTextview);
                            }
                        }
                    }, 250); // Delay of 50 milliseconds between each increment
                }
            });

        }

    }

    private void blinkWorldMap(View view, Context context) {
        ImageView worldmap;

        worldmap = view.findViewById(R.id.worldmap);
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.blink_animation);
        worldmap.startAnimation(animation);

        blinkhandler = new Handler();
        blinkhandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                blinkWorldMap(view, context);
            }
        }, 1000);

    }

    private void setButtonAnimation(View view, Context context) {

        ImageView Shine, worldmap;
        TextView btnTextview;

        btnTextview = view.findViewById(R.id.img);
        btnTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermission(); // this method will check permissions and if permission are granted it will take to BeforeCameraActivity

            }
        });
        Shine = view.findViewById(R.id.shine);


        btnRelativelayout = view.findViewById(R.id.btnRelativelayout);

        //Start the animations preoidically by calling 'shineStart' method with ScheduledExecutorService
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Animation animation = new TranslateAnimation(0, btnTextview.getWidth() + Shine.getWidth(), 0, 0);
                        animation.setDuration(1500);
                        animation.setFillAfter(false);
                        animation.setInterpolator(new AccelerateDecelerateInterpolator());
                        Shine.startAnimation(animation);
                        startAnimation(context);

                    }
                });
            }
        }, 2, 5, TimeUnit.SECONDS);


        handlerAnimation = new Handler();

    }

    public void checkPermissionDialog() {


        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View promptView = inflater.inflate(R.layout.dialog_allow_permission, null);
        builder.setView(promptView);
        builder.setCancelable(true);

        LinearLayout notificationLayout = promptView.findViewById(R.id.notificationLayout);
        LinearLayout locationLayout = promptView.findViewById(R.id.locationLayout);
        LinearLayout storageLayout = promptView.findViewById(R.id.storageLayout);

        notificationLayout.setVisibility(View.GONE);
        locationLayout.setVisibility(View.GONE);
        storageLayout.setVisibility(View.GONE);

        TextView allow = promptView.findViewById(R.id.allow);
        allow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                permissionDialog.dismiss();
                requestPermission();
//                ActivityCompat.requestPermissions((Activity) context, PERMISSIONS, 1);

            }
        });


        permissionDialog = builder.create();
        permissionDialog.show();
        permissionDialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT); //Controlling width and height.


        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        permissionDialog.getWindow().setBackgroundDrawable(inset);
        permissionDialog.getWindow().setLayout(750, WindowManager.LayoutParams.WRAP_CONTENT); //Controlling width and height.

    }


    private void startAnimation(Context context) {
        // Assuming you have a view object, e.g., myView
        Animation animationUp = AnimationUtils.loadAnimation(context, R.anim.bottom_scaleup);
        btnRelativelayout.startAnimation(animationUp);


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation animationDown = AnimationUtils.loadAnimation(context, R.anim.bottom_scaledown);
                btnRelativelayout.startAnimation(animationDown);
            }
        }, 800);


    }

    @Override
    public void onResume() {
        super.onResume();
        if (recyclerView1 != null) {
            moveImages();

        }


    }

    private void setimagesScrolling() {
        recyclerView1 = view.findViewById(R.id.recyclerView1);
        recyclerView2 = view.findViewById(R.id.recyclerView2);
        recyclerView3 = view.findViewById(R.id.recyclerView3);


        LinearLayoutManager layoutManager1 = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, true) {
            @Override
            public boolean canScrollHorizontally() {
                return false; // Disable horizontal scrolling for recyclerView1
            }
        };
        recyclerView1.setLayoutManager(layoutManager1);

        LinearLayoutManager layoutManager2 = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, true) {
            @Override
            public boolean canScrollHorizontally() {
                return false; // Disable horizontal scrolling for recyclerView2
            }
        };
        recyclerView2.setLayoutManager(layoutManager2);

        LinearLayoutManager layoutManager3 = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, true) {
            @Override
            public boolean canScrollHorizontally() {
                return false; // Disable horizontal scrolling for recyclerView3
            }
        };
        recyclerView3.setLayoutManager(layoutManager3);


        // Create a list of image resources from the drawable folder
        List<String> imageList = new ArrayList<>();
        List<String> imageList2 = new ArrayList<>();
        List<String> imageList3 = new ArrayList<>();

        int listSize = imageList_MomingIMages.size();
        int partSize = listSize / 3;
        for (int i = 0; i < imageList_MomingIMages.size(); i++) {
            String imageUrl = imageList_MomingIMages.get(i);
            if (i < partSize) {
                imageList.add(imageUrl);
            } else if (i < partSize * 2) {
                imageList2.add(imageUrl);
            } else {
                imageList3.add(imageUrl);
            }
        }


        imageAdapter = new ImageAdapter(context, imageList);
        imageAdapter2 = new ImageAdapter(context, imageList2);
        imageAdapter3 = new ImageAdapter(context, imageList3);
        recyclerView1.setAdapter(imageAdapter);
        recyclerView2.setAdapter(imageAdapter2);
        recyclerView3.setAdapter(imageAdapter3);
        recyclerView2.scrollToPosition(imageList2.size());


    }

    private void moveImages() {


        smoothScroller1 = new LinearSmoothScroller(recyclerView1.getContext()) {
            @Override
            protected int calculateTimeForScrolling(int dx) {
                return super.calculateTimeForScrolling(dx) * 200; // scroll speed
            }
        };

        smoothScroller1.setTargetPosition(Integer.MAX_VALUE);
        recyclerView1.getLayoutManager().startSmoothScroll(smoothScroller1);

//-----------------------------------------------------------------------------------------

        smoothScroller2 = new LinearSmoothScroller(recyclerView2.getContext()) {
            @Override
            protected int calculateTimeForScrolling(int dx) {
                return super.calculateTimeForScrolling(dx) * 200; // scroll speed
            }
        };

        smoothScroller2.setTargetPosition(-Integer.MAX_VALUE);
        recyclerView2.getLayoutManager().startSmoothScroll(smoothScroller2);

//-----------------------------------------------------------------------------------------


        smoothScroller3 = new LinearSmoothScroller(recyclerView3.getContext()) {
            @Override
            protected int calculateTimeForScrolling(int dx) {
                return super.calculateTimeForScrolling(dx) * 200; // scroll speed
            }
        };
        smoothScroller3.setTargetPosition(Integer.MAX_VALUE);
        recyclerView3.getLayoutManager().startSmoothScroll(smoothScroller3);
    }

    private void loadDatabase() {
        imageList_MomingIMages=new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Open the JSON file from the assets folder
                    InputStream inputStream = context.getAssets().open("users.json");

                    // Create a BufferedReader to read the file
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                    // Read the file line by line
                    String line;
                    StringBuilder jsonString = new StringBuilder();
                    while ((line = bufferedReader.readLine()) != null) {
                        jsonString.append(line);
                    }

                    // Close the input stream
                    inputStream.close();

                    // Parse the JSON string
                    JSONArray jsonArray = new JSONArray(jsonString.toString());

                    List<String> shuffledList = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        shuffledList.add(jsonArray.getString(i));
                    }
                    Collections.shuffle(shuffledList);

                    // Read the first 100 items
                    int count = Math.min(shuffledList.size(), 100);
                    for (int i = 0; i < count; i++) {
                        String item = shuffledList.get(i);
                        imageList_MomingIMages.add(MyApplication.databaseURL_images + "VideoChatProfiles/" + Utils.decryption(item) + "/profile.jpg");
                    }

                } catch (JSONException | IOException e) {
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

//                        createJSON();
                        setimagesScrolling();
                    }
                });
            }
        }).start();

    }

    private void createJSON() {
        try {
            JSONArray jsonArray = new JSONArray();

            for (String item : imageList_MomingIMages) {
                jsonArray.put(item);
            }


            FileWriter fileWriter = new FileWriter(context.getFilesDir() + "/myjsonfile.json");
            fileWriter.write(jsonArray.toString());
            fileWriter.close();

        } catch (Exception e) {
            Log.d(MyApplication.TAG, "run: " + e.getMessage());
        }


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handlerAnimation.removeCallbacksAndMessages(null);
        blinkhandler.removeCallbacksAndMessages(null);

    }


}


