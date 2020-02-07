package ca.staugustinechs.staugustineapp.Objects;

import android.graphics.Bitmap;

public class CafMenuItem {

    private String name;
    private Bitmap image;
    private double price;

    public CafMenuItem(String name, double price, Bitmap image) {
        this.name = name;
        this.price = price;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public Bitmap getImage() {
        return image;
    }
}
