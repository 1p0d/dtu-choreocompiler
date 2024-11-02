import java.util.List;

public class Knowledge extends AST {
    String agent;
    List<Term> knowledge;

    public Knowledge(String agent, List<Term> knowledge) {
        this.agent = agent;
        this.knowledge = knowledge;
    }
}
