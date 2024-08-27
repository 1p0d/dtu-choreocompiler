public enum RegisteredFunction {
    CRYPT("crypt"),
    DCRYPT("dcrypt"),
    PAIR("pair"),
    MAC("mac");

    private final String name;

    RegisteredFunction(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
