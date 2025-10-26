package ca.otams.group36.activities;

import android.content.Intent;
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

public class PendingRequestsActivity extends AppCompatActivity {

    RecyclerView recycler;
    TextView txtEmpty;
    UsersAdapter adapter;
    ArrayList<User> users = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_requests);

        // Toolbar
        setSupportActionBar(findViewById(R.id.toolbarPending));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // UI elements
        txtEmpty = findViewById(R.id.txtEmptyPending);
        recycler = findViewById(R.id.recyclerPending);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UsersAdapter(users, user -> {
            Intent intent = new Intent(this, ReviewUserActivity.class);
            intent.putExtra("userId", user.getEmail());
            startActivity(intent);
        });
        recycler.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPendingUsers();
    }

    private void loadPendingUsers() {
        db.collection("users")
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(query -> {
                    users.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        users.add(doc.toObject(User.class));
                    }
                    adapter.notifyDataSetChanged();

                    // Show empty message if no users
                    txtEmpty.setVisibility(users.isEmpty() ? TextView.VISIBLE : TextView.GONE);
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
