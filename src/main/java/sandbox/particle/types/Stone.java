package sandbox.particle.types;

import sandbox.particle.ElementRegistry;
import sandbox.particle.defs.ImmovableSolid;


public class Stone extends ImmovableSolid {
    public Stone() {
        super(ElementRegistry.ID.STONE, "Stone", new int[]{100, 100, 100});
    }
    
}
