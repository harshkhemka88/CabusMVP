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

public class CustomerRegisterActivity extends AppCompatActivity {

    // STEP 51
    // Declare variables
    private static final String TAG = "CustomerRegisterActivity";
    private ProgressBar progress_login;
    private TextView progress_load;
    private LinearLayout login_form;
    private EditText etEmail, etPassword, etPhone, etName;
    private Button btnRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_register);

        // STEP 52
        // Initialize Variables
        progress_login = findViewById(R.id.progress_login);
        progress_load = findViewById(R.id.progress_load);
        login_form = findViewById(R.id.login_form);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        etName = findViewById(R.id.etName);
        btnRegister = findViewById(R.id.btnRegister);
        mAuth = FirebaseAuth.getInstance();
        // Step 53 is outside onCreate method

        // STEP 55
        // Setup register button
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // STEP 56
                // Extract data from all fields
                final String email = etEmail.getText().toString().trim();
                final String password = etPassword.getText().toString();
                final String phone = etPhone.getText().toString();
                final String name = etName.getText().toString();

                // STEP 57
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

                // STEP 58
                // show progress bar
                // After extracting text from each field, create a user with email and password in database -- createUserWithEmailAndPassword
                // addOnCompleteListener -- automatically creates onComplete method
                showProgress(true);
                progress_load.setText("Registering user... Please wait...");
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(CustomerRegisterActivity.this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                // STEP 59
                                // if registration is not successful hide progress bar and show a toast
                                if (!task.isSuccessful())
                                {
                                    showProgress(false);
                                    Toast.makeText(CustomerRegisterActivity.this, "Sign-up error", Toast.LENGTH_SHORT).show();
                                }

                                // STEP 60
                                // if registration is successful
                                else
                                {
                                    // STEP 61
                                    // If registration is successful, we want to add the user to our database
                                    // To do that, First, get the User id of the user
                                    String user_id = mAuth.getCurrentUser().getUid();

                                    // STEP 62
                                    // Second, determine where in the database you want to add this user
                                    // So create and initialize DatabaseReference
                                    // We want to put it under -- Users -- Customers -- userId
                                    DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference()
                                            .child("Users").child("Customers").child(user_id);

                                    // STEP 357
                                    // Put data of customer in firebase
                                    Map userInfo = new HashMap();
                                    userInfo.put("name", name);
                                    userInfo.put("phone", phone);

                                    // STEP 358
                                    // Code in step 63 -- current_user_db.setValue(true);
                                    // New code below
                                    // if we run the app, it is working fine, let's do the same thing for customer where their info is put from registration to database
                                    // step
                                    current_user_db.updateChildren(userInfo);
                                    // Step 359 in build.gradle (app)

                                    // STEP 63
                                    // Third, set value to true
                                    // current_user_db.setValue(true);

                                    // STEP 64
                                    // Now, stop showing progress, show toast, and redirect user to login screen
                                    // Step 65 is in CustomerLoginActivity.java file
                                    showProgress(false);
                                    Toast.makeText(CustomerRegisterActivity.this, "Sign-up successful", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(CustomerRegisterActivity.this, com.example.cabusmvp.CustomerLoginActivity.class);
                                    startActivity(intent);
                                }
                            }
                        });
            }
        });
    }

    // STEP 53
    // Create new method to show/hide progress bar
    // Step 54 is back in onCreate method
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
