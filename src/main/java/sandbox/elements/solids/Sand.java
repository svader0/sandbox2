package sandbox.elements.solids;

import sandbox.elements.ElementRegistry;

public class Sand extends MovableSolid {
    public Sand() {
        super(ElementRegistry.ID.SAND, "Sand", new int[]{194, 178, 128}, 0.05f, 0.2f);
    }
}