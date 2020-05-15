package com.example.cabusmvp.model;

// STEP 493
// First let us see through a demo link, how to work with json files – the demo link is as below
// https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=30.3280983,-81.4855&radius=10000&type=market&sensor=true&key=AIzaSyBevtphLtjL8eONUTeExX_IaQEJre8Pw3E
// Copy-paste it in a browser – the key here is my project maps api key, but can change depending on your project google maps api key
// My browser key is -- AIzaSyDT7AtOorKRK_4kHYtCgT1iWzybUsS0-RM

// STEP 494
// After going on the above link, copy all the data on the web-page, go to website:
// http://jsoneditoronline.org/
// And paste the data on the left-side window and press the Copy button on the web-page – We can see the result of this on the ride-side window

// STEP 495
// Now we need to convert our json into pojo class – how? Go to the website below (Remember to copy the original JSON content from step 17:
// http://pojo.sodhanalibrary.com/
// In the enter JSON or XML here box, paste the content and click on the Submit button – it automatically opens a webpage with multiple tabs and java code per tab

// STEP 496
// Copy the content of MyPogo.java tab – now go to Android Studio – left-most pane  app  java  package  right-click  New  Package – call it com.example.cabsmvp.model
// Then right click on Model package  New  Java Class – call it MyPlaces
public class MyPlaces {

    // STEP 497
    // Here, paste what we had copied in step 496
    // It shows an error -- Results[] -- cannot resolve symbol Results
    // To remove this error -- create a new Java class under model -- call it Results (same as Results[])
    // Error in MyPlaces.java will be removed when we create the new class
    // Step 498 is in Results.java
    private String next_page_token;

    private String[] html_attributions;

    private Results[] results;

    private String status;

    public String getNext_page_token ()
    {
        return next_page_token;
    }

    public void setNext_page_token (String next_page_token)
    {
        this.next_page_token = next_page_token;
    }

    public String[] getHtml_attributions ()
    {
        return html_attributions;
    }

    public void setHtml_attributions (String[] html_attributions)
    {
        this.html_attributions = html_attributions;
    }

    public Results[] getResults ()
    {
        return results;
    }

    public void setResults (Results[] results)
    {
        this.results = results;
    }

    public String getStatus ()
    {
        return status;
    }

    public void setStatus (String status)
    {
        this.status = status;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [next_page_token = "+next_page_token+", html_attributions = "+html_attributions+", results = "+results+", status = "+status+"]";
    }
}
