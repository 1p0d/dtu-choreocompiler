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
        return null;
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
        return null;
    }
}
