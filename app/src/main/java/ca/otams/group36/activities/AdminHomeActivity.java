package ca.otams.group36.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import ca.otams.group36.R;

public class AdminHomeActivity extends AppCompatActivity {

    TextView txtWelcomeAdmin;
    MaterialButton btnPending, btnRejected, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        txtWelcomeAdmin = findViewById(R.id.txtWelcomeAdmin);

        btnPending = findViewById(R.id.btnPending);
        btnRejected = findViewById(R.id.btnRejected);
        btnLogout = findViewById(R.id.btnLogout);

        btnPending.setOnClickListener(v ->
                startActivity(new Intent(this, PendingRequestsActivity.class)));

        btnRejected.setOnClickListener(v ->
                startActivity(new Intent(this, RejectedRequestsActivity.class)));

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
