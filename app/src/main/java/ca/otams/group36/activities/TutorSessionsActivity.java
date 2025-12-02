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
import java.util.Comparator;

import ca.otams.group36.R;
import ca.otams.group36.adapters.TutorSessionsAdapter;
import ca.otams.group36.models.Session;

public class TutorSessionsActivity extends AppCompatActivity {

    public enum Filter { UPCOMING, PAST }

    private RecyclerView recycler;
    private TextView txtEmptySessions;
    private Spinner spinnerFilter;

    private TutorSessionsAdapter adapter;
    private ArrayList<Session> sessions = new ArrayList<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String tutorEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_sessions);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tutorEmail = getIntent().getStringExtra("email");

        recycler = findViewById(R.id.recyclerSessions);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        txtEmptySessions = findViewById(R.id.txtEmptySessions);
        spinnerFilter = findViewById(R.id.spinnerFilter);

        adapter = new TutorSessionsAdapter(sessions, (req, action) -> {}, false);
        recycler.setAdapter(adapter);


        ArrayAdapter<CharSequence> spinnerAdapter =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.tutor_session_filters,
                        R.layout.spinner_uottawa_item
                );

        spinnerAdapter.setDropDownViewResource(R.layout.spinner_uottawa_dropdown);
        spinnerFilter.setAdapter(spinnerAdapter);


        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (pos == 0) loadSessions(Filter.UPCOMING);
                else loadSessions(Filter.PAST);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
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

    private void loadSessions(Filter filter) {
        if (tutorEmail == null) {
            Toast.makeText(this, "Missing tutor email", Toast.LENGTH_SHORT).show();
            return;
        }

        Timestamp now = Timestamp.now();

        Query base = db.collection("sessions")
                .whereEqualTo("tutorEmail", tutorEmail)
                .whereEqualTo("status", "approved");

        Query q;

        if (filter == Filter.UPCOMING) {
            q = base.whereGreaterThanOrEqualTo("startAt", now)
                    .orderBy("startAt", Query.Direction.ASCENDING);
        } else {
            q = base.whereLessThan("startAt", now)
                    .orderBy("startAt", Query.Direction.DESCENDING);
        }

        q.get().addOnSuccessListener(snapshot -> {
            sessions.clear();

            for (DocumentSnapshot d : snapshot.getDocuments()) {
                Session s = d.toObject(Session.class);
                if (s != null) {
                    s.setId(d.getId());
                    sessions.add(s);
                }
            }

            // fallback local sort
            if (filter == Filter.UPCOMING) {
                sessions.removeIf(s -> s.getStartAt() == null ||
                        s.getStartAt().compareTo(now) < 0);
                sessions.sort(Comparator.comparing(Session::getStartAt));
            } else {
                sessions.removeIf(s -> s.getStartAt() == null ||
                        s.getStartAt().compareTo(now) >= 0);
                sessions.sort((a, b) -> b.getStartAt().compareTo(a.getStartAt()));
            }

            adapter.notifyDataSetChanged();
            txtEmptySessions.setVisibility(sessions.isEmpty() ? View.VISIBLE : View.GONE);

        }).addOnFailureListener(e -> {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }
}
