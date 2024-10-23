import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.misc.Triple;

import java.util.*;

public class Environment {
    public String currentAgent;

    public Environment() {
    }

    public Map<String, String> compile(Map<String, List<Pair<Frame, Choreo>>> agentPairsMap) {
        Map<String, String> agentTranslations = new HashMap<>();
        agentPairsMap.forEach((agent, value) -> {
            this.currentAgent = agent;
            StringBuilder knwlBuilder = new StringBuilder(agent + ": ");
            value.getFirst().a.knowledge.forEach((label, term) ->
                    knwlBuilder.append("[").append(label).append("] ").append(term.compile(this)).append(", ")
            );
            knwlBuilder.deleteCharAt(knwlBuilder.length() - 1).deleteCharAt(knwlBuilder.length() - 1).append(".\n");
            agentTranslations.put(agent, compileAgent(agent, agentPairsMap.get(agent), knwlBuilder.toString()));
        });
        return agentTranslations;
    }

    public String compileAgent(String agent, List<Pair<Frame, Choreo>> incomingAgentPairs, String translation) {
        if (agent == null || agent.isBlank() || incomingAgentPairs == null || incomingAgentPairs.isEmpty()) return translation;
        List<Pair<Frame, Choreo>> agentPairs = new ArrayList<>(incomingAgentPairs);
        ListIterator<Pair<Frame, Choreo>> listIterator = agentPairs.listIterator();
        while (listIterator.hasNext()) {
            Pair<Frame, Choreo> pair = listIterator.next();
            Frame frame = pair.a;
            Choreo choreo = pair.b;
            // a choreo starts with a message that agent is not involved in, continue
            if (choreo instanceof Message message && !message.agentFrom.equals(agent) && !message.agentTo.equals(agent)) {
                listIterator.remove();
                message.choices.forEach(choice -> {
                    if (choice.choreography != null) {
                        listIterator.add(new Pair<>(frame, choice.choreography));
                        listIterator.previous();
                    }
                });
            }
            // a choreo is a fresh creation by another agent
            else if (choreo instanceof Definition definition && !definition.agent.equals(agent)) {
                listIterator.remove();
                if (definition.choreography != null) {
                    listIterator.add(new Pair<>(frame, definition.choreography));
                    listIterator.previous();
                }
            }
        }
        StringBuilder translationBuilder = new StringBuilder(translation);
        List<Pair<Frame, Choreo>> newAgentPairs = new ArrayList<>();
        // all choreos are 0
        if (agentPairs.stream().allMatch(pair -> pair.b instanceof Empty))
            return translationBuilder.toString();
        // all choreos start with a definition and define the same constants
        if (agentPairs.stream().allMatch(pair -> pair.b instanceof Definition definition &&
                definition.constants.equals(((Definition) agentPairs.getFirst().b).constants))) {
            for (Pair<Frame, Choreo> pair : agentPairs) {
                Frame frame = new Frame(pair.a);
                Definition definition = (Definition) pair.b;
                definition.constants.forEach(constant -> {
                    Term label = frame.add(constant);
                    translationBuilder.append("var [").append(label).append("] ")
                            .append(constant.compile(this)).append(".\n");
                });
                if (definition.choreography != null) newAgentPairs.add(new Pair<>(frame, definition.choreography));
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
                translationBuilder.append("send(");
                message.choices.forEach(choice -> {
                    Term composedMessage = frame.compose(choice.message);
                    translationBuilder.append(composedMessage.compile(this)).append(" +\n\t");
                    if (choice.choreography != null)
                        newAgentPairs.add(new Pair<>(frame, choice.choreography));
                });
                translationBuilder.delete(translationBuilder.length() - 4, translationBuilder.length()).append(").\n");
            }
            return this.compileAgent(agent, newAgentPairs, translationBuilder.toString());
        }
        // all choreos are a message that agent is receiver of
        if (agentPairs.stream().allMatch(pair -> pair.b instanceof Message message && message.agentTo.equals(agent))) {
            for (Pair<Frame, Choreo> pair : agentPairs) {
                Frame frame = pair.a;
                Message message = (Message) pair.b;
//                Choice firstChoice = message.choices.getFirst();
//                Frame firstChoiceFrame = new Frame(pair.a);
//                firstChoiceFrame.add(firstChoice.message);
//                List<Triple<Boolean, Term, Term>> firstChoiceChecks = firstChoiceFrame.analyze();
//                translationBuilder.append(message.compile(this));
                for (int i = 0; i < message.choices.size(); i++) {
                    Choice choice = message.choices.get(i);
                    Frame newFrame = new Frame(pair.a);
                    Term label = newFrame.add(choice.message);
                    translationBuilder.append("receive([").append(label).append("] ")
                            .append(choice.message.compile(this)).append(").\n");
                    List<Triple<Boolean, Term, Term>> choiceChecks = newFrame.analyze();
                    choiceChecks.forEach(check -> {
                        if (check.a) {
                            translationBuilder.append("try ").append(check.b.compile(this)).append(" = ").append(check.c.compile(this)).append("\n");
                        } else {
                            translationBuilder.append("if (").append(check.b.compile(this)).append(" = ").append(check.c.compile(this)).append(")\n");
                        }
                    });
//                    if (!choiceChecks.equals(firstChoiceChecks))
//                        throw new Error("The specification is ill-defined: All choices should require the same checks");
                    if (choice.choreography != null) newAgentPairs.add(new Pair<>(newFrame, choice.choreography));
                }
            }
            return this.compileAgent(agent, newAgentPairs, translationBuilder.toString());
        }
        throw new Error("The specification is ill-defined: It did not match any expectations");
    }
}
