package ca.otams.group36.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ca.otams.group36.R;

public class TutorCreateSlotActivity extends AppCompatActivity {

    private EditText editDate, editStart, editEnd;
    private Switch switchAuto;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String tutorEmail;
    private final Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_create_slot);

        // --- Toolbar ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Create Slot");
        }

        // Tutor identity passed from previous screen
        tutorEmail = getIntent().getStringExtra("email");

        editDate = findViewById(R.id.editDate);
        editStart = findViewById(R.id.editStart);
        editEnd = findViewById(R.id.editEnd);
        switchAuto = findViewById(R.id.switchAuto);

        editDate.setFocusable(false);
        editDate.setOnClickListener(v -> showDatePicker());

        editStart.setFocusable(false);
        editStart.setOnClickListener(v -> showTimePicker(editStart));

        editEnd.setFocusable(false);
        editEnd.setOnClickListener(v -> showTimePicker(editEnd));

        findViewById(R.id.btnSaveSlot).setOnClickListener(v -> saveSlot());
    }

    private void showDatePicker() {
        Calendar today = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    editDate.setText(sdf.format(calendar.getTime()));
                },
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
        );
        // Disallow past dates
        dialog.getDatePicker().setMinDate(today.getTimeInMillis());
        dialog.show();
    }

    private void showTimePicker(EditText target) {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    // Round minutes to 0 or 30; if >=45, roll to next hour
                    int roundedMinute = (selectedMinute < 15) ? 0 : (selectedMinute < 45 ? 30 : 0);
                    if (selectedMinute >= 45 && selectedHour < 23) selectedHour++;
                    target.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, roundedMinute));
                },
                hour,
                minute,
                true
        );
        dialog.show();
    }

    // --- Helpers -------------------------------------------------------------

    /** Convert "HH:mm" into total minutes since 00:00 (e.g., "09:30" -> 570). */
    private static int toMinutes(@NonNull String hhmm) {
        String[] p = hhmm.split(":");
        return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
    }

    /** Build a Firestore Timestamp from "yyyy-MM-dd" (date) and "HH:mm" (time). */
    private static Timestamp toTimestamp(@NonNull String ymd, @NonNull String hhmm) {
        Calendar cal = Calendar.getInstance();
        String[] d = ymd.split("-");   // 2025-11-10
        String[] t = hhmm.split(":");  // 09:30
        cal.set(Calendar.YEAR, Integer.parseInt(d[0]));
        cal.set(Calendar.MONTH, Integer.parseInt(d[1]) - 1);
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(d[2]));
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(t[0]));
        cal.set(Calendar.MINUTE, Integer.parseInt(t[1]));
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new Timestamp(new java.util.Date(cal.getTimeInMillis()));
    }

    // --- Save with validation & overlap check -------------------------------

    private void saveSlot() {
        String date = editDate.getText().toString().trim();   // "yyyy-MM-dd"
        String start = editStart.getText().toString().trim(); // "HH:mm"
        String end   = editEnd.getText().toString().trim();   // "HH:mm"
        boolean autoApprove = switchAuto.isChecked();

        // Basic validation
        if (date.isEmpty()) { editDate.setError("Date is required"); return; }
        if (start.isEmpty()) { editStart.setError("Start time is required"); return; }
        if (end.isEmpty())   { editEnd.setError("End time is required"); return; }
        if (tutorEmail == null || tutorEmail.isEmpty()) {
            Toast.makeText(this, "Missing tutor email", Toast.LENGTH_LONG).show();
            return;
        }

        int startMin = toMinutes(start);
        int endMin   = toMinutes(end);
        if (endMin <= startMin) {
            editEnd.setError("End time must be after start time");
            return;
        }

        // Must be in the future (client-side guard)
        Timestamp startAt = toTimestamp(date, start);
        if (startAt.compareTo(Timestamp.now()) <= 0) {
            editStart.setError("Start time must be in the future");
            return;
        }

        // Overlap check: same tutor + same date
        db.collection("availability")
                .whereEqualTo("tutorEmail", tutorEmail)
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener(snap -> {
                    for (var doc : snap.getDocuments()) {
                        int oStart = doc.contains("startMinutes")
                                ? doc.getLong("startMinutes").intValue()
                                : toMinutes(String.valueOf(doc.get("startTime")));
                        int oEnd   = doc.contains("endMinutes")
                                ? doc.getLong("endMinutes").intValue()
                                : toMinutes(String.valueOf(doc.get("endTime")));

                        // Overlap if: newStart < oldEnd && oldStart < newEnd
                        if (startMin < oEnd && oStart < endMin) {
                            editStart.setError("Overlaps with an existing slot");
                            editEnd.setError("Overlaps with an existing slot");
                            return; // Do not proceed to save
                        }
                    }

                    // No overlap -> save with helper fields for future queries
                    Map<String, Object> data = new HashMap<>();
                    data.put("tutorEmail", tutorEmail);
                    data.put("date", date);
                    data.put("startTime", start);
                    data.put("endTime", end);
                    data.put("autoApprove", autoApprove);
                    // helper fields
                    data.put("startMinutes", startMin);
                    data.put("endMinutes", endMin);
                    data.put("startAt", startAt);
                    data.put("createdAt", FieldValue.serverTimestamp());

                    db.collection("availability")
                            .add(data)
                            .addOnSuccessListener(doc -> {
                                Toast.makeText(this, "Slot saved successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
