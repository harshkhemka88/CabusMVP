package com.example.cabusmvp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    // STEP 2
    // Declare buttons
    private Button btnDriver;
    private Button btnCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // STEP 3
        // Initialize buttons
        btnCustomer = findViewById(R.id.btnCustomer);
        btnDriver = findViewById(R.id.btnDriver);

        // STEP 4
        // Set on click listeners for the 2 buttons
        btnDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // STEP 5
                // When the user clicks the button, we want to take them to driver login activity -- start an intent
                // Also finish this activity after taking them to driver login activity
                // Do the same thing for customer -- just change destination to CustomerLoginActivity
                // Of course there is an error that this activity does not exist -- create it -- step 6 in activity_driver_login.xml
                Intent intent = new Intent(MainActivity.this, com.example.cabusmvp.DriverLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        btnCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, com.example.cabusmvp.CustomerLoginActivity.class);
                startActivity(intent);
                finish();
                return;

            }
        });
    }
}
