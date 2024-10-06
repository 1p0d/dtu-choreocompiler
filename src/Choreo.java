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
    List<Constant> constants;
    Choreo choreography;

    public Definition(String agent, List<Constant> constants, Choreo choreography) {
        this.agent = agent;
        this.constants = constants;
        this.choreography = choreography;
    }

    @Override
    public String compile(Environment env) {
        if (!env.currentAgent.equals(this.agent)) return this.choreography.compile(env);
        for (Constant constant : this.constants) {
            env.agentsFrames.get(env.currentAgent).getLast().a.add(constant);
        }
        StringBuilder sb = new StringBuilder();
        for (Constant constant : this.constants) {
            sb.append("var ");
            sb.append(constant.compile(env));
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
        StringBuilder sb = new StringBuilder();
        if (env.currentAgent.equals(this.agentTo))
            sb.append("receive(").append(this.label).append(").\n");
        for (int i = 0; i < this.choices.size(); i++) {
            if (this.choices.size() > 1 && i == 0) sb.append("(");
            String compiledChoice = this.choices.get(i).compile(env, this.agentFrom);
            if (compiledChoice != null) {
                sb.append(compiledChoice);
                if (i < this.choices.size() - 1) sb.append(" +\n");
            }
            if (this.choices.size() > 1 && i == this.choices.size() - 1) sb.append(")\n");
        }
        return sb.toString();
    }
}
