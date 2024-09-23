import org.antlr.v4.runtime.misc.Pair;

import java.util.List;

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
        frame.add(this.message);
        List<Pair<Term, Term>> checks = frame.analyze();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < checks.size(); i++) {
            Pair<Term, Term> pair = checks.get(i);
            sb.append("if (").append(pair.b.compile(env)).append(") then\n");
            if (i < checks.size() - 1) {
                sb.append("send(").append(this.message.compile(env)).append(").\n").append(this.choreography != null ? this.choreography.compile(env) : "");
            }
            sb.append("else 0\n");
        }
        return sb.toString();
    }
}
