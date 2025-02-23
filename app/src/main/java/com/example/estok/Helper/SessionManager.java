package com.example.estok.Helper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.estok.MainActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SessionManager {

    // Constants for SharedPreferences keys
    private static final String PREF_NAME = "LoginSession";
    private static final String IS_LOGIN = "IsLoggedIn";
    private static final String KEY_EMAIL = "userEmail";
    private static final String KEY_NAME = "userName";
    private static final String KEY_USERID = "userId";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private final Context context;

    /**
     * Constructor that accepts a Context to initialize SharedPreferences.
     *
     * @param context The application or activity context.
     */
    public SessionManager(Context context) {
        this.context = context;
        this.pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = pref.edit();
    }

    /**
     * Creates a new login session by saving user details to SharedPreferences.
     *
     * @param email  The user's email address.
     * @param name   The user's name.
     * @param userId The user's unique ID.
     */
    public void createLoginSession(String email, String name, String userId) {
        editor.putBoolean(IS_LOGIN, true);  // Marks user as logged in
        editor.putString(KEY_EMAIL, email);  // Saves user email
        editor.putString(KEY_NAME, name);  // Saves user name
        editor.putString(KEY_USERID, userId);  // Saves user ID
        editor.apply();  // Commit changes
    }

    /**
     * Checks if the user is currently logged in.
     *
     * @return True if logged in, false otherwise.
     */
    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }

    /**
     * Retrieves the logged-in user's email.
     *
     * @return The user's email, or null if not found.
     */
    public String getUserEmail() {
        return pref.getString(KEY_EMAIL, null);
    }

    /**
     * Retrieves the logged-in user's name.
     *
     * @return The user's name, or null if not found.
     */
    public String getUserName() {
        return pref.getString(KEY_NAME, null);
    }

    /**
     * Retrieves the logged-in user's ID.
     *
     * @return The user's ID, or null if not found.
     */
    public String getUserId() {
        return pref.getString(KEY_USERID, null);
    }

    /**
     * Logs out the current user by clearing all saved session data
     * and redirecting to the main activity.
     */
    public void logoutUser() {
        FirebaseAuth.getInstance().signOut();  // Sign out from Firebase Auth
        editor.clear();  // Clear all stored preferences
        editor.apply();

        // Redirect to the main login activity
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}