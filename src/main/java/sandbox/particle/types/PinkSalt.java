package sandbox.particle.types;

import sandbox.particle.defs.MovableSolid;
import sandbox.particle.ElementRegistry;

public class PinkSalt extends MovableSolid {
    public PinkSalt() {
        super(ElementRegistry.ID.PINK_SALT, "Pink Salt", new int[]{255, 105, 180}, 0.9f);
    }
    
}
