package com.bhola.livevideochat4;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AdminUserlistAdapter extends RecyclerView.Adapter<AdminUserlistAdapter.GridViewHolder> {

    public static List<UserModel> userlist;
    private final Context context;


    public AdminUserlistAdapter(Context context, ArrayList<UserModel> userlist) {
        this.context = context;
        this.userlist = userlist;
    }


    @androidx.annotation.NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adminpanel_user_item, parent, false);
        return new GridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull GridViewHolder holder, int position) {
        UserModel item = userlist.get(position);

        if (item.getProfilepic().length() < 10) {
            if (item.getSelectedGender().equals("male")) {
                holder.profileImage.setImageResource(R.drawable.male);
            } else {
                holder.profileImage.setImageResource(R.drawable.female_logo);
            }
        } else {
            Picasso.get().load(item.getProfilepic()).into(holder.profileImage);
        }
        holder.name.setText(item.getFullname());
        holder.id.setText(String.valueOf(item.getUserId()));

        SimpleDateFormat dateFormat = new SimpleDateFormat("E MMM dd HH:mm");

        // Format the date using the SimpleDateFormat
        String formattedDate = dateFormat.format(item.getDate());

        holder.timestamp.setText(formattedDate);
        holder.coins.setText(String.valueOf(item.getCoins()) + " Coins");

        if (item.isStreamer()) {
            int tintColor = ContextCompat.getColor(context, R.color.green); // Use your desired color resource
            holder.streamerTick.setColorFilter(tintColor);
        } else {
            holder.streamerTick.clearColorFilter();

        }

        if (item.isBanned()) {
            int tintColor = ContextCompat.getColor(context, R.color.themeColor); // Use your desired color resource
            holder.ban.setColorFilter(tintColor);
        } else {
            holder.ban.clearColorFilter();
        }

        holder.ban.setOnClickListener(view -> {
            updateProfile(String.valueOf(item.getUserId()), "banned", !item.isBanned());
            item.setBanned(!item.isBanned());
            AdminPanel_Userlist.adapter.notifyDataSetChanged();

        });

        holder.streamerTick.setOnClickListener(view -> {
            updateProfile(String.valueOf(item.getUserId()), "streamer", !item.isStreamer());
            item.setStreamer(!item.isStreamer());
            AdminPanel_Userlist.adapter.notifyDataSetChanged();

        });

        holder.profileImage.setOnClickListener(view -> {
            String userModelJson = new Gson().toJson(item); // Using Google's Gson library for JSON serialization
            Intent intent = new Intent(context, Profile2.class);
            intent.putExtra("userModelJson", userModelJson);
            context.startActivity(intent);
        });
        holder.delete.setOnClickListener(view -> {
            if (item.getUserId() == MyApplication.userModel.getUserId()) {
                Toast.makeText(context, "you cannot delete yourself", Toast.LENGTH_SHORT).show();
                return;
            }
            createConfirmDialog(item.getUserId());
        });

    }

    private void createConfirmDialog(int userId) {


        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View promptView = inflater.inflate(R.layout.dialog_deleteuser_comfirm, null);
        builder.setView(promptView);
        builder.setCancelable(true);

        TextView id = promptView.findViewById(R.id.id);
        TextView confirm = promptView.findViewById(R.id.confirm);


        AlertDialog deleteConfirmDialog = builder.create();
        deleteConfirmDialog.show();

        id.setText(String.valueOf(userId));
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteUserFromDatabase(userId);
                deleteConfirmDialog.dismiss();
            }
        });


        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        deleteConfirmDialog.getWindow().setBackgroundDrawable(inset);


    }

    private void deleteUserFromDatabase(int userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("Users");
        CollectionReference ChatroomRef = db.collection("Chatrooms");
        CollectionReference CallroomRef = db.collection("Call_logs");




        FirebaseUtil.deleteFolderAndContents(String.valueOf(userId));


        usersRef.document(String.valueOf(userId)).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });

        for( int i=0;i<userlist.size();i++){
            UserModel userModel=userlist.get(i);
            if(userModel.getUserId() == userId){
                userlist.remove(i);
                AdminPanel_Userlist.adapter.notifyDataSetChanged();
                break;
            }
        }
    }

    public void updateProfile(String userId, String key, boolean value) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("Users");
        DocumentReference userDocRef = usersRef.document(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put(key, value);

        userDocRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, key + " updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that might occur during the update
                });

    }


    @Override
    public int getItemCount() {
        return userlist.size();
    }


    public static class GridViewHolder extends RecyclerView.ViewHolder {
        TextView name, id, timestamp, coins;
        ImageView profileImage, streamerTick, ban, delete;
        RelativeLayout userCard;

        public GridViewHolder(@androidx.annotation.NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            name = itemView.findViewById(R.id.name);
            timestamp = itemView.findViewById(R.id.timestamp);
            streamerTick = itemView.findViewById(R.id.streamerTick);
            ban = itemView.findViewById(R.id.ban);
            id = itemView.findViewById(R.id.id);
            coins = itemView.findViewById(R.id.coins);
            delete = itemView.findViewById(R.id.delete);
            userCard = itemView.findViewById(R.id.card);


        }
    }

}






