import java.util.ArrayList;
import java.util.List;

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
        return contents != null;
    }
}
