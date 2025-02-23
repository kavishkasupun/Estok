package com.example.estok;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {

    private EditText nameField, emailField, passwordField, confirmPasswordField;

    TextView haveaccount;
    private Button signUpButton;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.Signup), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameField = findViewById(R.id.signup_name);
        emailField = findViewById(R.id.signup_email);
        passwordField = findViewById(R.id.signup_password);
        confirmPasswordField = findViewById(R.id.signup_confirm_password);
        signUpButton = findViewById(R.id.signup_btn);
        progressBar = new ProgressBar(this);
        haveaccount = findViewById(R.id.have_acc);

        haveaccount.setOnClickListener(v -> openMainActivity());



        signUpButton.setOnClickListener(v -> registerUser());
    }


    private void openMainActivity() {
        startActivity(new Intent(SignUp.this, MainActivity.class));
    }

    private void registerUser() {
        String name = nameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            nameField.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            emailField.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordField.setError("Password is required");
            return;
        }
        if (password.length() < 6) {
            passwordField.setError("Password must be at least 6 characters");
            return;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordField.setError("Passwords do not match");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user.getUid(), name, email);
                        }
                    } else {
                        Toast.makeText(SignUp.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void saveUserToFirestore(String userId, String name, String email) {
        DocumentReference userRef = db.collection("users").document(userId);
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("role", "user");

        userRef.set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SignUp.this, "Registration Successful", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(SignUp.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(SignUp.this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}