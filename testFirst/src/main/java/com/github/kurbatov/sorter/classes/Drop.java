package com.github.kurbatov.sorter.classes;

public class Drop {
    private int colorId;
    /*===========================================*/
    public Drop() {
        
    }

    public Drop(Drop other) {
        this.colorId = other.colorId;
    }
    /*===========================================*/
    public int getColorId() {
        return colorId;
    }

    public void setColorId(int colorId) {
        this.colorId = colorId;
    }
}
