/**
 * OTAMS Project
 * Author: Tianqi Jiang
 * University of Ottawa
 *
 * Description:
 * Handles the student registration process using Firebase Authentication and Firestore.
 * This activity allows students to create an account by entering personal details such as
 * first name, last name, email, password, and program of study. Upon successful registration,
 * the student's information is stored in the Firestore database and awaits administrator approval.
 */

package ca.otams.group36.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

import ca.otams.group36.R;

public class StudentRegistrationActivity extends AppCompatActivity {

    private EditText editFirstName, editLastName, editEmail, editPassword, editProgram;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_registration);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle("OTAMS");
        }

        editFirstName = findViewById(R.id.editFirstName);
        editLastName = findViewById(R.id.editLastName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editProgram = findViewById(R.id.editProgram);
        Button buttonRegisterSubmit = findViewById(R.id.buttonRegister);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        buttonRegisterSubmit.setOnClickListener(v -> {
            String first = editFirstName.getText().toString().trim();
            String last = editLastName.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String pwd = editPassword.getText().toString().trim();
            String program = editProgram.getText().toString().trim();

            if (first.isEmpty() || last.isEmpty() || email.isEmpty() || pwd.isEmpty() || program.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create account
            auth.createUserWithEmailAndPassword(email, pwd)
                    .addOnSuccessListener(result -> {
                        String uid = result.getUser().getUid();

                        Map<String, Object> student = new HashMap<>();
                        student.put("firstName", first);
                        student.put("lastName", last);
                        student.put("email", email);
                        student.put("program", program);

                        db.collection("students").document(uid).set(student)
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(this, "Registration complete!", Toast.LENGTH_SHORT).show()
                                )
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Database error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                );
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Auth failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}