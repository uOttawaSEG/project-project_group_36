package ca.otams.group36.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ca.otams.group36.R;
import ca.otams.group36.adapters.SessionsAdapter;
import ca.otams.group36.models.Session;

public class TutorRequestsActivity extends AppCompatActivity {

    RecyclerView recycler;
    ArrayList<Session> requests = new ArrayList<>();
    SessionsAdapter adapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String tutorEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_requests);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tutorEmail = getIntent().getStringExtra("email");

        recycler = findViewById(R.id.recyclerRequests);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SessionsAdapter(requests, (session, action) -> handleAction(session, action));
        recycler.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPending();
    }

    private void loadPending() {
        db.collection("sessions")
                .whereEqualTo("tutorEmail", tutorEmail)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(q -> {
                    requests.clear();
                    for (DocumentSnapshot d : q.getDocuments()) {
                        Session s = d.toObject(Session.class);
                        if (s != null) {
                            s.setId(d.getId());
                            requests.add(s);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void handleAction(Session s, String action) {
        String newStatus;
        switch (action) {
            case "approve":
                newStatus = "approved";
                break;
            case "reject":
                newStatus = "rejected";
                break;
            default:
                return;
        }

        // Build minimal update map
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        // Ensure startAt exists so time-based queries (Upcoming/Past) work
        if (s.getStartAt() == null && s.getDate() != null && s.getStartTime() != null) {
            updates.put("startAt", toTimestamp(s.getDate(), s.getStartTime()));
        }
        // Optional but useful for auditing/sorting
        updates.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("sessions").document(s.getId())
                .update(updates)
                .addOnSuccessListener(x -> {
                    Toast.makeText(this, "Session " + newStatus, Toast.LENGTH_SHORT).show();
                    loadPending();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // Helper: build a Timestamp from "yyyy-MM-dd" and "HH:mm"
    private static Timestamp toTimestamp(String ymd, String hhmm) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        String[] d = ymd.split("-");
        String[] t = hhmm.split(":");
        c.set(java.util.Calendar.YEAR, Integer.parseInt(d[0]));
        c.set(java.util.Calendar.MONTH, Integer.parseInt(d[1]) - 1);
        c.set(java.util.Calendar.DAY_OF_MONTH, Integer.parseInt(d[2]));
        c.set(java.util.Calendar.HOUR_OF_DAY, Integer.parseInt(t[0]));
        c.set(java.util.Calendar.MINUTE, Integer.parseInt(t[1]));
        c.set(java.util.Calendar.SECOND, 0);
        c.set(java.util.Calendar.MILLISECOND, 0);
        return new Timestamp(new java.util.Date(c.getTimeInMillis()));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
