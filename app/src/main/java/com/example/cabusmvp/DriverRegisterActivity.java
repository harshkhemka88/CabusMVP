package com.example.cabusmvp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class DriverRegisterActivity extends AppCompatActivity {

    // STEP 20
    // Declare variables
    private static final String TAG = "DriverRegisterActivity";
    private ProgressBar progress_login;
    private TextView progress_load;
    private LinearLayout login_form;
    private EditText etEmail, etPassword, etPhone, etName, etCarNumber;
    private Button btnRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_register);

        // STEP 21
        // Initialize Variables
        progress_login = findViewById(R.id.progress_login);
        progress_load = findViewById(R.id.progress_load);
        login_form = findViewById(R.id.login_form);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        etName = findViewById(R.id.etName);
        etCarNumber = findViewById(R.id.etCarNumber);
        btnRegister = findViewById(R.id.btnRegister);
        mAuth = FirebaseAuth.getInstance();
        // Step 22 is outside onCreate method

        // STEP 23
        // Setup register button
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // STEP 24
                // Extract data from all fields
                final String email = etEmail.getText().toString().trim();
                final String password = etPassword.getText().toString();
                final String phone = etPhone.getText().toString();
                final String name = etName.getText().toString();
                final String car = etCarNumber.getText().toString();

                // STEP 25
                // Check to see if there is data in each field
                if (email.isEmpty())
                {
                    etEmail.setError("Please enter email");
                    return;
                }

                if (password.isEmpty())
                {
                    etPassword.setError("Please enter password");
                    return;
                }

                if (phone.isEmpty())
                {
                    etPhone.setError("Please enter phone");
                    return;
                }

                if (name.isEmpty())
                {
                    etName.setError("Please enter name");
                    return;
                }

                if (car.isEmpty())
                {
                    etCarNumber.setError("Please enter car number");
                    return;
                }

                // STEP 26
                // show progress bar
                // After extracting text from each field, create a user with email and password in database -- createUserWithEmailAndPassword
                // addOnCompleteListener -- automatically creates onComplete method
                showProgress(true);
                progress_load.setText("Registering user... Please wait...");
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(DriverRegisterActivity.this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                // STEP 27
                                // if registration is not successful hide progress bar and show a toast
                                if (!task.isSuccessful())
                                {
                                    showProgress(false);
                                    Toast.makeText(DriverRegisterActivity.this, "Sign-up error", Toast.LENGTH_SHORT).show();
                                }

                                // STEP 28
                                // if registration is successful
                                else
                                {
                                    // STEP 29
                                    // If registration is successful, we want to add the user to our database
                                    // To do that, First, get the User id of the user
                                    String user_id = mAuth.getCurrentUser().getUid();

                                    // STEP 30
                                    // Second, determine where in the database you want to add this user
                                    // So create and initialize DatabaseReference
                                    // We want to put it under -- Users -- Drivers -- userId
                                    DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(user_id);

                                    // STEP 355
                                    // Put data of driver in firebase
                                    Map userInfo = new HashMap();
                                    userInfo.put("name", name);
                                    userInfo.put("phone", phone);
                                    userInfo.put("car", car);

                                    // STEP 356
                                    // Code in step 31 -- current_user_db.setValue(true);
                                    // New code below
                                    // if we run the app, it is working fine, let's do the same thing for customer where their info is put from registration to database
                                    // step 357 in CustomerRegisterActivity.java
                                    current_user_db.updateChildren(userInfo);

                                    // STEP 31
                                    // Third, set value to true
                                    // current_user_db.setValue(true);

                                    // STEP 32
                                    // Now, stop showing progress, show toast, and redirect user to login screen
                                    // Step 33 is in DriverLoginActivity.java file
                                    showProgress(false);
                                    Toast.makeText(DriverRegisterActivity.this, "Sign-up successful", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(DriverRegisterActivity.this, com.example.cabusmvp.DriverLoginActivity.class);
                                    startActivity(intent);
                                }
                            }
                        });
            }
        });
    }

    // STEP 22
    // Create new method to show/hide progress bar
    // Step 23 is back in onCreate method
    public void showProgress (boolean show){

        if (!show)
        {
            progress_load.setVisibility(View.GONE);
            progress_login.setVisibility(View.GONE);
            login_form.setVisibility(View.VISIBLE);
        }

        else
        {
            progress_load.setVisibility(View.VISIBLE);
            progress_login.setVisibility(View.VISIBLE);
            login_form.setVisibility(View.GONE);
        }
    }
}
