package com.example.cabusmvp.model;

public class Northeast {

    // STEP 509
    // Go to -- http://pojo.sodhanalibrary.com/Convert -- the page with multiple tabs
    // Look for Northeast.java tab (there are multiple) -- select any 1 and copy paste the content here
    // There are no errors here
    // Step 510 in Southwest.java
    private String lng;

    private String lat;

    public String getLng ()
    {
        return lng;
    }

    public void setLng (String lng)
    {
        this.lng = lng;
    }

    public String getLat ()
    {
        return lat;
    }

    public void setLat (String lat)
    {
        this.lat = lat;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [lng = "+lng+", lat = "+lat+"]";
    }
}
