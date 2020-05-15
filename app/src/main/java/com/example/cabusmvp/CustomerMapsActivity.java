package com.example.cabusmvp;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.cabusmvp.model.MyPlaces;
import com.example.cabusmvp.model.Results;
import com.example.cabusmvp.remote.IGoogleAPIService;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// STEP 107
// After the line -- public class CustomerMapsActivity -- copy and paste the entire DriverMapsActivity
// REMEMBER - in onCreate method -- chnage layout to activity_customer_maps.xml
// Remove the steps and comments of copied code
// So only comments and steps will be mentioned in new or edited code
// Step 108 is in onStop method

// STEP 559
// We want to implement routes and maybe polylines later as well for customers now
// Original code -- public class CustomerMapsActivity extends FragmentActivity implements OnMapReadyCallback
// Now -- public class CustomerMapsActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener
// throws an error -- option + return --> implement methods -- resolves errors by creating -- onRoutingFailure, onRoutingSuccess, onRoutingCancelled, onRoutingStart
// Step 560 in getRouteToDestination method
public class CustomerMapsActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener {

    // STEP 563
    // Create global variables for to store route distance and duration values and text
    // Global so that we can reuse them anywhere we want
    // Step 564 in onRoutingSuccess method
    private String distanceText, durationText;
    private int distanceValue, durationValue;
    private double distanceValueKms;

    // STEP 549
    // Set up cardview and tv
    // Also, create a boolean and set it to false
    private CardView destination_info;
    private TextView tvDistance, tvPrice, tvDuration;
    private boolean locationOutOfBounds = false;

    // STEP 544 (contd)
    // Main step 544 in onActivityResult method
    private Marker markerDestination, markerPickup;

    // STEP 531
    // For nearby places, we want to create global variables so that we can play around with them in other methods as well
    private MarkerOptions markerOptionsNearby;
    private double latNearby;
    private double lngNearby;
    private LatLng latLngNearby;
    private String placeName;
    private String vicinity;
    private Marker markerNearby;
    // Step 532 is back in nearByPlace method

    // STEP 523
    // Declare Google API Service for Nearby
    // step 524 is in onCreate to initialize it
    IGoogleAPIService mService;

    // STEP 430
    private Button btnHistory;

    // STEP 378
    // We are making these variables global so that they can be turned on-off depending on whether the driver is working or not
    // step 379 is back in hasDriveEnded method
    private DatabaseReference driveHasEndedRef;
    private ValueEventListener driveHasEndedRefListener;

    // STEP 344
    private CardView driver_info;
    private ImageView ivDriverImage;
    private TextView tvName, tvPhone, tvCarNumber;

    // STEP 327
    // Create global variable for the map fragment
    SupportMapFragment mapFragment;
    // step 328 is onCreate

    // STEP 302
    // Create a string variable to store destination by customer
    private String destination;

    // STEP 248
    private Button btnSettings;

    // STEP 229
    // Create a global marker variable so that we can remove it on cancel
    // It was created under getDriverLocation method
    private Marker pickupMarker;
    // step 230 in is onMapReady method

    // STEP 218
    // Create 2 variables for getDriverLocation method -- one for DatabaseReference and another for the listener
    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;
    // Step 219 is in getDriverLocation method

    // STEP 215
    // Create a global GeoQuery variable
    private GeoQuery geoQuery;
    // Now go to getClosestDriver method and initialize this geoquery instead of old one -- step 216

    // STEP 178
    // Create a global marker -- why? -- refresh the marker for the driver each time he moves
    private Marker mDriverMarker;
    // step 179 is back in getDriverLocation method

    // STEP 158
    // Create radius, but also create other variables that will help us with geo-query later
    private int radius = 1;
    private boolean driverFound = false;
    private String driverFoundID;
    // step 159 is back in getClosestDriver method

    // STEP 142
    // Create another lat lng variable called pickupLocation and also for destinationLocation (used later)
    LatLng pickupLocation;
    LatLng destinationLocation;
    // step 143 is back in btnOrigin on click listener

    // STEP 122
    // Create a boolean variable to decide which location data to send to db -- clicked or current location
    private boolean originClicked = false;
    // step 123 is in onCreate method

    // STEP 118
    // To get address from location we need to use geocoder -- declare variable
    private Geocoder geocoder;
    // Step 119 in onMapReady method

    // STEP 112
    // Declare all variables created for this ui
    private EditText etOrigin, etDestination;
    private Button btnCancel, btnOrigin, btnDestination;
    private CardView search_box_origin;
    private CardView search_box_destination;
    // Initialize in onCreate method

    private Button ibLogout;

    private FirebaseAuth mAuth;
    private GeoFire geoFire;

    private static final String TAG = "CustomerMapsActivity";
    final int LOCATION_PERMISSION = 100;
    private int AUTOCOMPLETE_REQUEST_ORIGIN = 101;
    private int AUTOCOMPLETE_REQUEST_DESTINATION = 102;

    FusedLocationProviderClient mFusedLocationClient;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    LocationCallback mLocationCallback;
    LatLng latLng;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_maps);

        // STEP 328
        // Default code -- SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        // Now below -- mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        // Step 329 is in onRequestPermissionsResult method
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // STEP 550
        destination_info = findViewById(R.id.destination_info);
        tvDistance = findViewById(R.id.tvDistance);
        tvPrice = findViewById(R.id.tvPrice);
        tvDuration = findViewById(R.id.tvDuration);
        // Step 551 in onActivityResult method

        // STEP 524
        mService = Common.getGoogleAPIService();
        // Step 525 is after onCreate method

        // STEP 431
        btnHistory = findViewById(R.id.btnHistory);

        // STEP 374
        // Initialize destinationLocation with 0,0 coordinates
        destinationLocation = new LatLng(0.0, 0.0);
        // We have already extracted lat long in step 150, now we just need to place it in our database -- getClosestDriver method -- step 375

        // STEP 345
        driver_info = findViewById(R.id.driver_info);
        ivDriverImage = findViewById(R.id.ivDriverImage);
        tvName = findViewById(R.id.tvName);
        tvPhone = findViewById(R.id.tvPhone);
        tvCarNumber = findViewById(R.id.tvCarNumber);
        // Now we will show driver info in getClosestDriver method -- step 346

        // STEP 249
        btnSettings = findViewById(R.id.btnSettings);
        // Step 250 below -- on click listener

        // STEP 117
        // Initialize Places API
        Places.initialize(this, "AIzaSyBiRd9uQo1BruJ4B9wLVGxKPtPFoodQua8");
        // Step 118 is on top of this file

        // STEP 113
        // Initialize newly created variables
        etOrigin = findViewById(R.id.etOrigin);
        etDestination = findViewById(R.id.etDestination);
        btnCancel = findViewById(R.id.btnCancel);
        btnOrigin = findViewById(R.id.btnOrigin);
        btnDestination = findViewById(R.id.btnDestination);
        search_box_origin = findViewById(R.id.search_box_origin);
        search_box_destination = findViewById(R.id.search_box_destination);
        // Step 114 in onMapReady method

        // STEP 432
        // on click listener
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // STEP 433
                // Start a new activity -- HistoryActivity
                // We are doing -- putExtra("CustomerOrDriver", "Customer"); -- so that the History activity knows who is asking for info and connects with that database accordingly
                // Create a new empty activity -- step 434 -- activity_history.xml
                Intent intent = new Intent(CustomerMapsActivity.this, HistoryActivity.class);
                intent.putExtra("CustomerOrDriver", "Customers"); // "Customers" needs to be t6he same name as in our database
                startActivity(intent);
                return;
            }
        });

        // STEP 250
        // on click listener
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // STEP 251
                // Start a new activity to take the customer to settings screen
                Intent intent = new Intent(CustomerMapsActivity.this, com.example.cabusmvp.CustomerSettingsActivity.class);
                startActivity(intent);
                return; // notice that we didn't finish this activity to ensure that this activity keeps running even when we go to the next activity
                // throws an error because -- CustomerSettingsActivity does not exist -- create it
                // step 252 in activity_customer_settings.xml -- Text tab
            }
        });

        // STEP 151
        // set up on click listener for btn Destination
        btnDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // SEP 553
                // When the user presses the destination button, hide destination_info
                // If we run the app now, it is working fine
                // We want that whenever the customer finds the driver location, the camera should zoom to their lat long bounds
                // Step 554 in getDriverLocation method
                destination_info.setVisibility(View.GONE);

                // STEP 303
                // Ensure that destination is set
                // step 304 in getClosestDriver method
                destination = etDestination.getText().toString();
                if (destination.isEmpty())
                {
                    etDestination.setError("Please set destination");
                    return;
                }

                // STEP 152
                // For now we do not send destination location anywhere
                // Just make edit text non-editable and BOOK cab
                etDestination.setEnabled(false);
                // run the app, it is working

                // STEP 153
                // Now let us try and get the closest available driver -- create a new method and call it here -- getClosestDriver
                // Throws an error since we have to create it -- step 154 -- after onCreate method
                getClosestDriver();
            }
        });

        // STEP 145
        // set up on click listener for destination edit text
        etDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // STEP 146
                // We want to do the same thing here that we did for etOrigin
                // Copy-paste steps 125 to 127
                // The only change is to replace AUTOCOMPLETE_REQUEST_ORIGIN with AUTOCOMPLETE_REQUEST_DESTINATION
                List<Place.Field> placeField = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG,
                                                            Place.Field.TYPES);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, placeField)
                        .setCountry("IN")
                        .build(CustomerMapsActivity.this);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_DESTINATION);
                // Step 147 is in onActivityResult method
            }
        });

        // STEP 137
        // Set up on click listener for btnOrigin
        btnOrigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // STEP 138
                // We then want to send this location to firebase
                // First initialize mAuth
                // Then create a variable to get current authenticated user
                // Check if this user is not null -- why? -- when we create logout button in the future it creates a problem if we do not check for not null
                mAuth = FirebaseAuth.getInstance();
                FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
                if (mFirebaseUser != null)
                {
                    // STEP 139
                    // Now for this authenticated user, we want to go to our database and save location
                    // But, for that we need to create a new collection (folder) called Customer Requests
                    String userId = mFirebaseUser.getUid();
                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("customerRequest");

                    // STEP 140
                    // Now initialize Geofire for this collection (folder)
                    geoFire = new GeoFire(dbRef);

                    // STEP 141
                    // Now set data in this collection/folder
                    // userId is the name of our file/document, and new GeoLocation is the values that we set inside the document/file
                    geoFire.setLocation(userId, new GeoLocation(latLng.latitude, latLng.longitude));
                    // Now we want to put it on map
                    // First let us create another global lat long variable called pickupLocation
                    // This is just for our convenience and has no use as such -- so is OPTIONAL -- step 142 on top

                    // STEP 143
                    // Initialize pickup location variable with new latLng -- OPTIONAL
                    // Also set markers and all
                    pickupLocation = new LatLng(latLng.latitude, latLng.longitude);

                    // STEP 540
                    // call the nearby place method
                    nearByPlace("subway_station");
                    // If we run the app now, it is working fine
                    // We want new icon for metro stations -- create a new method called BitmapDescriptor -- step 541

                    // STEP 144
                    // Hide btnOrigin and show btnDestination, and search_box_destination
                    // Also ensure that etOrigin cannot be edited now -- set enabled false
                    etOrigin.setEnabled(false);
                    btnOrigin.setVisibility(View.GONE);
                    btnDestination.setVisibility(View.VISIBLE);
                    search_box_destination.setVisibility(View.VISIBLE);
                    // Now we want to set up on click listener for etDestination -- step 145 -- in oncreate method above
                }
            }
        });

        // STEP 123
        // Set up on click listener for etOrigin
        etOrigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // STEP 124
                // If the user clicks on this search bar, then we want to take them to google autocomplete
                // But first set origin clicked as true
                originClicked = true;

                // STEP 125
                // On click, we should first create a list of Place.Field to store all the addresses
                // We want name, address, and coordinates in our list
                List<Place.Field> placeField = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);

                // STEP 126
                // Now the main thing, on click we want to start a new intent for autocomplete
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, placeField)
                        .setCountry("IN")
                        .build(CustomerMapsActivity.this);
                // AutocompleteActivityMode can be OVERLAY or FULLSCREEN (takes up the entire screen for list) -- and you want to place your list in it
                // .build: we want to build in this activity

                // STEP 127
                // start activity for result
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_ORIGIN);
                // Remember that whenever we have startActivityForResult, we must always create a new method
                // onActivityResult -- step 128 -- below onMapReady method
            }
        });

        ibLogout = findViewById(R.id.ibLogout);

        ibLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(CustomerMapsActivity.this, com.example.cabusmvp.MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    // STEP 560
    // Let us initialize routing through a new method
    private void getRouteToDestination (LatLng markerLocation) {

        // STEP 561
        // Initialize, build, and execute route
        // Step 562 is in onRoutingFailure method
        Routing routing = new Routing.Builder()
                .key("AIzaSyBcBXWbKw2DABqPYhdY3KSASWUknUC3_Ls") // we have to declare server key everytime we run Routing
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(pickupLocation, destinationLocation)
                .build();

        routing.execute();
    }

    // STEP 541
    // Create a method for using vector as location pins
    // Step 542 in nearByPlace method
    private BitmapDescriptor bitmapDescriptorFromVector (Context context, @DrawableRes int vectorDrawableResourceId) {

        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    // STEP 535
    private String getUrl(double latitude, double longitude, String placeType){

        // STEP 536
        // Connect to the big google api url -- https://maps.googleapis.com/maps/api/place/nearbysearch/json?
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        // Till json because other info will be provided below

        // STEP 537
        // Provide other info needed in the url above -- be careful and check the big url to ensure that you do not make a mistake here
        // We want to build -- https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=30.3280983,-81.4855&radius=10000&type=market&sensor=true&key=AIzaSyDT7AtOorKRK_4kHYtCgT1iWzybUsS0-RM
        googlePlacesUrl.append("location="+latitude+","+longitude);
        googlePlacesUrl.append("&radius="+5500);
        googlePlacesUrl.append("&type="+placeType);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=AIzaSyDT7AtOorKRK_4kHYtCgT1iWzybUsS0-RM"); // This is BROWSER key, not API key

        // STEP 538
        // log it
        Log.d("getUrl", "getUrl: " + googlePlacesUrl.toString());

        // STEP 539
        // Return statement
        return googlePlacesUrl.toString();
        // Now where do we want to call nearByPlace method? -- in onCreate -- step 540
    }

    // STEP 525
    // Create a new method nearByPlace method -- parameter String placeType
    // This method will be called after the customer has selected their origin location and clicks on the confirm my location button
    private void nearByPlace(String placeType){

        // STEP 526
        // mMap.clear(); // Not sure about this, most probably would not be needing it
        // Get url with lat, long, and placeType params -- we will create this getUrl method -- later
        // For now -- String url = getUrl(pickupLocation.latitude, pickupLocation.longitude, placeType);
        String url = getUrl(pickupLocation.latitude, pickupLocation.longitude, placeType);

        // STEP 527
        // Call our interface method -- getNearbyPlaces -- created in step 512
        // And put the url variable created above and enqueue it
        // automatically creates onResponse and onFailure methods
        mService.getNearbyPlaces(url).enqueue(new Callback<MyPlaces>() {
            @Override
            public void onResponse(Call<MyPlaces> call, Response<MyPlaces> response) {

                // STEP 528
                // The response param will have a list of all the places
                // First check if it is successful
                if (response.isSuccessful())
                {
                    // STEP 529
                    // If successful, create a for loop to go through each item and show a marker
                    for (int i = 0; i < response.body().getResults().length; i++)
                    {
                        // STEP 530
                        // Get result at each point and store in a Results (which we got under model) variable
                        Results googlePlace = response.body().getResults()[i];
                        // Step 531 on top

                        // STEP 532
                        // Show marker at each location
                        // First initialize new marker options
                        // Also get latitude and longitude from the response and store in variables
                        // And store them in a lat long variable
                        // Also initialize variables for name and vicinity
                        // Calculate distance as well
                        markerOptionsNearby = new MarkerOptions();
                        latNearby = Double.parseDouble(googlePlace.getGeometry().getLocation().getLat()); // getPlace has all info for a particular location
                        lngNearby = Double.parseDouble(googlePlace.getGeometry().getLocation().getLng());
                        latLngNearby = new LatLng(latNearby, lngNearby);
                        placeName = googlePlace.getName();
                        vicinity = googlePlace.getVicinity();

                        Location loc1 = new Location("");
                        loc1.setLatitude(pickupLocation.latitude);
                        loc1.setLongitude(pickupLocation.longitude);

                        Location loc2 = new Location("");
                        loc2.setLatitude(latLngNearby.latitude);
                        loc2.setLongitude(latLngNearby.longitude);

                        float distance = loc1.distanceTo(loc2)/1000;

                        // STEP 533
                        // Set the above info on marker
                        markerOptionsNearby.position(latLngNearby).title(placeName)
                                .snippet(distance + " kilometers away")
                                .icon(bitmapDescriptorFromVector(CustomerMapsActivity.this, R.drawable.subway_blue));
                        Log.d(TAG, "onResponse: " + distance);

                        // STEP 542
                        // Add icon in code of step 533 -- with drawable -- bitmapDescriptorFromVector
                        // If we run the app now, it is working fine
                        // Now what if we want to clear all this markers when
                        // Step 543 in onActivityResult method

                        // STEP 534
                        // Set marker, move camera
                        markerNearby = mMap.addMarker(markerOptionsNearby);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                        // Step 535 is in getUrl method above -- it was called in step 526, but never created
                    }
                }
            }

            @Override
            public void onFailure(Call<MyPlaces> call, Throwable t) {
            }
        });


    }

    // STEP 384
    // If the driver rejects/ends the ride, endRide is activated
    private void endRide() {

        // STEP 385
        // Noe for endRide, the effect is the same for when the customer cancels cab
        // So whether the driver ends the ride or customer cancels the cab, it is the same thing
        // copy the code inside canCab method here and call this method in cancelCab -- step 386 in cancelCab

        // STEP 387
        // Paste the copied code here
        geoQuery.removeAllListeners();
        driverLocationRef.removeEventListener(driverLocationRefListener);

        // STEP 388
        // After copying code, we need to add the listener that we just set up for driver ending ride
        driveHasEndedRef.removeEventListener(driveHasEndedRefListener);
        // We call this endRide method in cancelCab, but we also have to call getHasRideEnded when driver ends the ride
        // We do that in getClosestDriver method -- step 389

        if (driverFoundID != null)
        {
            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users")
                    .child("Drivers").child(driverFoundID).child("customerRequest");

            driverRef.removeValue();

            driverFoundID = null;
        }

        driverFound = false;
        radius = 1;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(ref);

        geoFire.removeLocation(userId);

        if (pickupMarker != null)
        {
            pickupMarker.remove();
        }

        driver_info.setVisibility(View.GONE);
        tvPhone.setText("");
        tvName.setText("");
        tvCarNumber.setText("");

        btnCancel.setVisibility(View.VISIBLE);
        btnCancel.setText("Please restart the app to book again");
        btnDestination.setVisibility(View.GONE);
        search_box_destination.setVisibility(View.GONE);
    }

    // STEP 377
    // Create a method to show ride has ended -- hasRideEnded
    // Go to the top to create new variable -- 378
    private void getHasRideEnded()
    {
        // STEP 379
        // Get to the database
        driveHasEndedRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child("Drivers").child(driverFoundID).child("customerRequest").child("customerRideId");

        // STEP 380
        // add listener -- automatically creates onDataChange and onCancelled
        driveHasEndedRefListener = driveHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // STEP 381
                // check if data exists
                if (dataSnapshot.exists())
                {

                }

                // STEP 382
                // If the driver cancels thee ride, then else is triggered -- like it happened for getAssignedCustomer for the driver
                else
                {
                    // STEP 383
                    // Call a method called endRide
                    endRide(); // create this method above -- step 384
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    // STEP 347
    private void getDriverInfo(){

        // STEP 348
        // Copy-paste getAssignedCustomerInfo method from DriverMapsActivity.java

        // STEP 349
        // Database ref should be mDriverDatabase, not mCustomerDatabase
        // child -- "Drivers" not "Customers"

        // STEP 350
        // Important -- replace child(customerId) with child(driverFoundID)
        DatabaseReference mDriverDatabase = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Drivers").child(driverFoundID);

        mDriverDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                driver_info.setVisibility(View.VISIBLE);

                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0)
                {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("name") != null) // "name" is the key in our database
                    {
                        tvName.setText("Driver Name: " + map.get("name").toString());
                    }

                    if (map.get("phone") != null) // "phone" is the key in our database
                    {
                        tvPhone.setText("Driver Phone: " + map.get("phone").toString());
                    }

                    if (map.get("profileImageUri") != null) // "profileImageUri" is the key in our database
                    {
                        Glide.with(getApplication()).load(map.get("profileImageUri").toString()).into(ivDriverImage);
                    }

                    // STEP 351
                    // get car number as well
                    // step 352 in cancelCab method
                    if (map.get("car") != null) // "car" is the key in our database
                    {
                        tvCarNumber.setText("Car Number: " + map.get("car").toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Database error in retrieving customer info " + databaseError.getMessage());
            }
        });

    }

    // STEP 214
    // Setup an on click method for btnCancel
    // Now to cancel a cab we need to do 2 things in our database -- 1. Delete values from database
    // 2. Remove all listeners -- otherwise they will keep firing -- to do this we need to first create a global GeoQuery -- step 215 at top of this file
    public void cancelCab(View view){

        // STEP 386
        // call endRide method here
        // Also comment out steps 217 onward in this method
        // step 387 in endRide method
        endRide();

        /*// STEP 217
        // Now on the global geoQuery, remove all listeners
        geoQuery.removeAllListeners();
        // Now we need to remove another listener that we had created
        // Again create 2 variables at the top -- step 218

        // STEP 221
        // Remove database reference and listener for getDriverLocation method
        driverLocationRef.removeEventListener(driverLocationRefListener);
        // This takes care of all listeners, now we need to remove values from database

        // STEP 225
        // After removing customerRequest folder, we also need to remove customerRideId from under Drivers folder
        // An easy way to remove it is just to set the driverId to true and that will over-write the document itself
        // First check that driver found id is not null
        if (driverFoundID != null)
        {
            // STEP 353
            // By writing --  driverRef.setValue(true); -- in step 227, we are deleting data about cancelled requests
            // We may need this data for analysis -- so we do not want to delete it
            // code in step 226 -- DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID);
            // comment it out and write the code below
            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users")
                    .child("Drivers").child(driverFoundID).child("customerRequest");

            // STEP 226
            // Go to the driver id file/document
            // DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID);

            // STEP 354
            // In the FIRST line of code in step 227 -- driverRef.setValue(true); -- we delete all data
            // Let us not do that -- comment it out -- write the code below
            driverRef.removeValue();
            // But this creates another problem, if we remove customerRequest and there is no other attribute inside driverFoundID document then driver id will be deleted completely
            // The solution is to send an attribute for driver during registration -- when an id is assigned to the driver
            // step 355 in DriverRegisterActivity.java

            // STEP 227
            // Set the value to true
            // Also change the found id to null
            // driverRef.setValue(true);
            driverFoundID = null;
        }

        // STEP 228
        // Now for our geoquery that used driverFound and radius, we call them back
        // Reset the driver found to false, so that the driver can go back in to the system
        // And reset his radius to 1
        driverFound = false;
        radius = 1;
        // Now we need to remove marker, make a global marker variable -- step 229

        // STEP 222
        // First get the current authenticated user
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // STEP 223
        // For this user, we first want to remove the customerRequest folder that is created in our database
        // So refer to that folder and start geo-fire
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(ref);

        // STEP 224
        // Remove location of this user from the customerRequest folder
        // Also, get the screen back to how it should look like if the customer wants to book again -- WILL DO LATER
        geoFire.removeLocation(userId);

        // STEP 231
        // If pickup marker is not null, then remove it
        // This all on the customer side, now we need to notify the driver -- DriverMapsActivity.java -- step 232 near the top of that file
        if (pickupMarker != null)
        {
            pickupMarker.remove();
        }

        // STEP 352
        // Hide stuff
        // When the user cancels a ride, all info is lost, we do not want that
        // Go to step 225 above to understand it better -- step 353
        driver_info.setVisibility(View.GONE);
        tvPhone.setText("");
        tvName.setText("");
        tvCarNumber.setText("");

        btnCancel.setVisibility(View.VISIBLE);
        btnCancel.setText("Please restart the app to book again");
        btnDestination.setVisibility(View.GONE);
        search_box_destination.setVisibility(View.GONE);
        // step 225 is in cancelCab, but closer to the top*/
    }

    // STEP 169
    // Create a method to show driver location to the customer
    private void getDriverLocation()
    {
        // STEP 219
        // Step 170 code -- DatabaseReference driverLocationRef = FirebaseDatabase.getInstance().getReference().child("driversWorking").child(driverFoundID).child("l");
        // Our code -- driverLocationRef = FirebaseDatabase.getInstance().getReference().child("driversWorking").child(driverFoundID).child("l");

        // STEP 220
        // Step 171 code -- driverLocationRef.addValueEventListener(new ValueEventListener()
        // Our code -- driverLocationRefListener = driverLocationRef.addValueEventListener(new ValueEventListener()
        // Step 221 in cancelCab method

        // STEP 170
        // In later steps we will move driver from drivers available to driver working (new folder) and document -- driver found Id
        // Inside this document -- geo-query stores data in g, l -- g is some string value, and l has latitude and longitude
        // But, right now we assume that move has happened, so we want to reach till "l" child
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("driversWorking")
                .child(driverFoundID).child("l");

        // STEP 171
        // Now this "l" has latitude and long which we will need everytime the location of driver changes -- addValueEventListener
        // Automatically creates -- onDataChange and onCancelled
        driverLocationRefListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // STEP 172
                // first check data exists
                if (dataSnapshot.exists())
                {
                    // STEP 173
                    // If data exists, create a variable to get data from snap shot -- in the form of a list
                    List<Object> map = (List<Object>) dataSnapshot.getValue();

                    // STEP 174
                    // Now from this list, we will need lat and long, create and initialize 2 new variables for that
                    // Also change text of button to Driver Found
                    double locationLat = 0;
                    double locationLng = 0;
                    btnDestination.setText("Driver Found...");

                    // STEP 175
                    // Check if lat is not null
                    // In geo query, latitude is stored as 0, and longitude as 1 -- that is why get(0) represents latitude
                    if (map.get(0) != null)
                    {
                        // STEP 176
                        // Get the value and store in locationLat
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }

                    // STEP 177
                    // Do the same for longitude
                    // Step 178 is at the top of this file
                    if (map.get(1) != null)
                    {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }

                    // STEP 179
                    // Create a lat long position and set to lat and long above
                    LatLng driverLatLng = new LatLng(locationLat, locationLng);

                    // STEP 180
                    // if there is already a marker at driver marker, then we should remove it
                    if (mDriverMarker != null)
                    {
                        mDriverMarker.remove();
                    }

                    // STEP 209
                    // Now we want to measure distance between driver location and customer location
                    // For that create a location variable for pickup location
                    // And set the lat and long for that variable
                    Location loc1 = new Location("");
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);

                    // STEP 210
                    // Now do the same thing for driver location
                    Location loc2 = new Location("");
                    loc2.setLatitude(driverLatLng.latitude);
                    loc2.setLongitude(driverLatLng.longitude);

                    // STEP 211
                    // Now create another variable for distance and convert to kms
                    float distance = loc1.distanceTo(loc2)/1000;

                    // STEP 212
                    // Now if distance is less than 100 meters, assume the driver has arrived and show message to user
                    // Also give option to cancel cabus
                    if (distance < 100)
                    {
                        btnDestination.setText("Driver is Here");
                        btnDestination.setVisibility(View.GONE);
                        btnCancel.setVisibility(View.VISIBLE);
                    }

                    // STEP 213
                    // else show another message
                    else
                    {
                        btnDestination.setText("Driver Found: " + distance/1000 + " metres away"); // Convert meters in to kms -- decimal is required because distance is float
                        btnDestination.setVisibility(View.GONE);
                        btnCancel.setVisibility(View.VISIBLE);
                        // If you run the app now, it should be working fine
                        // Now let us create a new method to cancel cab, which is called whenever btnCancel is clicked -- step 214 -- below onCreate
                    }

                    // STEP 554
                    // We want the same code as step 181, but we want we want customized zoom based on lat long
                    // Comment out step 181 -- use same mDriverMarker code
                    // Now let us go to activity_driver_xml and work on its appearance -- step 555
                    mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.car_icon_red_top_smaller))
                            .title("Driver Location").rotation(loc2.getBearing()).anchor((float) 0.5, (float) 0.5));

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();

                    builder.include(pickupMarker.getPosition());
                    builder.include(mDriverMarker.getPosition());

                    LatLngBounds bounds = builder.build();

                    int width = getResources().getDisplayMetrics().widthPixels;
                    int height = getResources().getDisplayMetrics().heightPixels;
                    int padding = (int) (height * 0.20); // offset from edges of the map 10% of screen

                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
                    mMap.animateCamera(cu);

                    // STEP 181
                    // if driver marker is null, set it to our map
                    // mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.car_icon_red_top_smaller))
                    //        .title("Driver Location").rotation(loc2.getBearing()).anchor((float) 0.5, (float) 0.5));
                    // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(driverLatLng, 14));

                    /*mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Driver Location")
                            .icon(bitmapDescriptorFromVector(CustomerMapsActivity.this, R.drawable.car_black_36)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(driverLatLng, 11));*/
                    // Now we go to the driver maps activity java file -- step 182 -- at the top
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // STEP 154
    // Create a new method to get closest driver
    private void getClosestDriver(){

        // STEP 155
        // Get into the database and inside the driversAvailable folder/collection
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driversAvailable");

        // STEP 156
        // Setup geo fire in that folder
        GeoFire geoFire = new GeoFire(driverLocation);

        // STEP 216
        // Step 157 code -- GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        // Our code -- geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        // This initialize our global variable, IF there were other places using geoQuery, we would need to do the same
        // Now go to cancelCab method -- step 217

        // STEP 157
        // Now raise a geo-query
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        // The geo-query is centered at the lat and long of the pickup location within a set radius
        // There is an error as we have not defined a radius, go to the top and define one, also add a couple more variables to be used later -- step 158

        // STEP 162
        // Remove listeners -- this is done to avoid the listener to keep running even if we find a driver
        geoQuery.removeAllListeners();
        // Step 163 in onKeyEntered method

        // STEP 159
        // Now add an event listener to this query -- automatically creates -- onKeyEntered, onKeyExited, onKeyMoved, onGeoQueryReady, onGeoQueryError
        // onKeyEntered -- if driver is within radius, this method is called -- gives us the key (id of driver) and location
        // onGeoQueryReady -- if all drivers within the radius are found, this method is called
        // Step 160 in onGeoQueryReady method
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                // STEP 163
                // If a driver has been found then this method will be called
                // But Geo query will keep calling this method as and when more driver are found
                // What we want is that as soon as an available driver is found, we need to stop the loop
                // So create an if statement -- if driver is not found but we are still in this method, it means that the driver has been booked for this customer request
                if (!driverFound)
                {
                    // STEP 164
                    // Driver has already been found -- so -- driverFound should be true and we should get driver id
                    driverFound = true;
                    driverFoundID = key; // key is a parameter of this method that has the driver ID
                    // if you run the app in DEBUGGER mode, you will see that driver is found at the right locations
                    // for debugger -- place one red dot at step 164 (1st line of code) and another red dot at step 160
                    Log.d(TAG, "onKeyEntered: driver found "+ driverFoundID);

                    // STEP 304
                    // We want to create a new folder in database to keep customer request and destination
                    // Edit in step 165 -- original -- DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users")
                    //                            .child("Drivers").child(driverFoundID);
                    // New -- DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID)
                    //                                          .child(customerRequest);
                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users")
                            .child("Drivers").child(driverFoundID).child("customerRequest");

                    // STEP 165
                    // In our database -- under User --> Drivers --> driverId, we want to put information about the customer id
                    // So our driverId document will now have the customer id of the customer who has sent a pickup request
                    // To do this, first create a database reference with the path
                    // DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID);

                    // STEP 166
                    // Now get the userId of the customer. This is the data we want to put in our document above
                    // First get the customer id and then create a new hash map and put data (customerId)
                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map = new HashMap();
                    map.put("customerRideId", customerId);

                    // STEP 305
                    // In the database, also put destination
                    map.put("destination", destination);
                    // Now what happens when the user cancel -- he already took care of it in earlier window
                    // Now let us go to activity_driver_maps.xml -- Text tab -- step 306

                    // STEP 375
                    // In the database also put the destination lat and destination long
                    map.put("destinationLat", destinationLocation.latitude);
                    map.put("destinationLng", destinationLocation.longitude);
                    // This is done, but now we want to give a button to the driver -- for when they pick up the customer
                    // And also for when they drop the customer -- to end the ride
                    // For this let us copy the getAssignedCustomer method in DriverMapsActivity and paste it in this file -- step 376
                    // go to above get driver info

                    // STEP 167
                    // Now put this data in our database document
                    driverRef.updateChildren(map);
                    Log.d(TAG, "onKeyEntered: customer id informed to driver in firebase");
                    // So we have put the customer id in our driver database

                    // STEP 168
                    // Now we should be getting driver location -- getDriverLocation() -- we have to create this method, just call it for now
                    // And while we get the driver location, let us update the button to show a message to the user
                    getDriverLocation();

                    // STEP 346
                    // Call a method to get driver info here
                    // Create the method in step 347 above
                    getDriverInfo();

                    // STEP 389
                    // Call getHasRideEnded method for when the driver ends the ride after dropping us
                    getHasRideEnded();
                    // step 390 in activity_driver_maps.xml

                    btnDestination.setText("Getting driver location...");
                    // getDriverLocation throws an error because we have yet to create the method -- step 169 above -- after onCreate
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                // STEP 160
                // If no driver is found
                if (!driverFound)
                {
                    // STEP 161
                    // Increment the radius by 1 and start the getClosestDriver method again
                    radius++;
                    getClosestDriver();
                    // Step 162 is above
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // STEP 114
        // Now for the customer, we do not want to continuously track change in location based on time
        // So remove setFastestInterval and setInterval
        // Instead just ask for location request once -- rest all code for getting location will remain same
        // If we run the app now, it is working fine
        // Now from this location, we need to extract places and set it on our etOrigin -- we need places api for that -- how?

        // STEP 115
        // https://console.developers.google.com/ -- enable api -- Places -- Enable
        // Go to build.gradle to add places dependency -- step 116
        // Step 117 in onCreate method

        // STEP 136
        // We want to search for current location only if the user does not click on etOrigin field -- so origin clicked is false
        // Make an if statement and paste the all the code inside it
        // If you run the app now, the user can set either current location or new one
        // Now we want to send the info to the database on click of the origin button, but we also want to show the destination edit text and destination button
        // Go to onCreate method -- step 137
        if (!originClicked)
        {
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            mLocationCallback = new LocationCallback(){

                @Override
                public void onLocationAvailability(LocationAvailability locationAvailability) {
                    super.onLocationAvailability(locationAvailability);
                    Log.d(TAG, "onLocationAvailability: It is available");
                }

                @Override
                public void onLocationResult(LocationResult locationResult) {
                    mLastLocation = locationResult.getLastLocation();

                    Log.d(TAG, "onSuccess: Latitude for updating check " + mLastLocation.getLatitude());
                    latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

                    // STEP 230
                    // Original code -- mMap.addMarker(new MarkerOptions().position(latLng).title("Pickup Location"));
                    // This step -- pickupMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Pickup Location"));
                    // Step 231 in cancelCab method
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Pickup Location")
                            .icon(bitmapDescriptorFromVector(CustomerMapsActivity.this, R.drawable.location_yellow_36)));

                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));

                    // STEP 119
                    // Now from this mLastLocation, we also need to extract address and set it to our origin edit text
                    // Create a list variable to keep a list of addresses
                    // Also initialize geocoder
                    List<Address> addresses;
                    geocoder = new Geocoder(CustomerMapsActivity.this);

                    // STEP 120
                    // Initialize addresses to only 1 location from geocoder -- getFromLocation method
                    // It throws an IO Exception -- so use try-catch
                    try {
                        addresses = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);

                        // STEP 121
                        // Extract address line from addresses list -- 0 is the first result
                        // And then set it to etOrigin
                        String address = addresses.get(0).getAddressLine(0);
                        etOrigin.setText(address);
                        // If you run the app now, it works perfectly and we are getting address in our edit text
                        // Now we want to be able to click the edit text and set our own location -- then send to database
                        // But which data to send? the one clicked or current location -- if user clicks on etOrigin and selects a location, then the clicked one, not current location
                        // To do this we need to create a new boolean variable on top -- step 122

                    }catch (IOException e){
                        Log.d(TAG, "onLocationResult: Geocoder throwing error ");
                    }

                    // STEP 110
                    // Remove getting current user and sending location data to firebase
                    // His removed code is under onLocationChanged
                    // Step 111 in activity_customer_maps.xml
                }
            };

            startGettingLocation();
        }
    }

    // STEP 128
    // Right-click --> Generate --> Override methods --> onActivityResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // STEP 129
        // First check if the result is okay
        if (resultCode == RESULT_OK)
        {
            // STEP 130
            // Then check for the same request code and origin clicked is true
            if (requestCode == AUTOCOMPLETE_REQUEST_ORIGIN && originClicked)
            {
                // STEP 131
                // If everything is okay, then get the place selected by the user from the intent -- store in new variable
                Place placeOrigin = Autocomplete.getPlaceFromIntent(data); // data is the parameter of this method

                // STEP 132
                // Now store info about this data in various variables which we will need for our map and database
                latLng = placeOrigin.getLatLng();
                String addressOrigin = placeOrigin.getAddress();
                etOrigin.setText(addressOrigin);

                // STEP 133
                // Setup map, marker and move camera
                mMap.addMarker(new MarkerOptions().position(latLng).title("Pickup Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        }

        // STEP 134
        // If result is not okay
        else
        {
            // STEP 135
            // Get status and print message as Toast
            // Select the status option -- common.api
            Status status = Autocomplete.getStatusFromIntent(data);
            Log.d(TAG, "onActivityResult: Result error " + status.getStatusMessage());
            // Now our app may be confused about current location and this address -- so go to onMapReady -- step 136
        }

        // STEP 147
        // If the intent was started by etDestination, first check result okay
        if (resultCode == RESULT_OK)
        {
            // STEP 148
            // Check request code for destination edit text intent
            if (requestCode == AUTOCOMPLETE_REQUEST_DESTINATION)
            {
                // STEP 149
                // If everything is okay, then get the place selected by the user from the intent -- store in new variable
                Place placeDestination = Autocomplete.getPlaceFromIntent(data); // data is the parameter of this method

                // STEP 150
                // For now we just want to set it on our edit text, and get lat long so that we have that location reference for later
                // We will figure out something to do with it later
                destinationLocation = placeDestination.getLatLng();
                String addressDestination = placeDestination.getAddress();
                List<Place.Type> typeDestination = placeDestination.getTypes();
                etDestination.setText(addressDestination);

                // STEP 543
                // clear map
                Log.d(TAG, "onActivityResult: Place types - " + typeDestination);
                mMap.clear();

                // STEP 547
                // Show distance between pickup and drop in snippet
                // If we run the app now, it is working
                // Now for step 548, we want to show distance and price in a cardview -- activity_customer_maps.xml
               /* Location loc1 = new Location("");
                loc1.setLatitude(pickupLocation.latitude);
                loc1.setLongitude(pickupLocation.longitude);

                Location loc2 = new Location("");
                loc2.setLatitude(destinationLocation.latitude);
                loc2.setLongitude(destinationLocation.longitude);

                float distance = loc1.distanceTo(loc2)/1000;*/

                // STEP 544
                // Now set both pickup and destination location markers
                // Make them global variables and come back here
               /* markerPickup = mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup Location")
                        .icon(bitmapDescriptorFromVector(CustomerMapsActivity.this, R.drawable.location_yellow_36)));
                markerDestination = mMap.addMarker(new MarkerOptions().position(destinationLocation).title("Destination")
                        .snippet("Distance: " + distance + " kilometers" ).icon(bitmapDescriptorFromVector(CustomerMapsActivity.this, R.drawable.location_blue_36)));*/

                // STEP 568
                // First call the getRouteToDestination method
                getRouteToDestination(destinationLocation);

                // STEP 569
                // Comment out steps 544, 545, 546, 547, 551, 552
                // If we run the app now, it is working fine
                //

                // STEP 551
                // Show a cardview with distance and price values
                /*destination_info.setVisibility(View.VISIBLE);
                if (distance <= 10.0)
                {
                    tvDistance.setText("Estimated distance is within 10 kilometers");
                    tvPrice.setText("Estimated price: Rs. 50");
                    locationOutOfBounds = false;
                    btnDestination.setVisibility(View.VISIBLE);
                }*/

                // STEP 552
                // If distance is greater than 10
                // step 553 in onCreate method
               /* if (distance > 10)
                {
                    tvDistance.setText("Selected location is not serviced by Cabus currently");
                    tvPrice.setText("Please select another location and try again");
                    locationOutOfBounds = true;
                    btnDestination.setVisibility(View.GONE);
                }*/

                // STEP 545
                // Create Lat and long bounds so that the camera can zoom in and out automatically depending on this bounds
                // If we run the app now, it is working fine
                /*LatLngBounds.Builder builder = new LatLngBounds.Builder();

                builder.include(markerPickup.getPosition());
                builder.include(markerDestination.getPosition());

                LatLngBounds bounds = builder.build();

                int width = getResources().getDisplayMetrics().widthPixels;
                int height = getResources().getDisplayMetrics().heightPixels;
                int padding = (int) (height * 0.20); // offset from edges of the map 20% of screen*/

                // STEP 546
                // Now move camera
                // If we run the app now, it is working fine
                /*CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
                mMap.animateCamera(cu);
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                // Now we want to show the distance between pickup and destination -- How?
                // Step 547 above

                Log.d(TAG, "onActivityResult: Destination latitude " + placeDestination.getLatLng().latitude);*/
                // If we run the app now, it is running -- Now we setup on click for btnDestination -- step 151 in onCreate method
            }
        }
    }


    private void startGettingLocation(){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {

                    Log.d(TAG, "onLocationResult: Location result is successful");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Log.d(TAG, "onFailure: Error while getting message " + e.getMessage());
                }
            });
        }
    }

    // STEP 329
    // From Nexus 6, Google wants us to ask for permissions through onRequestPermissionsResult -- create it
    // Right-click --> generate --> override methods
    // Copy-paste the same method from DriverMapsActivity
    // Activity_driver_maps.xml – step 330
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

    @Override
    protected void onStop() {
        super.onStop();
        // STEP 108
        // Remove all copied code under onStop method
        // Step 109 in onMapReady
    }

    @Override
    public void onRoutingFailure(RouteException e) {

        // STEP 562
        // Show the error
        // Step 563 right at the top of this file
        Log.d(TAG, "onRoutingFailure: " + e.getMessage());
        Toast.makeText(this, "The app encountered the following error: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> arrayList, int shortestRouteIndex) {

        // STEP 564
        // Since we have not created any alternate routes, we will get just one route
        // Let us set our global variables to them
        Log.d(TAG, "onRoutingSuccess: success");
        for (int i=0; i < arrayList.size(); i++)
        {
            distanceValue = arrayList.get(i).getDistanceValue();
            distanceText = arrayList.get(i).getDistanceText();
            distanceValueKms = distanceValue/1000.00;

            durationText = arrayList.get(i).getDurationText();
            durationValue = arrayList.get(i).getDurationValue();

            Log.d(TAG, "onRoutingSuccess: " + distanceValueKms + "\n" + distanceText + "\n" + durationValue + "\n" + durationText);
        }

        // STEP 565
        // Set textviews to these values -- using if-else
        destination_info.setVisibility(View.VISIBLE);
        if (distanceValueKms > 11.0)
        {
            tvDistance.setText("Selected location is not serviced by Cabus currently");
            tvDuration.setText("Please select another location and try again");
            tvPrice.setText("");
            locationOutOfBounds = true;
            btnDestination.setVisibility(View.GONE);
        }

        if (distanceValueKms <= 11.0)
        {
            tvDistance.setText("Estimated distance: " + distanceValueKms + " kms away");
            tvDuration.setText("Estimated time: " + durationText);
            tvPrice.setText("Estimated price: Rs. 50 per person per seat");
            locationOutOfBounds = false;
            btnDestination.setVisibility(View.VISIBLE);

            // STEP 566
            // Create Lat and long bounds so that the camera can zoom in and out automatically depending on this bounds
            markerPickup = mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup Location")
                    .icon(bitmapDescriptorFromVector(CustomerMapsActivity.this, R.drawable.location_yellow_36)));
            markerDestination = mMap.addMarker(new MarkerOptions().position(destinationLocation).title("Destination")
                    .snippet("Distance: " + distanceValueKms + " kilometers" ).icon(bitmapDescriptorFromVector(CustomerMapsActivity.this, R.drawable.location_blue_36)));

            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            builder.include(markerPickup.getPosition());
            builder.include(markerDestination.getPosition());

            LatLngBounds bounds = builder.build();

            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (height * 0.20); // offset from edges of the map 20% of screen

            // STEP 567
            // Now move camera
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
            mMap.animateCamera(cu);
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            // Now where do we call the routing method? in onActivityResult -- step 568
        }

    }

    @Override
    public void onRoutingCancelled() {

    }
}

