package ca.otams.group36.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import ca.otams.group36.R;
import ca.otams.group36.adapters.SessionsAdapter;
import ca.otams.group36.models.Session;

public class StudentSessionsActivity extends AppCompatActivity {

    enum Filter {UPCOMING, PENDING, PAST}

    private RecyclerView recycler;
    private TextView txtEmpty;
    private Spinner spinner;
    private ArrayList<Session> sessions = new ArrayList<>();
    private SessionsAdapter adapter;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String studentEmail; // fill from intent/session manager

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_sessions);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        studentEmail = getIntent().getStringExtra("email");

        recycler = findViewById(R.id.recyclerSessions);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        txtEmpty = findViewById(R.id.txtEmpty);

        // Adapter with action handler: cancel or rate (rate action just toast here)
        adapter = new SessionsAdapter(sessions, (s, action) -> {
            if ("cancel".equals(action)) cancelSession(s);
            else if ("rate".equals(action)) {
                Toast.makeText(this, "Open rating dialog (TBD)", Toast.LENGTH_SHORT).show();
            }
        });
        recycler.setAdapter(adapter);

        spinner = findViewById(R.id.spinnerFilter);
        ArrayAdapter<CharSequence> sp = ArrayAdapter.createFromResource(
                this, R.array.student_session_filters, android.R.layout.simple_spinner_item);
        sp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(sp);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (pos == 0) loadSessions(Filter.UPCOMING);
                else if (pos == 1) loadSessions(Filter.PENDING);
                else loadSessions(Filter.PAST);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        spinner.setSelection(0); // default to Upcoming
    }

    private void loadSessions(Filter filter) {
        if (studentEmail == null || studentEmail.isEmpty()) {
            Toast.makeText(this, "Missing student email", Toast.LENGTH_SHORT).show();
            return;
        }

        Timestamp now = Timestamp.now();
        var base = db.collection("sessions")
                .whereEqualTo("studentEmail", studentEmail);

        Query q;
        switch (filter) {
            case PENDING:
                q = base.whereEqualTo("status", "pending")
                        .orderBy("requestedAt", Query.Direction.DESCENDING); // optional if exists
                break;
            case UPCOMING:
                q = base.whereEqualTo("status", "approved")
                        .whereGreaterThanOrEqualTo("startAt", now)
                        .orderBy("startAt", Query.Direction.ASCENDING);
                break;
            default: // PAST
                q = base.whereEqualTo("status", "approved")
                        .whereLessThan("startAt", now)
                        .orderBy("startAt", Query.Direction.DESCENDING);
                break;
        }

        q.get().addOnSuccessListener(snap -> {
            sessions.clear();
            for (DocumentSnapshot d : snap.getDocuments()) {
                Session s = d.toObject(Session.class);
                if (s != null) {
                    s.setId(d.getId());
                    sessions.add(s);
                }
            }
            // Fallback local sort/filter if your index still building
            if (filter == Filter.UPCOMING) {
                sessions.removeIf(s -> s.getStartAt() == null || s.getStartAt().compareTo(now) < 0);
                sessions.sort(Comparator.comparing(Session::getStartAt));
            } else if (filter == Filter.PAST) {
                sessions.removeIf(s -> s.getStartAt() == null || s.getStartAt().compareTo(now) >= 0);
                sessions.sort((a, b) -> b.getStartAt().compareTo(a.getStartAt()));
            }

            adapter.notifyDataSetChanged();
            txtEmpty.setVisibility(sessions.isEmpty() ? View.VISIBLE : View.GONE);
        }).addOnFailureListener(e -> {
            // Index not ready fallback: simple fetch then local filter
            if (e.getMessage() != null && e.getMessage().contains("FAILED_PRECONDITION")) {
                simpleFetchAndLocalFilter(filter);
            } else {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Fallback that does not require composite index
    private void simpleFetchAndLocalFilter(Filter filter) {
        Timestamp now = Timestamp.now();
        db.collection("sessions")
                .whereEqualTo("studentEmail", studentEmail)
                .whereIn("status", filter == Filter.PENDING
                        ? Arrays.asList("pending")
                        : Arrays.asList("approved"))
                .get()
                .addOnSuccessListener(snap -> {
                    sessions.clear();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Session s = d.toObject(Session.class);
                        if (s != null) {
                            s.setId(d.getId());
                            sessions.add(s);
                        }
                    }
                    if (filter == Filter.UPCOMING) {
                        sessions.removeIf(s -> s.getStartAt() == null || s.getStartAt().compareTo(now) < 0);
                        sessions.sort(Comparator.comparing(Session::getStartAt));
                    } else if (filter == Filter.PAST) {
                        sessions.removeIf(s -> s.getStartAt() == null || s.getStartAt().compareTo(now) >= 0);
                        sessions.sort((a, b) -> b.getStartAt().compareTo(a.getStartAt()));
                    }
                    adapter.notifyDataSetChanged();
                    txtEmpty.setVisibility(sessions.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    // Cancel rules: pending always allowed; approved allowed only if >= 24h
    private void cancelSession(Session s) {
        if ("pending".equals(s.getStatus())) {
            updateStatus(s.getId(), "canceled");
            return;
        }
        if (!"approved".equals(s.getStatus()) || s.getStartAt() == null) {
            Toast.makeText(this, "Cannot cancel", Toast.LENGTH_SHORT).show();
            return;
        }
        long msLeft = s.getStartAt().toDate().getTime() - System.currentTimeMillis();
        if (msLeft < 24L * 60 * 60 * 1000) {
            Toast.makeText(this, "Cancellation requires at least 24h in advance", Toast.LENGTH_LONG).show();
            return;
        }
        updateStatus(s.getId(), "canceled");
        // Optional: also mark related slot as unbooked if you keep that flag
        // db.collection("availability").document(s.getSlotId()).update("booked", false);
    }

    private void updateStatus(String sessionId, String newStatus) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        db.collection("sessions").document(sessionId)
                .update(updates)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Updated: " + newStatus, Toast.LENGTH_SHORT).show();
                    spinner.getOnItemSelectedListener().onItemSelected(null, null, spinner.getSelectedItemPosition(), 0);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
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
