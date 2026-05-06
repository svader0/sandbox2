package sandbox.particle.types;

import sandbox.particle.ElementRegistry;
import sandbox.particle.defs.Gas;

public class Smoke extends Gas {
    public Smoke() {
        super(ElementRegistry.ID.SMOKE, "Smoke", new int[]{100, 100, 100}, 500);
    }
    
}
