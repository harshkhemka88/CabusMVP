package com.example.cabusmvp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// STEP 360
// To show routes, we have to implement RoutingListener
// old code -- public class DriverMapsActivity extends FragmentActivity implements OnMapReadyCallback
// New -- public class DriverMapsActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener
// throws an error -- option + return --> implement methods -- resolves errors by creating -- onRoutingFailure, onRoutingSuccess, onRoutingCancelled, onRoutingStart
// step 361 in getAssignedCustomerPickupLocation method
public class DriverMapsActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener {

    // STEP 556
    private Button ivCollapse;
    private LinearLayout llView;
    private TextView tvDetailsDestination;
    private boolean isDetailed = true;

    // STEP 391
    private Button btnStatus;
    private int status = 0; // this will enable us to keep track of status -- customer picked up / dropped etc
    private String destination;
    private LatLng destinationLatLng;
    private LatLng pickupLatLng; // He has not created the variable but I have
    private TextView tvRoute;

    // STEP 367
    // step 368 in onCreate
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorAccentBlue,R.color.colorPrimaryDark,R.color.colorAccent,R.color.primary_dark_material_light};

    // STEP 331
    Button btnSettings;

    // STEP 323
    // Create global variable for the map fragment
    SupportMapFragment mapFragment;
    // step 324 is onCreate

    // STEP 319
    // Create constants for onRequestPermissionsResult method
    final int LOCATION_REQUEST_CODE = 101;
    // step 320 back in onRequestPermissionsResult

    // STEP 307
    private TextView tvDestination;

    // STEP 295
    private CardView customer_info;
    private ImageView ivCustomerImage;
    private TextView tvName, tvPhone;

    // STEP 243
    // Create isLoggingOut and initialize to false
    // step 244 is in ibLogout in onCreate
    private Boolean isLoggingOut = false;

    // STEP 232
    // Like we did in CustomerMapsActivity, we have to undo a lot of things if the customer cancels the cab
    private Marker pickupMarker;
    // Step 233 in assignedCustomerPickupLocation method

    // STEP 182
    // Declare a variable to store customer id
    private String customerId = "";
    // Now go in onCreate method -- step 183

    // STEP 101
    // Declare logout button
    private Button ibLogout;

    // STEP 89
    // Let us create a variable for firebase authorisation
    // Also create a Geofire variable to store geo-points in database
    private FirebaseAuth mAuth;
    private GeoFire geoFire;
    // Step 90 is in onMapReady

    // STEP 71
    // Constants - Declare TAG, request code for location permission and update interval and distance for location updates
    private static final String TAG = "DriverMapsActivity";
    final int LOCATION_PERMISSION = 100;

    // STEP 72
    // Variables -- for Getting user location and location updates -- 5 nos
    // We will only get this option if implementation 'com.google.android.gms:play-services-location:17.0.0' is added in our build.gradle
    FusedLocationProviderClient mFusedLocationClient;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    LocationCallback mLocationCallback;
    LatLng latLng;
    // Step 73 is in onCreate -- initializing these location variables

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        // STEP 324
        // Default code -- SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        // Now below -- mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        // Step 325 is back in onRequestPermissionsResult method
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        }

        else
        {
            mapFragment.getMapAsync(this);
        }

        // STEP 557
        tvDetailsDestination = findViewById(R.id.tvDetailsDestination);
        ivCollapse = findViewById(R.id.ivCollapse);
        llView = findViewById(R.id.llView);
        // Setup ivCollapse button click

        // STEP 392
        btnStatus = findViewById(R.id.btnStatus);
        tvRoute = findViewById(R.id.tvRoute);
        // setup on click listener -- step 393

        // STEP 368
        polylines = new ArrayList<>();
        // step 369 in onRoutingSuccess method

        // STEP 332
        btnSettings = findViewById(R.id.btnSettings);
        // Set it up -- step 333

        // STEP 308
        tvDestination = findViewById(R.id.tvDestination);
        // step 309 in getAssignedCustomer method

        // STEP 296
        customer_info = findViewById(R.id.customer_info);
        ivCustomerImage = findViewById(R.id.ivCustomerImage);
        tvName = findViewById(R.id.tvName);
        tvPhone = findViewById(R.id.tvPhone);
        // step 297 in getAssignedCustomer method

        // STEP 102
        ibLogout = findViewById(R.id.ibLogout);

        // STEP 558
        // set up click on ivCollapse
        // If we run the app, it is working fine
        // Step 559 in CustomerMapsActivity.java
        ivCollapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // STEP 562
                // Write if-else logic
                if (isDetailed)
                {
                    llView.setVisibility(View.GONE);
                }

                else
                {
                    llView.setVisibility(View.VISIBLE);
                }

                // STEP 563
                // And change false to true
                // If we run the app now
                isDetailed = !isDetailed;
            }
        });

        // STEP 393
        btnStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // STEP 394
                // If the driver is on the way to pick up the customer -- we will change status to 1
                // Make if block with a return statement
                switch (status)
                {
                    // STEP 408
                    // When the driver is on way to pick up the customer, we want to draw routes
                    // First from driver to pickup location and then from pickup location to destination
                    // First thing is first change status
                    case 1:

                        status = 2;

                        // STEP 409
                        // Erase existing polylines
                        erasePolylines();

                        // STEP 410
                        // check if destination lat long are not global declared value
                        if (destinationLatLng.latitude != 0.0 && destinationLatLng.longitude != 0.0)
                        {
                            // STEP 411
                            // This means that destination has been set by the customer
                            // So draw polylines -- route to marker
                            getRouteToMarker(destinationLatLng);
                        }

                        // STEP 412
                        // set text on button
                        btnStatus.setText("Ride completed");
                        // now we want to show the lat long of the destination on the map -- step 413 in get assigned customer destination

                        break;

                    // STEP 395
                    // If the driver ends the ride after dropping the customer -- we will change status to 2
                    // Make if block with a return statement
                    // step 396 in getAssignedCustomer method
                    case 2:
                    {
                        // STEP 422
                        // Let us create a new folder -- history -- in our database to record ride history
                        // Each ride history will be its own unique id
                        // Let us call a new method and create it in step 423
                        recordRide();

                        // STEP 397
                        // Call the endRide method when the driver stops the ride
                        // create the method below -- step 398
                        endRide();
                        break;
                    }
                }
            }
        });

        // STEP 333
        // on click listener
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // STEP 334
                // Start a new activity to take the driver to settings screen
                Intent intent = new Intent(DriverMapsActivity.this, com.example.cabusmvp.DriverSettingsActivity.class);
                startActivity(intent);
                return; // notice that we didn't finish this activity to ensure that this activity keeps running even when we go to the next activity
                // throws an error because -- DriverSettingsActivity does not exist -- create it -- Empty Activity
                // step 335 in activity_driver_settings.xml -- Text tab
            }
        });

        // STEP 103
        ibLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // STEP 244
                // Set the isLoggingOut variable to true
                isLoggingOut = true;

                // STEP 245
                // Call the disconnect driver method and disconnect callback also
                LocationServices.getFusedLocationProviderClient(DriverMapsActivity.this).removeLocationUpdates(mLocationCallback);
                // The above line is a risk not there in his code, so may crash the app -- BEWARE
                disconnectDriver();

                // STEP 246
                // Comment out step 104 and just sign out here
                // step 247 in Activity_customer_maps.xml – Text Tab
                FirebaseAuth.getInstance().signOut();

                // STEP 104
                // Sign out
                // Also remove them from drivers available folder in database
                /*mAuth = FirebaseAuth.getInstance();

                FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
                if (mFirebaseUser != null)
                {
                    String userIdLogout = mFirebaseUser.getUid();
                    DatabaseReference dbRefLogout = FirebaseDatabase.getInstance().getReference("driversAvailable");
                    geoFire = new GeoFire(dbRefLogout);
                    geoFire.removeLocation(userIdLogout);
                    mAuth.signOut();
                }*/

                // STEP 105
                // And take user back to the main activity
                Intent intent = new Intent(DriverMapsActivity.this, com.example.cabusmvp.MainActivity.class);
                startActivity(intent);
                finish();
                // Run the app and check -- itn is working fine
                // Now create a new empty activity -- CustomerMapsActivity.java --
                // Why empty? Since we will copy-paste everything it does not matter if it is empty or maps
                // step 106 in activity_customer_maps.xml
            }
        });

        // STEP 73
        // Initialize location variables
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Step 74 -- initialize mLocationRequest and mLocationCallback in onMapReady

        // STEP 183
        // call a new method -- getAssignedCustomer -- this method keeps running in the app to assign new customers to the driver
        // Go before the onMapReady method to create it -- step 184
        getAssignedCustomer();
    }

    // STEP 480
    // getCurrentTimestamp is of the type long -- integer but long value
    private Long getCurrentTimestamp(){

        // STEP 481
        // Create a timestamp and return it
        Long timestamp = System.currentTimeMillis()/1000; // To convert to seconds
        return timestamp;
        // Step 482 in HistoryActivity.java
    }

    // STEP 423
    private void recordRide() {

        // STEP 424
        // Get current user's uid
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // STEP 425
        // Go to folders -- Drivers and Customers and create a new folder called history
        // Also create a new "history" folder
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("history");

        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers")
                .child(userId).child("history");

        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers")
                .child(customerId).child("history");

        // STEP 426
        // Now for the history main folder -- we need to create a unique id -- let us call it historyId
        // Simcoder has called it requestId, but that may be confused with customer request id
        String historyId = historyRef.push().getKey();

        // STEP 427
        // We want this historyId to be visible in customer and driver folders
        // We do this by creating a history id field and setting it to true
        driverRef.child(historyId).setValue(true);
        customerRef.child(historyId).setValue(true);

        // STEP 428
        // In the main history folder, we want to put info about customer, driver, and rating for now -- create has map
        // We can add say cost, destination and stuff later
        // If we run the code, it is working
        // Activity_customer_maps.xml – Step 429
        HashMap map = new HashMap();
        map.put("driver", userId);
        map.put("customer", customerId);
        map.put("rating", 0); // If no rating then rating is 0 (which we will ignore when calculating average ratings

        // STEP 479
        // Now we also want to put time and destination
        map.put("destination", destination);
        map.put("timestamp", getCurrentTimestamp()); // since we do not have the value of timestamp, we will create a new method above -- step 480 -- getCurrentTimestamp

        historyRef.child(historyId).updateChildren(map);

    }

    // STEP 398
    // Create endRide method -- we need to do 3 main things when the driver ends the ride
    // One: We need to remove location from the main Customer Request folder as that task has been completed now when the trip ends -- customer id will remain
    // Second: Under Users --> Drivers folder there is a sub-folder of customer request with customer info -- that needs to be deleted
    // Third: Driver needs to move from driversWorking to driverAvailable
    // Extras: deleting markers, erasing polylines and setting global variables to how they were before the ride
    private void endRide() {

        // STEP 404
        btnStatus.setText("Start Trip");

        // STEP 405
        // call the erase polylines method to remove polylines
        erasePolylines();

        // STEP 401
        // Get the driver userId
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // STEP 402
        // Access the customerRequest folder in database under Drivers collection
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child("Drivers").child(userId).child("customerRequest");

        // STEP 403
        // Remove contents inside the customerRequest folder
        driverRef.removeValue();

        // STEP 399
        // Go to customer request folder and remove location from there
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(customerId);

        // STEP 400
        // Also set customer id to blank again -- as is in our global variable
        customerId = "";
        if (pickupMarker != null)
        {
            pickupMarker.remove();
        }

        // STEP 406
        // Ui stuff
        // step 407 in getAssignedCustomer method
        customer_info.setVisibility(View.GONE);
        tvPhone.setText("Customer Phone: ");
        tvName.setText("Customer Name: ");
        tvRoute.setText("Route details: ");
    }

    // STEP 184
    private void getAssignedCustomer(){

        // STEP 185
        // Get the current authenticated driver id using if statement to avoid null exception pointer
        // So get instance for mAuth
        mAuth = FirebaseAuth.getInstance();

        // STEP 186
        // Then get current user and check if they are not null
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if (mFirebaseUser != null)
        {
            // STEP 187
            // If current user is not null, then get the user id of the driver
            String driverId = mFirebaseUser.getUid();

            // STEP 309
            // customerRideId is not a folder anymore, it is a document inside customerRequest folder -- so
            // code in step 188 -- DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users")
            //                    .child("Drivers").child(driverId).child("customerRideId");
            // New code below --
            DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users")
                    .child("Drivers").child(driverId).child("customerRequest").child("customerRideId");
            // step 310 is below

            // STEP 188
            // Now in steps 165 and 166 in CustomerMapsActivity.java, we had put customer id of customers requesting pickup
            // We had put this data inside our drivers Id document -- so let us get that from the database
            // DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users")
            //        .child("Drivers").child(driverId).child("customerRideId");

            // STEP 189
            // Now whenever new customer id is added to this, we want to inform the driver -- addValue event listener
            // addValueEventListener -- creates onCancelled and onDataChange
            assignedCustomerRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    // STEP 396
                    // Change status to 1 meaning that driver is on way to pick up the customer
                    status = 1;
                    // step 397 in btnStatus (onCreate)

                    // STEP 190
                    if (dataSnapshot.exists())
                    {
                        // STEP 191
                        // If there is data inside the document, then we just want to get the value and set it to customerId
                        customerId = dataSnapshot.getValue().toString();
                        Log.d(TAG, "onDataChange: accessed customer id data" + customerId);

                        // STEP 192
                        // Now we also want to get this particular customer's location from the database -- getAssignedCustomerPickupLocation
                        // We will create a new method for it -- step 193 -- before onMapReady
                        getAssignedCustomerPickupLocation();

                        // STEP 310
                        // Call a method to get user destination -- create it in step 311 outside of this method
                        getAssignedCustomerDestination();

                        // STEP 297
                        // Call a method to get user info -- create it in step 298 outside of this method
                        getAssignedCustomerInfo();
                    }

                    // STEP 235
                    // If the dataSnapshot does not exist, that means the customer ride id has been removed in step 225 in CustomerMapsActivity.java
                    // So use an else statement here
                    else
                    {
                        // STEP 407
                        // If the customer cancels, we want to call the method endRide
                        // So, comment out previous steps and just call the function
                        // step 408 in btnStatus in onCreate method
                        endRide();

                        /*// STEP 373
                        // Remove polylines and route
                        // If you run the app now, it should work fine -- could not check it because laptop is slow
                        // step 374 in CustomerMapsActivity.java
                        erasePolylines();

                        // STEP 236
                        // set customer id to blank
                        customerId = "";

                        // STEP 237
                        // if pickup marker is still there, remove it
                        // if we run the app, it works fine
                        // step 238 in
                        if (pickupMarker != null)
                        {
                            pickupMarker.remove();
                        }

                        // STEP 300
                        // Hide customer info box if request is cancelled
                        // Also set text views to blank -- OPTIONAL for good measure
                        // run the app, it is working fine
                        // step 301 in build.gradle (app)
                        customer_info.setVisibility(View.GONE);
                        tvPhone.setText("");
                        tvName.setText("");*/
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: get assigned customer error " + databaseError.getMessage());
                }
            });
        }
    }

    // STEP 362
    // Param is LatLng -- you can call it anything
    private void getRouteToMarker(LatLng customerLatLng){

        // STEP 363
        // call routing
        // waypoints -- takes start and end latlngs
        // start: driver last location, end: customerLatLng
        Routing routing = new Routing.Builder()
                .key("AIzaSyBcBXWbKw2DABqPYhdY3KSASWUknUC3_Ls") // we have to declare server key everytime we run Routing
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), customerLatLng)
                .build();

        // STEP 364
        // Execute it
        // step 365 in onRoutingFailure method below
        routing.execute();
    }

    // STEP 311
    private void getAssignedCustomerDestination(){

        // STEP 312
        // Copy-paste steps 185 to 187, make changes
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if (mFirebaseUser != null)
        {
            String driverId = mFirebaseUser.getUid();

            // STEP 413
            // Now we do not want to just listen for destination but also lat and long
            // So now customer request folder will have -- destination and destination latlng
            // so earlier code -- DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest").child("destination");
            // Comment it out and write new code
            // DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest").child("destination");
            DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest");

            // STEP 313
            // Add singleEventListener -- not value event listener
            assignedCustomerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    // STEP 314
                    if (dataSnapshot.exists())
                    {
                        // STEP 414
                        // Create a new map object to store value from the data snapshot
                        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                        // STEP 415
                        // Write an if statement -- if there is destination in our database
                        if (map.get("destination") != null)
                        {
                            // STEP 416
                            // step 315 code -- String destination = dataSnapshot.getValue().toString();
                            // But we want to set the global variable destination to that data so --
                            destination = map.get("destination").toString();

                            // STEP 417
                            // copy and paste the second line of code from step 315 inside our if block -- and comment out the old codee
                            tvDestination.setText("Metro station: " + destination);
                        }

                        // STEP 418
                        // Create 2 double variables to store lat and long info from the data
                        double destinationLat = 0.0;
                        double destinationLng = 0.0;

                        // STEP 419
                        // Create an if statement to check if value exists
                        // If yes, then set our above variable to it
                        if (map.get("destinationLat") != null)// destinationLat is the key we gave in step 375 in customer maps activity
                        {
                            destinationLat = Double.parseDouble(map.get("destinationLat").toString());
                        }

                        // STEP 420
                        // Do the same for long
                        if (map.get("destinationLng") != null)// destinationLng is the key we gave in step 375 in customer maps activity
                        {
                            destinationLng = Double.parseDouble (map.get("destinationLng").toString());

                            // STEP 421
                            // Now set our global destination lat lng to these variables
                            // If you run the app now, it is working fine
                            destinationLatLng = new LatLng(destinationLat, destinationLng);
                            // Step 422 is in onCreate method
                        }



                        // STEP 315
                        // Create a variable to store the data that we got
                        // And set the textView to this data
                        // String destination = dataSnapshot.getValue().toString();
                        // tvDestination.setText("Metro station: " + destination);
                        // No else because user in my case HAS to set destination
                        // step 316 is at the end of this file
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: Could not get customer location " + databaseError.getMessage());
                }
            });
        }
    }

    // STEP 297
    // Create method to get customer info
    private void getAssignedCustomerInfo(){

        // STEP 298
        // Just copy the contents of getUserInfo method in CustomerSettingsActivity and paste here and start editing
        // Here addListenerForSingleValueEvent is used instead of addValueEventListener -- why?
        // addListenerForSingleValueEvent does NOT keep listening for change, which is what we want
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Customers").child(customerId);
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // STEP 299
                // Show the linear layout
                // step 300 in getAssignedCustomer
                customer_info.setVisibility(View.VISIBLE);

                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0)
                {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("name") != null) // "name" is the key in our database
                    {
                        tvName.setText("Customer Name: " + map.get("name").toString());
                    }

                    if (map.get("phone") != null) // "phone" is the key in our database
                    {
                        tvPhone.setText("Customer Phone: " + map.get("phone").toString());
                    }

                    if (map.get("profileImageUri") != null) // "profileImageUri" is the key in our database
                    {
                        Glide.with(getApplication()).load(map.get("profileImageUri").toString()).into(ivCustomerImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Database error in retrieving customer info " + databaseError.getMessage());
            }
        });
    }

    // STEP 193
    // Create getAssignedCustomerPickupLocation method
    private void getAssignedCustomerPickupLocation(){

        // STEP
        // Code in step 194 -- DatabaseReference assignedCustomerPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("customerRequest");
        // Now -- assignedCustomerPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("customerRequest");
        // step 234 is further down in this method

        // STEP 194
        // The customer location can be found in customerRequest folder --> customerId document --> "l"
        DatabaseReference assignedCustomerPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("customerRequest");
                //.child(customerId).child("l");

        // STEP 195
        // We want to get the value as when they change -- so add a value change listener
        // BUT, in our app, the location will not change for customer -- so value will not change
        // So first let us initiate geofire
        GeoFire geoFire = new GeoFire(assignedCustomerPickupLocationRef);

        // STEP 196
        // Now on geo-fire, we want to get location -- getLocation -- takes a key (customerId) and LocationCallback
        // LocationCallback creates -- onLocationResult and onCancelled
        geoFire.getLocation(customerId, new com.firebase.geofire.LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location)
            {
                // STEP 197
                // Check if location is not null
                // !customerId.equals("") code added later in step 235
                if (location != null && !customerId.equals(""))
                {
                    // STEP 198
                    // If not null, log it
                    Log.d(TAG, "onDataChange: Latitude received from firebase " + location.latitude);

                    // STEP 199
                    // Create 2 double variables to store latitude and longitude
                    double locationLat;
                    double locationLng;

                    // STEP 200
                    // Initialize these variables
                    // location is the parameter of this method containing lat and long details
                    locationLat = location.latitude;
                    locationLng = location.longitude;

                    // STEP 201
                    // Create a new lat long variable to store info that we got above
                    LatLng customerLatLng = new LatLng(locationLat, locationLng);

                    // STEP 202
                    // Log it
                    Log.d(TAG, "onDataChange: Customer Location " + customerLatLng);

                    // STEP 233, and 234
                    // code in step 203 -- mMap.addMarker(new MarkerOptions().position(customerLatLng).title("Customer Location"));
                    // now -- pickupMarker = mMap.addMarker(new MarkerOptions().position(customerLatLng).title("Customer Location"));
                    // step 235 in getAssignedCustomer method

                    // STEP 203
                    // Add marker to map and move camera
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(customerLatLng).title("Customer Location")
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(customerLatLng, 11));
                    // step 204 in onMapReady -- onLocationResult method

                    // STEP 361
                    // call a new method -- getRouteToMarker(customerLatLng); -- then create it above in step 362
                    getRouteToMarker(customerLatLng);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.d(TAG, "onCancelled: Error message " + databaseError.getMessage());
            }
        });


        /*  THIS IS THE CODE GIVEN IN THE VIDEO WHICH IS NOT USEFUL FOR US

            assignedCustomerPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // STEP 196
                // check if data exists
                if (dataSnapshot.exists())
                {
                    // STEP 197
                    // If data exists, create a variable to get data from snap shot -- in the form of a list
                    List<Object> map = (List<Object>) dataSnapshot.getValue();

                    // STEP 198
                    // Now from this list, we will need lat and long, create and initialize 2 new variables for that
                    double locationLat = 0;
                    double locationLng = 0;

                    // STEP 199
                    // If data is not null
                    if (map.get(0) != null)
                    {
                        // STEP 200
                        // Get the value and store in locationLat
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }

                    // STEP 201
                    // Do the same for longitude
                    if (map.get(1) != null)
                    {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }

                    // STEP 202
                    // Create a lat long position and set to lat and long above
                    LatLng customerLatLng = new LatLng(locationLat, locationLng);
                    Log.d(TAG, "onDataChange: Customer Location " + customerLatLng);

                    // STEP 203
                    // Add marker
                    mMap.addMarker(new MarkerOptions().position(customerLatLng).title("Customer Location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(customerLatLng, 5));
                    // step 204 in onMapReady -- onLocationResult method
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // STEP 74
        // Delete default mMap sydney code and initialize location request here
        // Also set interval and priority
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // STEEP 75
        // Initialize mLocationCallback = new LocationCallback(){}
        mLocationCallback = new LocationCallback(){

            // STEP 76
            // Location call back has a method called onLocationResult -- start typing it -- automatically creates onLocationResult method
            // Do the same for onLocationAvailability and just let it be for now -- will come back to it in later steps
            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
                Log.d(TAG, "onLocationAvailability: It is available");
            }

            @Override
            public void onLocationResult(LocationResult locationResult) {
                // STEP 85
                // Delete Super statement
                // Store the location result in mLastLocation
                mLastLocation = locationResult.getLastLocation();

                // STEP 86
                // Convert this to a latlng variable to show on Map
                // setMyLocationEnabled as true and move camera there -- but remove button
                // Also, log it so that we can see it is being logged continuously updated -- every second
                // Log.d(TAG, "onSuccess: Latitude for updating check " + mLastLocation.getLatitude());
                latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                // mMap.addMarker(new MarkerOptions().position(latLng)); -- cannot add marker here, leaves a trail of markers behind
                if (mMap != null)
                {
                    // mMap.clear(); // If we clear this, no polylines are formed
                    mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.car_icon_red_top))
                                    .rotation(mLastLocation.getBearing()).anchor((float) 0.5, (float) 0.5));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }

                // STEP 90
                // After getting user location in step 86, we want to save the location data in our firebase database
                // First initialize mAuth
                // Then create a variable to get current authenticated user
                // Check if this user is not null -- why? -- when we create logout button in the future it creates a problem if we do not check for not null
                mAuth = FirebaseAuth.getInstance();
                FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
                if (mFirebaseUser != null)
                {
                    // STEP 91
                    // Now for this authenticated user, we want to go to our database and save location
                    // But, for that we need to create a new collection (folder) called Drivers Available
                    String userId = mFirebaseUser.getUid();
                    DatabaseReference dbRefAvailable = FirebaseDatabase.getInstance().getReference("driversAvailable");

                    // STEP 204
                    // Create another database reference for driversWorking -- same name as step 170
                    // And change dbRef in step 91 to dbRefAvailable
                    DatabaseReference dbRefWorking = FirebaseDatabase.getInstance().getReference("driversWorking");

                    // STEP 205
                    // Create 2 geofires as well
                    // Edit the one in step 92 -- geoFire = new GeoFire(dbRef); -- to -- GeoFire geoFireAvailable = new GeoFire(dbRefAvailable);
                    // And create a new one for dbRefWorking
                    // Also comment out step 93 -- geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                    GeoFire geoFireAvailable = new GeoFire(dbRefAvailable);
                    GeoFire geoFireWorking = new GeoFire(dbRefWorking);

                    // STEP 206
                    // If customer id is blank
                    if (customerId == "")
                    {
                        // STEP 207
                        // If there is no customer id -- the driver is available -- set location under driver available
                        // And remove the driver id from working
                        Log.d(TAG, "onLocationResult: customer id is empty");
                        geoFireWorking.removeLocation(userId);
                        geoFireAvailable.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                    }
                    else
                    {
                        // STEP 208
                        // Do the opposite
                        // If you run the app now, it is working fine
                        // Step 209 in Customer Map ACtivity . java -- getDriverLocation method
                        Log.d(TAG, "onLocationResult: customer id is not empty");
                        geoFireAvailable.removeLocation(userId);
                        geoFireWorking.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

                    }
                    // STEP 92
                    // Now initialize our geoFire variable and create collection
                    // geoFire = new GeoFire(dbRef);

                    // STEP 93
                    // Now set data in this collection/folder
                    // userId is the name of our file/document, and new GeoLocation is the values that we set inside the document/file
                    // geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                    // step 94 after startGettingLocation method

                    // mMap.addMarker(new MarkerOptions().position(latLng));
                }
            }
        };

        // STEP 87
        // Call method to get location
        startGettingLocation();
        // If we run the app now, it is working
        // Step 88 is in build.gradle(app)
    }

    // STEP 77
    // Create a new method to start getting location
    private void startGettingLocation(){

        // STEP 78
        // Now ask for location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // STEP 79
            // If permission is not granted, check location permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        }

        // STEP 80
        // If permission request has been granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            // STEP 81
            // Request location updates from fused location provider
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

            // STEP 82
            // Get last known location and -- add on success and on failure listener
            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {

                    // STEP 83
                    // Log it
                    Log.d(TAG, "onLocationResult: Location result is successful");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    
                    // STEP 84
                    // Log it
                    Log.d(TAG, "onFailure: Error while getting message " + e.getMessage());
                    // Step 85 is in onMapReady
                }
            });
        }
    }

    // STEP 240
    // Create a new method since we will use the same code twice
    private void disconnectDriver(){

        // STEP 317
        // Let us remove location in case the driver is working or if he is available
        // If you run the app now, it is working perfectly for onStop and onDestroy
        // step 318 is below
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference dbRefAvailable = FirebaseDatabase.getInstance().getReference("driversAvailable");
        DatabaseReference dbRefWorking = FirebaseDatabase.getInstance().getReference("driversWorking");
        GeoFire geoFireAvailable = new GeoFire(dbRefAvailable);
        GeoFire geoFireWorking = new GeoFire(dbRefWorking);

        if (customerId == "") // meaning that the driver is available
        {
            geoFireAvailable.removeLocation(userId);
        }

        else
        {
            geoFireWorking.removeLocation(userId);
        }

        // STEP 241
        // Copy-paste step 239 here -- comment it out there

        /*String userIdStop = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference dbRefStop = FirebaseDatabase.getInstance().getReference("driversAvailable");
        GeoFire geoFire = new GeoFire(dbRefStop);
        geoFire.removeLocation(userIdStop);*/

        // Step 242 in onStop
    }

    // STEP 318
    // From Nexus 6, Google wants us to ask for permissions through onRequestPermissionsResult -- create it
    // Right-click --> generate --> override methods
    // This medthod requires contants so create at top -- step 319 -- and come back here
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // STEP 320
        // Use switch for request code
        switch (requestCode)
        {
            // If request code = LOCATION_REQUEST_CODE, then user has allowed us to use maps \
            case LOCATION_PERMISSION:
            {
                // STEP 321
                // Double check using an if statement -- grantResults is the parameter of this method
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // STEP 322
                    // Then start map -- mapFragment.getMapAsync(this);
                    // Throws an error -- we need to create a global variable for mapFragment -- go to the top -- step 323
                    mapFragment.getMapAsync(this);
                }

                // STEP 325
                // else show a Toast
                else
                {
                    Toast.makeText(this, "Please provide location permission to continue using Cabus", Toast.LENGTH_LONG).show();
                }

                // STEP 326
                // write break statement
                // Now where to call for this permission? -- I have already done it (they way we do it usually is enough) -- simcoder was following an old way
                // If we run the app, it is working fine -- do the same thing in customer maps activity java file -- step327
                break;
            }
        }
    }

    // STEP 94
    // Create a new override method -- onStop -- so that regular updation of location to the database is stopped when activity stops
    // start typing onStop .. option will automatically appear
    @Override
    protected void onStop() {
        super.onStop();

        // STEP 238
        // Although I had done written some logout to avoid app crash (commented out -- steps 95 and 96), changing it to his code
        // So start an if statement -- if user is not logging out
        // Of course it throws an error because isLoggingOut variable does not exist yet
        if (!isLoggingOut)
        {
            // STEP 242
            // Call the method
            disconnectDriver();
            // step 243 is on top of this file

            // STEP 239
            // Get current user, remove from drivers available, and remove location updates
            // Step 240 is above this method
            // LocationServices.getFusedLocationProviderClient(DriverMapsActivity.this).removeLocationUpdates(mLocationCallback);
            // The above line is a risk not there in his code, so may crash the app -- BEWARE
            // String userIdStop = FirebaseAuth.getInstance().getCurrentUser().getUid();
            // DatabaseReference dbRefStop = FirebaseDatabase.getInstance().getReference("driversAvailable");
            // GeoFire geoFire = new GeoFire(dbRefStop);
            // geoFire.removeLocation(userIdStop);
        }

        // STEP 95
        // Copy-paste step 90 to 92
        /*mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if (mFirebaseUser != null)
        {
            String userIdStop = mFirebaseUser.getUid();
            DatabaseReference dbRefStop = FirebaseDatabase.getInstance().getReference("driversAvailable");
            GeoFire geoFire = new GeoFire(dbRefStop);

            // STEP 96
            // Now instead of setting location for this user, remove location
            // This will mean that the driver is no longer available for any customer
            geoFire.removeLocation(userIdStop);
            // If we run the app, we should be able to see that database being updated regularly
            // Now we want to create a logout button in the top right corner -- How? -- step 97 in build.gradle(app)
        }*/
    }

    // STEP 316
    // This is my code so may be incorrect
    // Create an onDestroy method with the same things as Logout, but without signout
    // step 317 in disconnectDriver method
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!isLoggingOut)
        {
            LocationServices.getFusedLocationProviderClient(DriverMapsActivity.this).removeLocationUpdates(mLocationCallback);
            disconnectDriver();
        }
    }

    // STEP 370
    private void erasePolylines(){

        // STEP 371
        // we have to remove each line in a polyline -- use for loop
        for (Polyline line : polylines)
        {
            line.remove();
        }

        // STEP 372
        // then clear our array list polylines variable
        polylines.clear();
        // now where do we call this method? in getAssignedCustomer method, when the user cancels -- step 373
    }

    @Override
    public void onRoutingFailure(RouteException e) {

        // STEP 365
        // Note that code for onRoutingFailure and onRoutingSuccess have been copied from --
        // https://github.com/jd-alexander/Google-Directions-Android/blob/master/sample/src/main/java/com/directions/sample/MainActivity.java
        // If error is resolveable show the error, otherwise just show that something went wrong
        // step 366 in onRoutingSuccess
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingStart() {

    }

    // STEP 366
    // Change parameter from -- ArrayList<Route> arrayList -- to -- ArrayList<Route> route
    // Change the int parameter from -- int i -- to int shortestRouteIndex
    // Required to do this to remove errors from the code copied from github -- url in step 365
    // Also, go to the top to declare a variable and a constant -- step 367
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        // STEP 369
        // If routing is done, we want to show polylines -- code copied from github -- url in step 365
        // step 370 is to create a method to clear route from map on cancellation / reaching destination -- above onRoutingFailure method
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            // Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
            tvRoute.setText("Distance: " + route.get(i).getDistanceValue() + "\n" +
                            "Duration: " + route.get(i).getDurationValue());
        }

    }

    @Override
    public void onRoutingCancelled() {

    }
}
