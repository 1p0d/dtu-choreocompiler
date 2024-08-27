import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Frame extends AST {
    Map<String, Term> knowledge;
    Map<String, String> recipes;

    public Frame(List<Term> knowledge) {
        this.knowledge = new HashMap<String, Term>();
        for (int i = 0; i < knowledge.size(); i++) {
            this.knowledge.put("l" + i, knowledge.get(i));
        }
    }

    public void add(String key, Term term) {
        this.knowledge.put(key, term);
    }

    public boolean compose(Term term) {
        if (term instanceof Variable) {
            return this.knowledge.containsValue(term);
        } else if (term instanceof Function function) {
            for (Term arg : function.args) {
                if (!compose(arg)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void analyze() {

    }
}