import java.util.List;

public abstract class AST {
    public static void error(String msg) {
        System.err.println("Compilation error: " + msg);
        System.exit(1);
    }
}

abstract class Term extends AST {
    abstract public String compile(Environment env);
}

class Variable extends Term {
    String x;

    public Variable(String x) {
        this.x = x;
    }

    @Override
    public String compile(Environment env) {
        return x;
    }
}

class Function extends Term {
    String name;
    Arguments args;

    public Function(String name, Arguments args) {
        this.name = name;
        this.args = args;
    }

    @Override
    public String compile(Environment env) {
        return this.name + "(" + this.args.compile(env) + ")";
    }
}

class Crypt extends Term {
    Term message, key;

    public Crypt(Term message, Term key) {
        this.message = message;
        this.key = key;
    }

    @Override
    public String compile(Environment env) {
        return "dcrypt(" + this.key.compile(env) + "," + this.message.compile(env) + ")";
    }
}

class MAC extends Term {
    Term message, key;

    public MAC(Term message, Term key) {
        this.message = message;
        this.key = key;
    }

    @Override
    public String compile(Environment env) {
        return "mac(" + this.key.compile(env) + "," + this.message.compile(env) + ")";
    }
}

abstract class Arguments extends AST {
    abstract public List<Term> getArguments();

    abstract public String compile(Environment env);
}

class Args extends Arguments {
    List<Term> args;

    public Args(List<Term> args) {
        this.args = args;
    }

    @Override
    public List<Term> getArguments() {
        return this.args;
    }

    @Override
    public String compile(Environment env) {
        StringBuilder stringBuilder = new StringBuilder("(");
        for (Term arg : this.args) {
            stringBuilder.append(arg.compile(env));
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
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
        // TODO: implement
        return null;
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

    @Override
    public String compile(Environment env) {
        // TODO: implement
        return null;
    }
}

class Choice extends AST {
    Cont continuation;
    Choice nextChoice;

    public Choice(Cont continuation, Choice nextChoice) {
        this.continuation = continuation;
        this.nextChoice = nextChoice;
    }
}

class Cont extends AST {
    Term message;
    Choreo choreography;

    public Cont(Term message, Choreo choreography) {
        this.message = message;
        this.choreography = choreography;
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
