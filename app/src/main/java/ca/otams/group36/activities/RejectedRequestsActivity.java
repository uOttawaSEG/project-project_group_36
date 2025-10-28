package ca.otams.group36.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;
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
import ca.otams.group36.adapters.UsersAdapter;
import ca.otams.group36.models.User;

import android.content.Intent;

public class RejectedRequestsActivity extends AppCompatActivity {

    RecyclerView recycler;
    TextView txtEmpty;
    UsersAdapter adapter;
    ArrayList<User> users = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rejected_requests);

        // Toolbar setup
        setSupportActionBar(findViewById(R.id.toolbarRejected));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Bind UI
        txtEmpty = findViewById(R.id.txtEmptyRejected);
        recycler = findViewById(R.id.recyclerRejected);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        // When a rejected user is clicked → prompt deletion
        adapter = new UsersAdapter(users, user -> {
            Intent intent = new Intent(this, ReviewUserActivity.class);
            intent.putExtra("userId", user.getEmail());
            intent.putExtra("fromRejected", true); // 用于隐藏“拒绝”按钮
            startActivity(intent);
        });
        recycler.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRejectedUsers();
    }

    private void loadRejectedUsers() {
        db.collection("users")
                .whereEqualTo("status", "rejected")
                .get()
                .addOnSuccessListener(query -> {
                    users.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        users.add(doc.toObject(User.class));
                    }
                    adapter.notifyDataSetChanged();

                    txtEmpty.setVisibility(users.isEmpty() ? TextView.VISIBLE : TextView.GONE);
                });
    }

    /**
     * Confirm deletion dialog for rejected users.
     */
    private void showDeleteDialog(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to permanently delete " + user.getFirstName() + " " + user.getLastName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteUserFromFirestore(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Delete user record from Firestore.
     */
    private void deleteUserFromFirestore(User user) {
        db.collection("users")
                .whereEqualTo("email", user.getEmail())
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        String docId = query.getDocuments().get(0).getId();
                        db.collection("users").document(docId)
                                .delete()
                                .addOnSuccessListener(x -> {
                                    Toast.makeText(this, "Deleted: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                                    loadRejectedUsers(); // refresh list
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    }
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