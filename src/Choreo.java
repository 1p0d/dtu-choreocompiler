import java.util.List;

public abstract class Choreo extends AST {
    abstract public String compile(Environment env);
}

class Empty extends Choreo {
    @Override
    public String compile(Environment env) {
        return "0";
    }
}

class Definition extends Choreo {
    String agent;
    List<Variable> variables;
    Choreo choreography;

    public Definition(String agent, List<Variable> variables, Choreo choreography) {
        this.agent = agent;
        this.variables = variables;
        this.choreography = choreography;
    }

    @Override
    public String compile(Environment env) {
        if (!env.currentAgent.equals(this.agent)) return this.choreography.compile(env);
        for (Variable variable : this.variables) {
            env.agentFrames.get(env.currentAgent).getLast().a.add(variable);
        }
        StringBuilder sb = new StringBuilder();
        for (Variable variable : this.variables) {
            sb.append("var ");
            sb.append(variable.compile(env));
            sb.append(".\n");
        }
        return sb.append(this.choreography.compile(env)).toString();
    }
}

class Message extends Choreo {
    String agentFrom;
    String agentTo;
    List<Choice> choices;
    String label;

    public Message(String agentFrom, String agentTo, List<Choice> choices, String label) {
        this.agentFrom = agentFrom;
        this.agentTo = agentTo;
        this.choices = choices;
        this.label = label;
    }

    public Message(String agentFrom, String agentTo, List<Choice> choices) {
        this.agentFrom = agentFrom;
        this.agentTo = agentTo;
        this.choices = choices;
    }

    @Override
    public String compile(Environment env) {
        for (Choice choice : this.choices) {
            choice.compile(env);
        }
        /*StringBuilder sb = new StringBuilder();
        for (Choice choice : this.choices) {
            sb.append(choice.compile(env));
        }
        if (env.currentAgent.equals(this.agentTo))
            sb.append("receive(").append(this.label).append(").");
        return sb.toString();*/
        return "";
    }
}
