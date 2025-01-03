import java.util.List;

public abstract class Term extends AST {
    abstract public String compile(Environment env);

    abstract public Term getKey();
    abstract public List<Term> getContent();
}

class Constant extends Term {
    String x;

    public Constant(String x) {
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
    public List<Term> getContent() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Constant constant = (Constant) o;
        return this.x.equals(constant.x);
    }

    @Override
    public String toString() {
        return x;
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
        RegisteredFunction registeredFunction = RegisteredFunction.getRegisteredFunction(this.name);
        if (registeredFunction == null || !registeredFunction.hasKey) return null;
        switch (registeredFunction) {
            case RegisteredFunction.CRYPT:
                return new Function(RegisteredFunction.INV.name, this.args.subList(0, 1));
            case RegisteredFunction.SIGN:
                if (this.args.getFirst() instanceof Function function && function.name.equals(RegisteredFunction.INV.name) && !function.args.isEmpty())
                    return function.args.getFirst();
            default:
                return this.args.getFirst();
        }
    }

    @Override
    public List<Term> getContent() {
        RegisteredFunction registeredFunction = RegisteredFunction.getRegisteredFunction(this.name);
        if (RegisteredFunction.CRYPT_FUNCTIONS.contains(registeredFunction))
            return List.of(this.args.get(1));
        if (registeredFunction != null && registeredFunction.analyzable)
            return this.args;
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
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(this.name + "(");
        for (int i = 0; i < this.args.size(); i++) {
            stringBuilder.append(this.args.get(i));
            if (i < this.args.size() - 1) stringBuilder.append(",");
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() + this.args.hashCode();
    }
}
