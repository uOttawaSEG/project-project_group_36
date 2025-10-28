package ca.otams.group36.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import ca.otams.group36.R;
import ca.otams.group36.models.User;

public class ReviewUserActivity extends AppCompatActivity {

    TextView txtName, txtEmail, txtRole, txtPhone;
    MaterialButton btnApprove, btnReject;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_user);

        setSupportActionBar(findViewById(R.id.toolbarReviewUser));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get userId from intent (IMPORTANT)
        userId = getIntent().getStringExtra("userId");

        // Bind UI
        txtName = findViewById(R.id.txtReviewName);
        txtEmail = findViewById(R.id.txtReviewEmail);
        txtRole = findViewById(R.id.txtReviewRole);
        txtPhone = findViewById(R.id.txtReviewPhone);

        btnApprove = findViewById(R.id.btnApprove);
        btnReject = findViewById(R.id.btnReject);

        loadUserInfo();

        btnApprove.setOnClickListener(v -> updateStatus("approved"));
        btnReject.setOnClickListener(v -> updateStatus("rejected"));
        boolean fromRejected = getIntent().getBooleanExtra("fromRejected", false);
        if (fromRejected) {
            btnReject.setVisibility(View.GONE);
        }
    }

    private void loadUserInfo() {
        db.collection("users")
                .whereEqualTo("email", userId)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        DocumentSnapshot doc = query.getDocuments().get(0);
                        User user = doc.toObject(User.class);

                        txtName.setText(user.getFirstName() + " " + user.getLastName());
                        txtEmail.setText(user.getEmail());
                        txtRole.setText(user.getRole());
                        txtPhone.setText(user.getPhone());
                    }
                });
    }

    private void updateStatus(String status) {
        db.collection("users")
                .whereEqualTo("email", userId)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        String docId = query.getDocuments().get(0).getId();
                        db.collection("users").document(docId)
                                .update("status", status)
                                .addOnSuccessListener(x -> {
                                    Toast.makeText(this, "User " + status, Toast.LENGTH_SHORT).show();
                                    finish(); // Return to list automatically
                                });
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
