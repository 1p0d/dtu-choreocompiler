import org.antlr.v4.runtime.misc.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Environment {
    Map<String, List<Pair<Frame, Choreo>>> agentsFrames;
    String currentAgent;

    public Environment() {
        this.agentsFrames = new HashMap<>();
    }

    public void compile() {
        for (String agent : agentsFrames.keySet()) {
            List<Pair<Frame, Choreo>> pairs = agentsFrames.get(agent);
            for (Pair<Frame, Choreo> pair : pairs) {
                Frame frame = pair.a;
                Choreo choreo = pair.b;
                // choreo starts with a message that agent is not involved in, continue
                if (choreo instanceof Message message && !message.agentFrom.equals(agent) && !message.agentTo.equals(agent)) {
                    // TODO: replace choreo with split pairs
                    // TODO: remove pair
                    continue;
                }
                // choreo is a fresh creation by another agent
                if (choreo instanceof Definition definition && !definition.agent.equals(agent)) {
                    // TODO: definition.choreography
                    // TODO: remove pair
                }
            }
            // all choreos are 0
            if (pairs.stream().allMatch(pair -> pair.b instanceof Empty)) {
                // TODO: translation is 0
                // TODO: remove pair
                continue;
            }
            // all choreos start with a definition
            if (pairs.stream().allMatch(pair -> pair.b instanceof Definition)) {
                // TODO: translation is definition
                // TODO: remove pair
                // TODO: split definition.choreography
                continue;
            }
            // all choreos are a message that agent is sender of and have the same number of choices
            if (pairs.stream().allMatch(pair -> pair.b instanceof Message message && message.agentFrom.equals(agent) &&
                    message.choices.size() == ((Message) pairs.getFirst().b).choices.size())) {
                // TODO: check that recipes exist for every choice.message
                // TODO: translation is send
                // TODO: remove pair
                // TODO: split choice.choreography
                continue;
            }
            // all choreos are a message that agent is receiver of
            if (pairs.stream().allMatch(pair -> pair.b instanceof Message message && message.agentTo.equals(agent))) {
                // TODO: use analyze() to compute a finite set of checks
                // TODO: remove pair
                continue;
            }
            throw new Error("The specification is ill-defined");
        }
    }
}
