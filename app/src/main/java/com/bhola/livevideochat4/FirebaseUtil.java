package com.bhola.livevideochat4;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class FirebaseUtil {





    public static void deleteFolderAndContents(String userID) {
        FirebaseStorage storage = FirebaseStorage.getInstance();


        // Create a reference to the folder
        StorageReference folderRef = storage.getReference().child("Users").child(userID);

        // List all items in the folder
        Task<ListResult> listTask = folderRef.listAll();
        listTask.addOnSuccessListener(listResult -> {

            //delete files inside videoRecording , chatimages, gallery
            for (StorageReference reference : listResult.getPrefixes()) {
                Task<ListResult> listTask2 = reference.listAll();
                listTask2.addOnSuccessListener(listResult1 -> {
                    for (StorageReference item : listResult1.getItems()) {
                        item.delete();
                    }
                });
            }

            // Delete profile image
            for (StorageReference item : listResult.getItems()) {
                item.delete();
            }
        });
    }

    public static void addUserCoins(int newCoins) {

        int currentCoin = MyApplication.userModel.getCoins();
        int coinsAfterAdding = currentCoin + newCoins;
        MyApplication.userModel.setCoins(coinsAfterAdding);
        updateUserCoinsonFireStore(coinsAfterAdding);

    }

    public static void updateUserCoinsonFireStore(int value) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("Users");
        String userId = String.valueOf(MyApplication.userModel.getUserId()); // Replace with the actual user ID
        DocumentReference userDocRef = usersRef.document(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("coins", value);

        userDocRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    // The field(s) were successfully updated
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that might occur during the update
                });

    }

}









