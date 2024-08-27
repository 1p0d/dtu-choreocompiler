import java.util.List;

public abstract class AST {
    public static void error(String msg) {
        System.err.println("Compilation error: " + msg);
        System.exit(1);
    }
}

abstract class Choreo extends AST {
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
        StringBuilder sb = new StringBuilder();
        sb.append(this.agent).append(" = ");
        
        // Compile variables
        if (!this.variables.isEmpty()) {
            sb.append("(");
            for (int i = 0; i < this.variables.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(this.variables.get(i).compile(env));
            }
            sb.append(") -> ");
        }
        
        // Compile choreography
        sb.append(this.choreography.compile(env));
        
        return sb.toString();
    }
}

class Message extends Choreo {
    String agentFrom;
    String agentTo;
    String label;
    Choice choice;

    public Message(String agentFrom, String agentTo, String label, Choice choice) {
        this.agentFrom = agentFrom;
        this.agentTo = agentTo;
        this.label = label;
        this.choice = choice;
    }

    public Message(String agentFrom, String agentTo, Choice choice) {
        this.agentFrom = agentFrom;
        this.agentTo = agentTo;
        this.choice = choice;
    }

    @Override
    public String compile(Environment env) {
        if (this.agentFrom.equals(this.agentTo)) {
            error("Agent cannot send message to itself.");
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (env.getCurrentAgent().equals(agentFrom)) {
            sb.append("send(").append(label).append(", ");
            sb.append(choice.continuation.message.compile(env));
            sb.append(").\n");
        } else if (env.getCurrentAgent().equals(agentTo)) {
            sb.append("receive(").append(label).append(").\n");
            sb.append("if (verify(").append(choice.continuation.message.compile(env)).append(")) then\n");
            sb.append("    ").append(choice.continuation.choreography.compile(env));
            sb.append("else 0\n");
        }
        return sb.toString();
    }
}

class Choice extends AST {
    Cont continuation;
    Choice nextChoice;

    public Choice(Cont continuation, Choice nextChoice) {
        this.continuation = continuation;
        this.nextChoice = nextChoice;
    }

    public Choice(Cont continuation) {
        this.continuation = continuation;
    }
}

class Cont extends AST {
    Term message;
    Choreo choreography;

    public Cont(Term message, Choreo choreography) {
        this.message = message;
        this.choreography = choreography;
    }

    public Cont(Term message) {
        this.message = message;
    }
}

abstract class Local extends AST {}

class Receive extends Local {
    String variable;
    Local local;
}

class Send extends Local {
    String variable;
    Local local;
}
