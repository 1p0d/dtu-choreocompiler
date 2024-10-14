import org.antlr.v4.runtime.misc.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Environment {
    public Environment() {
    }

    public Map<String, String> compile(Map<String, List<Pair<Frame, Choreo>>> agentPairsMap) {
        Map<String, String> agentTranslations = new HashMap<>();
        agentPairsMap.keySet().forEach(agent -> agentTranslations.put(agent, compileAgent(agent, agentPairsMap.get(agent))));
        return agentTranslations;
    }

    public String compileAgent(String agent, List<Pair<Frame, Choreo>> agentPairs) {
        while (agentPairs.stream().anyMatch(pair -> (pair.b instanceof Message message && !message.agentFrom.equals(agent) && !message.agentTo.equals(agent)) ||
                (pair.b instanceof Definition definition && !definition.agent.equals(agent)))) {
            // TODO
        }
        List<Pair<Frame, Choreo>> pairs = new ArrayList<>();
        List<Pair<Frame, Choreo>> newPairs = new ArrayList<>();
        for (Pair<Frame, Choreo> pair : uncheckedPairs) {
            Frame frame = pair.a;
            Choreo choreo = pair.b;
            // a choreo starts with a message that agent is not involved in, continue
            if (choreo instanceof Message message && !message.agentFrom.equals(agent) && !message.agentTo.equals(agent)) {
                message.choices.forEach(choice -> pairs.add(new Pair<>(frame, choice.choreography)));
                continue;
            }
            // a choreo is a fresh creation by another agent
            if (choreo instanceof Definition definition && !definition.agent.equals(agent)) {
                definition.constants.forEach(frame::add);
                pairs.add(new Pair<>(frame, definition.choreography));
            }
            pairs.add(pair);
        }
        StringBuilder translationBuilder = new StringBuilder(agentsTranslation.getOrDefault(agent, ""));
        // all choreos are 0
        if (pairs.stream().allMatch(pair -> pair.b instanceof Empty)) {
            agentsTranslation.put(agent, translationBuilder.append("0").toString());
            this.agentsFrames.put(agent, newPairs);
            continue;
        }
        // all choreos start with a definition
        if (pairs.stream().allMatch(pair -> pair.b instanceof Definition)) {
            for (Pair<Frame, Choreo> pair : pairs) {
                Frame frame = pair.a;
                Definition definition = (Definition) pair.b;
                definition.constants.forEach(frame::add);
                translationBuilder.append(definition.compile(this));
                newPairs.add(new Pair<>(frame, definition.choreography));
            }
            this.agentsFrames.put(agent, newPairs);
            continue;
        }
        // all choreos are a message that agent is sender of, have the same number of choices and recipes exist for every choice.message
        if (pairs.stream().allMatch(pair -> pair.b instanceof Message message && message.agentFrom.equals(agent) &&
                message.choices.size() == ((Message) uncheckedPairs.getFirst().b).choices.size() &&
                message.choices.stream().allMatch(choice -> pair.a.compose(choice.message) != null))) {
            for (Pair<Frame, Choreo> pair : pairs) {
                Frame frame = pair.a;
                Message message = (Message) pair.b;
                translationBuilder.append(message.compile(this));
                message.choices.forEach(choice -> newPairs.add(new Pair<>(frame, choice.choreography)));
            }
            this.agentsFrames.put(agent, newPairs);
            continue;
        }
        // all choreos are a message that agent is receiver of
        if (pairs.stream().allMatch(pair -> pair.b instanceof Message message && message.agentTo.equals(agent))) {
            for (Pair<Frame, Choreo> pair : pairs) {
                Message message = (Message) pair.b;
                translationBuilder.append(message.compile(this));
                for (Choice choice : message.choices) {
                    Frame newFrame = pair.a;
                    newFrame.add(choice.message);
                    List<Pair<Term, Term>> checks = newFrame.analyze();
                    checks.forEach(check -> translationBuilder.append("try ").append(check.a.compile(this)).append(" = ").append(check.b.compile(this)).append("\n"));
                    newPairs.add(new Pair<>(newFrame, choice.choreography));
                }
            }
            this.agentsFrames.put(agent, newPairs);
            continue;
        }
        throw new Error("The specification is ill-defined");
    }
}
