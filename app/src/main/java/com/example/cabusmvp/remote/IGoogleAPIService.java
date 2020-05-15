package com.example.cabusmvp.remote;

// STEP 511
// Left-most pane --> app --> java --> package --> right-click --> New --> package -- call it com.example.cabusmvp.remote

import com.example.cabusmvp.model.MyPlaces;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface IGoogleAPIService {

    // STEP 512
    // We want to get -- So call @get
    // Now what do we want to receive? -- we want to get places options under MyPlaces class
    // @Url is used because we will use a url in CustomerMapsActivity.java later
    // Step 513 is creation of a new Java class under remote package -- call it RetrofitClient
    @GET
    Call<MyPlaces> getNearbyPlaces (@Url String url);
}
