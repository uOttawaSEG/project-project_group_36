package ca.otams.group36;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import android.util.Log;

import ca.otams.group36.activities.LoginActivity;
import ca.otams.group36.activities.RoleChooseActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        Log.d("Firebase", "Firebase initialized: " + FirebaseApp.getApps(this).size());

        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonRegister = findViewById(R.id.buttonRegister);

        buttonLogin.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LoginActivity.class))
        );

        buttonRegister.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RoleChooseActivity.class))
        );
    }
}