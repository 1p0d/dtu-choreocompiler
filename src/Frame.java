import java.util.List;

public class Frame extends AST {
    private Choreo choreography;
    private String agent;
    private List<String> variables;

    public Frame(Choreo choreography, String agent, List<String> variables) {
        this.choreography = choreography;
        this.agent = agent;
        this.variables = variables;
    }

    public Choreo getChoreography() {
        return choreography;
    }

    public String getAgent() {
        return agent;
    }

    public List<String> getVariables() {
        return variables;
    }

    public String compile(Environment env) {
        StringBuilder localCode = new StringBuilder();

        // Add knowledge
        localCode.append(agent).append(": ").append(String.join(", ", variables)).append("\n");

        // Compile choreography to local code
        localCode.append(compileChoreography(choreography, env));

        return localCode.toString();
    }

    private String compileChoreography(Choreo choreo, Environment env) {
        if (choreo instanceof Empty) {
            return "0\n";
        } else if (choreo instanceof Message) {
            return compileMessage((Message) choreo, env);
        } else if (choreo instanceof Definition) {
            return compileDefinition((Definition) choreo, env);
        } else {
            // Handle other types of choreographies
            return "";
        }
    }

    private String compileMessage(Message message, Environment env) {
        StringBuilder localCode = new StringBuilder();

        if (message.agentFrom.equals(agent)) {
            // Sending a message
            localCode.append("send(").append(message.label).append(", ");
            localCode.append(message.choice.continuation.message.compile(env));
            localCode.append(").\n");
        } else if (message.agentTo.equals(agent)) {
            // Receiving a message
            localCode.append("receive(").append(message.label).append(").\n");
            // Add checks based on the received message
            localCode.append("if (verify(").append(message.choice.continuation.message.compile(env)).append(")) then\n");
            localCode.append("    ").append(compileChoreography(message.choice.continuation.choreography, env));
            localCode.append("else 0\n");
        }

        return localCode.toString();
    }

    private String compileDefinition(Definition definition, Environment env) {
        // Add new variables to the environment
        for (Variable var : definition.variables) {
            variables.add(var.x);
        }

        // Compile the inner choreography
        return compileChoreography(definition.choreography, env);
    }
}