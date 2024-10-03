import org.antlr.v4.runtime.misc.Pair;

import java.util.ArrayList;
import java.util.List;

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

    public String compile(Environment env, String agentFrom) {
        if (!env.currentAgent.equals(agentFrom)) return this.choreography != null ? this.choreography.compile(env) : "";
        // update agents frames
        List<Pair<Frame, Choreo>> agentFrames = new ArrayList<>(env.agentsFrames.get(env.currentAgent));
        Frame frame = new Frame(agentFrames.getLast().a.knowledge.values().stream().toList());
        frame.add(this.message);
        agentFrames.add(new Pair<>(frame, choreography));
        env.agentsFrames.put(env.currentAgent, agentFrames);
        // compile
        List<Pair<Term, Term>> checks = frame.analyze();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < checks.size(); i++) {
            Pair<Term, Term> pair = checks.get(i);
            sb.append("try ").append(pair.a.compile(env)).append(" = ").append(pair.b.compile(env)).append("\n");
            if (i == checks.size() - 1)
                sb.append("send(").append(this.message.compile(env)).append(").").append(this.choreography != null ? this.choreography.compile(env) : "");
            sb.append("catch 0\n");
        }
        if (checks.isEmpty())
            sb.append("send(").append(this.message.compile(env)).append(").").append(this.choreography != null ? this.choreography.compile(env) : "");
        return sb.toString();
    }
}
