package com.example.estok;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

import com.example.estok.Helper.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private EditText emailField, passwordField;
    private Button loginButton;
    private TextView forgotPasswordTextView, noAccountTextView;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private SessionManager sessionManager;

    private static final String ADMIN_EMAIL = "admin@email.com";
    private static final String ADMIN_PASSWORD = "admin123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(getApplicationContext());

        if (sessionManager.isLoggedIn()) {
            redirectToDashboard();
        }

        emailField = findViewById(R.id.check_email);
        passwordField = findViewById(R.id.check_password);
        loginButton = findViewById(R.id.login_btn);
        forgotPasswordTextView = findViewById(R.id.forgot_pw);
        noAccountTextView = findViewById(R.id.no_acc);
        progressBar = new ProgressBar(this);

        loginButton.setOnClickListener(v -> handleLogin());
        noAccountTextView.setOnClickListener(v -> openSignupActivity());
        forgotPasswordTextView.setOnClickListener(v -> handleForgotPassword());
    }

    private void handleLogin() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (email.isEmpty()) {
            emailField.setError("Please enter your email");
            emailField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordField.setError("Please enter your password");
            passwordField.requestFocus();
            return;
        }

        if (email.equals(ADMIN_EMAIL) && password.equals(ADMIN_PASSWORD)) {
            Toast.makeText(MainActivity.this, "Admin login successful", Toast.LENGTH_SHORT).show();
            sessionManager.createLoginSession(email, "Admin", "admin");
            startActivity(new Intent(MainActivity.this, AdminDashbord.class));
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            user.reload();
                            if (user.isEmailVerified()) {
                                sessionManager.createLoginSession(email, "User", user.getUid());
                                Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(MainActivity.this, Home.class));
                                finish();
                            } else {
                                user.sendEmailVerification().addOnCompleteListener(verificationTask -> {
                                    if (verificationTask.isSuccessful()) {
                                        Toast.makeText(MainActivity.this, "Verification email sent. Please check your inbox.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                auth.signOut();
                            }
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("LoginError", "Error: ", task.getException());
                    }
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void handleForgotPassword() {
        String email = emailField.getText().toString().trim();
        if (email.isEmpty()) {
            emailField.setError("Please enter your email to reset password");
            emailField.requestFocus();
            return;
        }

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to send reset email", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openSignupActivity() {
        startActivity(new Intent(MainActivity.this, SignUp.class));
    }

    private void redirectToDashboard() {
        if (sessionManager.getUserId().equals("admin")) {
            startActivity(new Intent(MainActivity.this, AdminDashbord.class));
        } else {
            startActivity(new Intent(MainActivity.this, Home.class));
        }
        finish();
    }
}
