/**
 * OTAMS Project
 * Author: Tianqi Jiang
 * University of Ottawa
 *
 * Description:
 * Allows users to choose between Student and Tutor registration.
 */

package ca.otams.group36.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import ca.otams.group36.R;

public class RoleChooseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_choose);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("OTAMS");
        }

        Button buttonStudent = findViewById(R.id.buttonStudent);
        Button buttonTutor = findViewById(R.id.buttonTutor);

        buttonStudent.setOnClickListener(v ->
                startActivity(new Intent(this, StudentRegistrationActivity.class))
        );

        buttonTutor.setOnClickListener(v ->
                startActivity(new Intent(this, TutorRegistrationActivity.class))
        );
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}