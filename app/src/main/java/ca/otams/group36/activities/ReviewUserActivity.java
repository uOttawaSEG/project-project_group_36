package ca.otams.group36.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import ca.otams.group36.R;
import ca.otams.group36.models.User;

/**
 * ReviewUserActivity
 * - Loads a pending user by email (passed via Intent extra "userId", which is the email in current flow)
 * - Approve/Reject updates Firestore and triggers user notifications (email + optional sms via Intent)
 * - If you later add backend Cloud Functions for automatic email/FCM, you can remove notify*() calls.
 */
public class ReviewUserActivity extends AppCompatActivity {

    private TextView txtName, txtEmail, txtRole, txtPhone;
    private MaterialButton btnApprove, btnReject;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // NOTE: in your current flow this extra carries the EMAIL, not a Firestore doc id.
    private String emailFromIntent;

    // Cached Firestore document id after first query (avoids querying again)
    private String docId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_user);

        setSupportActionBar(findViewById(R.id.toolbarReviewUser));
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get "userId" extra (currently this is the email according to the list activities)
        emailFromIntent = getIntent().getStringExtra("userId");

        // Bind UI
        txtName = findViewById(R.id.txtReviewName);
        txtEmail = findViewById(R.id.txtReviewEmail);
        txtRole  = findViewById(R.id.txtReviewRole);
        txtPhone = findViewById(R.id.txtReviewPhone);

        btnApprove = findViewById(R.id.btnApprove);
        btnReject  = findViewById(R.id.btnReject);

        loadUserInfo();

        btnApprove.setOnClickListener(v -> updateStatusAndNotify("approved"));
        btnReject.setOnClickListener(v -> updateStatusAndNotify("rejected"));
        boolean fromRejected = getIntent().getBooleanExtra("fromRejected", false);
        if (fromRejected) {
            btnReject.setVisibility(View.GONE);
        }
    }

    private void loadUserInfo() {
        if (TextUtils.isEmpty(emailFromIntent)) {
            Toast.makeText(this, "Missing user email", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        db.collection("users")
                .whereEqualTo("email", emailFromIntent)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        DocumentSnapshot doc = query.getDocuments().get(0);
                        docId = doc.getId();
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            String fullName = safe(user.getFirstName()) + " " + safe(user.getLastName());
                            txtName.setText(fullName.trim());
                            txtEmail.setText(safe(user.getEmail()));
                            txtRole.setText(safe(user.getRole()));
                            txtPhone.setText(safe(user.getPhone()));
                        }
                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    /** Update status in Firestore and trigger email/SMS intents for the bonus demo. */
    private void updateStatusAndNotify(String status) {
        if (TextUtils.isEmpty(docId)) {
            Toast.makeText(this, "Document not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("users").document(docId)
                .update(updates)
                .addOnSuccessListener(x -> {
                    Toast.makeText(this, "User " + status, Toast.LENGTH_SHORT).show();

                    // === Demo notifications on device (email + sms) ===
                    String email = txtEmail.getText().toString().trim();
                    String phone = txtPhone.getText().toString().trim();
                    notifyByEmail(email, status);
                    notifyBySms(phone, status);

                    // Return to previous list; onResume() in list will refresh
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    // --- Local demo notifications (no extra permissions required) ---

    /** Opens email compose UI with prefilled subject/body. User taps send. */
    private void notifyByEmail(String toEmail, String status) {
        if (TextUtils.isEmpty(toEmail)) return;
        String subject = "Application status";
        String body = "Your application is " + status + ".";
        Intent i = new Intent(Intent.ACTION_SENDTO);
        i.setData(Uri.parse("mailto:" + Uri.encode(toEmail)));
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT, body);
        try { startActivity(Intent.createChooser(i, getString(R.string.app_name))); } catch (Exception ignored) {}
    }

    /** Opens default SMS app with prefilled content if a phone exists. */
    private void notifyBySms(String phone, String status) {
        if (TextUtils.isEmpty(phone)) return;
        Uri uri = Uri.parse("smsto:" + Uri.encode(phone));
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.putExtra("sms_body", "Your application is " + status + ".");
        try { startActivity(i); } catch (Exception ignored) {}
    }

    private static String safe(String s) { return s == null ? "" : s; }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
