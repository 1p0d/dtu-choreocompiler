import java.util.List;

public enum RegisteredFunction {
    CRYPT("crypt"),
    DCRYPT("dcrypt"),
    DSCRYPT("dscrypt"),
    HASH("h"),
    INV("inv", false),
    MAC("mac"),
    PAIR("pair"),
    PK("pk"),
    SCRYPT("scrypt"),
    SIGN("sign");

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
