import java.util.List;

public abstract class Term extends AST {
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
    List<Term> args;

    public Function(String name, List<Term> args) {
        this.name = name;
        this.args = args;
    }

    @Override
    public String compile(Environment env) {
        StringBuilder stringBuilder = new StringBuilder(this.name + "(");
        for (Term arg : this.args) {
            stringBuilder.append(arg.compile(env));
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }
}
