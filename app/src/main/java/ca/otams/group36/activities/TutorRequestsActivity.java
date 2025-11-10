package ca.otams.group36.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
                        s.setId(d.getId());
                        requests.add(s);
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

        db.collection("sessions").document(s.getId())
                .update("status", newStatus)
                .addOnSuccessListener(x -> {
                    Toast.makeText(this, "Session " + newStatus, Toast.LENGTH_SHORT).show();
                    loadPending();
                });
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
