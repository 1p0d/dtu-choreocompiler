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
        if (agent == null || agent.isBlank() || incomingAgentPairs == null || incomingAgentPairs.isEmpty())
            return translation;
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
                    Term label = frame.add(constant).a;
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
            // produce list of choices and their checks
            List<Pair<Choice, List<Check>>> choicesChecks = new ArrayList<>();
            for (int i = 0; i < agentPairs.size(); i++) {
                Pair<Frame, Choreo> pair = agentPairs.get(i);
                Message message = (Message) pair.b;
                for (int j = 0; j < message.choices.size(); j++) {
                    Choice choice = message.choices.get(j);
                    Frame choiceFrame = new Frame(pair.a);
                    Term label = choiceFrame.add(choice.message).a;
                    choicesChecks.add(new Pair<>(choice, choiceFrame.analyze()));
                    if (i == 0 && j == 0) translationBuilder.append("receive(").append(label).append(").\n");
                }
            }
            // now we want to render mutual assignment checks (tries)
            // get assignment checks of all choices
            List<List<Check>> assignmentChecksForEachChoice = new ArrayList<>();
            // TODO
            // get mutual checks
            List<Check> mutualChecks = new ArrayList<>();
            List<Check> referenceAssignmentChecks = choicesChecks.stream().reduce(choicesChecks.getFirst(), (a, b) -> a.b.size() > b.b.size() ? a : b)
                    .b.stream().filter(check -> check.isAssignment).toList();
            for (int i = 0; i < referenceAssignmentChecks.size(); i++) {
                Check referenceCheck = referenceAssignmentChecks.get(i);
                for (Pair<Choice, List<Check>> pair : choicesChecks) {
                    Check check = pair.b.get(i);
                    // TODO
                }
            }
            for (Check check : choicesChecks.getFirst().b) {
                if (choicesChecks.stream().allMatch(pair -> pair.b.stream().anyMatch(check::equals))) mutualChecks.add(check);
            }
            for (Check check : mutualChecks) {
                translationBuilder.append(check.isAssignment ? "try " : "if ").append(check.left.compile(this))
                        .append(" = ").append(check.right.compile(this)).append(check.isAssignment ? "do" : "then").append("\n");
            }
            // filter out mutual checks
            choicesChecks = choicesChecks.stream().map(pair ->
                new Pair<>(pair.a, pair.b.stream().filter(check -> !mutualChecks.contains(check)).toList())).toList();
            // render individual checks if any and append this.compileAgent to translation builder with
            for (Pair<Choice, List<Check>> pair : choicesChecks) {
                Choice choice = pair.a;
                List<Check> checks = pair.b;
                // TODO: Render each choice with any leftover checks one after the other (IF block -> choice -> ELSE block -> nextChoice -> END block),
                //  any choice without leftover checks should go in the first else block

                // TODO: newAgentPairs should be gathered for each choice in the same block adding choice.choreography,
                //  and this.compileAgent should be called recursively to compile the choice(s)
            }
            for (Check check : mutualChecks) {
                translationBuilder.append(check.isAssignment ? "catch" : "else").append(" 0\n").append("end");
            }
            return this.compileAgent(agent, newAgentPairs, translationBuilder.toString());
        }
        throw new Error("The specification is ill-defined: It did not match any expectations");
    }

    private String renderChecks(List<Pair<Choice, List<Triple<Boolean, Term, Term>>>> choicesChecks) {

        return "";
    }
}
