import java.util.ArrayList;
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
    Integer counter;

    public Frame(List<Term> knowledge) {
        this.knowledge = new HashMap<>();
        this.recipes = new HashMap<>();
        this.labelsNew = new ArrayList<>();
        this.labelsHold = new ArrayList<>();
        this.labelsDone = new ArrayList<>();
        this.counter = 0;
        for (Term term : knowledge) this.add(term);
    }

    public void add(Term term) {
        String label = getLabel();
        this.labelsNew.add(label);
        this.knowledge.put(label, term);
    }

    public void add(Term term, String label) {
        if (this.knowledge.containsKey(label)) return;
        this.labelsNew.add(label);
        this.knowledge.put(label, term);
    }

    public Term compose(Term term) {
        // if agent knows about term and term is checked, return known term
        for (Map.Entry<String, Term> entry : knowledge.entrySet()) {
            String label = entry.getKey();
            Term knownTerm = entry.getValue();
            if (knownTerm.equals(term) && this.labelsDone.contains(label))
                return new Variable(label);
        }
        if (term instanceof Function function) {
            // if function is not globally callable and agent does not know the term, the agent is not allowed to compose
            if (!RegisteredFunction.isGlobal(function.name)) return null;
            List<Term> composedArgLabels = new ArrayList<>();
            // if one of the functions' args cannot be composed, the function cannot be composed
            for (Term arg : function.args) {
                Term composedArgLabel = compose(arg);
                if (composedArgLabel == null) return null;
                composedArgLabels.add(composedArgLabel);
            }
            // if all functions' args can be composed, the function can be composed
            return new Function(function.name, composedArgLabels);
        }
        return null;
    }

    public void analyze() {
        // go through all new labels
        while (!this.labelsNew.isEmpty()) {
            String label = this.labelsNew.removeFirst();
            Term term = this.knowledge.get(label);
            // if term can be composed, continue
            if (this.compose(term) != null) {
                this.labelsDone.add(label);
                continue;
            }
            // if term is keyed function...
            if (term instanceof Function function &&
                    (function.name.equals(RegisteredFunction.CRYPT.name) ||
                    function.name.equals(RegisteredFunction.SCRYPT.name) ||
                    function.name.equals(RegisteredFunction.SIGN.name))) {
                // if key can be composed
                Term keyLabel = this.compose(function.getKey());
                if (keyLabel != null) {
                    this.labelsNew.addAll(this.labelsHold);
                    this.labelsHold.clear();
                    this.labelsDone.add(label);
                } else {
                    this.labelsHold.add(label);
                }
            }
        }
    }

    private String getLabel() {
        // get label uniquely identifying term
        return this.counter++ + "";
    }
}