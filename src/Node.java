import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * This class is used for creating trees in which each node only contains unique children. Therefore, a Set is used.
 * @param <T> payload type
 */
public class Node<T> {
    T payload;
    Set<Node<T>> children;

    public Node(T payload) {
        this.payload = payload;
        this.children = new HashSet<>();
    }

    public Node<T> addChild(Node<T> child) {
        if (this.children.add(child)) return child;
        for (Node<T> existingChild : this.children)
            if (existingChild.equals(child)) return existingChild;
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node<?> node = (Node<?>) o;
        return Objects.equals(payload, node.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payload);
    }

    public String toString(Integer depth) {
        StringBuilder sb = new StringBuilder();
        sb.append("\t".repeat(Math.max(0, depth))).append(this.payload).append(" (").append(this.children.size()).append(" children)\n");
        for (Node<T> child : this.children)
            sb.append(child.toString(depth + 1));
        return sb.toString();
    }

    @Override
    public String toString() {
        return this.toString(0);
    }
}
