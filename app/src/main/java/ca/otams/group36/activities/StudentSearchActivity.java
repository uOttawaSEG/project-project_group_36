package ca.otams.group36.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.otams.group36.R;

public class StudentSearchActivity extends AppCompatActivity {

    private EditText editCourse;
    private Button btnSearch;
    private RecyclerView recycler;
    private ProgressBar progress;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String studentEmail; // from intent/session
    private String studentName;  // optional for writing into session

    // Minimal in-memory list of slots
    private final List<Map<String, Object>> slots = new ArrayList<>();
    private SlotsAdapter adapter; // a tiny adapter defined below

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_search);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        studentEmail = getIntent().getStringExtra("email");
        studentName = getIntent().getStringExtra("name");

        editCourse = findViewById(R.id.editCourseCode);
        btnSearch = findViewById(R.id.btnSearch);
        recycler = findViewById(R.id.recycler);
        progress = findViewById(R.id.progress);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SlotsAdapter(slots, this::requestBooking);
        recycler.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> doSearch());
    }

    private void doSearch() {
        String code = editCourse.getText().toString().trim().toUpperCase();
        if (TextUtils.isEmpty(code)) {
            Toast.makeText(this, "Enter course code", Toast.LENGTH_SHORT).show();
            return;
        }
        progress.setVisibility(View.VISIBLE);
        Timestamp now = Timestamp.now();

        db.collection("availability")
                .whereEqualTo("courseCode", code)
                .whereEqualTo("booked", false)
                .whereGreaterThanOrEqualTo("startAt", now)
                .orderBy("startAt") // ASC default
                .get()
                .addOnSuccessListener(qs -> {
                    slots.clear();
                    for (DocumentSnapshot d : qs.getDocuments()) {
                        Map<String, Object> m = new HashMap<>(d.getData());
                        if (m == null) continue;
                        m.put("id", d.getId());
                        slots.add(m);
                    }
                    adapter.notifyDataSetChanged();
                    progress.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // Click handler: do local conflict check then create a pending session
    private void requestBooking(Map<String, Object> slot) {
        if (studentEmail == null || studentEmail.isEmpty()) {
            Toast.makeText(this, "Missing student email", Toast.LENGTH_SHORT).show();
            return;
        }
        int startMin = ((Long) slot.get("startMinutes")).intValue();
        int endMin = ((Long) slot.get("endMinutes")).intValue();
        String tutorEmail = (String) slot.get("tutorEmail");
        String date = (String) slot.get("date");
        String startTime = (String) slot.get("startTime");
        String endTime = (String) slot.get("endTime");
        Timestamp startAt = (Timestamp) slot.get("startAt");
        String course = (String) slot.get("courseCode");
        String slotId = (String) slot.get("id");

        // Local conflict check with student's pending/approved sessions
        db.collection("sessions")
                .whereEqualTo("studentEmail", studentEmail)
                .whereIn("status", java.util.Arrays.asList("pending", "approved"))
                .get()
                .addOnSuccessListener(qs -> {
                    for (DocumentSnapshot d : qs.getDocuments()) {
                        Integer os = safeInt(d.get("startMinutes"));
                        Integer oe = safeInt(d.get("endMinutes"));
                        if (os != null && oe != null) {
                            boolean conflict = (startMin < oe) && (os < endMin);
                            if (conflict) {
                                Toast.makeText(this, "Time conflict with existing booking", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                    }
                    // No conflict → create session (pending) and optionally auto-approve
                    Map<String, Object> data = new HashMap<>();
                    data.put("slotId", slotId);
                    data.put("tutorEmail", tutorEmail);
                    data.put("studentEmail", studentEmail);
                    data.put("studentName", studentName);
                    data.put("subject", course);
                    data.put("date", date);
                    data.put("startTime", startTime);
                    data.put("endTime", endTime);
                    data.put("startAt", startAt);
                    data.put("startMinutes", startMin);
                    data.put("endMinutes", endMin);
                    data.put("status", ((Boolean) slot.get("autoApprove")) ? "approved" : "pending");
                    data.put("requestedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

                    db.collection("sessions").add(data).addOnSuccessListener(ref -> {
                        // If auto-approve, you may also mark slot as booked=true
                        if (Boolean.TRUE.equals(slot.get("autoApprove"))) {
                            db.collection("availability").document(slotId).update("booked", true);
                        }
                        Toast.makeText(this, "Request sent", Toast.LENGTH_SHORT).show();
                        // Remove from current list
                        slots.remove(slot);
                        adapter.notifyDataSetChanged();
                    }).addOnFailureListener(e ->
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                });
    }

    private static Integer safeInt(Object v) {
        if (v instanceof Long) return ((Long) v).intValue();
        if (v instanceof Integer) return (Integer) v;
        return null;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ----- Minimal inline adapter for slots list -----
    private static class SlotsAdapter extends RecyclerView.Adapter<SlotVH> {
        interface OnRequest {
            void onRequest(Map<String, Object> slot);
        }

        private final List<Map<String, Object>> data;
        private final OnRequest onRequest;

        SlotsAdapter(List<Map<String, Object>> data, OnRequest onRequest) {
            this.data = data;
            this.onRequest = onRequest;
        }

        @NonNull
        @Override
        public SlotVH onCreateViewHolder(@NonNull android.view.ViewGroup p, int vType) {
            android.view.View v = android.view.LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_slot_search, p, false);
            return new SlotVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull SlotVH h, int pos) {
            Map<String, Object> m = data.get(pos);
            String title = (String) m.get("courseCode");
            String tutor = (String) m.get("tutorEmail");
            String date = (String) m.get("date");
            String start = (String) m.get("startTime");
            String end = (String) m.get("endTime");
            h.txtTitle.setText(title + "  •  " + date + " " + start + "-" + end);
            h.txtSub.setText("Tutor: " + tutor + "   Avg: (fetch users/{tutor})");
            h.btnRequest.setOnClickListener(v -> onRequest.onRequest(m));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    private static class SlotVH extends RecyclerView.ViewHolder {
        TextView txtTitle, txtSub;
        android.widget.Button btnRequest;

        SlotVH(@NonNull android.view.View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtSub = itemView.findViewById(R.id.txtSub);
            btnRequest = itemView.findViewById(R.id.btnRequest);
        }
    }
}
