import org.antlr.v4.runtime.misc.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Environment {
    Map<String, List<Pair<Frame, Choreo>>> agentFrames;
    String currentAgent;

    public Environment() {
        this.agentFrames = new HashMap<>();
    }
}
