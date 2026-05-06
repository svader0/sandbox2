package sandbox.particle;

public class ElementRegistry {

    public static final class ID {
        public static final int EMPTY = 0;
        public static final int SAND = 1;
        public static final int WATER = 2;
        public static final int SMOKE = 3;
        public static final int STONE = 4;
        public static final int PINK_SALT = 5;
    }
    
    private static final Element[] DEFS = new Element[256];

    public static void register(Element def) {
        if (def == null) throw new IllegalArgumentException("def == null");
        if (def.id < 0 || def.id >= DEFS.length) throw new IndexOutOfBoundsException("Element id out of range: " + def.id);
        if (DEFS[def.id] != null) throw new IllegalStateException("Element id already registered: " + def.id);
        DEFS[def.id] = def;
    }

    public static Element get(int id) {
        if (id < 0 || id >= DEFS.length) return null;
        return DEFS[id];
    }
}