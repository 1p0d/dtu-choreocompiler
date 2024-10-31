import org.antlr.v4.runtime.misc.Pair;

import java.util.Objects;

public class ChoiceCheckNode {
    Check check;
    Pair<Frame, Choreo> agentPair;

    public ChoiceCheckNode(Check check) {
        this.check = check;
        this.agentPair = null;
    }

    public ChoiceCheckNode(Pair<Frame, Choreo> agentPair) {
        this.check = null;
        this.agentPair = agentPair;
    }

    public ChoiceCheckNode() {
        this.check = null;
        this.agentPair = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChoiceCheckNode that = (ChoiceCheckNode) o;
        return Objects.equals(check, that.check) && Objects.equals(agentPair, that.agentPair);
    }

    @Override
    public int hashCode() {
        return Objects.hash(check, agentPair);
    }

    @Override
    public String toString() {
        if (check != null)
            return check.toString();
        if (agentPair != null)
            return agentPair.toString();
        return "ROOT";
    }
}
