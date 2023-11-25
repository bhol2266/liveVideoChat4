package com.bhola.livevideochat4;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;


public class Fragment_CustomerCare extends Fragment {

    DatabaseReference chatRef;

    CardView sendbtn;
    EditText textmsg;
    TextView clearMessages;
    String userEmail;
    RecyclerView recyclerview;
    ArrayList<Message_Modelclass> messagesArrayList;
    MessageAdapter messageAdapter;


    public Fragment_CustomerCare() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for context fragment
        View view = inflater.inflate(R.layout.fragment_desi_girls_team, container, false);

        Context context = getContext();
        init(view, context);

        gotoAdminPanel(view,context);

        return view;
    }

    private void gotoAdminPanel(View view, Context context) {

        LinearLayout adminPanel=view.findViewById(R.id.adminPanel);
        adminPanel.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                EditText passwordEdittext;
                Button passwordLoginBtn;


                AlertDialog dialog;

                final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(v.getContext());
                LayoutInflater inflater = LayoutInflater.from(context);
                View promptView = inflater.inflate(R.layout.admin_panel_entry, null);
                builder.setView(promptView);
                builder.setCancelable(true);


                passwordEdittext = promptView.findViewById(R.id.passwordEdittext);
                passwordLoginBtn = promptView.findViewById(R.id.passwordLoginBtn);

                passwordLoginBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (passwordEdittext.getText().toString().equals("5555")) {
                            startActivity(new Intent(context, admin_panel.class));

                        } else {
                            Toast.makeText(v.getContext(), "Enter Password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


                dialog = builder.create();
                dialog.show();
                return false;
            }
        });
    }

    private void init(View view, Context context) {

        Date date = new Date();

        messagesArrayList = new ArrayList<Message_Modelclass>();
        clearMessages = view.findViewById(R.id.clearMessages);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (messagesArrayList.size() == 0) {
                    Message_Modelclass messages1 = new Message_Modelclass("preset", date.getTime(), 3); //viewType 1 is sender 2 is receiver
                    messagesArrayList.add(messages1);
                    messageAdapter.notifyDataSetChanged();
                }
            }
        }, 500);


        sendbtn = view.findViewById(R.id.sendbtnn);
        textmsg = view.findViewById(R.id.textmsg);
        recyclerview = view.findViewById(R.id.recylerview);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setStackFromEnd(true);
        recyclerview.setLayoutManager(linearLayoutManager);
        messageAdapter = new MessageAdapter(context, messagesArrayList, recyclerview, chatRef);
        recyclerview.setAdapter(messageAdapter);

        if (MyApplication.userLoggedIAs.equals("Google")) {
            userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            chatRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userEmail.replace(".", "_")).child("customer_care");

            chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Message_Modelclass messages = dataSnapshot.getValue(Message_Modelclass.class);
                        messagesArrayList.add(messages);
                    }

                    messageAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        } else {
            getArraylist_Sharedpreference(view, context);
        }


        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date date = new Date();

                String message = textmsg.getText().toString();
                if (message.isEmpty()) {
                    Toast.makeText(context, "Enter The Message First", Toast.LENGTH_SHORT).show();
                    return;
                }

                textmsg.setText("");
                Message_Modelclass messages = new Message_Modelclass(message, date.getTime(), 1); //viewType 1 is sender 2 is receiver
                messagesArrayList.add(messages);

                if (MyApplication.userLoggedIAs.equals("Google")) {
                    chatRef.push().setValue(messages, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                // Value was successfully written to the database
                                // Add your success logic here
                                Log.d(MyApplication.TAG, "Data saved successfully.: ");
                            } else {
                                // Failed to write value to the database
                                // Add your failure logic here
                                Log.d(MyApplication.TAG, "Data saved Failed.: " + databaseError.getMessage());

                            }
                        }
                    });
                } else {
                    sharedPreferences(view, context);
                }

                String[] presetsMsg = {"How to be a member?", "Cancel subscription", "Why am I banned", "Report & Complaints"};


                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Message_Modelclass msg = new Message_Modelclass("preset", date.getTime(), 3); //viewType 1 is sender 2 is receiver
                        messagesArrayList.add(msg);
                        recyclerview.scrollToPosition(messagesArrayList.size() - 1);

                        if (MyApplication.userLoggedIAs.equals("Google")) {
                            chatRef.push().setValue(msg, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if (databaseError == null) {
                                        // Value was successfully written to the database
                                        // Add your success logic here
                                        Log.d(MyApplication.TAG, "Data saved successfully.: ");
                                    } else {
                                        // Failed to write value to the database
                                        // Add your failure logic here
                                        Log.d(MyApplication.TAG, "Data saved Failed.: " + databaseError.getMessage());

                                    }
                                }
                            });
                        } else {
                            sharedPreferences(view, context);
                        }
                    }
                }, 1000);

                textmsg.setText("");
                recyclerview.scrollToPosition(messagesArrayList.size() - 1);


                if (messagesArrayList.size() > 4) {
                    clearMessaage(view, context);

                }
            }
        });

    }

    private void clearMessaage(View view, Context context) {
        Date date = new Date();

        clearMessages.setVisibility(View.VISIBLE);
        clearMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messagesArrayList.clear();
                Message_Modelclass msg = new Message_Modelclass("preset", date.getTime(), 3); //viewType 1 is sender 2 is receiver
                messagesArrayList.add(msg);
                recyclerview.scrollToPosition(messagesArrayList.size() - 1);
                if (MyApplication.userLoggedIAs.equals("Google")) {

                    chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                // Remove each child node using the key
                                childSnapshot.getRef().removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } else {
                    sharedPreferences(view, context);
                }
                Toast.makeText(context, "Cleared", Toast.LENGTH_SHORT).show();
                clearMessages.setVisibility(View.GONE);

            }
        });
    }


    private void sharedPreferences(View view, Context context) {

        Gson gson = new Gson();
        String json = gson.toJson(messagesArrayList);

        SharedPreferences preferences = context.getSharedPreferences("customer_care_chats", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("messagesArrayList", json);
        editor.apply();


    }

    private void getArraylist_Sharedpreference(View view, Context context) {

        SharedPreferences preferences = context.getSharedPreferences("customer_care_chats", Context.MODE_PRIVATE);
        String json = "";
        json = preferences.getString("messagesArrayList", "");
        Type type = new TypeToken<ArrayList<Message_Modelclass>>() {
        }.getType();
        Gson gson = new Gson();
        ArrayList<Message_Modelclass> myObjectList = gson.fromJson(json, type);


        if (json.length() > 20) {
            messagesArrayList.clear();
            messagesArrayList.addAll(myObjectList);
        }

    }
}


class MessageAdapter extends RecyclerView.Adapter {
    Context context;
    ArrayList<Message_Modelclass> messagesAdpterArrayList;
    int SENDER = 1;
    int RECEIVER = 2;
    int RECEIVER_Template = 3;
    RecyclerView recyclerview;
    DatabaseReference chatRef;

    public MessageAdapter(Context context, ArrayList<Message_Modelclass> messagesAdpterArrayList, RecyclerView recyclerview, DatabaseReference chatRef) {
        this.context = context;
        this.messagesAdpterArrayList = messagesAdpterArrayList;
        this.recyclerview = recyclerview;
        this.chatRef = chatRef;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENDER) {
            View view = LayoutInflater.from(context).inflate(R.layout.sender_layout, parent, false);
            return new SenderVierwHolder(view);
        } else if (viewType == RECEIVER_Template) {
            View view = LayoutInflater.from(context).inflate(R.layout.reciver_template_layout, parent, false);
            return new ReciverTemplateViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.reciver_layout, parent, false);
            return new ReciverViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message_Modelclass messages = messagesAdpterArrayList.get(position);

        Date date = new Date(messages.getTimeStamp());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault());
        String formattedDate = sdf.format(date);


        if (messages.getViewType() == 1) {
            SenderVierwHolder senderVierwHolder = (MessageAdapter.SenderVierwHolder) holder;
            senderVierwHolder.msgtxt.setText(messages.getMessage());
            senderVierwHolder.timeStamp.setText(formattedDate);
senderVierwHolder.errorLayout.setVisibility(View.GONE);

            if (MyApplication.userLoggedIAs.equals("Google")) {
                SharedPreferences sh = context.getSharedPreferences("UserInfo", MODE_PRIVATE);
                String urll = sh.getString("photoUrl", "not set");
                Picasso.get()
                        .load(urll)
                        .into(senderVierwHolder.profile);
            }

        } else if (messages.getViewType() == 3) {
            ReciverTemplateViewHolder reciverTemplateViewHolder = (ReciverTemplateViewHolder) holder;


            reciverTemplateViewHolder.msgClick0.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    optionClicked(reciverTemplateViewHolder.msgClick0.getText(), "Click[Profile]->[Membership center], and then purchase the corresponding membership package as needed");

                }
            });
            reciverTemplateViewHolder.msgClick1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    optionClicked(reciverTemplateViewHolder.msgClick1.getText(), "You can unsubscribe in Google Pay if necessary");

                }
            });
            reciverTemplateViewHolder.msgClick2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    optionClicked(reciverTemplateViewHolder.msgClick2.getText(), "You can check the most common reasons to be banned:\na) creating fake account\nb) sending in appropriate texts or picture\nc) asking others for money\nd)teenagers under 18: According to the rules and laws, this app could only be used by adult over 18 years old\\ne) selling or recommending other websites, or introducing users to other sites\nf) a user will be banned when we receive multiple reports from other users");

                }
            });

            reciverTemplateViewHolder.msgClick3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    optionClicked(reciverTemplateViewHolder.msgClick3.getText(), "In order to solve your problem, we need you to provide more detailed information, ");

                }
            });
        } else {

            ReciverViewHolder reciverViewHolder = (MessageAdapter.ReciverViewHolder) holder;
            reciverViewHolder.msgtxt.setText(messages.getMessage());


            if (messages.getMessage().equals("In order to solve your problem, we need you to provide more detailed information, ")) {
                reciverViewHolder.submitFeedback.setVisibility(View.VISIBLE);
                reciverViewHolder.submitFeedback.setPaintFlags(reciverViewHolder.submitFeedback.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                reciverViewHolder.submitFeedback.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        context.startActivity(new Intent(context, Feedback.class));

                    }
                });
            }
        }
    }


    private void optionClicked(CharSequence text, String message) {
        Date date = new Date();
        Message_Modelclass messages = new Message_Modelclass((String) text, date.getTime(), 1); //viewType 1 is sender 2 is receiver
        messagesAdpterArrayList.add(messages);
        insertToDataBase(messages);
        recyclerview.scrollToPosition(messagesAdpterArrayList.size() - 1);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Date date = new Date();
                Message_Modelclass messages = new Message_Modelclass(message, date.getTime(), 2); //viewType 1 is sender 2 is receiver
                messagesAdpterArrayList.add(messages);
                insertToDataBase(messages);
                recyclerview.scrollToPosition(messagesAdpterArrayList.size() - 1);

            }
        }, 1000);

    }


    private void insertToDataBase(Message_Modelclass messages) {
        if (MyApplication.userLoggedIAs.equals("Google")) {

            String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            chatRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userEmail.replace(".", "_")).child("customer_care");

            chatRef.push().setValue(messages, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        // Value was successfully written to the database
                        // Add your success logic here
                        Log.d(MyApplication.TAG, "Data saved successfully.: ");
                    } else {
                        // Failed to write value to the database
                        // Add your failure logic here
                        Log.d(MyApplication.TAG, "Data saved Failed.: " + databaseError.getMessage());

                    }
                }
            });
        } else {
            sharedPreferences();
        }

    }

    private void sharedPreferences() {

        Gson gson = new Gson();
        String json = gson.toJson(messagesAdpterArrayList);

        SharedPreferences preferences = context.getSharedPreferences("customer_care_chats", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("messagesArrayList", json);
        editor.apply();


    }

    @Override
    public int getItemCount() {
        return messagesAdpterArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message_Modelclass messages = messagesAdpterArrayList.get(position);
        if (messages.getViewType() == 1) {
            return SENDER;
        } else if (messages.getViewType() == 3) {
            return RECEIVER_Template;
        } else {
            return RECEIVER;
        }
    }

    static class SenderVierwHolder extends RecyclerView.ViewHolder {
        CircleImageView profile;
        TextView msgtxt;
        TextView timeStamp;
        FrameLayout errorLayout;

        public SenderVierwHolder(@NonNull View itemView) {
            super(itemView);

            profile = itemView.findViewById(R.id.profileImage);
            msgtxt = itemView.findViewById(R.id.message);
            timeStamp = itemView.findViewById(R.id.timeStamp);
            errorLayout = itemView.findViewById(R.id.errorLayout);

        }
    }

    static class ReciverViewHolder extends RecyclerView.ViewHolder {
        TextView msgtxt, submitFeedback;


        public ReciverViewHolder(@NonNull View itemView) {
            super(itemView);
            msgtxt = itemView.findViewById(R.id.message);
            submitFeedback = itemView.findViewById(R.id.submitFeedback);


        }
    }

    static class ReciverTemplateViewHolder extends RecyclerView.ViewHolder {

        TextView msgClick0, msgClick1, msgClick2, msgClick3;

        public ReciverTemplateViewHolder(@NonNull View itemView) {
            super(itemView);
            msgClick0 = itemView.findViewById(R.id.msgClick0);
            msgClick1 = itemView.findViewById(R.id.msgClick1);
            msgClick2 = itemView.findViewById(R.id.msgClick2);
            msgClick3 = itemView.findViewById(R.id.msgClick3);

        }
    }

}


class Message_Modelclass {
    String message;
    long timeStamp;
    int viewType;//viewType 1 is sender 2 is receiver

    public Message_Modelclass() {
    }

    public Message_Modelclass(String message, long timeStamp, int viewType) {
        this.message = message;
        this.timeStamp = timeStamp;
        this.viewType = viewType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }
}

