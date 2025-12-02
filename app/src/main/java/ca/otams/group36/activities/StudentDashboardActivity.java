package ca.otams.group36.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import ca.otams.group36.R;

public class StudentDashboardActivity extends AppCompatActivity {

    private String email, name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        email = getIntent().getStringExtra("email");
        name = getIntent().getStringExtra("name");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Student Dashboard");
        }

        TextView txtWelcome = findViewById(R.id.txtStudentDashboardTitle);
        txtWelcome.setText("Welcome " + name);

        // Buttons
        findViewById(R.id.btnSearch).setOnClickListener(v -> {
            Intent i = new Intent(this, StudentSearchActivity.class);
            i.putExtra("email", email);
            i.putExtra("name", name);
            startActivity(i);
        });

        findViewById(R.id.btnSessions).setOnClickListener(v -> {
            Intent i = new Intent(this, StudentSessionsActivity.class);
            i.putExtra("email", email);
            startActivity(i);
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });
    }
}
