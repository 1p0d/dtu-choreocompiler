import java.util.List;

public class Environment {
    private List<String> agents;

    public Environment() {}

    public void registerAgent(String agent) {
        agents.add(agent);
    }

    public List<String> getAgents() {
        return agents;
    }
}
