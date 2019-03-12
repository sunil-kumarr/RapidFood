package com.example.rapidfood.Models;

import java.util.List;

public class VendorPackageItem {
    String name;
    String description;
    String image;
    List<String> packlist;

    public List<String> getPacklist() {
        return packlist;
    }

    public void setPacklist(List<String> pPacklist) {
        packlist = pPacklist;
    }

    public String getName() {
        return name;
    }

    public void setName(String pName) {
        name = pName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String pDescription) {
        description = pDescription;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String pImage) {
        image = pImage;
    }
}
