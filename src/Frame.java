import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.misc.Triple;

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

    public Frame(Frame other) {
        this.knowledge = new HashMap<>(other.knowledge);
        this.recipes = new HashMap<>(other.recipes);
        this.labelsNew = new ArrayList<>(other.labelsNew);
        this.labelsHold = new ArrayList<>(other.labelsHold);
        this.labelsDone = new ArrayList<>(other.labelsDone);
        this.counter = other.counter;
    }

    public Term add(Term term, String label) {
        Term composedTerm = this.compose(term);
        if (composedTerm != null) return composedTerm;
        if (label == null || this.knowledge.containsKey(label)) label = getLabel();
        this.labelsNew.add(label);
        this.knowledge.put(label, term);
        this.labelsNew.addAll(this.labelsHold);
        this.labelsHold.clear();
        return new Constant(label);
    }

    public Term add(Term term) {
        return this.add(term, null);
    }

    public Term compose(Term term) {
        if (term == null) return null;
        // if agent knows about term and term is checked, return known term
        for (Map.Entry<String, Term> entry : knowledge.entrySet()) {
            String label = entry.getKey();
            Term knownTerm = entry.getValue();
            if (knownTerm.equals(term)) return new Constant(label);
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

    // instead of adding the arg and resuming
    public List<Triple<Boolean, Term, Term>> analyze() {
        List<Triple<Boolean, Term, Term>> checks = new ArrayList<>();
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
            for (Term arg : args) {
                Term composedArg = this.compose(arg);
                if (composedArg != null && !this.labelsNew.contains(composedArg.toString())) {
                    // IF arg = composedArg
                    checks.add(new Triple<>(false, arg, composedArg));
                } else {
                    argLabels.add(this.add(arg));
                }
            }
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
                for (int i = 0; i < argLabels.size(); i++)
                    // TRY label = destructor(arg)
                    checks.add(new Triple<>(true, argLabels.get(i), new Function(registeredFunction.destructor + (i + 1), List.of(new Constant(label)))));
            } else if (argLabels.getFirst() != null) {
                // TRY label = destructor(arg)
                checks.add(new Triple<>(true, argLabels.getFirst(), new Function(registeredFunction.destructor, List.of(new Constant(label)))));
            }
            this.labelsDone.add(label);
        }
        return checks;
    }

    private String getLabel() {
        // TODO: perhaps a value based ID is needed for uniqueness across frames, e.g. label of crypt(pk(B),pair(msg,M)) is _crypt_pk_B_pair_msg_M
        return "l" + this.counter++;
    }
}