import org.antlr.v4.runtime.misc.Pair;

import java.util.List;

public class Start extends AST {
    public Start() {}

    public String compile(Environment env) {
        StringBuilder sb = new StringBuilder(env.currentAgent).append(": ");
        Pair<Frame, Choreo> agentFrame = env.agentFrames.get(env.currentAgent).getFirst();
        List<Term> knowledge = agentFrame.a.knowledge.values().stream().toList();
        for (int i = 0; i < knowledge.size(); i++) {
            sb.append(knowledge.get(i).compile(env));
            if (i < knowledge.size() - 1) sb.append(", ");
        }
        return sb.append(".\n").append(agentFrame.b.compile(env)).toString();
    }
}
