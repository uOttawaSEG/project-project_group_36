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
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
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

    private void saveSlot() {
        String date = editDate.getText().toString().trim();
        String start = editStart.getText().toString().trim();
        String end = editEnd.getText().toString().trim();
        boolean autoApprove = switchAuto.isChecked();

        if (date.isEmpty() || start.isEmpty() || end.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("tutorEmail", tutorEmail);
        data.put("date", date);
        data.put("startTime", start);
        data.put("endTime", end);
        data.put("autoApprove", autoApprove);

        db.collection("availability")
                .add(data)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Slot saved successfully!", Toast.LENGTH_SHORT).show();
                    finish();
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
