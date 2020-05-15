package com.example.cabusmvp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CustomerSettingsActivity extends AppCompatActivity {

    // STEP 273
    private ImageView ivCustomerImage;
    private final int IMAGE_REQUEST_CODE = 103;
    private Uri resultUri; // Uri is for pictures
    private String mProfileImageUrl;

    // STEP 253
    private Button btnBack, btnConfirm;
    private EditText etName, etPhone;
    private FirebaseAuth mAuth;
    private DatabaseReference mCustomerDatabase;
    private String userID; // Variable to get userId of current user
    private String mName;
    private String mPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_settings);

        // STEP 274
        ivCustomerImage = findViewById(R.id.ivCustomerImage);

        // STEP 254
        btnBack = findViewById(R.id.btnBack);
        btnConfirm = findViewById(R.id.btnConfirm);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mCustomerDatabase = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Customers").child(userID); // Go to the userId document in Customers collections

        // STEP 270
        getUserInfo();
        // If you run the app, it is working fine
        // To store files and pictures, we need to go to Tools --> Firebase --> Storage
        // step 271 in build.gradle(app)

        // STEP 275
        ivCustomerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // STEP 276
                // Start an implicit intent for user to pick picture from gallery
                Intent intent = new Intent(Intent.ACTION_PICK);

                // STEP 277
                // set type of pick action -- image in our case
                // And start activity for result
                intent.setType("image/*");
                startActivityForResult(intent, IMAGE_REQUEST_CODE);
                // whenever we have startActivityForResult, we also have onActivityResult -- create it -- step 278 after onCreate
            }
        });

        // STEP 255
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // STEP 257
                // Call save user info method -- of course will throw an error -- as not created yet -- create it in step 257
                saveUserInformation();
            }
        });


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // STEP 256
                // Just finish is sufficient here, no need to write back code because we have not closed the previous activitys
                finish();
                return;
            }
        });
    }

    // STEP 278
    // onActivityResult -- right-click --> generate --> override methods
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // STEP 279
        // Check for request code and result
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_REQUEST_CODE)
        {
            // STEP 280
            // Get picture data and store in a Uri variable
            // Also set this picture in our global uri
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            // This uri variable will be used for intent

            // STEP 281
            // Set the image to ivImage
            ivCustomerImage.setImageURI(resultUri);
            // Now when the user clicks on the confirm button, we want to put this image in database
            // Step 282 is in save user information method below
        }
    }

    // STEP 263
    private void getUserInfo(){

        // STEP 264
        // Go to the database and add a listener there
        // Automatically creates -- onDataChange and onCancelled
        mCustomerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // STEP 265
                // check if data exists and -- also children count is more than 0 (just to be sure)
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0)
                {
                    // STEP 266
                    // Get data and store in a variable
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    // STEP 267
                    // Check if details is not null
                    if (map.get("name") != null) // "name" is the key in our database
                    {
                        // STEP 268
                        // Save this data in our global variable and show it in oue edit text
                        mName = map.get("name").toString();
                        etName.setText(mName);
                    }

                    // STEP 269
                    // Do the same for phone
                    if (map.get("phone") != null) // "phone" is the key in our database
                    {
                        mPhone = map.get("phone").toString().trim();
                        etPhone.setText(mPhone);
                        // Now call this method in onCreate -- Step 270
                    }

                    // STEP 293
                    // If the user has an image as well --
                    // If we run the app now, it is working in that it is uploaded in firebase -- but cannot see it -- maybe it will take some time
                    // Activity_driver_maps.xml – step 294
                    if (map.get("profileImageUri") != null) // "profileImageUri" is the key in our database
                    {
                        mProfileImageUrl = map.get("profileImageUri").toString();
                        Glide.with(getApplication()).load(mProfileImageUrl).into(ivCustomerImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // STEP 258
    private void saveUserInformation(){

        // STEP 259
        // To save info -- create a new map variable
        Map userInfo = new HashMap();

        // STEP 260
        // Now extract data from edit text
        mName = etName.getText().toString();
        mPhone = etPhone.getText().toString().trim();

        // STEP 261
        // Put in map
        userInfo.put("name", mName);
        userInfo.put("phone", mPhone);

        // STEP 262
        // Now put it on the database
        // We use updateChildren instead of put in case there is an old value
        mCustomerDatabase.updateChildren(userInfo);
        // This is done, but what about when the user comes to this screen again, we need to display old info
        // For that let us create another method getUserInfo on top of this method -- step 263

        // STEP 282
        // If the user selected an image
        if (resultUri != null)
        {
            // STEP 283
            // Initiate storage in firebase
            StorageReference filePath = FirebaseStorage.getInstance().getReference()
                    .child("profile_images").child(userID);
            // We want to create a folder called -- profile_images and document userID

            // STEP 284
            // Create a new bitmap variable
            Bitmap bitmap = null;

            // STEP 285
            // Convert uri to picture
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // STEP 286
            // Compress the picture
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);

            // STEP 287
            // Create a new array and move the image in to an array
            byte[] data = baos.toByteArray();

            // STEP 288
            // Now upload to firebase storage
            UploadTask uploadTask = filePath.putBytes(data);

            // STEP 289
            // Add listener to check if success -- addOnSuccessListener and on failure listeners
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    return;
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    // STEP 290
                    // If image is added to storage, download url
                    Task<Uri> downloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl();

                    // STEP 291
                    // And transfer this url to our realtime database
                    Map newImage = new HashMap();
                    newImage.put("profileImageUri", downloadUrl.toString());
                    mCustomerDatabase.updateChildren(newImage);
                    finish();
                    return;
                }
            });
        }

        // STEP 292
        // else statement
        // step 293 in getUserInformation method
        else
        {
            finish();
        }
    }
}
