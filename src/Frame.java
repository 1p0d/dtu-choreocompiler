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

    public Frame(Frame other) {
        this.knowledge = new HashMap<>(other.knowledge);
        this.recipes = new HashMap<>(other.recipes);
        this.labelsNew = new ArrayList<>(other.labelsNew);
        this.labelsHold = new ArrayList<>(other.labelsHold);
        this.labelsDone = new ArrayList<>(other.labelsDone);
        this.counter = other.counter;
    }

    /**
     * Adds term to frame knowledge
     * @param term to add
     * @param label to assign
     * @return pair of label assigned and boolean depicting if term is new
     */
    public Pair<Term, Boolean> add(Term term, String label) {
        Term composedTerm = this.compose(term);
        if (composedTerm != null) return new Pair<>(composedTerm, false);
        if (label == null || this.knowledge.containsKey(label)) label = getLabel();
        this.labelsNew.add(label);
        this.knowledge.put(label, term);
        this.labelsNew.addAll(this.labelsHold);
        this.labelsHold.clear();
        return new Pair<>(new Constant(label), true);
    }

    /**
     * Adds term to frame knowledge and auto generates label
     * @param term to add
     * @return pair of label assigned and boolean depicting if term is new
     */
    public Pair<Term, Boolean> add(Term term) {
        return this.add(term, null);
    }

    /**
     * Composes a given term with the current knowledge
     * @param term to compose
     * @return label or function of with labels as args that compose term
     */
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

    /**
     * Analyzes all labels in labelsNew
     * @return List of Checks that are required for all new labels
     */
    public List<Check> analyze() {
        List<Check> checks = new ArrayList<>();
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
            // if function requires key to analyze
            if (RegisteredFunction.CRYPT_FUNCTIONS.contains(registeredFunction) || registeredFunction.equals(RegisteredFunction.SIGN)) {
                Term keyLabel = this.compose(function.getKey());
                // if key cannot be composed, continue
                if (keyLabel == null) {
                    this.labelsHold.add(label);
                    continue;
                }
                Pair<Term, Boolean> addedMessage = this.add(args.getLast());
                checks.add(new Check(addedMessage.b, addedMessage.a, new Function(registeredFunction.destructor,
                        List.of(keyLabel, new Constant(label)))));
            }
            // if function has key
            else if (registeredFunction.hasKey) {
                Pair<Term, Boolean> addedMessage = this.add(args.getLast());
                checks.add(new Check(addedMessage.b, addedMessage.a, new Function(registeredFunction.destructor,
                        List.of(new Constant(label)))));
            }
            // if function is a pair
            else if (registeredFunction.equals(RegisteredFunction.PAIR)) {
                for (int i = 0; i < args.size(); i++) {
                    Pair<Term, Boolean> addedTerm = this.add(args.get(i));
                    checks.add(new Check(addedTerm.b, addedTerm.a,
                            new Function(registeredFunction.destructor + (i + 1), List.of(new Constant(label)))));
                }
            }
            // if function is any other registered function (has one arg)
            else {
                Pair<Term, Boolean> addedTerm = this.add(args.getFirst());
                checks.add(new Check(addedTerm.b, addedTerm.a,
                        new Function(registeredFunction.destructor, List.of(new Constant(label)))));
            }
            this.labelsDone.add(label);
        }
        // return sorted checks (assignment checks first since if/equal checks may depend on them)
        return checks.stream().sorted((a, b) -> a.isAssignment.equals(b.isAssignment) ? 0 : a.isAssignment ? -1 : 1).toList();
    }

    /**
     * Generates a new label
     * @return new string label
     */
    private String getLabel() {
        // TODO: perhaps a value based ID is needed for uniqueness across frames, e.g. label of crypt(pk(B),pair(msg,M)) is _crypt_pk_B_pair_msg_M
        return "l" + this.counter++;
    }
}