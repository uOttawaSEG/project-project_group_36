package ca.otams.group36.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

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

        // Toolbar
        setSupportActionBar(findViewById(R.id.toolbarRejected));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // UI Widgets
        txtEmpty = findViewById(R.id.txtEmptyRejected);
        recycler = findViewById(R.id.recyclerRejected);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        // List Adapter (no click action needed for rejected list)
        adapter = new UsersAdapter(users, user -> {});
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

                    // Show or hide empty hint
                    txtEmpty.setVisibility(users.isEmpty() ? TextView.VISIBLE : TextView.GONE);
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Return to Admin Home
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
