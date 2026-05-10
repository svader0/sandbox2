package sandbox.elements.liquids;

import sandbox.elements.ElementRegistry;

public class Water extends Liquid {
    public Water() {
        super(ElementRegistry.ID.WATER, "Water", new int[]{60, 130, 220}, 100, 8);
    }
}
