import java.util.Objects;

public class Check {
    Boolean isAssignment;
    Term left;
    Term right;

    public Check(Boolean isAssignment, Term left, Term right) {
        this.isAssignment = isAssignment;
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Check check = (Check) o;
        return Objects.equals(isAssignment, check.isAssignment) && Objects.equals(left, check.left) && Objects.equals(right, check.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isAssignment, left, right);
    }

    @Override
    public String toString() {
        if (isAssignment == null) return "null";
        return (isAssignment ? "try " : "if ") + "(" + left + " = " + right + ")";
    }
}
