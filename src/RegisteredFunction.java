import java.util.List;

public enum RegisteredFunction {
    CRYPT(  "crypt",    "vcrypt",   "dcrypt",   true,   true,   true),
    SCRYPT( "scrypt",   "vscrypt",  "dscrypt",  true,   true,   true),
    SIGN(   "sign",     "vsign",    "open",     true,   true,   true),
    PAIR(   "pair",     "vpair",    "π",        true,   true,   false),
    HASH(   "h",        "h",        null,       true,   false,  false),
    MAC(    "mac",      "mac",      "dmac",       true,   true,   true),
    PK(     "pk",       null,       null,       true,   false,  false),
    INV(    "inv",      null,       null,       false,  false,  false);

    final String name;
    final String verifier;
    final String destructor;
    final boolean global;
    final boolean analyzable;
    final boolean keyed;

    static final List<RegisteredFunction> KEYED_FUNCTIONS = List.of(CRYPT, SCRYPT, SIGN);

    RegisteredFunction(String name, String verifier, String destructor, boolean global, boolean analyzable, boolean keyed) {
        this.name = name;
        this.verifier = verifier;
        this.destructor = destructor;
        this.analyzable = analyzable;
        this.global = global;
        this.keyed = keyed;
    }

    static RegisteredFunction getRegisteredFunction(String name) {
        for (RegisteredFunction registeredFunction : RegisteredFunction.values()) {
            if (registeredFunction.name.equals(name)) {
                return registeredFunction;
            }
        }
        return null;
    }

    static boolean isGlobal(String name) {
        RegisteredFunction registeredFunction = getRegisteredFunction(name);
        return registeredFunction != null && registeredFunction.global;
    }
}
