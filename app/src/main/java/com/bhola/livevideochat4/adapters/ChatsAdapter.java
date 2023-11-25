package com.bhola.livevideochat4.adapters;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bhola.livevideochat4.ChatScreen_User;
import com.bhola.livevideochat4.Fragment_LargePhotoViewer;
import com.bhola.livevideochat4.Models.ChatItem_ModelClass;
import com.bhola.livevideochat4.Models.Chats_Modelclass;
import com.bhola.livevideochat4.MyApplication;
import com.bhola.livevideochat4.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsAdapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<Chats_Modelclass> chatsArrayList;
    int SENDER = 1; // mobile user
    int RECEIVER = 2; // from outside
    RecyclerView recyclerview;
    MediaPlayer mediaPlayer;
    ChatItem_ModelClass modelClass;

    public ChatsAdapter(Context context, ArrayList<Chats_Modelclass> chatsArrayList, RecyclerView recyclerview, MediaPlayer mediaPlayer, ChatItem_ModelClass modelClass) {
        this.context = context;
        this.chatsArrayList = chatsArrayList;
        this.recyclerview = recyclerview;
        this.mediaPlayer = mediaPlayer;
        this.modelClass = modelClass;

    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENDER) {
            View view = LayoutInflater.from(context).inflate(R.layout.sender_layout, parent, false);
            return new SenderVierwHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.userchat_reciver_layout, parent, false);
            return new ReciverViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Chats_Modelclass chats = chatsArrayList.get(position);


        long timestamp = Long.parseLong(chats.getTimeStamp()); // Example timestamp value

        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm MM-dd");
        String formattedDate = sdf.format(date);

        if (chats.getViewType() == 2) {
            ChatsAdapter.ReciverViewHolder reciverViewHolder = (ReciverViewHolder) holder;
            reciverViewHolder.timeStamp.setText(formattedDate);
            Picasso.get().load(chats.getProfileUrl()).into(reciverViewHolder.profileImage);

            if (chats.getMessageType().equals("mimeType/text")) {
                reciverViewHolder.textMsg.setText(chats.getMessage());
                reciverViewHolder.picMsgLayout.setVisibility(View.GONE);
                reciverViewHolder.audioMsg.setVisibility(View.GONE);
                translateMessage(chats.getMessage(), reciverViewHolder.translatedMessage,reciverViewHolder.translatedMessageDivider);

            }
            if (chats.getMessageType().equals("mimeType/audio")) {
                reciverViewHolder.picMsgLayout.setVisibility(View.GONE);
                reciverViewHolder.audioMsg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            return;
                        }
                        try {
                            reciverViewHolder.audioProgressBar.setVisibility(View.VISIBLE);
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build());
                            mediaPlayer.setDataSource(chats.getExtraMsg());
                            mediaPlayer.prepareAsync();
                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    reciverViewHolder.audioProgressBar.setVisibility(View.GONE);
                                    reciverViewHolder.playAudiolottie.playAnimation();
                                    mediaPlayer.start();
                                }
                            });
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {
                                    reciverViewHolder.playAudiolottie.cancelAnimation();
                                    mediaPlayer.stop();

                                }
                            }); // Set the OnCompletionListener


                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                reciverViewHolder.textMsgLayout.setVisibility(View.GONE);

            }

            if (chats.getMessageType().equals("mimeType/image")) {
                Picasso.get().load(chats.getExtraMsg()).into(reciverViewHolder.picMsg);
                reciverViewHolder.textMsgLayout.setVisibility(View.GONE);
                reciverViewHolder.audioMsg.setVisibility(View.GONE);

                reciverViewHolder.picMsgLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ArrayList<Map<String, String>> imageList = new ArrayList<>();

                        for (int i = 0; i < modelClass.getUserBotMsg().size(); i++) {
                            if (modelClass.getUserBotMsg().get(i).getMimeType().equals("mimeType/image") && modelClass.getUserBotMsg().get(i).getSent() == 1) {
                                Map<String, String> stringMap2 = new HashMap<>();
                                stringMap2.put("url", modelClass.getUserBotMsg().get(i).getExtraMsg());
                                stringMap2.put("type", "free");
                                imageList.add(stringMap2);
                            }
                        }
                        if (modelClass.isContainsQuestion()) {
                            for (int i = 0; i < modelClass.getQuestionWithAns().getReplyToUser().size(); i++) {
                                if (modelClass.getQuestionWithAns().getReplyToUser().get(i).getMimeType().equals("mimeType/image") && modelClass.getQuestionWithAns().getReplyToUser().get(i).getSent() == 1) {
                                    Map<String, String> stringMap2 = new HashMap<>();
                                    stringMap2.put("url", modelClass.getQuestionWithAns().getReplyToUser().get(i).getExtraMsg());
                                    stringMap2.put("type", "free");
                                    imageList.add(stringMap2);
                                }
                            }
                        }

                        int index = 0;
                        for (int i = 0; i < imageList.size(); i++) {
                            if (imageList.get(i).get("url").equals(chats.getExtraMsg())) {
                                index = i;
                            }
                        }


                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        int originalScreenWidth = displayMetrics.widthPixels;
                        int screenHeight = displayMetrics.heightPixels;


                        Log.d("SDfsd", "onClick: " + imageList.size());
                        // Decrease the screen width by 15%
                        int screenWidth = (int) (originalScreenWidth * 0.85);
                        Fragment_LargePhotoViewer fragment = Fragment_LargePhotoViewer.newInstance(context, (ArrayList<Map<String, String>>) imageList, index, screenWidth, screenHeight);

                        FragmentManager fragmentManager = ((Activity) context).getFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment) // Replace with your container ID
                                .addToBackStack(null) // Optional, for back navigation
                                .commit();

                    }
                });


            }

        }

        if (chats.getViewType() == 1) {
            ChatsAdapter.SenderVierwHolder senderVierwHolder = (ChatsAdapter.SenderVierwHolder) holder;

            if (chats.getMessageType().equals("mimeType/text")) {
                senderVierwHolder.textMsg.setText(chats.getMessage());
                senderVierwHolder.picMsgLayout.setVisibility(View.GONE);
                senderVierwHolder.audioMsg.setVisibility(View.GONE);


            }
            if (chats.getMessageType().equals("mimeType/audio")) {
                senderVierwHolder.picMsgLayout.setVisibility(View.GONE);
                senderVierwHolder.audioMsg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            return;
                        }

                        try {
                            senderVierwHolder.audioProgressBar.setVisibility(View.VISIBLE);
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build());

                            String audioSource = chats.getExtraMsg(); // Get the audio source (URL or URI as a string)

                            // Check if the audio source is a URL or a local file URI
                            if (audioSource.startsWith("http")) {
                                // It's an audio URL
                                mediaPlayer.setDataSource(audioSource);
                            } else {
                                // It's a local file URI as a string, so convert it back to a Uri
                                Uri audioUri = Uri.parse(audioSource);
                                mediaPlayer.setDataSource(context, audioUri);
                            }

                            mediaPlayer.prepareAsync();
                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    senderVierwHolder.audioProgressBar.setVisibility(View.GONE);
                                    senderVierwHolder.playAudiolottie.playAnimation();
                                    mediaPlayer.start();
                                }
                            });
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {
                                    senderVierwHolder.playAudiolottie.cancelAnimation();
                                    mediaPlayer.stop();
                                }
                            }); // Set the OnCompletionListener
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                senderVierwHolder.textMsg.setVisibility(View.GONE);
                senderVierwHolder.audioMsgLayout.setVisibility(View.VISIBLE);
            }
            if (chats.getMessageType().equals("mimeType/image")) {

                senderVierwHolder.picMsgLayout.setVisibility(View.VISIBLE);

                Log.d("asdf", "onBindViewHolder: " + chats.getExtraMsg());
                if (chats.getExtraMsg().startsWith("http")) {
                    Picasso.get().load(chats.getExtraMsg()).into(senderVierwHolder.picMsg);

                } else {
                    try {
                        Bitmap bitmap = checkOrientation(Uri.parse(chats.getExtraMsg())); //change orientation to default
                        senderVierwHolder.picMsg.setImageBitmap(bitmap);

                    } catch (Exception e) {
                    }
                }

                senderVierwHolder.textMsg.setVisibility(View.GONE);
                senderVierwHolder.audioMsg.setVisibility(View.GONE);

                senderVierwHolder.picMsgLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ArrayList<Map<String, String>> imageList = new ArrayList<>();
                        for (int i = 0; i < modelClass.getUserBotMsg().size(); i++) {
                            if (modelClass.getUserBotMsg().get(i).getMimeType().equals("mimeType/image") && modelClass.getUserBotMsg().get(i).getSent() == 1) {
                                Map<String, String> stringMap2 = new HashMap<>();
                                stringMap2.put("url", modelClass.getUserBotMsg().get(i).getExtraMsg());
                                stringMap2.put("type", "free");
                                imageList.add(stringMap2);
                            }
                        }
                        if (modelClass.isContainsQuestion()) {
                            for (int i = 0; i < modelClass.getQuestionWithAns().getReplyToUser().size(); i++) {
                                if (modelClass.getQuestionWithAns().getReplyToUser().get(i).getMimeType().equals("mimeType/image") && modelClass.getQuestionWithAns().getReplyToUser().get(i).getSent() == 1) {
                                    Map<String, String> stringMap2 = new HashMap<>();
                                    stringMap2.put("url", modelClass.getQuestionWithAns().getReplyToUser().get(i).getExtraMsg());
                                    stringMap2.put("type", "free");
                                    imageList.add(stringMap2);
                                }
                            }
                        }

                        int index = 0;
                        for (int i = 0; i < imageList.size(); i++) {
                            if (imageList.get(i).get("url").equals(chats.getExtraMsg())) {
                                index = i;
                            }
                        }


                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        int originalScreenWidth = displayMetrics.widthPixels;
                        int screenHeight = displayMetrics.heightPixels;


                        Log.d("SDfsd", "onClick: " + imageList.size());
                        // Decrease the screen width by 15%
                        int screenWidth = (int) (originalScreenWidth * 0.85);
                        Fragment_LargePhotoViewer fragment = Fragment_LargePhotoViewer.newInstance(context, (ArrayList<Map<String, String>>) imageList, index, screenWidth, screenHeight);

                        FragmentManager fragmentManager = ((Activity) context).getFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment) // Replace with your container ID
                                .addToBackStack(null) // Optional, for back navigation
                                .commit();

                    }
                });


            }

            senderVierwHolder.timeStamp.setText(formattedDate);


            if (MyApplication.userLoggedIAs.equals("Google")) {
                SharedPreferences sh = context.getSharedPreferences("UserInfo", MODE_PRIVATE);
                String urll = sh.getString("photoUrl", "not set");
                Picasso.get().load(urll).into(senderVierwHolder.profile);
            }

            updateErrorIcon(senderVierwHolder.errorLayout, senderVierwHolder.errorIcon, chats.getChatType());
        }

    }

    private void translateMessage(String message, TextView translatedMessage, View translatedMessageDivider) {
        translatedMessage.setVisibility(View.GONE);
        translatedMessageDivider.setVisibility(View.GONE);
//        LanguageTranslateAPI.postData(context, message, "hi", translatedMessage);

    }

    private Bitmap checkOrientation(Uri imageUri) {

        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(imageUri);
            ExifInterface exif = new ExifInterface(inputStream);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
            Bitmap rotatedBitmap = ChatScreen_User.rotateBitmap(originalBitmap, orientation);

            return rotatedBitmap;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateErrorIcon(FrameLayout errorLayout, ImageView errorIcon, String chatType) {
        if (!chatType.equals("premium") || MyApplication.coins > 0) {
            errorLayout.setVisibility(View.GONE);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                errorIcon.setVisibility(View.VISIBLE);
                errorIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ChatScreen_User.rechargeDialog(view.getContext());
                    }
                });
            }
        }, 3000);
    }

    @Override
    public int getItemCount() {
        return chatsArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Chats_Modelclass messages = chatsArrayList.get(position);
        if (messages.getViewType() == 1) {
            return SENDER;
        } else {
            return RECEIVER;
        }
    }

    static class SenderVierwHolder extends RecyclerView.ViewHolder {
        CircleImageView profile;
        TextView textMsg;
        TextView timeStamp;
        ImageView picMsg;
        CardView audioMsg;
        FrameLayout picMsgLayout;
        LottieAnimationView playAudiolottie;
        ProgressBar audioProgressBar;
        FrameLayout errorLayout;
        ImageView errorIcon;
        LinearLayout audioMsgLayout;

        public SenderVierwHolder(@NonNull View itemView) {
            super(itemView);

            profile = itemView.findViewById(R.id.profileImage);
            textMsg = itemView.findViewById(R.id.message);
            timeStamp = itemView.findViewById(R.id.timeStamp);

            picMsg = itemView.findViewById(R.id.picMsg);
            audioMsg = itemView.findViewById(R.id.audioMsg);
            picMsgLayout = itemView.findViewById(R.id.picMsgLayout);
            playAudiolottie = itemView.findViewById(R.id.playAudiolottie);
            audioProgressBar = itemView.findViewById(R.id.audioProgressBar);
            errorLayout = itemView.findViewById(R.id.errorLayout);
            errorIcon = itemView.findViewById(R.id.errorIcon);
            audioMsgLayout = itemView.findViewById(R.id.audioMsgLayout);

        }
    }

    static class ReciverViewHolder extends RecyclerView.ViewHolder {
        TextView textMsg, translatedMessage, timeStamp;
        ImageView picMsg, profileImage;
        CardView audioMsg;
        FrameLayout picMsgLayout;
        LottieAnimationView playAudiolottie;
        ProgressBar audioProgressBar;
        View translatedMessageDivider;
        LinearLayout textMsgLayout;


        public ReciverViewHolder(@NonNull View itemView) {
            super(itemView);
            textMsg = itemView.findViewById(R.id.textMsg);
            picMsg = itemView.findViewById(R.id.picMsg);
            audioMsg = itemView.findViewById(R.id.audioMsg);
            timeStamp = itemView.findViewById(R.id.timeStamp);
            profileImage = itemView.findViewById(R.id.profileImage);
            picMsgLayout = itemView.findViewById(R.id.picMsgLayout);
            playAudiolottie = itemView.findViewById(R.id.playAudiolottie);
            audioProgressBar = itemView.findViewById(R.id.audioProgressBar);
            textMsgLayout = itemView.findViewById(R.id.textMsgLayout);
            translatedMessage = itemView.findViewById(R.id.translatedMessage);
            translatedMessageDivider = itemView.findViewById(R.id.translatedMessageDivider);

        }
    }


}


