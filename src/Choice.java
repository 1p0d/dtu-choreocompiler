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
        Frame frame = env.frames.getFirst();
        frame.addUnknown(this.message);
        frame.analyze();
        return "send(" + this.message.compile(env) + ").\n" +
                (this.choreography != null ? this.choreography.compile(env) : "");
    }
}
