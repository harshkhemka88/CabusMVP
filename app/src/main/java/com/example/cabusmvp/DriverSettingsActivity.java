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

public class DriverSettingsActivity extends AppCompatActivity {

    // STEP 336
    // Copy-paste CustomerSettingsActivity.java page
    // Remember to change setContentView(R.layout.activity_driver_settings); in onCreate
    // Make changes as required
    private EditText etCarNumber;

    // STEP 337
    private String mCarNumber;

    private ImageView ivDriverImage;
    private final int IMAGE_REQUEST_CODE = 103;
    private Uri resultUri; // Uri is for pictures
    private String mProfileImageUrl;
    private Button btnBack, btnConfirm;
    private EditText etName, etPhone;
    private FirebaseAuth mAuth;
    private DatabaseReference mDriverDatabase;
    private String userID; // Variable to get userId of current user
    private String mName;
    private String mPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_settings);

        // STEP 338
        etCarNumber = findViewById(R.id.etCarNumber);

        ivDriverImage = findViewById(R.id.ivDriverImage);
        btnBack = findViewById(R.id.btnBack);
        btnConfirm = findViewById(R.id.btnConfirm);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();

        // STEP 339
        // Change reference to Users -- Drivers -- Uid
        // step 340 in getUserInfo
        mDriverDatabase = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Drivers").child(userID); // Go to the userId document in Customers collections

        getUserInfo();

        ivDriverImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, IMAGE_REQUEST_CODE);
            }
        });

        // STEP 255
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation();
            }
        });


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_REQUEST_CODE)
        {
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            ivDriverImage.setImageURI(resultUri);
        }
    }

    // STEP 263
    private void getUserInfo(){

        mDriverDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0)
                {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("name") != null) // "name" is the key in our database
                    {
                        mName = map.get("name").toString();
                        etName.setText(mName);
                    }

                    if (map.get("phone") != null) // "phone" is the key in our database
                    {
                        mPhone = map.get("phone").toString().trim();
                        etPhone.setText(mPhone);

                    }

                    if (map.get("profileImageUri") != null) // "profileImageUri" is the key in our database
                    {
                        mProfileImageUrl = map.get("profileImageUri").toString();
                        Glide.with(getApplication()).load(mProfileImageUrl).into(ivDriverImage);
                    }

                    // STEP 340
                    // Add if for car
                    // step 341 in saveUserInformation
                    if (map.get("car") != null) // "phone" is the key in our database
                    {
                        mCarNumber = map.get("car").toString().trim();
                        etCarNumber.setText(mCarNumber);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveUserInformation(){

        Map userInfo = new HashMap();

        // STEP 341
        // Do for car number
        mCarNumber = etCarNumber.getText().toString();

        mName = etName.getText().toString();
        mPhone = etPhone.getText().toString().trim();

        // STEP 342
        // Put for car
        // If you run the app now, it should work
        // Activity_customer_maps.xml – step 343
        userInfo.put("car", mCarNumber);

        userInfo.put("name", mName);
        userInfo.put("phone", mPhone);

        mDriverDatabase.updateChildren(userInfo);

        if (resultUri != null)
        {
            StorageReference filePath = FirebaseStorage.getInstance().getReference()
                    .child("profile_images").child(userID);

            Bitmap bitmap = null;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);

            byte[] data = baos.toByteArray();

            UploadTask uploadTask = filePath.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    return;
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Task<Uri> downloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl();

                    Map newImage = new HashMap();
                    newImage.put("profileImageUri", downloadUrl.toString());
                    mDriverDatabase.updateChildren(newImage);
                    finish();
                    return;
                }
            });
        }

        else
        {
            finish();
        }
    }
}
