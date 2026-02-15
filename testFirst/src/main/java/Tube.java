import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Tube {
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
