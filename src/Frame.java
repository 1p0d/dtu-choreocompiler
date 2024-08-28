import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Frame extends AST {
    // Map from labels to known terms
    Map<String, Term> knowledge;
    // Map of known recipes (from ingredient(s) to result)
    Map<Term, Term> recipes;
    // Lists tracking analyzed labels
    List<String> labelsNew;
    List<String> labelsHold;
    List<String> labelsDone;

    public Frame(List<Term> knowledge) {
        this.knowledge = new HashMap<>();
        for (int i = 0; i < knowledge.size(); i++) {
            this.knowledge.put("l" + i, knowledge.get(i));
        }
    }

    public void add(String key, Term term) {
        this.knowledge.put(key, term);
    }

    public Term compose(Term term) {
        // if agent knows about term, return knowledge entry
        for (Map.Entry<String, Term> entry : knowledge.entrySet()) {
            Term knownTerm = entry.getValue();
            if (knownTerm.equals(term)) {
                return knownTerm;
            }
        }
        if (term instanceof Function function) {
            // if function is not globally callable and agent does not know the term, the agent is not allowed to compose
            if (!RegisteredFunction.isGlobal(function.name)) return null;
            // if one of the functions' args cannot be composed, the function cannot be composed
            for (Term arg : function.args) {
                if (compose(arg) == null) {
                    return null;
                }
            }
            // if all functions' args can be composed, the function can be composed
            return term;
        }
        return null;
    }

    public void analyze() {

    }
}