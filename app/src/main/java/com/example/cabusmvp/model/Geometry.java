package com.example.cabusmvp.model;

public class Geometry {

    // STEP 502
    // Go to -- http://pojo.sodhanalibrary.com/Convert -- the page with multiple tabs
    // Look for Geometry.java tab (there are multiple) -- select any 1 and copy paste the content here

    // STEP 503
    // Errors -- Viewport and Location -- create new classes in model package
    // Create new java class under model -- called Viewport

    // STEP 504
    // Create new java class under model -- called Location
    // Step 505 in Location.java

    private Viewport viewport;

    private Location location;

    public Viewport getViewport ()
    {
        return viewport;
    }

    public void setViewport (Viewport viewport)
    {
        this.viewport = viewport;
    }

    public Location getLocation ()
    {
        return location;
    }

    public void setLocation (Location location)
    {
        this.location = location;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [viewport = "+viewport+", location = "+location+"]";
    }
}
