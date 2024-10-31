import java.util.List;
import java.util.Objects;

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
            return sb.append("receive(").append(this.label).append(").\n").toString();
        sb.append("(");
        for (int i = 0; i < this.choices.size(); i++) {
            String compiledChoice = this.choices.get(i).compile(env);
            if (compiledChoice != null) {
                sb.append(compiledChoice);
                if (i < this.choices.size() - 1) sb.append(" +\n");
            }
        }
        return sb.append(")\n").toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(agentFrom, message.agentFrom) && Objects.equals(agentTo, message.agentTo) && Objects.equals(choices, message.choices) && Objects.equals(label, message.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agentFrom, agentTo, choices, label);
    }
}
