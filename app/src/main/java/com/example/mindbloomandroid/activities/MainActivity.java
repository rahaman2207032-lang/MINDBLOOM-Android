package com.example.mindbloomandroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mindbloomandroid.R;

public class MainActivity extends AppCompatActivity {
    private RadioGroup roleRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        roleRadioGroup = findViewById(R.id.roleRadioGroup);

        roleRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            handleRoleSelection(checkedId);
        });


    }

    private void handleRoleSelection(int selectedId) {
        String selectedRole;
        if (selectedId == R.id.chk) {
            selectedRole = "User";
        } else if (selectedId == R.id.chk2) {
            selectedRole = "Instructor";
        } else {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            return;
        }


        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("SELECTED_ROLE", selectedRole);
        startActivity(intent);
    }
}

