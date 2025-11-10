package ca.otams.group36.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import ca.otams.group36.R;
import ca.otams.group36.adapters.AvailabilityAdapter;
import ca.otams.group36.models.Availability;

public class TutorSlotsActivity extends AppCompatActivity {

    RecyclerView recycler;
    ArrayList<Availability> slots = new ArrayList<>();
    AvailabilityAdapter adapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String tutorEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_slots);

        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("My Availability Slots");

        tutorEmail = getIntent().getStringExtra("email");

        recycler = findViewById(R.id.recyclerSlots);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AvailabilityAdapter(slots, this::confirmDeleteSlot);
        recycler.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSlots();
    }

    private void loadSlots() {
        db.collection("availability")
                .whereEqualTo("tutorEmail", tutorEmail)
                .get()
                .addOnSuccessListener(q -> {
                    slots.clear();
                    for (DocumentSnapshot d : q.getDocuments()) {
                        Availability a = d.toObject(Availability.class);
                        a.setId(d.getId());
                        slots.add(a);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void confirmDeleteSlot(Availability slot) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Slot")
                .setMessage("Are you sure you want to delete this slot?\n" +
                        slot.getDate() + " " + slot.getStartTime() + "-" + slot.getEndTime())
                .setPositiveButton("Delete", (dialog, which) -> deleteSlot(slot))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteSlot(Availability slot) {
        db.collection("availability").document(slot.getId())
                .delete()
                .addOnSuccessListener(x -> {
                    Toast.makeText(this, "Slot deleted", Toast.LENGTH_SHORT).show();
                    loadSlots();
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
