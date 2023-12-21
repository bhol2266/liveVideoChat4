package com.bhola.livevideochat4;

import android.animation.Animator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.collect.ImmutableList;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VipMembership extends AppCompatActivity {


    AlertDialog dialog;
    private BillingClient billingClient;
    LinearLayout progressBar;
    TextView buyNowTimer, offerTimer,offerTextview;

    private BroadcastReceiver timerUpdateReceiver, timerUpdateReceiverCheck;
    private boolean isTimerRunning = false;
    int backpressCount = 0;
    ArrayList<ProductDetails> productlist_offer;
    public static int[] selectedCard = {-1};
    Button btnContinue;
    private static final String CHANNEL_ID = "notification_channel_id";
    private static final int REQUEST_CODE = 123;
    public static GridAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vip_membership);
        actionBar();
        progressBar = findViewById(R.id.progressBar);
        offerTimer = findViewById(R.id.offerTimer);
        offerTextview = findViewById(R.id.offerTextview);

        addUnderlineTerms_privacy();
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener((billingResult, list) -> {

            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                boolean purchaseDone = false;
                for (Purchase purchase : list) {
                    consumePurchase(purchase);
                    if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged()) {
                        //first this is triggerd than onResume is called
                        verifyPurchase(purchase);
                        purchaseDone = true;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                cancelScheduledAlarm();
                                startActivity(new Intent(VipMembership.this, SplashScreen.class));
                            }
                        }, 5000);

                    }

                }
                if (!purchaseDone) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            cancelScheduledAlarm();
                            Toast.makeText(VipMembership.this, "Payment failed! try again", Toast.LENGTH_SHORT).show();

                        }
                    }, 5000);
                }
            } else {
                // Handle any other error codes.
                Toast.makeText(this, "Something went wrong try again!", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);

            }

        }).build();


        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {
                //start the connection after initializing the billing client
                connectGooglePlayBilling();
            }
        });
        checkTimeRunning();


//        createNotificationChannel();

    }


    private void checkTimeRunning() {
        isTimerRunning = isServiceRunning(TimerService.class);
        if (isTimerRunning) {
            backpressCount = 1;
            timerUpdateReceiverCheck = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    long remainingTime = intent.getLongExtra("remainingTime", 0);
                    updateTimerTextView(remainingTime);
                }
            };


            IntentFilter filter = new IntentFilter();
            filter.addAction("timer-update");
            filter.addAction("timer-finish");


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(timerUpdateReceiverCheck, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(timerUpdateReceiverCheck, filter);
            }

        }


    }

    void connectGooglePlayBilling() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    if (isTimerRunning) {
                        getProductDetails("with offer");
                    } else {
                        getProductDetails("no offer");
                    }
                    // The BillingClient is ready. You can query purchases here.
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                connectGooglePlayBilling();

                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });

    }

    private void getProductDetails(String offer) {

        List<String> productIds = new ArrayList<>();
        List<QueryProductDetailsParams.Product> list = new ArrayList<>();

        productIds.add("coins200");
        productIds.add("coins450");
        productIds.add("coins1000");
        productIds.add("coins2500");
        productIds.add("coins5000");
        productIds.add("coins10000");
        productIds.add("coins200_offer");
        productIds.add("coins450_offer");
        productIds.add("coins1000_offer");
        productIds.add("coins2500_offer");
        productIds.add("coins5000_offer");
        productIds.add("coins10000_offer");

// Add more product IDs as needed

        QueryProductDetailsParams.Builder queryBuilder = QueryProductDetailsParams.newBuilder();

        for (String productId : productIds) {
            QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder().setProductId(productId).setProductType(BillingClient.ProductType.INAPP).build();
            list.add(product);
        }
        queryBuilder.setProductList(list);


        QueryProductDetailsParams queryProductDetailsParams = queryBuilder.build();

        billingClient.queryProductDetailsAsync(queryProductDetailsParams, new ProductDetailsResponseListener() {
            public void onProductDetailsResponse(BillingResult billingResult, List<ProductDetails> productDetailsList) {
                // Handle the product details response for multiple products

                ((Activity) VipMembership.this).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        sendDatatoRecyclerview(productDetailsList, offer);


                    }
                });
            }
        });


    }

    private void sendDatatoRecyclerview(List<ProductDetails> productDetailsList, String offer) {


        ProgressBar proressbarRecycleview = findViewById(R.id.proressbarRecycleview);
        proressbarRecycleview.setVisibility(View.GONE);


        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setVisibility(View.VISIBLE);

        ArrayList<GridItem_ModelClass> gridItemList = new ArrayList<>();
        ArrayList<ProductDetails> productlist = new ArrayList<ProductDetails>();
        productlist_offer = new ArrayList<ProductDetails>();


        GridItem_ModelClass item_modelClass1 = new GridItem_ModelClass();
        for (ProductDetails productDetails : productDetailsList) {
            item_modelClass1.setCoins("200");
            if (productDetails.getProductId().equals("coins200")) {
                item_modelClass1.setMRP(productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice().replace(".00", ""));
                productlist.add(productDetails);
            }
            if (productDetails.getProductId().equals("coins200_offer")) {
                item_modelClass1.setDISCOUNTED_PRICE(productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice().replace(".00", ""));
                productlist_offer.add(productDetails);

            }
        }
        gridItemList.add(item_modelClass1);

        GridItem_ModelClass item_modelClass2 = new GridItem_ModelClass();
        for (ProductDetails productDetails : productDetailsList) {
            item_modelClass2.setCoins("450");
            if (productDetails.getProductId().equals("coins450")) {
                item_modelClass2.setMRP(productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice().replace(".00", ""));
                productlist.add(productDetails);

            }
            if (productDetails.getProductId().equals("coins450_offer")) {
                item_modelClass2.setDISCOUNTED_PRICE(productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice().replace(".00", ""));
                productlist_offer.add(productDetails);

            }
        }
        gridItemList.add(item_modelClass2);


        GridItem_ModelClass item_modelClass3 = new GridItem_ModelClass();
        for (ProductDetails productDetails : productDetailsList) {
            item_modelClass3.setCoins("1000");
            if (productDetails.getProductId().equals("coins1000")) {
                item_modelClass3.setMRP(productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice().replace(".00", ""));
                productlist.add(productDetails);

            }
            if (productDetails.getProductId().equals("coins1000_offer")) {
                item_modelClass3.setDISCOUNTED_PRICE(productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice().replace(".00", ""));
                productlist_offer.add(productDetails);

            }
        }
        gridItemList.add(item_modelClass3);


        GridItem_ModelClass item_modelClass4 = new GridItem_ModelClass();
        for (ProductDetails productDetails : productDetailsList) {
            item_modelClass4.setCoins("2500");
            if (productDetails.getProductId().equals("coins2500")) {
                item_modelClass4.setMRP(productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice().replace(".00", ""));
                productlist.add(productDetails);

            }
            if (productDetails.getProductId().equals("coins2500_offer")) {
                item_modelClass4.setDISCOUNTED_PRICE(productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice().replace(".00", ""));
                productlist_offer.add(productDetails);

            }
        }
        gridItemList.add(item_modelClass4);


        GridItem_ModelClass item_modelClass5 = new GridItem_ModelClass();
        for (ProductDetails productDetails : productDetailsList) {
            item_modelClass5.setCoins("5000");
            if (productDetails.getProductId().equals("coins5000")) {
                item_modelClass5.setMRP(productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice().replace(".00", ""));
                productlist.add(productDetails);

            }
            if (productDetails.getProductId().equals("coins5000_offer")) {
                item_modelClass5.setDISCOUNTED_PRICE(productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice().replace(".00", ""));
                productlist_offer.add(productDetails);

            }
        }
        gridItemList.add(item_modelClass5);


        GridItem_ModelClass item_modelClass6 = new GridItem_ModelClass();
        for (ProductDetails productDetails : productDetailsList) {
            item_modelClass6.setCoins("10000");
            if (productDetails.getProductId().equals("coins10000")) {
                item_modelClass6.setMRP(productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice().replace(".00", ""));
                productlist.add(productDetails);

            }
            if (productDetails.getProductId().equals("coins10000_offer")) {
                item_modelClass6.setDISCOUNTED_PRICE(productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice().replace(".00", ""));
                productlist_offer.add(productDetails);

            }
        }
        gridItemList.add(item_modelClass6);

        boolean discountApplied = false;
        if (offer.equals("with offer")) {
            discountApplied = true;
        }

        adapter = new GridAdapter(this, gridItemList, discountApplied);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(adapter);


        btnContinue = findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedCard[0] == -1) {
                    Toast.makeText(VipMembership.this, "Please select any card", Toast.LENGTH_SHORT).show();
                } else {
                    ProductDetails finalProductDetails;
                    if (offer.equals("with offer")) {
                        finalProductDetails = productlist_offer.get(selectedCard[0]);
                    } else {
                        finalProductDetails = productlist.get(selectedCard[0]);
                    }
                    // An activity reference from which the billing flow will be launched.
                    Activity activity = VipMembership.this;

                    ImmutableList productDetailsParamsList =
                            ImmutableList.of(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                            // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                            .setProductDetails(finalProductDetails)
                                            // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                            // for a list of offers that are available to the user
                                            .build()
                            );

                    BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParamsList)
                            .build();

// Launch the billing flow
                    billingClient.launchBillingFlow(activity, billingFlowParams);
                    progressBar.setVisibility(View.VISIBLE);
                }
            }
        });

    }


    private void verifyPurchase(Purchase purchase) {
        int coins = 0;
        String inputString = purchase.getProducts().get(0);
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(inputString);
        while (matcher.find()) {
            String number = matcher.group();
            coins = Integer.parseInt(number);
        }

        savePurchaseDetails_inSharedPreference(purchase.getPurchaseToken(), coins, purchase.getPurchaseTime());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentRef = db.collection("purchases").document(purchase.getPurchaseToken());

        documentRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
//                    ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
//                    billingClient.consumeAsync(
//                            consumeParams,
//                            new ConsumeResponseListener() {
//                                @Override
//                                public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String s) {
//                                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
//                                        Log.d("TAGAA", "onConsumeResponse: ");
//                                    }
//                                }
//                            }
//                    );
                } else {
                    Map<String, Object> data = new HashMap<>();
                    data.put("purchaseToken", purchase.getPurchaseToken());
                    data.put("purchaseTime", purchase.getPurchaseTime());
                    data.put("orderId", purchase.getOrderId());
                    data.put("date", new Date());
                    data.put("isValid", true);

                    documentRef.set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Data successfully written to Firestore
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Error writing data to Firestore
                        }
                    });


                    // Document doesn't exist
                }
            } else {
                // Error occurred while retrieving the document
                FirebaseFirestoreException exception = (FirebaseFirestoreException) task.getException();
                // Handle the exception
                Log.d(MyApplication.TAG, "FirebaseFirestoreException: " + exception.getMessage());
            }
        });


    }

    private void savePurchaseDetails_inSharedPreference(String purchaseToken, int coins, long purchaseTime) {
        //Reading purchase Token
        SharedPreferences sh = getSharedPreferences("UserInfo", MODE_PRIVATE);
        String a = sh.getString("purchaseToken", purchaseToken);

        // Creating purchase Token into SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString("purchaseToken", purchaseToken);
        myEdit.putInt("coins", coins);

        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateString = dateFormat.format(currentDate);
        myEdit.putString("purchase_date", dateString);
        myEdit.commit();
        FirebaseUtil.addUserCoins(coins);

    }


    private void exit_dialog() {

        if (productlist_offer == null || productlist_offer.size() == 0) {
            return;
        }
        getProductDetails("with offer");
        selectedCard[0] = -1;
        AlertDialog dialog;

        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(VipMembership.this);
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View promptView = inflater.inflate(R.layout.dialog_membership_exit, null);
        builder.setView(promptView);
        builder.setCancelable(true);

        LottieAnimationView lottie = promptView.findViewById(R.id.lottie);
        lottie.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                lottie.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        buyNowTimer = promptView.findViewById(R.id.buyNowTimer);


        dialog = builder.create();
        dialog.show();

        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        dialog.getWindow().setBackgroundDrawable(inset);


        if (isTimerRunning) {
            backpressCount = 1;
        } else {
            // Timer is not running, start the service
            startOfferTimer();
        }

        ProductDetails productDetails = productlist_offer.get(0);
        LinearLayout clickForPayment = promptView.findViewById(R.id.clickForPayment);
        TextView buyNowTimer = promptView.findViewById(R.id.buyNowTimer);
        TextView price = promptView.findViewById(R.id.price);
        TextView productName = promptView.findViewById(R.id.productName);

        productName.setText(productDetails.getTitle());
        price.setText(productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice().replace(".00", ""));
        buyNowTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // An activity reference from which the billing flow will be launched.
                Activity activity = VipMembership.this;

                ImmutableList productDetailsParamsList =
                        ImmutableList.of(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                        // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                        .setProductDetails(productDetails)
                                        // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                        // for a list of offers that are available to the user
                                        .build()
                        );

                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build();

// Launch the billing flow
                billingClient.launchBillingFlow(activity, billingFlowParams);
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        clickForPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // An activity reference from which the billing flow will be launched.
                Activity activity = VipMembership.this;

                ImmutableList productDetailsParamsList =
                        ImmutableList.of(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                        // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                        .setProductDetails(productDetails)
                                        // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                        // for a list of offers that are available to the user
                                        .build()
                        );

                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build();

// Launch the billing flow
                billingClient.launchBillingFlow(activity, billingFlowParams);
                progressBar.setVisibility(View.VISIBLE);
            }
        });


    }

    private void startOfferTimer() {


        timerUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long remainingTime = intent.getLongExtra("remainingTime", 0);
                updateTimerTextView(remainingTime);
            }
        };


        IntentFilter filter = new IntentFilter();
        filter.addAction("timer-update");
        filter.addAction("timer-finish");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(timerUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(timerUpdateReceiver, filter);
        }

        Intent intent = new Intent(this, TimerService.class);
        startService(intent);

    }


    private void updateTimerTextView(long remainingTime) {

        long minutes = (remainingTime / (1000 * 60)) % 60;
        long seconds = (remainingTime / 1000) % 60;
        String timeLeftFormatted = String.format("%02d:%02d", minutes, seconds);

        if (timeLeftFormatted.equals("00:10")) {
            offerTimer.setText("Timer Finished!");
            offerTimer.setVisibility(View.GONE);
            offerTextview.setVisibility(View.GONE);
            unregisterReceiver(timerUpdateReceiver);
        }

        if (!isTimerRunning) {
            buyNowTimer.setText("BUY NOW " + timeLeftFormatted.toString());
        }
        offerTimer.setText("Offer ends in " + timeLeftFormatted);
        offerTimer.setVisibility(View.VISIBLE);
        offerTextview.setVisibility(View.VISIBLE);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    private void actionBar() {

        ImageView back_arrow = findViewById(R.id.back_arrow);
        back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }


    private void addUnderlineTerms_privacy() {
        TextView terms = findViewById(R.id.terms);
        TextView privaciy = findViewById(R.id.privacy);
        terms.setPaintFlags(terms.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        privaciy.setPaintFlags(privaciy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(VipMembership.this, Terms_Conditions.class));
            }
        });
        privaciy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(VipMembership.this, PrivacyPolicy.class));

            }
        });
    }


    private void consumePurchase(Purchase purchase) {
        ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
        billingClient.consumeAsync(consumeParams, new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String s) {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.d("asdfsdaf", "Consumed: "+purchase.getPurchaseToken());
                }
            }
        });


    }

    @Override
    public void onBackPressed() {
        Log.d(MyApplication.TAG, "backpressCount: " + backpressCount);
        if (backpressCount == 0) {
            exit_dialog();
            backpressCount++;
        } else {
            showNotification();
            super.onBackPressed();
        }

    }


    private void cancelScheduledAlarm() {
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    private void showNotification() {


        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 10);

        Intent intent = new Intent(this, NotificationReceiver.class);

        SharedPreferences sh = VipMembership.this.getSharedPreferences("UserInfo", MODE_PRIVATE);
        String fullname = sh.getString("name", "not set");


        intent.putExtra("USERNAME", fullname); // Pass the data here
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE);


        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timerUpdateReceiver != null) {
            unregisterReceiver(timerUpdateReceiver);
        }
        if (timerUpdateReceiverCheck != null) {
            unregisterReceiver(timerUpdateReceiverCheck);
        }
    }

}

class GridAdapter extends RecyclerView.Adapter<GridAdapter.GridViewHolder> {

    private List<GridItem_ModelClass> gridItemList;
    private Context context;
    boolean discountApplied;

    private int selectedItemPosition = VipMembership.selectedCard[0];


    public GridAdapter(Context context, List<GridItem_ModelClass> gridItemList, boolean discountApplied) {
        this.context = context;
        this.gridItemList = gridItemList;
        this.discountApplied = discountApplied;
    }

    @NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.vip_carditem, parent, false);
        return new GridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GridViewHolder holder, int position) {
        GridItem_ModelClass item = gridItemList.get(position);
        holder.coins.setText(item.getCoins());
        if (!discountApplied) {
            holder.price.setText(item.getMRP());
            holder.mrp.setVisibility(View.GONE);
        } else {
            holder.price.setText(item.getDISCOUNTED_PRICE());
            holder.mrp.setVisibility(View.VISIBLE);
            holder.mrp.setText(item.getMRP());
            holder.mrp.setPaintFlags(holder.mrp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        if (position == selectedItemPosition) {
            holder.itemView.setSelected(true);

            int backgroundColor = R.color.themeColor;
            int color = ContextCompat.getColor(context, backgroundColor);
            holder.card.setCardBackgroundColor(color);
            int textColor = Color.parseColor("#FFFFFF"); // Replace "#FF0000" with your desired color
            holder.price.setTextColor(textColor);
            holder.mrp.setTextColor(textColor);
            VipMembership.selectedCard[0] = holder.getAbsoluteAdapterPosition();
        } else {
            holder.itemView.setSelected(false);
            int backgroundColor = R.color.white;
            int color = ContextCompat.getColor(context, backgroundColor);
            holder.card.setCardBackgroundColor(color);

            int textColor = Color.parseColor("#000000"); // Replace "#FF0000" with your desired color
            int textColor2 = Color.parseColor("#BF707070"); // Replace "#FF0000" with your desired color
            holder.price.setTextColor(textColor);
            holder.mrp.setTextColor(textColor2);
        }

        if (holder.getAbsoluteAdapterPosition() > 2) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.card.getLayoutParams();
            int leftMarginDp = 5;  // Set your left margin in pixels
            int topMarginDp = 5;   // Set your top margin in pixels
            int rightMarginDp = 5; // Set your right margin in pixels
            int bottomMarginDp = 20;// Set your bottom margin in pixels

            float scale = context.getResources().getDisplayMetrics().density;
            int leftMarginPx = (int) (leftMarginDp * scale + 0.5f);
            int topMarginPx = (int) (topMarginDp * scale + 0.5f);
            int rightMarginPx = (int) (rightMarginDp * scale + 0.5f);
            int bottomMarginPx = (int) (bottomMarginDp * scale + 0.5f);

// Create layout parameters with margins
            layoutParams.setMargins(leftMarginPx, topMarginPx, rightMarginPx, bottomMarginPx);
            holder.card.setLayoutParams(layoutParams);
        } else {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.card.getLayoutParams();
            int leftMarginDp = 5;  // Set your left margin in pixels
            int topMarginDp = 5;   // Set your top margin in pixels
            int rightMarginDp = 5; // Set your right margin in pixels
            int bottomMarginDp = 5;// Set your bottom margin in pixels

            float scale = context.getResources().getDisplayMetrics().density;
            int leftMarginPx = (int) (leftMarginDp * scale + 0.5f);
            int topMarginPx = (int) (topMarginDp * scale + 0.5f);
            int rightMarginPx = (int) (rightMarginDp * scale + 0.5f);
            int bottomMarginPx = (int) (bottomMarginDp * scale + 0.5f);

// Create layout parameters with margins
            layoutParams.setMargins(leftMarginPx, topMarginPx, rightMarginPx, bottomMarginPx);
            holder.card.setLayoutParams(layoutParams);

        }


    }

    @Override
    public int getItemCount() {
        return gridItemList.size();
    }

    public void setSelectedItemPosition(int position) {
        selectedItemPosition = position;
        notifyDataSetChanged();
    }

    public static class GridViewHolder extends RecyclerView.ViewHolder {
        TextView coins;
        TextView mrp;
        TextView price;
        CardView card;

        public GridViewHolder(@NonNull View itemView) {
            super(itemView);
            coins = itemView.findViewById(R.id.coins);
            mrp = itemView.findViewById(R.id.mrp);
            price = itemView.findViewById(R.id.price);
            card = itemView.findViewById(R.id.card);

            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int clickedPosition = getAbsoluteAdapterPosition();
                    if (clickedPosition != RecyclerView.NO_POSITION) {
                        VipMembership.adapter.setSelectedItemPosition(clickedPosition);
                    }
                }
            });
        }
    }


}


class GridItem_ModelClass {

    private String Coins;
    private String MRP;
    private String DISCOUNTED_PRICE;


    public GridItem_ModelClass() {
    }

    public GridItem_ModelClass(String coins, String MRP, String DISCOUNTED_PRICE) {
        Coins = coins;
        this.MRP = MRP;
        this.DISCOUNTED_PRICE = DISCOUNTED_PRICE;
    }

    public String getCoins() {
        return Coins;
    }

    public void setCoins(String coins) {
        Coins = coins;
    }

    public String getMRP() {
        return MRP;
    }

    public void setMRP(String MRP) {
        this.MRP = MRP;
    }

    public String getDISCOUNTED_PRICE() {
        return DISCOUNTED_PRICE;
    }

    public void setDISCOUNTED_PRICE(String DISCOUNTED_PRICE) {
        this.DISCOUNTED_PRICE = DISCOUNTED_PRICE;
    }
}



