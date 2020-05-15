package com.example.cabusmvp.historyRecView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cabusmvp.R;

import java.util.List;

// STEP 446
// Original code -- public class HistoryAdapter
// New code -- public class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolders>
// Error -- option + return --> Implement methods -- creates onCreateViewHolder, onBindViewHolder, and getItemCount
public class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolders> {

    // STEP 447
    // This adapter is for our History List object -- create a variable for that another one for context
    private List<HistoryObject> itemList;
    private Context context;

    // STEP 448
    // Generate a constructor with parameters
    public HistoryAdapter(List<HistoryObject> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
    }

    @NonNull
    @Override
    public HistoryViewHolders onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // STEP 449
        // Inflate item_history layout
        // Also, Create a new vieholder variable -- why?
        // Elements of this item_history layout are stored in which view holder? -- HistoryViewHolders
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, null, false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(layoutParams);

        HistoryViewHolders historyViewHolders = new HistoryViewHolders(layoutView);

        // STEP 450
        // return this viewholder
        return historyViewHolders;
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolders holder, int position) {

        // STEP 478
        // set text for time
        holder.tvTimestamp.setText(itemList.get(position).getTime());
        // Step 479 in DriverMapsActivity.java

        // STEP 451
        // onBindViewHolder takes care of what values to show in our recycler view
        // We want to set text to our variable called tvRideId
        // holder is the param of this method
        // itemList is the list containing history variables such as history id
        holder.tvRideId.setText(itemList.get(position).getRideId());

    }

    @Override
    public int getItemCount() {

        // STEP 452
        // default -- return 0
        // new -- return itemList.size();
        // Now go to HistoryActivity.java to implement all this -- step 453
        return itemList.size();
    }
}
