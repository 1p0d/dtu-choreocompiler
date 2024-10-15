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

    public Term add(Term term, String label) {
        if (this.knowledge.containsKey(label)) return new Constant(label);
        this.labelsNew.add(label);
        this.knowledge.put(label, term);
        this.labelsNew.addAll(this.labelsHold);
        this.labelsHold.clear();
        return new Constant(label);
    }

    public Term add(Term term) {
        String label = getLabel();
        return this.add(term, label);
    }

    public Term compose(Term term) {
        if (term == null) return null;
        // if agent knows about term and term is checked, return known term
        for (Map.Entry<String, Term> entry : knowledge.entrySet()) {
            String label = entry.getKey();
            Term knownTerm = entry.getValue();
            if (knownTerm.equals(term) && this.labelsDone.contains(label)) return new Constant(label);
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
            // if function is not registered or not analyzable, continue
            RegisteredFunction registeredFunction = RegisteredFunction.getRegisteredFunction(function.name);
            if (registeredFunction == null || !registeredFunction.analyzable) {
                this.labelsDone.add(label);
                continue;
            }
            List<Term> args = function.getContent();
            List<Term> argLabels = new ArrayList<>();
            for (Term arg : args)
                argLabels.add(this.add(arg));
            if (RegisteredFunction.KEYED_FUNCTIONS.contains(registeredFunction)) {
                Term keyLabel = this.compose(function.getKey());
                // if function is keyed but key cannot be composed, continue
                if (RegisteredFunction.KEYED_FUNCTIONS.contains(registeredFunction) && keyLabel == null) {
                    this.labelsHold.add(label);
                    continue;
                }
                argLabels.addFirst(keyLabel);
            }
            if (registeredFunction.equals(RegisteredFunction.PAIR)) {
                for (int i = 0; i < args.size(); i++)
                    checks.add(new Pair<>(argLabels.get(i), new Function(registeredFunction.destructor + (i + 1), List.of(args.get(i)))));
            } else {
                checks.add(new Pair<>(new Constant(label), new Function(registeredFunction.destructor, argLabels)));
            }
            this.labelsDone.add(label);
        }
        return checks;
    }

    private String getLabel() {
        // TODO: perhaps a value based ID is needed for uniqueness across frames, e.g. crypt(pk(B),pair(msg,M)) = _crypt_pk_B_pair_msg_M
        return "l" + this.counter++;
    }
}