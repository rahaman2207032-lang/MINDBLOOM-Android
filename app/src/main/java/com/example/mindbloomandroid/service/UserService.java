package com.example.mindbloomandroid.service;



import com.google.firebase.database.DataSnapshot;
import com.google. firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com. google.firebase.database.FirebaseDatabase;
import com.google. firebase.database.ValueEventListener;
import com.example.mindbloomandroid.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserService {
    private DatabaseReference usersRef;

    public UserService() {
        usersRef = FirebaseDatabase. getInstance().getReference("users");
    }


    public void getUserById(String userId, OnUserLoadedListener listener) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        user.setUserId(dataSnapshot.getKey());
                        listener.onUserLoaded(user);
                    } else {
                        listener.onError("User data is null");
                    }
                } else {
                    listener. onError("User not found");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                listener.onError(error. getMessage());
            }
        });
    }


    public void updateUser(User user, OnCompleteListener listener) {
        usersRef.child(user.getUserId()).setValue(user)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }


    public void getAllInstructors(OnInstructorsLoadedListener listener) {
        usersRef.orderByChild("role").equalTo("Instructor")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<User> instructors = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot. getChildren()) {
                            User user = snapshot.getValue(User. class);
                            if (user != null) {
                                user.setUserId(snapshot.getKey());
                                instructors.add(user);
                            }
                        }
                        listener.onInstructorsLoaded(instructors);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }


    public void getAllUsers(OnUsersLoadedListener listener) {
        usersRef.orderByChild("role").equalTo("User")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<User> users = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                user.setUserId(snapshot. getKey());
                                users. add(user);
                            }
                        }
                        listener.onUsersLoaded(users);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError(error. getMessage());
                    }
                });
    }

    // Interfaces
    public interface OnUserLoadedListener {
        void onUserLoaded(User user);
        void onError(String error);
    }

    public interface OnInstructorsLoadedListener {
        void onInstructorsLoaded(List<User> instructors);
        void onError(String error);
    }

    public interface OnUsersLoadedListener {
        void onUsersLoaded(List<User> users);
        void onError(String error);
    }

    public interface OnCompleteListener {
        void onSuccess();
        void onError(String error);
    }
}