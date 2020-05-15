package com.example.cabusmvp.model;

public class Photos {

    // STEP 501
    // Go to -- http://pojo.sodhanalibrary.com/Convert -- the page with multiple tabs
    // Look for Photos.java tab (there are multiple) -- select any 1 and copy paste the content here
    // There are no errors
    // Step 502 is in Geometry.java
    private String photo_reference;

    private String width;

    private String[] html_attributions;

    private String height;

    public String getPhoto_reference ()
    {
        return photo_reference;
    }

    public void setPhoto_reference (String photo_reference)
    {
        this.photo_reference = photo_reference;
    }

    public String getWidth ()
    {
        return width;
    }

    public void setWidth (String width)
    {
        this.width = width;
    }

    public String[] getHtml_attributions ()
    {
        return html_attributions;
    }

    public void setHtml_attributions (String[] html_attributions)
    {
        this.html_attributions = html_attributions;
    }

    public String getHeight ()
    {
        return height;
    }

    public void setHeight (String height)
    {
        this.height = height;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [photo_reference = "+photo_reference+", width = "+width+", html_attributions = "+html_attributions+", height = "+height+"]";
    }
}
