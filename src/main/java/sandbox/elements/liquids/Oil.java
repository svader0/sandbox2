package sandbox.elements.liquids;

import sandbox.elements.ElementRegistry;

public class Oil extends Liquid {
    public Oil() {
        super(ElementRegistry.ID.OIL, "Oil", new int[]{80, 60, 40}, 60, 5);
    }
}
