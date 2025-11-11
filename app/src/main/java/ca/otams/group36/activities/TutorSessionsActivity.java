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

import ca.otams.group36.R;
import ca.otams.group36.adapters.SessionsAdapter;
import ca.otams.group36.models.Session;

public class TutorSessionsActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private Spinner spinnerFilter;
    private TextView txtEmpty;
    private final ArrayList<Session> sessions = new ArrayList<>();
    private SessionsAdapter adapter;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String tutorEmail;

    // Filter type for spinner
    enum Filter { UPCOMING, PAST }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_sessions);

        // --- Toolbar ---
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Sessions");
        }

        tutorEmail = getIntent().getStringExtra("email");

        recycler = findViewById(R.id.recyclerSessions);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        txtEmpty = findViewById(R.id.txtEmptySessions);

        adapter = new SessionsAdapter(sessions, (session, action) -> {
            // (Optional) Add "Cancel" here for approved future sessions
        });
        recycler.setAdapter(adapter);

        // --- Filter spinner setup ---
        spinnerFilter = findViewById(R.id.spinnerFilter);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this, R.array.session_filters, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(spinnerAdapter);

        // Map spinner positions to Filter enum
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    loadSessions(Filter.UPCOMING);
                } else {
                    loadSessions(Filter.PAST);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        // Initial load
        loadSessions(Filter.UPCOMING);
    }

    /** Load sessions for this tutor: approved only, then split by time using startAt. */
    private void loadSessions(Filter filter) {
        if (tutorEmail == null || tutorEmail.isEmpty()) {
            Toast.makeText(this, "Missing tutor email", Toast.LENGTH_SHORT).show();
            return;
        }

        Timestamp now = Timestamp.now();

        Query q = db.collection("sessions")
                .whereEqualTo("tutorEmail", tutorEmail)
                .whereEqualTo("status", "approved"); // only approved sessions

        if (filter == Filter.UPCOMING) {
            // startAt >= now (ASC)
            q = q.whereGreaterThanOrEqualTo("startAt", now)
                    .orderBy("startAt", Query.Direction.ASCENDING);
        } else {
            // startAt < now (DESC)
            q = q.whereLessThan("startAt", now)
                    .orderBy("startAt", Query.Direction.DESCENDING);
        }

        q.get().addOnSuccessListener(qs -> {
            sessions.clear();
            for (DocumentSnapshot d : qs.getDocuments()) {
                Session s = d.toObject(Session.class);
                if (s != null) {
                    s.setId(d.getId());

                    // If some historical docs miss startAt, you can rebuild it here (optional):
                    // if (s.getStartAt() == null) {
                    //     String date = (String) d.get("date");      // "yyyy-MM-dd"
                    //     String time = (String) d.get("startTime"); // "HH:mm"
                    //     if (date != null && time != null) {
                    //         s.setStartAt(buildTimestamp(date, time));
                    //     }
                    // }

                    sessions.add(s);
                }
            }
            adapter.notifyDataSetChanged();
            txtEmpty.setVisibility(sessions.isEmpty() ? View.VISIBLE : View.GONE);
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Optional helper if you want to rebuild startAt from date+time for legacy docs:
    // private static Timestamp buildTimestamp(@NonNull String ymd, @NonNull String hhmm) {
    //     java.util.Calendar cal = java.util.Calendar.getInstance();
    //     String[] d = ymd.split("-");
    //     String[] t = hhmm.split(":");
    //     cal.set(java.util.Calendar.YEAR, Integer.parseInt(d[0]));
    //     cal.set(java.util.Calendar.MONTH, Integer.parseInt(d[1]) - 1);
    //     cal.set(java.util.Calendar.DAY_OF_MONTH, Integer.parseInt(d[2]));
    //     cal.set(java.util.Calendar.HOUR_OF_DAY, Integer.parseInt(t[0]));
    //     cal.set(java.util.Calendar.MINUTE, Integer.parseInt(t[1]));
    //     cal.set(java.util.Calendar.SECOND, 0);
    //     cal.set(java.util.Calendar.MILLISECOND, 0);
    //     return new Timestamp(new java.util.Date(cal.getTimeInMillis()));
    // }
}
