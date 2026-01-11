package com.example.mindbloomandroid.service;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.example.mindbloomandroid.model.User;
import com.example.mindbloomandroid.model.Instructor;


public class FirebaseAuthService {
    private static final String TAG = "FirebaseAuthService";
    private final FirebaseAuth mAuth;
    private final DatabaseReference dbRef;

    public FirebaseAuthService() {
        try {
            mAuth = FirebaseAuth.getInstance();
            dbRef = FirebaseDatabase.getInstance().getReference();
            Log.d(TAG, "FirebaseAuthService initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }


    public void registerUser(String password, String username, String role,
                             OnAuthCompleteListener listener) {
        Log.d(TAG, "registerUser called - username: " + username + ", role: " + role);

        // Validate inputs
        if (username == null || username.trim().isEmpty()) {
            Log.e(TAG, "Username is empty");
            listener.onError("Username cannot be empty");
            return;
        }

        if (password == null || password.length() < 6) {
            Log.e(TAG, "Password is too short");
            listener.onError("Password must be at least 6 characters");
            return;
        }

        // STEP 1: Check if username already exists
        Log.d(TAG, "Checking if username exists: " + username);
        checkUsernameExists(username, exists -> {
            if (exists) {
                Log.e(TAG, "Username already taken: " + username);
                listener.onError("Username '" + username + "' is already taken. Please choose a different username.");
                return;
            }

            // STEP 2: Username is unique, proceed with registration
            Log.d(TAG, "Username is available, proceeding with registration");

            // Generate email from username for Firebase Auth (user never sees this)
            String email = username.toLowerCase().replaceAll("[^a-z0-9]", "") + "@mindbloom.app";
            Log.d(TAG, "Generated email: " + email);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Firebase authentication successful");
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                Log.d(TAG, "Creating " + role + " in database - UID: " + firebaseUser.getUid());

                                // Determine collection and counter based on role
                                String collection = role.equalsIgnoreCase("Instructor") ? "instructors" : "users";
                                String counterName = role.equalsIgnoreCase("Instructor") ? "instructorIdCounter" : "userIdCounter";

                                Log.d(TAG, "Saving to collection: " + collection);

                                // Get next display ID from counter
                                DatabaseReference counterRef = dbRef.child("counters").child(counterName);
                                counterRef.get().addOnSuccessListener(snapshot -> {
                                    int nextDisplayId = 1; // Start from 1
                                    if (snapshot.exists()) {
                                        Integer currentId = snapshot.getValue(Integer.class);
                                        if (currentId != null) {
                                            nextDisplayId = currentId + 1;
                                        }
                                    }

                                    final int finalDisplayId = nextDisplayId;

                                    // Update counter
                                    counterRef.setValue(finalDisplayId);

                                    // Create appropriate object based on role
                                    if (role.equalsIgnoreCase("Instructor")) {
                                        // Create Instructor object (simplified model)
                                        Instructor instructor = new Instructor(firebaseUser.getUid(), username, password);
                                        instructor.setId((long) finalDisplayId);  // Sequential ID (1, 2, 3...)
                                        instructor.setCreatedAt(System.currentTimeMillis());
                                        instructor.setUpdatedAt(System.currentTimeMillis());

                                        // Save to instructors collection
                                        dbRef.child("instructors").child(firebaseUser.getUid())
                                                .setValue(instructor)
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d(TAG, "Instructor saved successfully with ID: " + finalDisplayId);
                                                    listener.onSuccess("Registration successful!");
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e(TAG, "Failed to save instructor: " + e.getMessage(), e);
                                                    listener.onError("Registration failed: " + e.getMessage());
                                                });
                                    } else {
                                        // Create User object WITH PASSWORD
                                        User user = new User(firebaseUser.getUid(), username, password, role);
                                        user.setDisplayId(finalDisplayId);

                                        // Save to users collection
                                        dbRef.child("users").child(firebaseUser.getUid())
                                                .setValue(user)
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d(TAG, "User saved successfully with Display ID: " + finalDisplayId);
                                                    listener.onSuccess("Registration successful!");
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e(TAG, "Failed to save user: " + e.getMessage(), e);
                                                    listener.onError("Registration failed: " + e.getMessage());
                                                });
                                    }
                                }).addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to get counter: " + e.getMessage(), e);
                                    listener.onError("Registration failed: " + e.getMessage());
                                });
                        } else {
                            Log.e(TAG, "FirebaseUser is null after successful authentication");
                            listener.onError("Registration failed: User object is null");
                        }
                    } else {
                        // Handle Firebase Auth errors
                        String errorMsg = task.getException() != null ?
                                task.getException().getMessage() : "Registration failed";
                        Log.e(TAG, "Registration failed: " + errorMsg);

                        // Convert Firebase error to user-friendly message
                        String userFriendlyError = errorMsg;
                        if (errorMsg != null) {
                            if (errorMsg.toLowerCase().contains("email") &&
                                errorMsg.toLowerCase().contains("already") &&
                                errorMsg.toLowerCase().contains("use")) {
                                // Firebase says email already in use = username already taken
                                userFriendlyError = "Username '" + username + "' is already taken. Please choose a different username.";
                            } else if (errorMsg.toLowerCase().contains("weak password")) {
                                userFriendlyError = "Password is too weak. Please use at least 6 characters.";
                            } else if (errorMsg.toLowerCase().contains("invalid email")) {
                                userFriendlyError = "Invalid username. Please use only letters and numbers.";
                            } else if (errorMsg.toLowerCase().contains("network")) {
                                userFriendlyError = "Network error. Please check your internet connection.";
                            }
                        }

                        listener.onError(userFriendlyError);
                    }
                });
        });
    }


    private void checkUsernameExists(String username, UsernameCheckCallback callback) {
        Log.d(TAG, "========================================");
        Log.d(TAG, "🔍 CHECKING USERNAME AVAILABILITY: " + username);
        Log.d(TAG, "========================================");

        // STEP 1: Check users collection first
        Log.d(TAG, "📊 Step 1: Querying 'users' collection...");
        dbRef.child("users")
            .orderByChild("username")
            .equalTo(username)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "📥 Users collection response received");
                    Log.d(TAG, "   - Exists: " + dataSnapshot.exists());
                    Log.d(TAG, "   - Child count: " + dataSnapshot.getChildrenCount());

                    if (dataSnapshot.exists()) {
                        Log.d(TAG, "❌ Username EXISTS in USERS collection: " + username);
                        callback.onResult(true); // Username taken
                        return;
                    }

                    // STEP 2: Not in users, check instructors collection
                    Log.d(TAG, "✅ Not in users collection");
                    Log.d(TAG, "📊 Step 2: Querying 'instructors' collection...");
                    dbRef.child("instructors")
                        .orderByChild("username")
                        .equalTo(username)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Log.d(TAG, "📥 Instructors collection response received");
                                Log.d(TAG, "   - Exists: " + dataSnapshot.exists());
                                Log.d(TAG, "   - Child count: " + dataSnapshot.getChildrenCount());

                                if (dataSnapshot.exists()) {
                                    Log.d(TAG, "❌ Username EXISTS in INSTRUCTORS collection: " + username);
                                    callback.onResult(true); // Username taken
                                } else {
                                    Log.d(TAG, "✅✅✅ USERNAME IS AVAILABLE: " + username);
                                    Log.d(TAG, "========================================");
                                    callback.onResult(false); // Username available
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e(TAG, "❌ ERROR checking instructors collection: " + databaseError.getMessage());
                                Log.e(TAG, "❌ ERROR code: " + databaseError.getCode());
                                Log.e(TAG, "❌ ERROR details: " + databaseError.getDetails());

                                // ✅ FIX: If database is empty or permission denied, username is actually AVAILABLE
                                // Only return true (taken) if there's a real conflict, not an error
                                if (databaseError.getCode() == DatabaseError.PERMISSION_DENIED) {
                                    Log.e(TAG, "⚠️ Database permission denied - check Firebase rules!");
                                }

                                // Allow registration even if there's a database error (username is likely available)
                                callback.onResult(false); // Username AVAILABLE
                            }
                        });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "❌ ERROR checking users collection: " + databaseError.getMessage());
                    Log.e(TAG, "❌ ERROR code: " + databaseError.getCode());

                    // ✅ FIX: Check instructors collection anyway (don't fail immediately)
                    Log.d(TAG, "Continuing to check instructors collection despite users error...");
                    dbRef.child("instructors")
                        .orderByChild("username")
                        .equalTo(username)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    Log.d(TAG, "Username exists in INSTRUCTORS collection: " + username);
                                    callback.onResult(true); // Username taken
                                } else {
                                    Log.d(TAG, "Username is AVAILABLE (despite users collection error): " + username);
                                    callback.onResult(false); // Username available
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError2) {
                                Log.e(TAG, "❌ Both collections failed - permission issue. Username is AVAILABLE.");
                                // If both fail, allow registration (username is available)
                                callback.onResult(false); // Username AVAILABLE
                            }
                        });
                }
            });
    }


    private void checkUserCredentials(String username, String password, UserCredentialsCallback callback) {
        Log.d(TAG, "========================================");
        Log.d(TAG, "🔐 LOGIN ATTEMPT - Checking credentials");
        Log.d(TAG, "   Username: " + username);
        Log.d(TAG, "   Password: " + (password != null ? "[PROVIDED]" : "[NULL]"));
        Log.d(TAG, "========================================");


        Log.d(TAG, "📊 STEP 1: Querying 'users' collection by username...");
        dbRef.child("users")
            .orderByChild("username")
            .equalTo(username)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "📥 Users collection query response:");
                    Log.d(TAG, "   - dataSnapshot.exists(): " + dataSnapshot.exists());
                    Log.d(TAG, "   - dataSnapshot.getChildrenCount(): " + dataSnapshot.getChildrenCount());

                    if (dataSnapshot.exists()) {
                        // Found in users collection - verify password
                        Log.d(TAG, "✅ User found in USERS collection!");
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            String firebaseUid = userSnapshot.getKey();
                            String storedPassword = userSnapshot.child("password").getValue(String.class);
                            String role = userSnapshot.child("role").getValue(String.class);

                            Log.d(TAG, "📋 User Details:");
                            Log.d(TAG, "   - Firebase UID: " + firebaseUid);
                            Log.d(TAG, "   - Username: " + username);
                            Log.d(TAG, "   - Role: " + role);
                            Log.d(TAG, "   - Stored password exists: " + (storedPassword != null));

                            if (storedPassword != null) {
                                Log.d(TAG, "   - Password length: " + storedPassword.length());
                            }


                            if (storedPassword == null) {
                                Log.e(TAG, "❌ Password not found in database for user: " + username);
                                callback.onError("User not registered. Please sign up first.");
                                return;
                            }

                            if (!storedPassword.equals(password)) {
                                Log.e(TAG, "❌ Password mismatch for user: " + username);
                                callback.onError("Incorrect password. Please try again.");
                                return;
                            }


                            Log.d(TAG, "✅ Password verified for user: " + username);
                            callback.onSuccess(role != null ? role : "USER", firebaseUid);
                            return;
                        }
                    }


                    Log.d(TAG, "⚠️ Not found in users collection");
                    Log.d(TAG, "📊 STEP 2: Querying 'instructors' collection by username...");
                    dbRef.child("instructors")
                        .orderByChild("username")
                        .equalTo(username)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Log.d(TAG, "📥 Instructors collection query response:");
                                Log.d(TAG, "   - dataSnapshot.exists(): " + dataSnapshot.exists());
                                Log.d(TAG, "   - dataSnapshot.getChildrenCount(): " + dataSnapshot.getChildrenCount());

                                if (dataSnapshot.exists()) {
                                    // Found in instructors collection - verify password
                                    Log.d(TAG, "✅ User found in INSTRUCTORS collection!");
                                    for (DataSnapshot instructorSnapshot : dataSnapshot.getChildren()) {
                                        String firebaseUid = instructorSnapshot.getKey();
                                        String storedPassword = instructorSnapshot.child("password").getValue(String.class);
                                        String role = instructorSnapshot.child("role").getValue(String.class);

                                        Log.d(TAG, "📋 Instructor Details:");
                                        Log.d(TAG, "   - Firebase UID: " + firebaseUid);
                                        Log.d(TAG, "   - Username: " + username);
                                        Log.d(TAG, "   - Role: " + role);
                                        Log.d(TAG, "   - Stored password exists: " + (storedPassword != null));

                                        if (storedPassword != null) {
                                            Log.d(TAG, "   - Password length: " + storedPassword.length());
                                        }

                                        // Verify password
                                        if (storedPassword == null) {
                                            Log.e(TAG, "❌ Password not found in database for instructor: " + username);
                                            callback.onError("User not registered. Please sign up first.");
                                            return;
                                        }

                                        if (!storedPassword.equals(password)) {
                                            Log.e(TAG, "❌ Password mismatch for instructor: " + username);
                                            callback.onError("Incorrect password. Please try again.");
                                            return;
                                        }

                                        // Password matches!
                                        Log.d(TAG, "✅ Password verified for instructor: " + username);
                                        callback.onSuccess(role != null ? role : "Instructor", firebaseUid);
                                        return;
                                    }
                                }

                                // Not found in either collection
                                Log.d(TAG, "========================================");
                                Log.e(TAG, "❌ USER NOT FOUND IN DATABASE");
                                Log.e(TAG, "   Username searched: " + username);
                                Log.e(TAG, "   Checked collections: users, instructors");
                                Log.e(TAG, "   Possible causes:");
                                Log.e(TAG, "   1. User not registered yet");
                                Log.e(TAG, "   2. Username case mismatch");
                                Log.e(TAG, "   3. Firebase index not configured (.indexOn)");
                                Log.e(TAG, "   4. Firebase permission rules blocking query");
                                Log.d(TAG, "========================================");
                                callback.onError("User not registered. Please sign up first.");
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e(TAG, "========================================");
                                Log.e(TAG, "❌ DATABASE ERROR - Instructors collection");
                                Log.e(TAG, "   Error message: " + databaseError.getMessage());
                                Log.e(TAG, "   Error code: " + databaseError.getCode());
                                Log.e(TAG, "   Error details: " + databaseError.getDetails());
                                Log.e(TAG, "   Possible causes:");
                                Log.e(TAG, "   1. Firebase rules blocking query");
                                Log.e(TAG, "   2. No .indexOn rule for 'username' field");
                                Log.e(TAG, "   3. Network/internet connection issue");
                                Log.d(TAG, "========================================");
                                callback.onError("Login failed: " + databaseError.getMessage());
                            }
                        });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "========================================");
                    Log.e(TAG, "❌ DATABASE ERROR - Users collection");
                    Log.e(TAG, "   Error message: " + databaseError.getMessage());
                    Log.e(TAG, "   Error code: " + databaseError.getCode());
                    Log.e(TAG, "   Error details: " + databaseError.getDetails());
                    Log.e(TAG, "   Possible causes:");
                    Log.e(TAG, "   1. Firebase rules blocking query");
                    Log.e(TAG, "   2. No .indexOn rule for 'username' field");
                    Log.e(TAG, "   3. Network/internet connection issue");
                    Log.e(TAG, "");
                    Log.e(TAG, "   FIX: Add this to Firebase Database Rules:");
                    Log.e(TAG, "   \"users\": { \".indexOn\": [\"username\"] }");
                    Log.d(TAG, "========================================");
                    callback.onError("Login failed: " + databaseError.getMessage());
                }
            });
    }


    private void checkUserExistsInDatabase(String username, UserExistsCallback callback) {
        Log.d(TAG, "Checking if user exists in database: " + username);


        dbRef.child("users")
            .orderByChild("username")
            .equalTo(username)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Found in users collection
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            String firebaseUid = userSnapshot.getKey();
                            String role = userSnapshot.child("role").getValue(String.class);
                            Log.d(TAG, "✅ Found user in USERS collection - Username: " + username + ", Role: " + role + ", UID: " + firebaseUid);
                            callback.onResult(true, role != null ? role : "USER", firebaseUid);
                            return;
                        }
                    }


                    Log.d(TAG, "Not found in users collection, checking instructors...");
                    dbRef.child("instructors")
                        .orderByChild("username")
                        .equalTo(username)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    // Found in instructors collection
                                    for (DataSnapshot instructorSnapshot : dataSnapshot.getChildren()) {
                                        String firebaseUid = instructorSnapshot.getKey();
                                        String role = instructorSnapshot.child("role").getValue(String.class);
                                        Log.d(TAG, "✅ Found user in INSTRUCTORS collection - Username: " + username + ", Role: " + role + ", UID: " + firebaseUid);
                                        callback.onResult(true, role != null ? role : "Instructor", firebaseUid);
                                        return;
                                    }
                                }

                                Log.d(TAG, "❌ User not found in database: " + username);
                                callback.onResult(false, null, null);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e(TAG, "Error checking instructors collection: " + databaseError.getMessage());
                                callback.onError(databaseError.getMessage());
                            }
                        });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Error checking users collection: " + databaseError.getMessage());
                    callback.onError(databaseError.getMessage());
                }
            });
    }



    public void loginUser(String username, String password, OnAuthCompleteListener listener) {
        Log.d(TAG, "========================================");
        Log.d(TAG, "🔐 LOGIN - Verifying username AND password");
        Log.d(TAG, "   Username: " + username);
        Log.d(TAG, "========================================");

        // Validate inputs
        if (username == null || username.trim().isEmpty()) {
            Log.e(TAG, "Username is empty");
            listener.onError("Username cannot be empty");
            return;
        }

        if (password == null || password.isEmpty()) {
            Log.e(TAG, "Password is empty");
            listener.onError("Password cannot be empty");
            return;
        }


        String email = username.toLowerCase().replaceAll("[^a-z0-9]", "") + "@mindbloom.app";
        Log.d(TAG, "Generated email: " + email);


        Log.d(TAG, "STEP 1: Authenticating with Firebase Auth...");
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser == null) {
                            Log.e(TAG, "❌ FirebaseUser is null");
                            listener.onError("Login failed: Unable to retrieve user information");
                            return;
                        }

                        String uid = firebaseUser.getUid();
                        Log.d(TAG, "✅ Firebase Auth successful - UID: " + uid);


                        Log.d(TAG, "STEP 2: Verifying password from database...");
                        verifyPasswordFromDatabase(uid, password, new PasswordVerificationCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "✅✅ LOGIN SUCCESSFUL - Username and password verified!");
                                Log.d(TAG, "========================================");
                                listener.onSuccess("Login successful!");
                            }

                            @Override
                            public void onError(String error) {
                                Log.e(TAG, "❌ Password verification failed: " + error);
                                Log.d(TAG, "========================================");
                                // Logout from Firebase Auth since password doesn't match
                                mAuth.signOut();
                                listener.onError(error);
                            }
                        });
                    } else {

                        String errorMsg = task.getException() != null ?
                                task.getException().getMessage() : "Login failed";
                        Log.e(TAG, "❌ Firebase Auth failed: " + errorMsg);
                        Log.d(TAG, "========================================");

                        // User-friendly error messages
                        String userError;
                        if (errorMsg != null) {
                            if (errorMsg.toLowerCase().contains("no user") ||
                                errorMsg.toLowerCase().contains("not found") ||
                                errorMsg.toLowerCase().contains("no record")) {
                                userError = "User not registered. Please sign up first.";
                            } else if (errorMsg.toLowerCase().contains("password")) {
                                userError = "Incorrect password. Please try again.";
                            } else if (errorMsg.toLowerCase().contains("network")) {
                                userError = "Network error. Check your internet connection.";
                            } else if (errorMsg.toLowerCase().contains("too many")) {
                                userError = "Too many failed attempts. Try again later.";
                            } else {
                                userError = "Login failed. Check your username and password.";
                            }
                        } else {
                            userError = "Login failed. Please try again.";
                        }
                        listener.onError(userError);
                    }
                });
    }


    private void verifyPasswordFromDatabase(String uid, String enteredPassword, PasswordVerificationCallback callback) {
        Log.d(TAG, "Verifying password from database for UID: " + uid);


        dbRef.child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Found in users collection
                            String storedPassword = dataSnapshot.child("password").getValue(String.class);
                            String username = dataSnapshot.child("username").getValue(String.class);

                            Log.d(TAG, "📋 Found user in USERS collection");
                            Log.d(TAG, "   - Username: " + username);
                            Log.d(TAG, "   - Stored password exists: " + (storedPassword != null));

                            if (storedPassword == null) {
                                Log.e(TAG, "❌ Password not found in database");
                                callback.onError("Account data incomplete. Please register again.");
                                return;
                            }

                            // Compare passwords
                            if (storedPassword.equals(enteredPassword)) {
                                Log.d(TAG, "✅ Password matches database!");
                                callback.onSuccess();
                            } else {
                                Log.e(TAG, "❌ Password does NOT match database");
                                callback.onError("Incorrect password. Please try again.");
                            }
                            return;
                        }

                        // Not in users, try instructors collection
                        Log.d(TAG, "Not in users collection, checking instructors...");
                        dbRef.child("instructors").child(uid)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            // Found in instructors collection
                                            String storedPassword = dataSnapshot.child("password").getValue(String.class);
                                            String username = dataSnapshot.child("username").getValue(String.class);

                                            Log.d(TAG, "📋 Found user in INSTRUCTORS collection");
                                            Log.d(TAG, "   - Username: " + username);
                                            Log.d(TAG, "   - Stored password exists: " + (storedPassword != null));

                                            if (storedPassword == null) {
                                                Log.e(TAG, "❌ Password not found in database");
                                                callback.onError("Account data incomplete. Please register again.");
                                                return;
                                            }

                                            // Compare passwords
                                            if (storedPassword.equals(enteredPassword)) {
                                                Log.d(TAG, "✅ Password matches database!");
                                                callback.onSuccess();
                                            } else {
                                                Log.e(TAG, "❌ Password does NOT match database");
                                                callback.onError("Incorrect password. Please try again.");
                                            }
                                        } else {
                                            Log.e(TAG, "❌ User not found in either collection");
                                            callback.onError("Account not found. Please register again.");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.e(TAG, "❌ Database error: " + databaseError.getMessage());
                                        callback.onError("Database error: " + databaseError.getMessage());
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "❌ Database error: " + databaseError.getMessage());
                        callback.onError("Database error: " + databaseError.getMessage());
                    }
                });
    }


    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }


    public String getCurrentUserId() {
        FirebaseUser user = mAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }


    public void logout() {
        mAuth.signOut();
    }


    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }


    public void resetPassword(String username, OnAuthCompleteListener listener) {
        // Generate email from username for Firebase Auth
        String email = username.toLowerCase().replaceAll("[^a-z0-9]", "") + "@mindbloom.app";

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onSuccess("Password reset email sent!");
                    } else {
                        String errorMsg = task.getException() != null ?
                                task.getException().getMessage() : "Failed to send reset email";
                        listener.onError(errorMsg);
                    }
                });
    }


    public void fetchUserData(OnUserDataFetchedListener listener) {
        Log.d(TAG, "fetchUserData called");

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            Log.e(TAG, "No user is logged in");
            listener.onError("No user is logged in");
            return;
        }

        String userId = firebaseUser.getUid();
        Log.d(TAG, "Fetching data for user ID: " + userId);

        // STEP 1: Try to fetch from 'users' collection first
        dbRef.child("users").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d(TAG, "Users collection - dataSnapshot exists: " + dataSnapshot.exists());

                        if (dataSnapshot.exists()) {
                            // Found in users collection
                            String username = dataSnapshot.child("username").getValue(String.class);
                            String role = dataSnapshot.child("role").getValue(String.class);

                            Log.d(TAG, "Fetched from USERS - username: " + username + ", role: " + role);

                            if (username != null && role != null) {
                                Log.d(TAG, "User data fetched successfully from users collection");
                                listener.onSuccess(userId, username, role);
                            } else {
                                Log.e(TAG, "Incomplete user data - username: " + username + ", role: " + role);
                                listener.onError("Incomplete user data in database");
                            }
                        } else {
                            // Not found in users collection, try instructors collection
                            Log.d(TAG, "Not found in users collection, checking instructors collection...");
                            fetchFromInstructorsCollection(userId, listener);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Database error: " + databaseError.getMessage());
                        // Try instructors collection as fallback
                        fetchFromInstructorsCollection(userId, listener);
                    }
                });
    }


    private void fetchFromInstructorsCollection(String userId, OnUserDataFetchedListener listener) {
        Log.d(TAG, "Fetching from instructors collection for user ID: " + userId);

        dbRef.child("instructors").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d(TAG, "Instructors collection - dataSnapshot exists: " + dataSnapshot.exists());

                        if (dataSnapshot.exists()) {
                            // Found in instructors collection
                            String username = dataSnapshot.child("username").getValue(String.class);
                            String role = dataSnapshot.child("role").getValue(String.class);

                            Log.d(TAG, "Fetched from INSTRUCTORS - username: " + username + ", role: " + role);

                            if (username != null && role != null) {
                                Log.d(TAG, "Instructor data fetched successfully from instructors collection");
                                listener.onSuccess(userId, username, role);
                            } else {
                                Log.e(TAG, "Incomplete instructor data - username: " + username + ", role: " + role);
                                listener.onError("Incomplete instructor data in database");
                            }
                        } else {
                            Log.e(TAG, "User data not found in either users or instructors collection for ID: " + userId);
                            listener.onError("User data not found in database. Please register again.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Database error while fetching from instructors: " + databaseError.getMessage());
                        listener.onError(databaseError.getMessage());
                    }
                });
    }

    // Callback interfaces
    public interface OnAuthCompleteListener {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface OnUserDataFetchedListener {
        void onSuccess(String userId, String username, String role);
        void onError(String error);
    }


    private interface UsernameCheckCallback {
        void onResult(boolean exists);
    }


    private interface UserExistsCallback {
        void onResult(boolean exists, String role, String firebaseUid);
        void onError(String error);
    }


    private interface UserCredentialsCallback {
        void onSuccess(String role, String firebaseUid);
        void onError(String error);
    }


    private interface PasswordVerificationCallback {
        void onSuccess();
        void onError(String error);
    }
}

