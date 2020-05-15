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
import com.google.firebase.auth.FirebaseUser;

public class CustomerLoginActivity extends AppCompatActivity {

    // STEP 38
    // Declare variables
    private static final String TAG = "CustomerLoginActivity";
    private ProgressBar progress_login;
    private TextView progress_load;
    private LinearLayout login_form;
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgot;
    private FirebaseAuth mAuth; // Authorization variable
    private FirebaseAuth.AuthStateListener firesbaseAuthListener; // If authorisation state changes listener -- for login/logout activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);

        // STEP 39
        // Initialize variables
        progress_login = findViewById(R.id.progress_login);
        progress_load = findViewById(R.id.progress_load);
        login_form = findViewById(R.id.login_form);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgot = findViewById(R.id.tvForgot);
        mAuth = FirebaseAuth.getInstance();

        // STEP 40
        // Now, if the user has logged in before, we want to send them directly to maps activity
        // FirebaseAuth.AuthStateListener determines if user's login state changed from last time they logged in
        // Automatically generates onAuthStateChanged method
        firesbaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                // STEP 41
                // First create and initialize current user
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                // STEP 42
                // Check if this user is not null, ie, data about this user exists on our firebase
                if (user != null)
                {
                    // STEP 43
                    // If existing user has logged in last time, then take the user to the map activity
                    // Throws an error -- we will create Maps Activity for them later
                    Intent intent = new Intent(CustomerLoginActivity.this, com.example.cabusmvp.CustomerMapsActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                    // Step 44 is creating a new method to show and hide progress bar
                }
            }
        };

        // STEP 45
        // If however, user has not logged in before or logged out the last time, then we want to show our page
        // setup on click listeners for button, tvRegister, and tvForgot
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // STEP 46
                // If the user clicks the login button extract text
                final String email = etEmail.getText().toString().trim();
                final String password = etPassword.getText().toString();

                // STEP 47
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

                // STEP 48
                // Sign in with the user after showing progress bar
                // NOTE: If the task is successful, the firesbaseAuthListener is called as user is not null
                showProgress(true);
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(CustomerLoginActivity.this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (!task.isSuccessful())
                                {
                                    showProgress(false);
                                    Toast.makeText(CustomerLoginActivity.this, "Sign-in error", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // STEP 49
                // If the user clicks on this text, then take them to CustomerRegisterActivity
                // Of course, it will throw an error -- go ahead and create that activity
                // Step 50 is in activity_customer_register.xml -- Text Tab
                Intent intent = new Intent(CustomerLoginActivity.this, com.example.cabusmvp.CustomerRegisterActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        tvForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    // STEP 65
    // AuthStateListener is being called in onCreate method, it should also be called in onStart
    // Create onStart --> Right click -- Generate --> Override methods
    @Override
    protected void onStart() {
        super.onStart();

        // STEP 66
        // Call AuthStateListener
        mAuth.addAuthStateListener(firesbaseAuthListener);
    }

    // STEP 67
    // AuthStateListener should NOT be called on onStop
    // Create onStop --> Right click -- Generate --> Override methods
    @Override
    protected void onStop() {
        super.onStop();

        // STEP 68
        // Remove AuthStateListener
        mAuth.removeAuthStateListener(firesbaseAuthListener);
        // If we run the app now, it is running fine
        // Step 68 in google_maps_api.xml

    }

    // STEP 44
    // Create new method to show/hide progress bar
    // Step 45 is back in onCreate method
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
