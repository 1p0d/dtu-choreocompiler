public enum RegisteredFunction {
    CRYPT("crypt"),
    SCRYPT("scrypt"),
    DCRYPT("dcrypt"),
    PAIR("pair"),
    MAC("mac"),
    INV("inv", false),
    HASH("h");

    final String name;
    final boolean global;

    RegisteredFunction(String name, boolean global) {
        this.name = name;
        this.global = global;
    }

    RegisteredFunction(String name) {
        this.name = name;
        this.global = true;
    }

    static boolean isGlobal(String name) {
        for (RegisteredFunction r : RegisteredFunction.values()) {
            if (r.name.equals(name)) {
                return r.global;
            }
        }
        return false;
    }
}
