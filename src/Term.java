import java.util.List;

public abstract class Term extends AST {
    abstract public String compile(Environment env);

    abstract public Term getKey();
    abstract public Term getContent();
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

    @Override
    public Term getKey() {
        return null;
    }

    @Override
    public Term getContent() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variable variable = (Variable) o;
        return this.x.equals(variable.x);
    }

    @Override
    public int hashCode() {
        return x.hashCode();
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
        for (int i = 0; i < this.args.size(); i++) {
            stringBuilder.append(this.args.get(i).compile(env));
            if (i < this.args.size() - 1) stringBuilder.append(",");
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    @Override
    public Term getKey() {
        if (List.of(RegisteredFunction.CRYPT.name, RegisteredFunction.SCRYPT.name, RegisteredFunction.SIGN.name).contains(this.name))
            return this.name.equals(RegisteredFunction.CRYPT.name) ? new Function(RegisteredFunction.INV.name, this.args.subList(0, 1)) : this.args.getFirst();
        return null;
    }

    @Override
    public Term getContent() {
        if (List.of(RegisteredFunction.CRYPT.name, RegisteredFunction.SCRYPT.name, RegisteredFunction.SIGN.name).contains(this.name))
            return this.args.get(1);
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Function function = (Function) o;
        if (!this.name.equals(function.name)) return false;
        return this.args.equals(function.args);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() + this.args.hashCode();
    }
}
