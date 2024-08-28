public abstract class Choice extends AST {
    abstract public String compile(Environment env);
}

class Continuation extends Choice {
    Term message;
    Choreo choreography;

    public Continuation(Term message, Choreo choreography) {
        this.message = message;
        this.choreography = choreography;
    }

    public Continuation(Term message) {
        this.message = message;
    }

    @Override
    public String compile(Environment env) {
        return null;
    }
}
