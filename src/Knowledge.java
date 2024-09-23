import java.util.List;

public abstract class Knowledge extends AST {
    abstract public String compile(Environment env);
}

class AgentKnowledge extends Knowledge {
    String agent;
    List<Term> knowledge;

    public AgentKnowledge(String agent, List<Term> knowledge) {
        this.agent = agent;
        this.knowledge = knowledge;
    }

    @Override
    public String compile(Environment env) {
        StringBuilder sb = new StringBuilder(agent).append(": ");
        for (Term term : this.knowledge) {
            sb.append(term.compile(env));
        }
        return sb.append(".").toString();
    }
}
