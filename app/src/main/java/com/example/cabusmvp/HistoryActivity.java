package com.example.cabusmvp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.format.DateFormat;

import com.example.cabusmvp.historyRecView.HistoryAdapter;
import com.example.cabusmvp.historyRecView.HistoryObject;
import com.example.cabusmvp.historyRecView.HistoryViewHolders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    // STEP 453
    // Create a variable for adapter and for layout manager
    // Also create an array list of history object type
    private RecyclerView.Adapter mHistoryAdapter;
    private RecyclerView.LayoutManager mHistoryLayoutManager;
    private ArrayList<HistoryObject> resultsHistory = new ArrayList<>();

    // STEP 435
    private RecyclerView rvHistory;
    private String customerOrDriver; // Who opened history window
    private String userId; // For firebase auth current user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // STEP 436
        rvHistory = findViewById(R.id.rvHistory);
        customerOrDriver = getIntent().getExtras().getString("CustomerOrDriver");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // STEP 437
        // Set nested scroll as false -- for smoother scroll -- OPTIONAL
        // Also set fixed size as true
        rvHistory.setNestedScrollingEnabled(true);
        rvHistory.setHasFixedSize(true);
        // Now for recycler view, we need to create an object and a custom adapter
        // Step 438 is in HistoryObject.java (we will create a new folder inside our package to keep recycler view stuff)

        // STEP 454
        // Initialize the layout manager
        // LinearLayoutManager -- takes context
        mHistoryLayoutManager = new LinearLayoutManager(HistoryActivity.this);
        rvHistory.setLayoutManager(mHistoryLayoutManager);

        // STEP 455
        // Initialize history adapter -- our adapter takes 2 params -- an item list and context
        // We will create the item list through a separate method called getDataSetHistory, we will create it later
        mHistoryAdapter = new HistoryAdapter(getDataSetHistory(), HistoryActivity.this);

        // STEP 456
        // Set the adapter to the recycler view variable
        rvHistory.setAdapter(mHistoryAdapter);
        // Now we go ahead and create getDataSetHistory method -- step 457

        // STEP 458
        // Call a new method to get user's history IDs
        // Create it in step 459
        getUserHistoryIds();
    }

    // STEP 487
    // Convert timestamp to date
    private String getDate(Long timestamp){

        // STEP 488
        // Get time zone of the user
        Calendar cal = Calendar.getInstance(Locale.getDefault());

        // STEP 489
        // Now set time
        cal.setTimeInMillis(timestamp*1000); // *1000 because we had divided by 1000 in an earlier step

        // STEP 490
        // Now convert to date
        // DateFormat is the android.text.format one
        String date = DateFormat.format("dd-MM-yyyy hh:mm", cal).toString();

        // STEP 491
        // return statement
        // If we run the app now it is working fine
        // step 492 in build.gradle (app)
        return date;

    }

    // STEP 459
    private void getUserHistoryIds(){

        // STEP 460
        // Create a database ref to get to the history folder inside users
        DatabaseReference userHistoryDatabase = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(customerOrDriver).child(userId).child("history");

        // STEP 461
        // Add listener to this folder
        userHistoryDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // STEP 462
                // check if data exists
                if (dataSnapshot.exists())
                {
                    // STEP 463
                    // Get the history id using for loop
                    // history folder will have multiple history Ids set to true -- so we will use getChildren function to get get data inside history folder
                    for (DataSnapshot history : dataSnapshot.getChildren())
                    {
                        // STEP 464
                        // for each for iteration, we want the app to go to that main history folder using the history id and get some info
                        // we will create a new method for it -- step 465 below
                        FetchRiderInformation(history.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    // STEP 465
    private void FetchRiderInformation(String rideKey) {

        // STEP 466
        // Now we are in the history main folder -- with a file/document of history id and inside that file, we have attributes like driver, rating etc
        DatabaseReference historyDatabase = FirebaseDatabase.getInstance().getReference().child("history")
                .child(rideKey);

        // STEP 467
        // set up a listener
        historyDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists())
                {
                    // STEP 482
                    // Create a variable to get timestamp and initialize it
                    // NOTE: if there is no timestamp, default is 1st Jan, 1970
                    Long timestamp = 0L;

                    // STEP 483
                    // Now create a for loop to get in to each file and get the time stamp
                    for (DataSnapshot child : dataSnapshot.getChildren())
                    {
                        // STEP 484
                        // If timestamp is a key
                        if (child.getKey().equals("timestamp"))
                        {
                            // STEP 485
                            // Then get value -- convert to string -- and then convert to long
                            timestamp = Long.parseLong(child.getValue().toString());
                        }
                    }

                    // STEP 468
                    // For now let us just get the ride id
                    String rideId = dataSnapshot.getKey();

                    // STEP 486
                    // in step 469, we have created a new object -- HistoryObject object = new HistoryObject(rideId);
                    // comment it out and new code is --
                    HistoryObject object = new HistoryObject(rideId, getDate(timestamp));
                    // We have use a new method -- getDate -- to convert the timestamp in to date -- step 487 above

                    // STEP 469
                    // Create a new History Object and add it to resultHistory list
                    // HistoryObject object = new HistoryObject(rideId);
                    resultsHistory.add(object);

                    // STEP 470
                    // Notify change to adapter
                    // If we run the app now, it is working fine
                    // Step 471 in item_history.xml -- text tab
                    mHistoryAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    // STEP 457
    // The return value of this method will be an array list
    // Step 458 in onCreate above
    private ArrayList<HistoryObject> getDataSetHistory(){
        return resultsHistory;
    }
}
