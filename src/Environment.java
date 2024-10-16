import org.antlr.v4.runtime.misc.Pair;

import java.util.*;

public class Environment {
    public String currentAgent;

    public Environment() {
    }

    public Map<String, String> compile(Map<String, List<Pair<Frame, Choreo>>> agentPairsMap) {
        Map<String, String> agentTranslations = new HashMap<>();
        agentPairsMap.keySet().forEach(agent -> {
            this.currentAgent = agent;
            agentTranslations.put(agent, compileAgent(agent, agentPairsMap.get(agent), ""));
        });
        return agentTranslations;
    }

    public String compileAgent(String agent, List<Pair<Frame, Choreo>> agentPairs, String translation) {
        if (agent == null || agent.isBlank() || agentPairs == null || agentPairs.isEmpty()) return translation;
        ListIterator<Pair<Frame, Choreo>> listIterator = agentPairs.listIterator();
        while (listIterator.hasNext()) {
            Pair<Frame, Choreo> pair = listIterator.next();
            Frame frame = pair.a;
            Choreo choreo = pair.b;
            // a choreo starts with a message that agent is not involved in, continue
            if (choreo instanceof Message message && !message.agentFrom.equals(agent) && !message.agentTo.equals(agent)) {
                listIterator.remove();
                message.choices.forEach(choice -> {
                    listIterator.add(new Pair<>(frame, choice.choreography));
                    listIterator.previous();
                });
                continue;
            }
            // a choreo is a fresh creation by another agent
            if (choreo instanceof Definition definition && !definition.agent.equals(agent)) {
                listIterator.remove();
                listIterator.add(new Pair<>(frame, definition.choreography));
                listIterator.previous();
            }
        }
        StringBuilder translationBuilder = new StringBuilder(translation);
        List<Pair<Frame, Choreo>> newAgentPairs = new ArrayList<>();
        // all choreos are 0
        if (agentPairs.stream().allMatch(pair -> pair.b instanceof Empty))
            return translationBuilder.append("0").toString();
        // all choreos start with a definition and define the same constants
        if (agentPairs.stream().allMatch(pair -> pair.b instanceof Definition definition &&
                definition.constants.equals(((Definition) agentPairs.getFirst().b).constants))) {
            for (Pair<Frame, Choreo> pair : agentPairs) {
                Frame frame = pair.a;
                Definition definition = (Definition) pair.b;
                definition.constants.forEach(frame::add);
                translationBuilder.append(definition.compile(this));
                newAgentPairs.add(new Pair<>(frame, definition.choreography));
            }
            return this.compileAgent(agent, newAgentPairs, translationBuilder.toString());
        }
        // all choreos are a message that agent is sender of, have the same number of choices and recipes exist for every choice.message
        // TODO: check that all sends have the same recipes
        if (agentPairs.stream().allMatch(pair -> pair.b instanceof Message message && message.agentFrom.equals(agent) &&
                message.choices.size() == ((Message) agentPairs.getFirst().b).choices.size() &&
                message.choices.stream().allMatch(choice -> pair.a.compose(choice.message) != null))) {
            for (Pair<Frame, Choreo> pair : agentPairs) {
                Frame frame = pair.a;
                Message message = (Message) pair.b;
                translationBuilder.append(message.compile(this));
                message.choices.forEach(choice -> newAgentPairs.add(new Pair<>(frame, choice.choreography)));
            }
            return this.compileAgent(agent, newAgentPairs, translationBuilder.toString());
        }
        // all choreos are a message that agent is receiver of
        // TODO: collect checks
        if (agentPairs.stream().allMatch(pair -> pair.b instanceof Message message && message.agentTo.equals(agent))) {
            for (Pair<Frame, Choreo> pair : agentPairs) {
                Message message = (Message) pair.b;
                translationBuilder.append(message.compile(this));
                for (Choice choice : message.choices) {
                    Frame newFrame = pair.a;
                    newFrame.add(choice.message);
                    List<Pair<Term, Term>> checks = newFrame.analyze();
                    checks.forEach(check -> translationBuilder.append("try ").append(check.a.compile(this)).append(" = ").append(check.b.compile(this)).append("\n"));
                    newAgentPairs.add(new Pair<>(newFrame, choice.choreography));
                }
            }
            return this.compileAgent(agent, newAgentPairs, translationBuilder.toString());
        }
        throw new Error("The specification is ill-defined");
    }
}
