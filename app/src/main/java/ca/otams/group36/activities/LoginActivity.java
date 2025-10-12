/**
 * OTAMS Project
 * Author: Tianqi Jiang
 * University of Ottawa
 *
 * Description:
 * Handles user login with Firebase Authentication.
 */

package ca.otams.group36.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import android.util.Log;

import ca.otams.group36.MainActivity;
import ca.otams.group36.R;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail, editPassword;
    private Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("OTAMS");
        }

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        FirebaseApp.initializeApp(this);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        Log.i("FirebaseTest", "Firebase connected: " + (auth != null));

        buttonLogin.setOnClickListener(view -> {
            String email = editEmail.getText().toString().trim();
            String pwd = editPassword.getText().toString().trim();

            if (email.isEmpty() || pwd.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, pwd)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                            Log.i("FirebaseAuth", "User logged in: " + email);
                        } else {
                            Log.e("FirebaseAuth", "Login failed", task.getException());
                            Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}