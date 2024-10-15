class Choice extends AST {
    Term message;
    Choreo choreography;

    public Choice(Term message, Choreo choreography) {
        this.message = message;
        this.choreography = choreography;
    }

    public Choice(Term message) {
        this.message = message;
    }

    public String compile(Environment env) {
        return "send(" + this.message.compile(env) + ").\n";
    }
}
