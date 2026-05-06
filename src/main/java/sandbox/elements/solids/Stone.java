package sandbox.elements.solids;

import sandbox.elements.ElementRegistry;

public class Stone extends MovableSolid {
    public Stone() {
        super(ElementRegistry.ID.STONE, "Stone", new int[]{50,50,50}, 0.6f, 0.5f);
    }


}
