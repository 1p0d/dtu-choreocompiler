import org.antlr.v4.runtime.misc.Pair;
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
            agentTranslations.put(agent, compileAgent(agent, agentPairsMap.get(agent), knwlBuilder.toString(), 0));
        });
        return agentTranslations;
    }

    public String compileAgent(String agent, List<Pair<Frame, Choreo>> incomingAgentPairs, String translation, Integer depth) {
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
                    translationBuilder.append("\t".repeat(depth)).append("var [").append(label).append("] ")
                            .append(constant.compile(this)).append(".\n");
                });
                if (definition.choreography != null) newAgentPairs.add(new Pair<>(frame, definition.choreography));
            }
            return this.compileAgent(agent, newAgentPairs, translationBuilder.toString(), depth);
        }
        // all choreos are a message that agent is sender of, have the same number of choices and recipes exist for every choice.message
        // TODO: check that all sends have the same recipes
        if (agentPairs.stream().allMatch(pair -> pair.b instanceof Message message && message.agentFrom.equals(agent) &&
                message.choices.size() == ((Message) agentPairs.getFirst().b).choices.size() &&
                message.choices.stream().allMatch(choice -> pair.a.compose(choice.message) != null))) {
            Set<String> composedSends = new HashSet<>();
            for (Pair<Frame, Choreo> pair : agentPairs) {
                Frame frame = pair.a;
                Message message = (Message) pair.b;
                StringBuilder sb = new StringBuilder();
                sb.append("\t".repeat(depth)).append("send(");
                for (int i = 0; i < message.choices.size(); i++) {
                    Choice choice = message.choices.get(i);
                    sb.append(frame.compose(choice.message).compile(this));
                    if (i < message.choices.size() - 1)
                        sb.append(" +\n").append("\t".repeat(depth + 1));
                    if (choice.choreography != null)
                        newAgentPairs.add(new Pair<>(frame, choice.choreography));
                }
                composedSends.add(sb.append(").\n").toString());
            }
            composedSends.forEach(translationBuilder::append);
            return this.compileAgent(agent, newAgentPairs, translationBuilder.toString(), depth);
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
                    choicesChecks.add(new Pair<>(choice.choreography != null ?
                            new Pair<>(choiceFrame, choice.choreography) : null, choiceFrame.analyze()));
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
                Node<ChoiceCheckNode> newNode = new Node<>(new ChoiceCheckNode(agentPair));
                node.addChild(newNode);
            }
            // render tree
            return translationBuilder.append(renderNodes(agent, root.children, depth)).toString();
        }
        throw new Error("The specification is ill-defined: It did not match any expectations");
    }

    public String renderNodes(String agent, Set<Node<ChoiceCheckNode>> nodes, Integer depth) {
        if (nodes.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        List<Node<ChoiceCheckNode>> checkNodes = nodes.stream().filter(node -> node.payload.check != null).toList();
        sb.append(this.compileAgent(agent, nodes.stream().filter(node -> node.payload.agentPair != null)
                .map(node -> node.payload.agentPair).toList(), "", depth));
        for (int i = 0; i < checkNodes.size(); i++) {
            Node<ChoiceCheckNode> checkNode = checkNodes.get(i);
            Check check = checkNode.payload.check;
            sb.append("\t".repeat(depth + i)).append(check.isAssignment ? "try " : "if ")
                    .append(check.left.compile(this)).append(" = ").append(check.right.compile(this))
                    .append(" ").append(check.isAssignment ? "do" : "then").append("\n");
            sb.append(renderNodes(agent, checkNode.children, depth + 1));
            if (i < checkNodes.size() - 1)
                sb.append("\t".repeat(depth + i)).append(check.isAssignment ? "catch" : "else").append("\n");
            else
                sb.append("\t".repeat(depth + i)).append(check.isAssignment ? "catch" : "else").append(" 0\n");
        }
        return sb.toString();
    }
}
