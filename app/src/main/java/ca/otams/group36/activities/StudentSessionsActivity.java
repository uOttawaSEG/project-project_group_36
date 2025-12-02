package ca.otams.group36.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
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

    public enum Filter {UPCOMING, PENDING, PAST}

    private RecyclerView recycler;
    private TextView txtEmpty;
    private Spinner spinner;
    private ArrayList<Session> sessions = new ArrayList<>();
    private SessionsAdapter adapter;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String studentEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_sessions);

//        ca.otams.group36.activities.InitTutorRatingFields.runOnce();

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        studentEmail = getIntent().getStringExtra("email");

        recycler = findViewById(R.id.recyclerSessions);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        txtEmpty = findViewById(R.id.txtEmpty);

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
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        spinner.setSelection(0);
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
                        .orderBy("requestedAt", Query.Direction.DESCENDING);
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

            // local filtering fallback logic
            if (filter == Filter.UPCOMING) {
                sessions.removeIf(s -> s.getStartAt() == null || s.getStartAt().compareTo(now) < 0);
                sessions.sort(Comparator.comparing(Session::getStartAt));
            } else if (filter == Filter.PAST) {
                sessions.removeIf(s -> s.getStartAt() == null || s.getStartAt().compareTo(now) >= 0);
                sessions.sort((a, b) -> b.getStartAt().compareTo(a.getStartAt()));
                checkRatingsForPastSessions();
            }

            adapter = new SessionsAdapter(
                    sessions,
                    filter,
                    (s, action) -> {
                        if ("cancel".equals(action)) {
                            cancelSession(s);
                        } else if ("rate".equals(action)) {
                            openRatingDialog(s);
                        }
                    }
            );

            recycler.setAdapter(adapter);
            txtEmpty.setVisibility(sessions.isEmpty() ? View.VISIBLE : View.GONE);

        }).addOnFailureListener(e -> {
            if (e.getMessage() != null && e.getMessage().contains("FAILED_PRECONDITION")) {
                simpleFetchAndLocalFilter(filter);
            } else {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void simpleFetchAndLocalFilter(Filter filter) {
        Timestamp now = Timestamp.now();
        db.collection("sessions")
                .whereEqualTo("studentEmail", studentEmail)
                .whereIn("status", filter == Filter.PENDING
                        ? Arrays.asList("pending","rejected")
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
                    txtEmpty.setVisibility(sessions.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

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
    }

    private void checkRatingsForPastSessions() {
        for (Session s : sessions) {
            db.collection("ratings")
                    .whereEqualTo("sessionId", s.getId())
                    .whereEqualTo("studentEmail", studentEmail)
                    .get()
                    .addOnSuccessListener(q -> {
                        s.setRated(!q.isEmpty());
                        if (adapter != null) adapter.notifyDataSetChanged();
                    });
        }
    }

    private void openRatingDialog(Session session) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_rate, null);
        builder.setView(view);

        ImageView[] stars = {
                view.findViewById(R.id.star1),
                view.findViewById(R.id.star2),
                view.findViewById(R.id.star3),
                view.findViewById(R.id.star4),
                view.findViewById(R.id.star5)
        };

        final int[] rating = {0};

        for (int i = 0; i < stars.length; i++) {
            int index = i;
            stars[i].setOnClickListener(v -> {
                rating[0] = index + 1;
                updateStarUI(stars, rating[0]);
            });
        }

        builder.setPositiveButton("Submit", (dialog, which) -> {
            if (rating[0] == 0) {
                Toast.makeText(this, "Please select a rating.", Toast.LENGTH_SHORT).show();
                return;
            }
            submitRating(session, rating[0]);
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private void updateStarUI(ImageView[] stars, int rating) {
        for (int i = 0; i < stars.length; i++) {
            stars[i].setImageResource(i < rating ? R.drawable.star_filled : R.drawable.star_empty);
        }
    }

    private void submitRating(Session session, int stars) {

        String tutorEmail = session.getTutorEmail();
        String sessionId = session.getId();

        db.collection("ratings")
                .whereEqualTo("sessionId", sessionId)
                .whereEqualTo("studentEmail", studentEmail)
                .get()
                .addOnSuccessListener(qs -> {
                    if (!qs.isEmpty()) {
                        Toast.makeText(this, "You already rated this session.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> data = new HashMap<>();
                    data.put("sessionId", sessionId);
                    data.put("tutorEmail", tutorEmail);
                    data.put("studentEmail", studentEmail);
                    data.put("rating", stars);
                    data.put("timestamp", FieldValue.serverTimestamp());

                    db.collection("ratings")
                            .add(data)
                            .addOnSuccessListener(doc -> {
                                updateTutorRating(tutorEmail, stars);
                                Toast.makeText(this, "Thanks for your rating!", Toast.LENGTH_SHORT).show();
                            });
                });
    }

    private void updateTutorRating(String tutorEmail, int stars) {

        DocumentReference ref = db.collection("users").document(tutorEmail);

        db.runTransaction(transaction -> {

            DocumentSnapshot doc = transaction.get(ref);

            long sum = doc.contains("ratingSum") ? doc.getLong("ratingSum") : 0;
            long count = doc.contains("ratingCount") ? doc.getLong("ratingCount") : 0;

            sum += stars;
            count++;

            Map<String, Object> update = new HashMap<>();
            update.put("ratingSum", sum);
            update.put("ratingCount", count);

            transaction.update(ref, update);

            return null;
        });
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
