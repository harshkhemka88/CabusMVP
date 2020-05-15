package com.example.cabusmvp.historyRecView;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cabusmvp.R;

import org.w3c.dom.Text;

// STEP 441
// Original code -- public class HistoryViewHolders
// New code -- public class HistoryViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener
// Throws an error -- option + return -- implement methods -- creates onClick method
// Throws an error -- option + return -- create constructor matching super -- creates HistoryViewHolders constructor
// Now we create a layout for how each row will look -- left-most pane --> res --> layout --> right-click --> new --> layout resource file
// step 442 in item_history.xml -- text tab
public class HistoryViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {

    // STEP 472
    public TextView tvTimestamp;

    // STEP 443
    // Let us only begin with showing the ride id -- we will add more elements to it later
    public TextView tvRideId;

    public HistoryViewHolders(@NonNull View itemView) {
        super(itemView);

        // STEP 444
        // sets on click listener so that we know what item was clicked
        itemView.setOnClickListener(this);

        // STEP 473
        tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        // Step 474 in HistoryObject.java

        // STEP 445
        // Initialize text view
        // Now we have to create a custom adapter to pass our History object -- HistoryAdapter
        // Step 446 in HistoryAdapter.java
        tvRideId = itemView.findViewById(R.id.tvRideId); // itemView is the parameter of this method
    }

    @Override
    public void onClick(View v) {

    }
}
