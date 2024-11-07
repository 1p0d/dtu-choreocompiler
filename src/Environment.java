import org.antlr.v4.runtime.misc.Pair;

import java.util.*;

public class Environment {
    public String currentAgent;

    public Environment() {
    }

    /**
     * Compiles a map of agent pairs extracted from a choreography to a local behavior
     *
     * @param agentPairsMap Map of agents that each have a list of pairs with a frame and belonging choreo
     * @return Map of agents to their translations
     */
    public Map<String, String> compile(Map<String, List<Pair<Frame, Choreo>>> agentPairsMap) {
        Map<String, String> agentTranslations = new HashMap<>();
        agentPairsMap.forEach((agent, value) -> {
            this.currentAgent = agent;
            StringBuilder knwlBuilder = new StringBuilder(agent + ": ");
            value.getFirst().a.knowledge.forEach((label, term) ->
                    knwlBuilder.append("[").append(label).append("] ").append(term.compile(this)).append(", ")
            );
            knwlBuilder.deleteCharAt(knwlBuilder.length() - 1).deleteCharAt(knwlBuilder.length() - 1).append(".\n");
            agentTranslations.put(agent, knwlBuilder + compileAgent(agent, agentPairsMap.get(agent), 0));
        });
        return agentTranslations;
    }

    /**
     * Compile an agent with their agent pairs extracted from a choreography to a local behavior
     *
     * @param agent              Name of agent
     * @param incomingAgentPairs List of pairs with a frame and belonging choreo
     * @param depth              Counter of depth passed recursively used for indentation
     * @return Translation string
     */
    public String compileAgent(String agent, List<Pair<Frame, Choreo>> incomingAgentPairs, Integer depth) {
        if (agent == null || agent.isBlank() || incomingAgentPairs == null || incomingAgentPairs.isEmpty())
            throw new Error("The specification is ill-defined: It did not match any expectations.");
        List<Pair<Frame, Choreo>> agentPairs = new ArrayList<>(incomingAgentPairs);
        ListIterator<Pair<Frame, Choreo>> listIterator = agentPairs.listIterator();
        // remove pairs that the agent is not part of
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
        if (agentPairs.isEmpty()) throw new Error("The specification is ill-defined: No matching pairs for agent " + agent + " left.");
        StringBuilder translationBuilder = new StringBuilder();
        List<Pair<Frame, Choreo>> newAgentPairs = new ArrayList<>();
        // all choreos are 0
        if (agentPairs.stream().allMatch(pair -> pair.b instanceof Empty))
            return translationBuilder.append("\t".repeat(depth)).append("0\n").toString();
        // all choreos start with a definition and define the same constants
        if (agentPairs.stream().allMatch(pair -> pair.b instanceof Definition definition &&
                definition.constants.equals(((Definition) agentPairs.getFirst().b).constants))) {
            for (Pair<Frame, Choreo> pair : agentPairs) {
                Frame frame = new Frame(pair.a);
                Definition definition = (Definition) pair.b;
                definition.constants.forEach(constant -> {
                    Term label = frame.add(constant).a;
                    translationBuilder.append("\t".repeat(depth)).append("var [").append(label).append("] ")
                            .append(constant.compile(this)).append(".\n");
                });
                newAgentPairs.add(new Pair<>(frame, definition.choreography != null ? definition.choreography : new Empty()));
            }
            return translationBuilder.append(this.compileAgent(agent, newAgentPairs, depth)).toString();
        }
        // all choreos are a message that agent is sender of, have the same number of choices and recipes exist for every choice.message
        // TODO: check that all sends have the same recipes
        if (agentPairs.stream().allMatch(pair -> pair.b instanceof Message message && message.agentFrom.equals(agent) &&
                message.choices.size() == ((Message) agentPairs.getFirst().b).choices.size() &&
                message.choices.stream().allMatch(choice -> pair.a.compose(choice.message) != null))) {
            for (Pair<Frame, Choreo> pair : agentPairs) {
                Frame frame = pair.a;
                Message message = (Message) pair.b;
                for (int i = 0; i < message.choices.size(); i++) {
                    Choice choice = message.choices.get(i);
                    translationBuilder.append("\t".repeat(depth)).append("send(").append(frame.compose(choice.message).compile(this)).append(").\n")
                            .append(this.compileAgent(agent, List.of(new Pair<>(frame, choice.choreography)), depth + 1));
                    if (i < message.choices.size() - 1)
                        translationBuilder.append("\t".repeat(depth)).append("+\n");
                }
            }
            return translationBuilder.toString();
        }
        // all choreos are a message that agent is receiver of
        if (agentPairs.stream().allMatch(pair -> pair.b instanceof Message message && message.agentTo.equals(agent))) {
            // produce list of choices and their checks
            List<Pair<Pair<Frame, Choreo>, List<Check>>> choicesChecks = new ArrayList<>();
            Term label = null;
            for (Pair<Frame, Choreo> pair : agentPairs) {
                Message message = (Message) pair.b;
                for (int j = 0; j < message.choices.size(); j++) {
                    Choice choice = message.choices.get(j);
                    Frame choiceFrame = new Frame(pair.a);
                    Term addedMessageLabel = choiceFrame.add(choice.message).a;
                    if (label == null) label = addedMessageLabel;
                    else if (!label.equals(addedMessageLabel))
                        throw new Error("Compilation error: Frame generated unexpected label for received message. " +
                                "Expected: " + label + ". Got: " + addedMessageLabel + ".");
                    choicesChecks.add(new Pair<>(
                            new Pair<>(choiceFrame, choice.choreography), choiceFrame.analyze()));
                }
            }
            translationBuilder.append("\t".repeat(depth)).append("receive(").append(label).append(").\n");
            // create tree positioning checks and choices
            // TODO: if checks do not require a specific order when rendered after all required tries
            Node<ChoiceCheckNode> root = new Node<>(new ChoiceCheckNode());
            for (Pair<Pair<Frame, Choreo>, List<Check>> pair : choicesChecks) {
                Pair<Frame, Choreo> agentPair = pair.a;
                List<Check> checks = pair.b;
                Node<ChoiceCheckNode> node = root;
                for (Check check : checks)
                    node = node.addChild(new Node<>(new ChoiceCheckNode(check)));
                if (agentPair == null) continue;
                node.addChild(new Node<>(new ChoiceCheckNode(agentPair)));
            }
            // render tree
            return translationBuilder.append(renderNodes(agent, root.children, depth)).toString();
        }
        throw new Error("The specification is ill-defined: It did not match any expectations.");
    }

    /**
     * Internal method used to render checks and choices recursively
     *
     * @param agent Name of agent
     * @param nodes Set of nodes supposed to position a check or choice
     * @param depth Counter of depth passed recursively used for indentation
     * @return Translation string
     */
    private String renderNodes(String agent, Set<Node<ChoiceCheckNode>> nodes, Integer depth) {
        if (nodes.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        // get only nodes that contain checks
        List<Node<ChoiceCheckNode>> checkNodes = nodes.stream().filter(node -> node.payload.check != null).toList();
        // first append all nodes that contain choices on this level
        List<Pair<Frame, Choreo>> pairs = nodes.stream().filter(node -> node.payload.agentPair != null)
                .map(node -> node.payload.agentPair).toList();
        if (!pairs.isEmpty()) sb.append(this.compileAgent(agent, pairs, depth));
        // then render all check nodes that their children recursively
        for (int i = 0; i < checkNodes.size(); i++) {
            Node<ChoiceCheckNode> checkNode = checkNodes.get(i);
            Check check = checkNode.payload.check;
            sb.append("\t".repeat(depth + i)).append(check.isAssignment ? "try " : "if ")
                    .append(check.left.compile(this)).append(" = ").append(check.right.compile(this))
                    .append(" ").append(check.isAssignment ? "do" : "then").append("\n")
                    .append(renderNodes(agent, checkNode.children, depth + i + 1))
                    .append("\t".repeat(depth + i)).append(check.isAssignment ? "catch" : "else");
            if (i == checkNodes.size() - 1)
                sb.append(" 0");
            sb.append("\n");
        }
        return sb.toString();
    }
}
