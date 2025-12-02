package ca.otams.group36.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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

    private String studentEmail;
    private String studentName;

    private final List<Map<String, Object>> slots = new ArrayList<>();
    private SlotsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_search);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        studentEmail = getIntent().getStringExtra("email");
        studentName  = getIntent().getStringExtra("name");

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
                .whereGreaterThanOrEqualTo("courseCode", code)
                .whereLessThanOrEqualTo("courseCode", code + '\uf8ff')
                .whereGreaterThan("startAt", now)
                .orderBy("courseCode")
                .orderBy("startAt")
                .get()
                .addOnSuccessListener(qs -> {
                    slots.clear();
                    for (DocumentSnapshot d : qs.getDocuments()) {
                        Map<String, Object> m = new HashMap<>(d.getData());
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

    private void requestBooking(Map<String, Object> slot) {

        if (studentEmail == null || studentEmail.isEmpty()) {
            Toast.makeText(this, "Missing student email", Toast.LENGTH_SHORT).show();
            return;
        }

        int startMin = safeInt(slot.get("startMinutes"));
        int endMin   = safeInt(slot.get("endMinutes"));

        String tutorEmail = (String) slot.get("tutorEmail");
        String tutorName  = (String) slot.get("tutorName");
        String date       = (String) slot.get("date");
        String startTime  = (String) slot.get("startTime");
        String endTime    = (String) slot.get("endTime");
        String slotId     = (String) slot.get("id");
        String course     = (String) slot.get("courseCode");

        Timestamp startAt = (Timestamp) slot.get("startAt");
        Timestamp endAt   = (Timestamp) slot.get("endAt");


        db.collection("sessions")
                .whereEqualTo("studentEmail", studentEmail)
                .whereIn("status", Arrays.asList("pending", "approved"))
                .get()
                .addOnSuccessListener(qs -> {

                    for (DocumentSnapshot d : qs.getDocuments()) {

                        String existingDate = d.getString("date");
                        if (existingDate == null || !existingDate.equals(date)) {
                            continue;
                        }

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



                    Map<String, Object> data = new HashMap<>();
                    data.put("slotId", slotId);
                    data.put("tutorEmail", tutorEmail);
                    data.put("tutorName", tutorName);

                    data.put("studentEmail", studentEmail);
                    data.put("studentName", studentName);

                    data.put("subject", course);

                    data.put("date", date);
                    data.put("startTime", startTime);
                    data.put("endTime", endTime);

                    data.put("startAt", startAt);
                    data.put("endAt", endAt);

                    data.put("startMinutes", startMin);
                    data.put("endMinutes", endMin);

                    boolean auto = Boolean.TRUE.equals(slot.get("autoApprove"));
                    data.put("status", auto ? "approved" : "pending");
                    data.put("requestedAt", FieldValue.serverTimestamp());

                    db.collection("sessions")
                            .add(data)
                            .addOnSuccessListener(ref -> {

                                db.collection("availability")
                                        .document(slotId)
                                        .update("booked", true);

                                if (auto) {
                                    db.collection("sessions")
                                            .document(ref.getId())
                                            .update("approvedAt", FieldValue.serverTimestamp());
                                }

                                Toast.makeText(this, "Request sent", Toast.LENGTH_SHORT).show();

                                slots.remove(slot);
                                adapter.notifyDataSetChanged();
                            });
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


    private static class SlotsAdapter extends RecyclerView.Adapter<SlotVH> {

        interface OnRequest { void onRequest(Map<String, Object> slot); }

        private final List<Map<String, Object>> data;
        private final OnRequest onRequest;

        SlotsAdapter(List<Map<String, Object>> data, OnRequest onRequest) {
            this.data = data;
            this.onRequest = onRequest;
        }

        @NonNull
        @Override
        public SlotVH onCreateViewHolder(@NonNull android.view.ViewGroup p, int vType) {
            View v = android.view.LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_slot_search, p, false);
            return new SlotVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull SlotVH h, int pos) {

            Map<String, Object> m = data.get(pos);
            String slotId = (String) m.get("id");

            String title = (String) m.get("courseCode");
            String date = (String) m.get("date");
            String start = (String) m.get("startTime");
            String end = (String) m.get("endTime");
            String tutorName = (String) m.get("tutorName");

            h.txtTitle.setText(title + "  •  " + date + " " + start + "-" + end);
            h.txtSub.setText("Tutor: " + tutorName);

            h.btnRequest.setEnabled(true);
            h.btnRequest.setText("Request");

            FirebaseFirestore.getInstance()
                    .collection("sessions")
                    .whereEqualTo("slotId", slotId)
                    .whereIn("status", Arrays.asList("pending", "approved"))
                    .get()
                    .addOnSuccessListener(qs -> {

                        if (qs.isEmpty()) {
                            h.btnRequest.setEnabled(true);
                            h.btnRequest.setText("Request");
                            return;
                        }

                        String status = qs.getDocuments().get(0).getString("status");

                        if ("approved".equals(status)) {
                            h.itemView.setVisibility(View.GONE);
                            h.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                            return;
                        }

                        else {
                            h.btnRequest.setEnabled(false);
                            h.btnRequest.setText("Pending Approval");
                        }
                    });

            h.btnRequest.setOnClickListener(v -> onRequest.onRequest(m));
        }


        @Override
        public int getItemCount() { return data.size(); }
    }

    private static class SlotVH extends RecyclerView.ViewHolder {
        TextView txtTitle, txtSub;
        Button btnRequest;

        SlotVH(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtSub = itemView.findViewById(R.id.txtSub);
            btnRequest = itemView.findViewById(R.id.btnRequest);
        }
    }
}
