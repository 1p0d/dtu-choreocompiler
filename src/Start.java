import java.util.List;

public class Start extends AST {
    Choreo choreo;
    List<Knowledge> knowledges;

    public Start(Choreo choreo, List<Knowledge> knowledges) {
        this.choreo = choreo;
        this.knowledges = knowledges;
    }
}
