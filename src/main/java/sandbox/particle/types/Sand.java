package sandbox.particle.types;

import sandbox.particle.ElementRegistry;
import sandbox.particle.defs.MovableSolid;

public class Sand extends MovableSolid {
    public Sand() {
        super(ElementRegistry.ID.SAND, "Sand", new int[]{194, 178, 128}, 0.01f);
    }
}