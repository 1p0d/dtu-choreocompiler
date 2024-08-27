import java.util.ArrayList;
import java.util.List;

public class Environment {
    List<Frame> frames;
    List<String> agents;

    public Environment() {
        frames = new ArrayList<>();
        agents = new ArrayList<>();
    }
}
