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

    private EditText editCourse, editDate, editStart, editEnd;
    private Switch switchAuto;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String tutorEmail;
    private final Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_create_slot);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Create Slot");
        }

        tutorEmail = getIntent().getStringExtra("email");

        editCourse = findViewById(R.id.editCourse);
        editDate   = findViewById(R.id.editDate);
        editStart  = findViewById(R.id.editStart);
        editEnd    = findViewById(R.id.editEnd);
        switchAuto = findViewById(R.id.switchAuto);

        editDate.setFocusable(false);
        editStart.setFocusable(false);
        editEnd.setFocusable(false);

        editDate.setOnClickListener(v -> showDatePicker());
        editStart.setOnClickListener(v -> showTimePicker(editStart));
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

    private static int toMinutes(@NonNull String hhmm) {
        String[] p = hhmm.split(":");
        return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
    }

    private static Timestamp toTimestamp(@NonNull String ymd, @NonNull String hhmm) {
        Calendar cal = Calendar.getInstance();
        String[] d = ymd.split("-");
        String[] t = hhmm.split(":");
        cal.set(Calendar.YEAR, Integer.parseInt(d[0]));
        cal.set(Calendar.MONTH, Integer.parseInt(d[1]) - 1);
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(d[2]));
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(t[0]));
        cal.set(Calendar.MINUTE, Integer.parseInt(t[1]));
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new Timestamp(new java.util.Date(cal.getTimeInMillis()));
    }

    private void saveSlot() {
        String courseCode = editCourse.getText().toString().trim().toUpperCase();
        String date  = editDate.getText().toString().trim();
        String start = editStart.getText().toString().trim();
        String end   = editEnd.getText().toString().trim();
        boolean autoApprove = switchAuto.isChecked();

        // Validation
        if (courseCode.isEmpty()) {
            editCourse.setError("Course is required");
            return;
        }
        if (date.isEmpty()) {
            editDate.setError("Date is required");
            return;
        }
        if (start.isEmpty()) {
            editStart.setError("Start time required");
            return;
        }
        if (end.isEmpty()) {
            editEnd.setError("End time required");
            return;
        }

        int startMin = toMinutes(start);
        int endMin = toMinutes(end);
        if (endMin <= startMin) {
            editEnd.setError("End time must be later");
            return;
        }

        Timestamp startAt = toTimestamp(date, start);
        if (startAt.compareTo(Timestamp.now()) <= 0) {
            editStart.setError("Must be in the future");
            return;
        }

        db.collection("users")
                .whereEqualTo("email", tutorEmail)
                .limit(1)
                .get()
                .addOnSuccessListener(userSnap -> {

                    String tutorName = "";

                    if (!userSnap.isEmpty()) {
                        var user = userSnap.getDocuments().get(0);
                        String fn = user.getString("firstName");
                        String ln = user.getString("lastName");
                        tutorName = ((fn == null ? "" : fn) + " " + (ln == null ? "" : ln)).trim();
                    }

                    Map<String, Object> data = new HashMap<>();
                    data.put("tutorEmail", tutorEmail);
                    data.put("tutorName", tutorName);
                    data.put("courseCode", courseCode);
                    data.put("date", date);
                    data.put("startTime", start);
                    data.put("endTime", end);
                    data.put("autoApprove", autoApprove);
                    data.put("startMinutes", startMin);
                    data.put("endMinutes", endMin);
                    data.put("startAt", startAt);
                    data.put("booked", false);
                    data.put("createdAt", FieldValue.serverTimestamp());

                    db.collection("availability")
                            .add(data)
                            .addOnSuccessListener(d -> {
                                Toast.makeText(this, "Slot saved!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
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
