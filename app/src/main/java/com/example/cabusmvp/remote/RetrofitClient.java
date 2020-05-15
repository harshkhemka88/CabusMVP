package com.example.cabusmvp.remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// STEP 513
// com.example.cabsmvp.remote --> right-click --> New --> Java Class -- call it RetrofitClient
public class RetrofitClient {

    // STEP 514
    // Declare and initialize retrofit variable
    private static Retrofit retrofit = null;

    // STEP 515
    // Create a Retrofit return method called getClient which accepts url as a parameter
    public static Retrofit getClient (String baseUrl){

        // STEP 516
        // If retrofit variable created is null
        if (retrofit == null)
        {
            // STEP 517
            // Then create a new retrofit with given url and convert it into gson
            retrofit = new Retrofit.Builder().baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            // .baseUrl choose the option with String
        }

        // STEP 518
        // return the newly created retrofit
        return retrofit;
        // Step 519 is in Common.java -- this java class is under the original package
    }
}
