package com.example.cabusmvp.historyRecView;

public class HistoryObject {

    // STEP 474
    private String time;

    // STEP 438
    // To begin with, we just need to show ride id in our recycler view
    // We will keep adding variables later
    private String rideId;

    // STEP 475
    // Add time to the constructor param

    // STEP 439
    // Generate constructor -- right-click --> generate --> constructor
    public HistoryObject(String rideId, String time) {
        this.rideId = rideId;

        // STEP 476
        // add timestamp for initializing
        this.time = time;
    }

    // STEP 477
    // generate getter and setter for time
    // Step 478 is in HistoryAdapter.java
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    // STEP 440
    // In the same way, generate getter and setter
    // Now we have to create a new viewholder called -- HistoryViewHolders
    // This mostly stores data about how a row_layout of the recycler view will look
    // Step 441 in HistoryViewHolders.java
    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }
}
