package ca.otams.group36.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import ca.otams.group36.R;

public class TutorDashboardActivity extends AppCompatActivity {

    MaterialButton btnCreateSlot, btnMySlots, btnPendingReq, btnSessions;

    String tutorEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_dashboard);

        tutorEmail = getIntent().getStringExtra("email");

        btnCreateSlot = findViewById(R.id.btnCreateSlot);
        btnMySlots = findViewById(R.id.btnMySlots);
        btnPendingReq = findViewById(R.id.btnPendingRequests);
        btnSessions = findViewById(R.id.btnSessions);

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
    }
}
