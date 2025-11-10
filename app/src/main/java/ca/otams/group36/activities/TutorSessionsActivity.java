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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import ca.otams.group36.R;
import ca.otams.group36.adapters.SessionsAdapter;
import ca.otams.group36.models.Session;

public class TutorSessionsActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private Spinner spinnerFilter;
    private TextView txtEmpty;
    private ArrayList<Session> sessions = new ArrayList<>();
    private SessionsAdapter adapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String tutorEmail;

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
        });
        recycler.setAdapter(adapter);

        // --- Filter spinner setup ---
        spinnerFilter = findViewById(R.id.spinnerFilter);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this, R.array.session_filters, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(spinnerAdapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString().toLowerCase();
                loadSessions(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadSessions(String status) {
        db.collection("sessions")
                .whereEqualTo("tutorEmail", tutorEmail)
                .whereEqualTo("status", status)
                .get()
                .addOnSuccessListener(q -> {
                    sessions.clear();
                    for (DocumentSnapshot d : q.getDocuments()) {
                        Session s = d.toObject(Session.class);
                        if (s != null) {
                            s.setId(d.getId());
                            sessions.add(s);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    txtEmpty.setVisibility(sessions.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
