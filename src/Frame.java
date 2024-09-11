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

    public Frame(List<Term> knowledge) {
        this.knowledge = new HashMap<>();
        this.recipes = new HashMap<>();
        this.labelsNew = new ArrayList<>();
        this.labelsHold = new ArrayList<>();
        this.labelsDone = new ArrayList<>();
        for (Term term : knowledge) this.addKnown(term);
    }

    public void addUnknown(Term term) {
        String label = getLabel(term);
        if (this.knowledge.containsKey(label)) return;
        this.knowledge.put(label, term);
        this.labelsNew.add(label);
        if (term instanceof Function function && (function.name.equals(RegisteredFunction.PK.name) ||
                function.name.equals(RegisteredFunction.INV.name))) {
            this.labelsNew.addAll(this.labelsHold);
            this.labelsHold.clear();
        }
    }

    public void addKnown(Term term) {
        String label = getLabel(term);
        this.labelsNew.remove(label);
        this.labelsHold.remove(label);
        this.labelsDone.add(label);
        if (this.knowledge.containsKey(label)) return;
        this.knowledge.put(label, term);
    }

    public Term compose(Term term) {
//        System.out.println(this.knowledge.values().stream().map(t -> t.compile(ChoreoGrammarVisitor.env)).collect(Collectors.joining(", ")));
//        System.out.println("compose: " + term.compile(ChoreoGrammarVisitor.env));
        // if agent knows about term and term is checked, return known term
        for (Map.Entry<String, Term> entry : knowledge.entrySet()) {
            String label = entry.getKey();
            Term knownTerm = entry.getValue();
//            System.out.println(term.compile(ChoreoGrammarVisitor.env) + " (" + term + ")" + " == " + knownTerm.compile(ChoreoGrammarVisitor.env) + " (" + knownTerm + ")" + "? " + (knownTerm.equals(term) ? "true" : "false"));
            if (knownTerm.equals(term) && this.labelsDone.contains(label)) {
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
        // go through all new labels
        while (!this.labelsNew.isEmpty()) {
            String label = this.labelsNew.removeFirst();
            Term term = this.knowledge.get(label);
            // if term can be composed, continue
            if (this.compose(term) != null) {
                this.labelsDone.add(label);
                continue;
            }
            // if term is function...
            if (term instanceof Function function && (function.name.equals(RegisteredFunction.CRYPT.name) ||
                    function.name.equals(RegisteredFunction.SCRYPT.name))) {
                Term key = function.name.equals(RegisteredFunction.CRYPT.name) ?
                        new Function(RegisteredFunction.INV.name, function.args.subList(0, 1)) : function.args.getFirst();
                // if key can be composed, args can be decrypted and are added to knowledge
                if (this.compose(key) != null) {
                    for (Term arg : function.args.subList(1, function.args.size())) {
                        this.addKnown(arg);
                    }
                    this.labelsDone.add(label);
                } else {
                    this.labelsHold.add(label);
                }
            } else error("Frame contains non-composable term " + term.compile(ChoreoGrammarVisitor.env));
        }
    }

    private String getLabel(Term term) {
        // get label uniquely identifying term
        return "" + term.hashCode();
    }
}