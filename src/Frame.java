import org.antlr.v4.runtime.misc.Pair;

import java.util.*;

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

    public String add(Term term, String label) {
        if (this.knowledge.containsKey(label)) return this.knowledge.get(label).toString();
        this.labelsNew.add(label);
        this.knowledge.put(label, term);
        this.labelsNew.addAll(this.labelsHold);
        this.labelsHold.clear();
        return label;
    }

    public String add(Term term) {
        String label = getLabel();
        return this.add(term, label);
    }

    public Term compose(Term term) {
        // if agent knows about term and term is checked, return known term
        for (Map.Entry<String, Term> entry : knowledge.entrySet()) {
            String label = entry.getKey();
            Term knownTerm = entry.getValue();
            if (knownTerm.equals(term) && this.labelsDone.contains(label)) return new Variable(label);
        }
        if (term instanceof Function function) {
            // if function is not globally callable and agent does not know the term, the agent is not allowed to compose
            if (!RegisteredFunction.isGlobal(function.name)) return null;
            List<Term> composedArgs = new ArrayList<>();
            // if one of the functions' args cannot be composed, the function cannot be composed
            for (Term arg : function.args) {
                Term composedArg = compose(arg);
                if (composedArg == null) return null;
                composedArgs.add(composedArg);
            }
            // if all functions' args can be composed, the function can be composed
            return new Function(function.name, composedArgs);
        }
        return null;
    }

    public List<Pair<Term, Term>> analyze() {
        List<Pair<Term, Term>> checks = new ArrayList<>();
        // go through all new labels
        while (!this.labelsNew.isEmpty()) {
            String label = this.labelsNew.removeFirst();
            Term term = this.knowledge.get(label);
            // if term is not a function, continue
            if (!(term instanceof Function function)) {
                this.labelsDone.add(label);
                continue;
            }
            // if function is not registered, continue
            RegisteredFunction registeredFunction = RegisteredFunction.getRegisteredFunction(function.name);
            if (registeredFunction == null) {
                this.labelsDone.add(label);
                continue;
            }
            // if function is keyed but key cannot be composed, continue
            if (RegisteredFunction.KEYED_FUNCTIONS.contains(registeredFunction) && this.compose(function.getKey()) == null) {
                this.labelsHold.add(label);
                continue;
            }
            List<String> argLabels = new ArrayList<>();
            // if function is analyzable, add args to frame
            if (registeredFunction.analyzable) {
                for (Term arg : function.getContent())
                    argLabels.add(this.add(arg));
            }
            if (registeredFunction.equals(RegisteredFunction.PAIR)) {
                for (int i = 0; i < function.getContent().size(); i++)
                    checks.add(new Pair<>(new Variable(argLabels.get(i)), new Function(registeredFunction.destructor, List.of(function.getContent().get(i)))));
            } else {
                checks.add(new Pair<>(new Variable(label), new Function(registeredFunction.destructor, function.args)));
            }
            this.labelsDone.add(label);
        }
        return checks;
    }

    private String getLabel() {
        // get label uniquely identifying term
        return "l" + this.counter++;
    }
}