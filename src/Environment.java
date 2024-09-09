import java.util.ArrayList;
import java.util.List;

public class Environment {
    List<Frame> frames;
    List<String> agents;
    String currentAgent;
    // TODO: maybe add a depth tracker, could be Map<Int, Int> that holds a counter for each depth to be able to form
    //  and access frames throughout the depths

    public Environment() {
        frames = new ArrayList<>();
        agents = new ArrayList<>();
    }
}
