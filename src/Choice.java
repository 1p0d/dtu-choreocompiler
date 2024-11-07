import java.util.Objects;

class Choice extends AST {
    Term message;
    Choreo choreography;

    public Choice(Term message, Choreo choreography) {
        this.message = message;
        this.choreography = choreography;
    }

    public String compile(Environment env) {
        return "send(" + this.message.compile(env) + ").\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Choice choice = (Choice) o;
        return Objects.equals(message, choice.message) && Objects.equals(choreography, choice.choreography);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, choreography);
    }

    @Override
    public String toString() {
        return "Choice{" +
                "message=" + message +
                ", choreography=" + choreography +
                '}';
    }
}
