package com.bhola.livevideochat4;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bhola.Models.GiftItemModel;
import com.bhola.livevideochat4.Models.ChatItem_ModelClass;
import com.bhola.livevideochat4.Models.Chats_Modelclass;
import com.bhola.livevideochat4.Models.CountryInfo_Model;
import com.bhola.livevideochat4.Models.Model_Profile;
import com.bhola.livevideochat4.Models.UserBotMsg;
import com.bhola.livevideochat4.Models.UserQuestionWithAns;
import com.bhola.livevideochat4.adapters.ChatsAdapter;
import com.devlomi.record_view.OnBasketAnimationEnd;
import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseSmartReply;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseTextMessage;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestionResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class ChatScreen_User extends Activity {


    ChatItem_ModelClass modelClass = null;
    AlertDialog block_user_dialog = null;
    AlertDialog report_user_dialog = null;
    AlertDialog report_userSucessfully_dialog = null;
    ArrayList<Chats_Modelclass> chatsArrayList;
    ChatsAdapter chatAdapter;
    RecyclerView recylerview;
    DatabaseReference chatRef;
    MediaPlayer mediaPlayer;

    private Handler handler;
    private Runnable myRunnable;
    private Thread myThread;
    public static TextView send;
    LinearLayout answerslayout, ll2;   //ll2 is message writting box
    // voice message stuffs
    private AudioRecorder audioRecorder;
    private File recordFile;
    public static ArrayList<FirebaseTextMessage> conversation;
    boolean isOnline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen_user);

        if (MyApplication.Ads_State.equals("active")) {
            showAds();
        }

        getModalClass();
        bottomBtns();


    }

    private void getModalClass() {
        try {
            //this is check if Fragment_Messenger.adapter.userList is not empty, if empty goback
            int sizee = Fragment_Messenger.adapter.userList.size();
        } catch (Exception e) {
            onBackPressed();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        String userName = sharedPreferences.getString("userName", "");

        for (int i = 0; i < Fragment_Messenger.adapter.userList.size(); i++) {
            if (Fragment_Messenger.adapter.userList.get(i).getUsername().equals(userName)) {
                modelClass = Fragment_Messenger.adapter.userList.get(i);
                sendDataRecyclerview();
            }
        }

        if (modelClass != null) {
            Fragment_Messenger.currentActiveUser = modelClass.getUsername();
        } else {
            // this code works when user click hello image on girl card which takes directly to chat screen and sends hi, this user doesnt exist in firebase default chatbots

            //create chatbot for this username and insert into Fragment_Messenger.adapter.userList and starting ti by reading this username from DB
            getGirlProfile_DB(userName);
        }


    }

    private void sendDataRecyclerview() {
        actionbar();// this is because when user clicks on hello image direclty , than we have to read username data from db which takes time than we call actionbar();

        chatsArrayList = new ArrayList<Chats_Modelclass>();

        if (modelClass.isContainsQuestion()) {

            UserQuestionWithAns userQuestionWithAns = modelClass.getQuestionWithAns();
            Chats_Modelclass chats_modelclass = new Chats_Modelclass(userQuestionWithAns.getQuestion(), "mimeType/text", "", "", modelClass.getProfileImage(), userQuestionWithAns.getDateTime(), 2);
            chatsArrayList.add(chats_modelclass);

            if (modelClass.getQuestionWithAns().getRead() == 0) {
                modelClass.getQuestionWithAns().setRead(1);
            }

            if (userQuestionWithAns.getReply().length() == 0) {
                //not replied yet
                setAnwswerOptions(userQuestionWithAns);
            } else {

                //adding reply message  only
                Chats_Modelclass chats_modelclass2 = new Chats_Modelclass(userQuestionWithAns.getReply(), "mimeType/text", "", "free", modelClass.getProfileImage(), userQuestionWithAns.getDateTime(), 1);
                chatsArrayList.add(chats_modelclass2);

                //after reply message is added, add all remainig replies which is sent already
                for (int i = 0; i < modelClass.getQuestionWithAns().getReplyToUser().size(); i++) {
                    UserBotMsg userBotMsg = modelClass.getQuestionWithAns().getReplyToUser().get(i);
                    if (userBotMsg.getSent() == 1) {
                        Chats_Modelclass chats_modelclass3 = new Chats_Modelclass(userBotMsg.getMsg(), userBotMsg.getMimeType(), userBotMsg.getExtraMsg(), userBotMsg.getMessageType(), modelClass.getProfileImage(), userBotMsg.getDateTime(), userBotMsg.getViewType());
                        chatsArrayList.add(chats_modelclass3);

                        modelClass.getQuestionWithAns().getReplyToUser().get(i).setRead(1);
                        update_userListTemp();

                    }
                }
            }


        } else {
            for (int i = 0; i < modelClass.getUserBotMsg().size(); i++) {
                if (modelClass.getUserBotMsg().get(i).getSent() == 1) {
                    UserBotMsg userBotMsg = modelClass.getUserBotMsg().get(i);
                    Chats_Modelclass chats_modelclass3 = new Chats_Modelclass(userBotMsg.getMsg(), userBotMsg.getMimeType(), userBotMsg.getExtraMsg(), userBotMsg.getMessageType(), modelClass.getProfileImage(), userBotMsg.getDateTime(), userBotMsg.getViewType());
                    chatsArrayList.add(chats_modelclass3);

                    if (modelClass.getUserBotMsg().get(i).getRead() == 0) {
                        modelClass.getUserBotMsg().get(i).setRead(1);

                        for (int j = 0; j < Fragment_Messenger.adapter.userList.size(); j++) {
                            if (Fragment_Messenger.adapter.userList.get(j).getUsername().equals(modelClass.getUsername())) {
                                Fragment_Messenger.adapter.userList.get(j).getUserBotMsg().get(i).setRead(1);
                                Fragment_Messenger.save_sharedPrefrence(ChatScreen_User.this, Fragment_Messenger.adapter.userList);

                            }
                        }

                    }
                }
            }
        }

        recylerview = findViewById(R.id.recylerview);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatScreen_User.this);
        linearLayoutManager.setStackFromEnd(true);
        recylerview.setLayoutManager(linearLayoutManager);
        chatAdapter = new ChatsAdapter(ChatScreen_User.this, chatsArrayList, recylerview, mediaPlayer, modelClass);
        recylerview.setAdapter(chatAdapter);

        scrollrecycelrvewToBottom();
        load_UnsentMessage();

    }


    private void createChatbot(Model_Profile modelProfile) {
        //create chatbot for this username and insert into Fragment_Messenger.adapter.userList

        //first read this username data from DB
        String name = modelProfile.getName();
        String username = modelProfile.getUsername();

        ArrayList<String> contentImages = new ArrayList<>();


        String nationality = "";
        for (CountryInfo_Model countryInfo_model : MyApplication.countryList) {
            if (modelProfile.getFrom().equals(countryInfo_model.getCountry())) {
                nationality = countryInfo_model.getNationality();
            }
        }
        String profileImage = MyApplication.databaseURL_images + "VideoChatProfiles/" + nationality + "/" + modelProfile.getUsername() + "/profile.jpg";
        boolean containsQuestion = false;

        String recommendationType = "Recommended";


        ArrayList<UserBotMsg> userBotMsgList = new ArrayList<>();
        Date currentTime = new Date();

        UserBotMsg userBotMsg = new UserBotMsg("hi", "mimeType/text", "hi", String.valueOf(currentTime.getTime()), 0, 1, 1, "free", 1);
        userBotMsgList.add(userBotMsg);
        if (isOnline) {
            smartReply("hi");
        }


        UserQuestionWithAns questionWithAns = null;


        ChatItem_ModelClass user = new ChatItem_ModelClass(name, username, profileImage, contentImages, containsQuestion, recommendationType, userBotMsgList, questionWithAns);

        modelClass = user;
        sendDataRecyclerview();


        Fragment_Messenger.adapter.userList.add(0, user);
        Fragment_Messenger.adapter.notifyItemInserted(0);

        Fragment_Messenger.save_sharedPrefrence(ChatScreen_User.this, Fragment_Messenger.adapter.userList);


    }

    private void getGirlProfile_DB(String userName) {

        final Model_Profile[] model_profile = {null};
        new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = new DatabaseHelper(ChatScreen_User.this, MyApplication.DB_NAME, MyApplication.DB_VERSION, "GirlsProfile").readSingleGirl(userName);
                if (cursor.moveToFirst()) {
                    model_profile[0] = Utils.readCursor(cursor);
                }
                cursor.close();
                ((Activity) ChatScreen_User.this).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        createChatbot(model_profile[0]);
                    }
                });
            }
        }).start();


    }


    private void bottomBtns() {
        conversation = new ArrayList<>();

        EditText newMessage = findViewById(R.id.newMessage);
        newMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int textLength = charSequence.length();
                CardView sendbtnn = findViewById(R.id.sendbtnn);
                RecordButton record_button = findViewById(R.id.record_button);
                if (textLength != 0) {
                    sendbtnn.setVisibility(View.VISIBLE);
                    record_button.setVisibility(View.GONE);
                } else {
                    sendbtnn.setVisibility(View.GONE);
                    record_button.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        CardView sendbtnn = findViewById(R.id.sendbtnn);
        sendbtnn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = newMessage.getText().toString();
                if (msg.length() == 0) {
                    return;
                }
                MediaPlayer mediaPlayer = MediaPlayer.create(ChatScreen_User.this, R.raw.msg_sent_sound);
                mediaPlayer.start();
                boolean freelimit = checkFreeLimit();
                insertCustomMsginChats(msg, "mimeType/text", freelimit ? "" : "premium", 1, freelimit); //this function handles the custom msg from user and updates the userlistTemp and all

                if (freelimit) smartReply(msg);
                newMessage.setText("");
            }
        });

        ImageView sendImage = findViewById(R.id.sendImage); // this is option for sending extra images
        ImageView lottiegift = findViewById(R.id.lottiegift);

        lottiegift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openBottomSheetDialog();
            }
        });
        ImageView videoCall = findViewById(R.id.videoCall);
        ImageView voiceCall = findViewById(R.id.voiceCall);

        sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadImageFromGallery();
            }
        });
        videoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rechargeDialog(view.getContext());
            }
        });
        voiceCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rechargeDialog(view.getContext());
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted; proceed with audio recording or other functionality.
            handleVoiceMessage();
        } else {
            // Permission is not granted; request it.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 123);
        }
    }

    private boolean checkFreeLimit() {
        int count = 0;

        if (modelClass.isContainsQuestion()) {
            for (int i = 0; i < modelClass.getQuestionWithAns().getReplyToUser().size(); i++) {
                UserBotMsg userBotMsg = modelClass.getQuestionWithAns().getReplyToUser().get(i);
                if (userBotMsg.getViewType() == 1) {
                    count = count + 1;
                }
            }
        } else {

            for (int i = 0; i < modelClass.getUserBotMsg().size(); i++) {
                UserBotMsg userBotMsg = modelClass.getUserBotMsg().get(i);
                if (userBotMsg.getViewType() == 1) {
                    count = count + 1;
                }
            }
        }
        Log.d("wdsfafdsa", "checkFreeLimit: " + count);
        if (count < 4) {
            return true;
        } else {
            return false;
        }

    }

    private void insertCustomMsginChats(String msg, String messageType, String chatType, int viewType, boolean freeLimt) {

        MediaPlayer mediaPlayer = MediaPlayer.create(ChatScreen_User.this, R.raw.msg_sent_sound);
        mediaPlayer.start();

        Date currentTime = new Date();
        scrollrecycelrvewToBottom();
        Chats_Modelclass chats_modelclass = null;
        if (messageType.equals("mimeType/text")) {
            chats_modelclass = new Chats_Modelclass(msg, messageType, freeLimt ? "hi" : "", chatType, modelClass.getProfileImage(), String.valueOf(currentTime.getTime()), viewType);

        }
        if (messageType.equals("mimeType/image")) {
            chats_modelclass = new Chats_Modelclass("[Image]", messageType, msg, chatType, modelClass.getProfileImage(), String.valueOf(currentTime.getTime()), viewType);

        }
        if (messageType.equals("mimeType/audio")) {
            chats_modelclass = new Chats_Modelclass("[Audio]", messageType, msg, chatType, modelClass.getProfileImage(), String.valueOf(currentTime.getTime()), viewType);

        }
        chatsArrayList.add(chats_modelclass);
        chatAdapter.notifyItemInserted(chatsArrayList.size() - 1);


        int index = -1;
        for (int i = 0; i < Fragment_Messenger.adapter.userList.size(); i++) {

            if (Fragment_Messenger.adapter.userList.get(i).getUsername().equals(modelClass.getUsername())) {
                index = i;
                UserBotMsg userBotMsg1 = new UserBotMsg();
                userBotMsg1.setDateTime(String.valueOf(currentTime.getTime()));
                userBotMsg1.setRead(1);
                userBotMsg1.setSent(1);
                userBotMsg1.setMimeType(messageType);
                userBotMsg1.setViewType(viewType);
                userBotMsg1.setMessageType(chatType);
                userBotMsg1.setNextMsgDelay(0);


                if (messageType.equals("mimeType/text")) {
                    userBotMsg1.setMsg(msg);
                    userBotMsg1.setExtraMsg("");

                } else {
                    if (messageType.equals("mimeType/image")) {
                        userBotMsg1.setMsg("[Image]");
                    } else {
                        userBotMsg1.setMsg("[Audio]");
                    }
                    userBotMsg1.setExtraMsg(msg);
                }


                ArrayList<UserBotMsg> temp = new ArrayList<>();

                if (modelClass.isContainsQuestion()) {

                    temp.addAll(modelClass.getQuestionWithAns().getReplyToUser());
                    for (int j = 0; j < modelClass.getQuestionWithAns().getReplyToUser().size(); j++) {
                        if (modelClass.getQuestionWithAns().getReplyToUser().get(j).getSent() == 0) {
                            if (j == 0) {
                                //first loop
                                temp.add(0, userBotMsg1);
                                break;
                            } else {
                                //middleloop
                                temp.add(j, userBotMsg1);
                                break;
                            }
                        }
                        if (j == modelClass.getQuestionWithAns().getReplyToUser().size() - 1) {
                            //last loop
                            temp.add(userBotMsg1);
                        }
                    }

                    modelClass.getQuestionWithAns().setReplyToUser(temp);
                } else {
                    temp.addAll(modelClass.getUserBotMsg());
                    for (int j = 0; j < modelClass.getUserBotMsg().size(); j++) {
                        if (modelClass.getUserBotMsg().get(j).getSent() == 0) {
                            if (j == 0) {
                                //first loop
                                temp.add(0, userBotMsg1);
                                break;
                            } else {
                                //middleloop
                                temp.add(j, userBotMsg1);
                                break;
                            }
                        }

                        if (j == modelClass.getUserBotMsg().size() - 1) {
                            //last loop
                            temp.add(userBotMsg1);
                        }

                    }
                    modelClass.setUserBotMsg(temp);
                }

            }

        }


        chatAdapter.notifyDataSetChanged();
        update_userListTemp();


        Fragment_Messenger.adapter.userList.remove(index);
        Fragment_Messenger.adapter.userList.add(0, modelClass);
        Fragment_Messenger.adapter.notifyItemMoved(index, 0);
        Fragment_Messenger.adapter.notifyItemChanged(0);

        Fragment_Messenger.save_sharedPrefrence(ChatScreen_User.this, Fragment_Messenger.adapter.userList);

    }

    private void smartReply(String msg) {


        Random random = new Random();
        int randomNumber = random.nextInt(10 - 5) + 5;
        conversation.clear();
        conversation.add(FirebaseTextMessage.createForRemoteUser(
                msg, System.currentTimeMillis(), "sadfsadf"));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseSmartReply smartReply = FirebaseNaturalLanguage.getInstance().getSmartReply();
                try {
                    smartReply.suggestReplies(conversation)
                            .addOnSuccessListener(new OnSuccessListener<SmartReplySuggestionResult>() {
                                @Override
                                public void onSuccess(SmartReplySuggestionResult result) {
                                    if (result.getStatus() == SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE) {
                                        // The conversation's language isn't supported, so the
                                        // the result doesn't contain any suggestions.
                                        Log.d("wdsfafdsa", "STATUS_NOT_SUPPORTED_LANGUAGE: ");

                                    } else if (result.getStatus() == SmartReplySuggestionResult.STATUS_SUCCESS) {
                                        Random random = new Random();
                                        int randomNumber = random.nextInt(3);
                                        try {

                                            insertCustomMsginChats(result.getSuggestions().get(randomNumber).getText().toString(), "mimeType/text", "free", 2, true);
                                        } catch (Exception e) {
                                            Log.d("wdsfafdsa", "Exception: " + e.getMessage());
                                        }

                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@android.support.annotation.NonNull Exception e) {
                                    // Task failed with an exception
                                    // ...
                                }
                            });


                } catch (Exception e) {
                    Log.d("Exceptiondd", "run: " + e.getMessage());
                }


            }
        }, randomNumber * 1000);


    }


    private void handleVoiceMessage() {

        RecordView recordView = (RecordView) findViewById(R.id.record_view);
        RecordButton recordButton = (RecordButton) findViewById(R.id.record_button);
        audioRecorder = new AudioRecorder();


//IMPORTANT
        recordButton.setRecordView(recordView);
//        recordView.setRecordButtonGrowingAnimationEnabled(false);

        recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                //Start Recording..
                ChangeVisiblityMic(true);
                recordFile = new File(getFilesDir(), UUID.randomUUID().toString() + ".3gp");
                if (recordFile.canWrite()) {
                    try {
                        audioRecorder.start(recordFile.getPath());
                    } catch (IOException e) {
                    }
                }

            }

            @Override
            public void onCancel() {
                //On Swipe To Cancel
                ChangeVisiblityMic(false);
                stopRecording(true);

            }

            @Override
            public void onFinish(long recordTime, boolean limitReached) {
                //Stop Recording..
                ChangeVisiblityMic(false);
                stopRecording(false);
                uploadVoiceMessageFirebase(recordFile);

            }

            @Override
            public void onLessThanSecond() {
                //When the record time is less than One Second
                ChangeVisiblityMic(false);
                stopRecording(true);


            }

            @Override
            public void onLock() {
                //When Lock gets activated
            }

        });


        recordView.setOnBasketAnimationEndListener(new OnBasketAnimationEnd() {
            @Override
            public void onAnimationEnd() {
                Log.d("RecordView", "Basket Animation Finished");
            }
        });

    }

    private void ChangeVisiblityMic(boolean micOn) {
        CardView edittextCardView = findViewById(R.id.edittextCardView);
        CardView sendbtnn = findViewById(R.id.sendbtnn);
        RecordView record_view = findViewById(R.id.record_view);
        RecordButton record_button = findViewById(R.id.record_button);
        if (micOn) {
            edittextCardView.setVisibility(View.GONE);
            record_view.setVisibility(View.VISIBLE);
        }
        if (!micOn) {
            edittextCardView.setVisibility(View.VISIBLE);
            record_view.setVisibility(View.GONE);
        }
    }


    private void stopRecording(boolean deleteFile) {
        audioRecorder.stop();
        if (recordFile != null && deleteFile) {
            recordFile.delete();
        }
    }


    private void uploadVoiceMessageFirebase(File audioFile) {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Start the task on the new thread
                StorageReference storageReference = FirebaseStorage.getInstance().getReference("VoiceRecordings/" + modelClass.getUsername() + System.currentTimeMillis());
                Uri audioUri = Uri.fromFile(audioFile);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        insertCustomMsginChats(audioUri.toString(), "mimeType/audio", "premium", 1, false);
                    }
                });

                storageReference.putFile(audioUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot success) {
                        storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> path) {
                                if (path.isSuccessful()) {
                                    String url = path.getResult().toString();
                                    Log.d("RecordView", "uploadVoiceMessageFirebase: " + url);
                                }
                            }
                        });
                    }
                });
            }
        });

        thread.start();


    }

    private void insertAudioinChatBox(Uri audioUri) {

        MediaPlayer mediaPlayer = MediaPlayer.create(ChatScreen_User.this, R.raw.msg_sent_sound);
        mediaPlayer.start();
        Date currentTime = new Date();
        assert audioUri != null;
        Chats_Modelclass chats_modelclass = new Chats_Modelclass("", "mimeType/audio", audioUri.toString(), "", modelClass.getProfileImage(), String.valueOf(currentTime.getTime()), 1);
        chatsArrayList.add(chats_modelclass);
        chatAdapter.notifyItemInserted(chatsArrayList.size() - 1);
        scrollrecycelrvewToBottom();


    }

    private void loadImageFromGallery() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 777);
        }

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 111);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 111 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            Uri saveFiledURI = saveImageTOAppDirectory(imageUri);
            insertCustomMsginChats(saveFiledURI.toString(), "mimeType/image", "premium", 1, false);

            Bitmap bitmap = null;
            try {
                bitmap = resizeImage(imageUri);
                int byteCount = bitmap.getByteCount();
                double kbSize = byteCount / 1024.0; // 1 KB = 1024 bytes
                Log.d("ADSfdsa", "kbSize: " + kbSize);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            uploadImageToFirebaseStorage(bitmap, imageUri);
        }
    }

    private Uri saveImageTOAppDirectory(Uri selectedImageUri) {
        Uri savedImageUri = null;
        try {
            // Create a directory for your app if it doesn't exist.
            File appDirectory = new File(getFilesDir(), "Images");
            if (!appDirectory.exists()) {
                appDirectory.mkdirs();
            }

            // Create a file in your app's directory.
            String fileName = modelClass.getUsername() + System.currentTimeMillis() + ".jpg"; // You can choose any file name.
            File imageFile = new File(appDirectory, fileName);

            // Copy the selected image to your app's directory.
            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            OutputStream outputStream = new FileOutputStream(imageFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();

            // The image is now saved in your app's directory, and you have its URI.
            savedImageUri = Uri.fromFile(imageFile);

            // You can use 'savedImageUri' as needed.

        } catch (IOException e) {
            e.printStackTrace();
        }
        return savedImageUri;
    }

    private Bitmap resizeImage(Uri imageUri) throws IOException {
        InputStream imageStream = getContentResolver().openInputStream(imageUri);
        Bitmap selectedBitmap = BitmapFactory.decodeStream(imageStream);

        int width = selectedBitmap.getWidth();
        int height = selectedBitmap.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = 720;
            height = (int) (width / bitmapRatio);
        } else {
            height = 1280;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(selectedBitmap, width, height, true);
    }

    private void uploadImageToFirebaseStorage(Bitmap bitmap, Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);

            if (inputStream != null) {
                ExifInterface exif = new ExifInterface(inputStream);

                int orientation = ImageResizer.getImageOrientation(imageUri, ChatScreen_User.this);


                // Rotate the image to its default orientation
                Bitmap rotatedBitmap = ImageResizer.rotateBitmap(bitmap, orientation);


                // Create a temporary file to save the rotated image
                File rotatedImageFile = createTempImageFile();
                FileOutputStream outputStream = new FileOutputStream(rotatedImageFile);
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.close();

                // Upload the rotated image to Firebase Storage
                Uri rotatedImageUri = Uri.fromFile(rotatedImageFile);
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                StorageReference imageRef = storageRef.child("customMsgImages/" + modelClass.getUsername() + rotatedImageUri.getLastPathSegment());

                imageRef.putFile(rotatedImageUri).addOnSuccessListener(taskSnapshot -> {
                    // Image uploaded successfully
                    // You can get the download URL of the image
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
//                        Toast.makeText(this, "Uploaded", Toast.LENGTH_SHORT).show();
                    });
                }).addOnFailureListener(exception -> {
                    // Handle any errors that may occur during the upload
                    Toast.makeText(this, "Upload failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to rotate the bitmap based on orientation
    public static Bitmap rotateBitmap(Bitmap sourceBitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return sourceBitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return sourceBitmap;
        }
        try {
            Bitmap rotatedBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight(), matrix, true);
            sourceBitmap.recycle();
            return rotatedBitmap;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return sourceBitmap;
        }
    }

    // Method to create a temporary image file
    private File createTempImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
        return imageFile;
    }


    private void setAnwswerOptions(UserQuestionWithAns userQuestionWithAns) {
        answerslayout = findViewById(R.id.answerslayout);
        ll2 = findViewById(R.id.ll2);


        answerslayout.setVisibility(View.VISIBLE);
        ll2.setVisibility(View.GONE);


        TextView option1, option2;
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);

        option1.setText(userQuestionWithAns.getAnswers().get(0));
        option2.setText(userQuestionWithAns.getAnswers().get(1));

        Date currentTime = new Date();


        option1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Chats_Modelclass chats_modelclass = new Chats_Modelclass(userQuestionWithAns.getAnswers().get(0), "mimeType/text", "", "", modelClass.getProfileImage(), String.valueOf(currentTime.getTime()), 1);
                chatsArrayList.add(chats_modelclass);
                chatAdapter.notifyItemInserted(chatsArrayList.size() - 1);

                modelClass.getQuestionWithAns().setReply(userQuestionWithAns.getAnswers().get(0));
                modelClass.getQuestionWithAns().setRead(1);
                update_userListTemp();

                answerslayout.setVisibility(View.GONE);
                ll2.setVisibility(View.VISIBLE);
            }
        });

        option2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Chats_Modelclass chats_modelclass = new Chats_Modelclass(userQuestionWithAns.getAnswers().get(1), "mimeType/text", "", "", modelClass.getProfileImage(), String.valueOf(currentTime.getTime()), 1);
                chatsArrayList.add(chats_modelclass);
                chatAdapter.notifyItemInserted(chatsArrayList.size() - 1);

                modelClass.getQuestionWithAns().setReply(userQuestionWithAns.getAnswers().get(1));
                modelClass.getQuestionWithAns().setRead(1);
                update_userListTemp();

                answerslayout.setVisibility(View.GONE);
                ll2.setVisibility(View.VISIBLE);
            }
        });


    }

    private void update_userListTemp() {

        for (int i = 0; i < Fragment_Messenger.adapter.userList.size(); i++) {
            if (Fragment_Messenger.adapter.userList.get(i).getUsername().equals(modelClass.getUsername())) {

                Fragment_Messenger.adapter.userList.set(i, modelClass);
                Fragment_Messenger.adapter.notifyItemChanged(i);
            }
        }

        Fragment_Messenger.save_sharedPrefrence(ChatScreen_User.this, Fragment_Messenger.adapter.userList);

    }


    private void scrollrecycelrvewToBottom() {
        NestedScrollView nestedScrollView = findViewById(R.id.nestedScrollview);
        // Replace R.id.recyclerView with the correct ID of your RecyclerView
        if (recylerview == null || chatsArrayList == null || chatsArrayList.size() == 0) {
            return;
        }
        recylerview.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    int lastItemPosition = chatsArrayList.size() - 1;
                    int y = recylerview.getChildAt(lastItemPosition).getTop();
                    nestedScrollView.smoothScrollTo(0, y);
                } catch (Exception e) {
                    // Handle any exception that might occur while scrolling
                    e.printStackTrace();
                }
            }
        }, 500);
    }

    private void load_UnsentMessage() {

        handler = new Handler();
        myRunnable = new Runnable() {
            @Override
            public void run() {
                checkForUpdate();
                // Schedule the task to run again after 1 second
                handler.postDelayed(this, 500);
            }
        };

        myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Start the task on the new thread
                handler.postDelayed(myRunnable, 1000);
            }
        });

        myThread.start();

    }

    private void checkForUpdate() {
        for (int i = 0; i < Fragment_Messenger.adapter.userList.size(); i++) {
            if (Fragment_Messenger.adapter.userList.get(i).getUsername().equals(modelClass.getUsername())) {

                if (modelClass.isContainsQuestion()) {
                    for (int j = 0; j < Fragment_Messenger.adapter.userList.get(i).getQuestionWithAns().getReplyToUser().size(); j++) {
                        UserBotMsg userBotMsg = Fragment_Messenger.adapter.userList.get(i).getQuestionWithAns().getReplyToUser().get(j);

                        if (userBotMsg.getSent() == 1) {
                            if (userBotMsg.getRead() == 0) {
                                Chats_Modelclass chats_modelclass = new Chats_Modelclass(userBotMsg.getMsg(), userBotMsg.getMimeType(), userBotMsg.getExtraMsg(), "", modelClass.getProfileImage(), userBotMsg.getDateTime(), 2);
                                chatsArrayList.add(chats_modelclass);
                                modelClass.getQuestionWithAns().getReplyToUser().get(j).setRead(1);
                                chatAdapter.notifyItemInserted(chatsArrayList.size() - 1);
                                scrollrecycelrvewToBottom();
                            }
                        }
                    }

                } else {


                    for (int j = 0; j < Fragment_Messenger.adapter.userList.get(i).getUserBotMsg().size(); j++) {
                        UserBotMsg userBotMsg = Fragment_Messenger.adapter.userList.get(i).getUserBotMsg().get(j);

                        if (userBotMsg.getSent() == 1) {
                            if (modelClass.getUserBotMsg().get(j).getRead() == 0) {


                                Chats_Modelclass chats_modelclass = new Chats_Modelclass(userBotMsg.getMsg(), userBotMsg.getMimeType(), userBotMsg.getExtraMsg(), "", modelClass.getProfileImage(), userBotMsg.getDateTime(), 2);
                                chatsArrayList.add(chats_modelclass);
                                modelClass.getUserBotMsg().get(j).setRead(1);
                                chatAdapter.notifyItemInserted(chatsArrayList.size() - 1);
                                scrollrecycelrvewToBottom();
                            }
                        }
                    }
                }

            }
        }

    }


    private void actionbar() {
        ImageView backArrow = findViewById(R.id.backArrow);
        ImageView warningSign = findViewById(R.id.warningSign);
        ImageView menuDots = findViewById(R.id.menuDots);
        RelativeLayout alertBar = findViewById(R.id.alertBar);
        TextView viewProfile = findViewById(R.id.viewProfile);
        TextView profileName = findViewById(R.id.profileName);

        ImageView onlineDot = findViewById(R.id.onlineDot);
        isOnline = getIntent().getBooleanExtra("online", false);
        if (isOnline) {
            onlineDot.setVisibility(View.VISIBLE);
        }
        profileName.setText(modelClass.getName());
        profileName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewProfile.performClick();
            }
        });

        ImageView profileImage = findViewById(R.id.profileImage);
        Picasso.get().load(modelClass.getProfileImage()).into(profileImage);
        viewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ChatScreen_User.this, Profile.class);
                intent.putExtra("userName", modelClass.getUsername());
                intent.putExtra("online", true);
                startActivity(intent);
            }
        });
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ChatScreen_User.this, Profile.class);
                intent.putExtra("userName", modelClass.getUsername());
                intent.putExtra("online", true);
                startActivity(intent);
            }
        });


        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        warningSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blockUserDialog();
            }
        });

        menuDots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportUserDialog();
            }
        });

        alertBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertBar.setVisibility(View.INVISIBLE);
            }
        });


    }

    @Override
    public void onBackPressed() {
        // Get the fragment manager
        FragmentManager fragmentManager = ((Activity) ChatScreen_User.this).getFragmentManager();

        // Check if there are any fragments in the back stack
        if (fragmentManager.getBackStackEntryCount() > 0) {
            // If there are fragments in the back stack, pop the top one (close it)
            fragmentManager.popBackStack();
        } else {
            // If there are no fragments in the back stack, perform the default back button behavior
            super.onBackPressed();
        }
    }


    private void blockUserDialog() {

        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(ChatScreen_User.this);
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View promptView = inflater.inflate(R.layout.dialog_block_user, null);
        builder.setView(promptView);
        builder.setCancelable(true);

        TextView confirm = promptView.findViewById(R.id.confirm);
        TextView cancel = promptView.findViewById(R.id.cancel);


        block_user_dialog = builder.create();
        block_user_dialog.show();


        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ChatScreen_User.this, "User blocked succesfully", Toast.LENGTH_SHORT).show();
                block_user_dialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                block_user_dialog.dismiss();
            }
        });

        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        block_user_dialog.getWindow().setBackgroundDrawable(inset);

    }

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


    private void reportUserDialog() {

        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(ChatScreen_User.this);
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View promptView = inflater.inflate(R.layout.dialog_report_user, null);
        builder.setView(promptView);
        builder.setCancelable(true);

        TextView report = promptView.findViewById(R.id.reportBtn);
        ImageView cross = promptView.findViewById(R.id.cross);


        report_user_dialog = builder.create();
        report_user_dialog.show();


        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                report_user_dialog.dismiss();
                reportUserSucessfullDialog();
            }
        });

        cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                report_user_dialog.dismiss();
            }
        });


        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        report_user_dialog.getWindow().setBackgroundDrawable(inset);

    }

    private void reportUserSucessfullDialog() {

        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(ChatScreen_User.this);
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View promptView = inflater.inflate(R.layout.dialog_report_user_sucessfull, null);
        builder.setView(promptView);
        builder.setCancelable(true);

        TextView confirm = promptView.findViewById(R.id.confirm);


        report_userSucessfully_dialog = builder.create();
        report_userSucessfully_dialog.show();


        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ChatScreen_User.this, "User Reported", Toast.LENGTH_SHORT).show();
                report_userSucessfully_dialog.dismiss();
            }
        });


        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        report_userSucessfully_dialog.getWindow().setBackgroundDrawable(inset);

    }


    private void openBottomSheetDialog() {
        BottomSheetDialog bottomSheetDialog;

        bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottomsheetdialog_gifts, null);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();

        send = view.findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rechargeDialog(view.getContext());

            }
        });
        TextView coinCount = view.findViewById(R.id.coin);
        coinCount.setText(String.valueOf(MyApplication.coins));
        TextView topup = view.findViewById(R.id.topup);
        topup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChatScreen_User.this, VipMembership.class));
            }
        });
        TextView problem = findViewById(R.id.problem);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        String[] items = {"Rose", "Penghua", "TeddyBear", "Ring", "CrystalShoes", "LaserBall", "Crown", "Ferrari", "Motorcycle", "Yacht", "Bieshu", "Castle"};

        List<GiftItemModel> itemList = new ArrayList<>();

        for (int i = 0; i < items.length; i++) {
            String item = items[i];
            int coin = 99 + (i * 100); // Calculate the "coin" value based on the index

            GiftItemModel giftItemModel = new GiftItemModel(item, coin, false);
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("gift", item);
            itemMap.put("coin", coin);

            itemList.add(giftItemModel);
        }

        GiftItemAdapter giftItemAdapter = new GiftItemAdapter(ChatScreen_User.this, itemList);
        GridLayoutManager layoutManager = new GridLayoutManager(ChatScreen_User.this, 4);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(giftItemAdapter);

    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the task and terminate the new thread
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (handler != null && handler.hasCallbacks(myRunnable)) {
                handler.removeCallbacks(myRunnable);
            }
        }

        if (myThread != null && myThread.isAlive()) {
            myThread.interrupt();
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 123) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted; proceed with audio recording.
                handleVoiceMessage();
            } else {
                // Permission is denied. You may want to show a message or disable recording functionality.
            }
        }
    }
}

