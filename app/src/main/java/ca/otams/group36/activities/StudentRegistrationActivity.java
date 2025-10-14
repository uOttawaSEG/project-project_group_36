/**
 * OTAMS Project
 * Author: Tianqi Jiang
 * University of Ottawa
 *
 * Description:
 * Handles user registration and Firestore record creation.
 */

package ca.otams.group36.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import ca.otams.group36.R;

public class StudentRegistrationActivity extends AppCompatActivity {

    private EditText editEmail, editPassword, editFirstName, editLastName, editPhone;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_registration);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Register");
        }

        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI elements
        editEmail = findViewById(R.id.etEmail);
        editPassword = findViewById(R.id.etPassword);
        editFirstName = findViewById(R.id.etFirstName);
        editLastName = findViewById(R.id.etLastName);
        editPhone = findViewById(R.id.etPhone);
        Button buttonRegister = findViewById(R.id.btnRegister);

        buttonRegister.setOnClickListener(view -> {
            String email = editEmail.getText().toString().trim();
            String pwd = editPassword.getText().toString().trim();
            String firstName = editFirstName.getText().toString().trim();
            String lastName = editLastName.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();

            if (email.isEmpty() || pwd.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = String.format("%08d", new java.util.Random().nextInt(100000000));

            Map<String, Object> user = new HashMap<>();
            user.put("email", email);
            user.put("password", pwd);
            user.put("firstName", firstName);
            user.put("lastName", lastName);
            user.put("phone", phone);
            // Newly registered users require admin approval
            user.put("approved", false);
            user.put("role", "Student");

            db.collection("users").document(uid)
                    .set(user)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show();
                        finish(); // go back to login
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}