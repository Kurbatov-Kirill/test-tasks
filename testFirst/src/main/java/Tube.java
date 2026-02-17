import java.util.ArrayList;
import java.util.List;

public class Tube {
    public Tube() {
    }

    public Tube(Tube other) {
        this.volume = other.volume;

        this.contents = new ArrayList<>(other.contents.size());
        for (Drop drop : other.contents) {
            this.contents.add(new Drop(drop));
        }
    }

    public Tube(int volume, List<Drop> contents) {
        this.volume = volume;
        this.contents = contents;
    }

    private int volume;
    private List<Drop> contents = new ArrayList<>();

    public int getVolume() {
        return volume;
    }

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
        return getContents().size() > 0;
    }

    public boolean hasFreeSpace() {
        return getContents().size() < volume;
    }

    public int getFreeCells() {
        return volume - getContents().size();
    }

    public Drop getTopDrop () {
        return getContents().getLast();
    }

    public boolean isContainsOneKindOfLiquid() {
        List<Integer> dropsColorsInTube = new ArrayList<>();
        for (int i = 0; i < getContents().size(); i++) {
            dropsColorsInTube.add(getContents().get(i).getColorId());
        }

        return (dropsColorsInTube.stream().distinct().count() == 1);
    }

    public boolean isSorted() {
        return !hasFreeSpace() && isContainsOneKindOfLiquid();
    }
}
