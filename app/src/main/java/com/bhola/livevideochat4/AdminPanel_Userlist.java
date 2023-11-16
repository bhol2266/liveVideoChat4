package com.bhola.livevideochat4;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class AdminPanel_Userlist extends AppCompatActivity {
    RecyclerView recyclerView;
   public static AdminUserlistAdapter adapter;
    ArrayList<UserModel> userlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel_userlist);

        userlist = new ArrayList<>();
        setupRecyclerView();
        searchId();
        queryBtns();

    }

    private void queryBtns() {
        TextView latestUsers = findViewById(R.id.latestUsers);
        latestUsers.setOnClickListener(view -> {
            getUserDetails("latest_users");
        });
        TextView femaleUsers = findViewById(R.id.femaleUsers);
        femaleUsers.setOnClickListener(view -> {
            getUserDetails("female_users");
        });

        TextView maleUsers = findViewById(R.id.maleUsers);
        maleUsers.setOnClickListener(view -> {
            getUserDetails("male_users");
        });
        TextView streamers = findViewById(R.id.streamers);
        streamers.setOnClickListener(view -> {
            getUserDetails("streamers");
        });

    }

    private void searchId() {
        EditText searchTerm = findViewById(R.id.searchTerm);
        TextView searchBtn = findViewById(R.id.searchBtn);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (searchTerm.getText().toString().length() < 5) {
                    Toast.makeText(AdminPanel_Userlist.this, "search key is empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                getUserFromId(Integer.parseInt(searchTerm.getText().toString()));

            }
        });

    }

    void setupRecyclerView() {
        getUserDetails("latest_users");
        recyclerView = findViewById(R.id.recyclerview);
        adapter = new AdminUserlistAdapter(AdminPanel_Userlist.this, userlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(AdminPanel_Userlist.this));
        recyclerView.setAdapter(adapter);


    }


    public void getUserDetails(String query_term) {
        Utils utils = new Utils();
        utils.showLoadingDialog(AdminPanel_Userlist.this, "loading..." + query_term);
        userlist.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("Users");

        Query query = null;
        if (query_term.equals("latest_users")) {
            query = usersRef.orderBy("date", Query.Direction.DESCENDING).limit(100);

        }
        if (query_term.equals("male_users")) {
            query = usersRef.orderBy("date", Query.Direction.DESCENDING).whereEqualTo("selectedGender", "male").limit(100);

        }
        if (query_term.equals("female_users")) {
            query = usersRef.orderBy("date", Query.Direction.DESCENDING).whereEqualTo("selectedGender", "female").limit(100);

        }
        if (query_term.equals("streamers")) {
            query = usersRef.orderBy("date", Query.Direction.DESCENDING).whereEqualTo("streamer", true).limit(100);

        }


        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        UserModel userModel = document.toObject(UserModel.class);
                        userlist.add(userModel);
                        adapter.notifyDataSetChanged();
                        utils.dismissLoadingDialog();
                    }

                } else {
                    utils.dismissLoadingDialog();

                    Log.d("dsafsadfsdaf", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    public void getUserFromId(int userId) {

        Utils utils = new Utils();
        utils.showLoadingDialog(AdminPanel_Userlist.this, "loading..." + userId);
        userlist.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("Users");

        Query query = usersRef.orderBy("date", Query.Direction.DESCENDING).whereEqualTo("userId", userId);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        UserModel userModel = document.toObject(UserModel.class);
                        userlist.add(userModel);
                        adapter.notifyDataSetChanged();
                        utils.dismissLoadingDialog();
                    }

                } else {
                    utils.dismissLoadingDialog();
                    Toast.makeText(AdminPanel_Userlist.this, "not found", Toast.LENGTH_SHORT).show();
                    Log.d("dsafsadfsdaf", "Error getting documents: ", task.getException());
                }
            }
        });


    }

}