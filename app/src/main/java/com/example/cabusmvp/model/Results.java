package com.example.cabusmvp.model;

public class Results {

    // STEP 498
    // Go to -- http://pojo.sodhanalibrary.com/Convert -- the page with multiple tabs
    // Look for Results.java tab (there are multiple) -- select any 1 and copy paste the content here

    // STEP 499
    // Again there are 2 errors -- Geometry and Photos
    // Create new Java classes Geometry and Photos - under model


    // STEP 500
    // Create a Photos class under model package
    // Step 501 is in Photos.java

    private String reference;

    private String[] types;

    private String scope;

    private String icon;

    private String name;

    private Geometry geometry;

    private String vicinity;

    private String id;

    private Photos[] photos;

    private String place_id;

    public String getReference ()
    {
        return reference;
    }

    public void setReference (String reference)
    {
        this.reference = reference;
    }

    public String[] getTypes ()
    {
        return types;
    }

    public void setTypes (String[] types)
    {
        this.types = types;
    }

    public String getScope ()
    {
        return scope;
    }

    public void setScope (String scope)
    {
        this.scope = scope;
    }

    public String getIcon ()
    {
        return icon;
    }

    public void setIcon (String icon)
    {
        this.icon = icon;
    }

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public Geometry getGeometry ()
    {
        return geometry;
    }

    public void setGeometry (Geometry geometry)
    {
        this.geometry = geometry;
    }

    public String getVicinity ()
    {
        return vicinity;
    }

    public void setVicinity (String vicinity)
    {
        this.vicinity = vicinity;
    }

    public String getId ()
    {
        return id;
    }

    public void setId (String id)
    {
        this.id = id;
    }

    public Photos[] getPhotos ()
    {
        return photos;
    }

    public void setPhotos (Photos[] photos)
    {
        this.photos = photos;
    }

    public String getPlace_id ()
    {
        return place_id;
    }

    public void setPlace_id (String place_id)
    {
        this.place_id = place_id;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [reference = "+reference+", types = "+types+", scope = "+scope+", icon = "+icon+", name = "+name+", geometry = "+geometry+", vicinity = "+vicinity+", id = "+id+", photos = "+photos+", place_id = "+place_id+"]";
    }
}
