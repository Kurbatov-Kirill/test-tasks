package com.github.kurbatov.sorter.classes;

import java.util.ArrayList;
import java.util.List;

public class Tube {
    private int volume;
    private List<Drop> contents = new ArrayList<>();
    /*===========================================*/
    public Tube() {
    }

    public Tube(Tube other) {
        this.volume = other.volume;

        this.contents = new ArrayList<>(other.contents.size());
        for (Drop drop : other.contents) {
            this.contents.add(new Drop(drop));
        }
    }
    /*===========================================*/
    public void setVolume(int volume) {
        this.volume = volume;
    }

    public List<Drop> getContents() {
        return contents;
    }

    public void setContents(List<Drop> contents) {
        this.contents = contents;
    }

    public boolean hasLiquid() {
        return !contents.isEmpty();
    }

    public boolean hasFreeSpace() {
        return contents.size() < volume;
    }

    public int getFreeCells() {
        return volume - contents.size();
    }

    public Drop getTopDrop() {
        return contents.getLast();
    }

    public boolean isContainsOneKindOfLiquid() {
        List<Integer> dropsColorsInTube = new ArrayList<>();
        for (Drop content : contents) {
            dropsColorsInTube.add(content.getColorId());
        }

        return (dropsColorsInTube.stream().distinct().count() == 1);
    }

    public boolean isSorted() {
        return !hasFreeSpace() && isContainsOneKindOfLiquid();
    }
}
