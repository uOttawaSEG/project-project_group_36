package ca.otams.group36.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import ca.otams.group36.R;

public class TutorDashboardActivity extends AppCompatActivity {

    MaterialButton btnCreateSlot, btnMySlots, btnPendingReq, btnSessions, btnLogout;
    String tutorEmail;

    private TextView txtRating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_dashboard);

        tutorEmail = getIntent().getStringExtra("email");

        // --- Toolbar setup ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        txtRating = findViewById(R.id.txtRating);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle("Tutor Dashboard");
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("email", tutorEmail)
                .addSnapshotListener((snap, e) -> {

                    if (e != null || snap == null || snap.isEmpty()) return;

                    DocumentSnapshot doc = snap.getDocuments().get(0);

                    long sum = doc.contains("ratingSum") ? doc.getLong("ratingSum") : 0;
                    long count = doc.contains("ratingCount") ? doc.getLong("ratingCount") : 0;

                    double avg = (count == 0) ? 0 : (double) sum / count;

                    txtRating.setText(String.format("⭐ %.2f (%d reviews)", avg, count));
                });




        btnCreateSlot = findViewById(R.id.btnCreateSlot);
        btnMySlots = findViewById(R.id.btnMySlots);
        btnPendingReq = findViewById(R.id.btnPendingRequests);
        btnSessions = findViewById(R.id.btnSessions);
        btnLogout = findViewById(R.id.btnLogout);

        btnCreateSlot.setOnClickListener(v -> {
            Intent i = new Intent(this, TutorCreateSlotActivity.class);
            i.putExtra("email", tutorEmail);
            startActivity(i);
        });

        btnMySlots.setOnClickListener(v -> {
            Intent i = new Intent(this, TutorSlotsActivity.class);
            i.putExtra("email", tutorEmail);
            startActivity(i);
        });

        btnPendingReq.setOnClickListener(v -> {
            Intent i = new Intent(this, TutorRequestsActivity.class);
            i.putExtra("email", tutorEmail);
            startActivity(i);
        });

        btnSessions.setOnClickListener(v -> {
            Intent i = new Intent(this, TutorSessionsActivity.class);
            i.putExtra("email", tutorEmail);
            startActivity(i);
        });

        // --- Logout logic ---
        btnLogout.setOnClickListener(v -> {
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });
    }
}
