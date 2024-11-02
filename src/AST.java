public abstract class AST {
    public static void error(String msg) {
        System.err.println("Compilation error: " + msg);
        System.exit(1);
    }
}
