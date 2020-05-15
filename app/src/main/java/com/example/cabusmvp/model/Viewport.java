package com.example.cabusmvp.model;

public class Viewport {

    // STEP 506
    // Go to -- http://pojo.sodhanalibrary.com/Convert -- the page with multiple tabs
    // Look for Viewport.java tab (there are multiple) -- select any 1 and copy paste the content here
    // There are 2 errors here -- Southwest and Northeast

    // STEP 507
    // Create new java class under model package -- Southwest

    // STEP 508
    // Create new java class under model package -- Northeast
    // Step 509 is in Northeast.java
    private Southwest southwest;

    private Northeast northeast;

    public Southwest getSouthwest ()
    {
        return southwest;
    }

    public void setSouthwest (Southwest southwest)
    {
        this.southwest = southwest;
    }

    public Northeast getNortheast ()
    {
        return northeast;
    }

    public void setNortheast (Northeast northeast)
    {
        this.northeast = northeast;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [southwest = "+southwest+", northeast = "+northeast+"]";
    }
}
