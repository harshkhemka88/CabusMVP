package com.example.cabusmvp;

import com.example.cabusmvp.remote.IGoogleAPIService;
import com.example.cabusmvp.remote.RetrofitClient;

// STEP 519
// Main package --> right-click --> New --> Java class -- call it Common
public class Common {

    // STEP 520
    // Create a constant for Google maps api url
    private static final String GOOGLE_API_URL = "https://maps.googleapis.com/";

    // STEP 521
    // Now create a method to call this google service using retrofit
    public static IGoogleAPIService getGoogleAPIService(){

        // STEP 522
        // We want it to return our maps api url
        return RetrofitClient.getClient(GOOGLE_API_URL).create(IGoogleAPIService.class);
        // Step 523 in CustomerMapsActivity.java
    }
}
