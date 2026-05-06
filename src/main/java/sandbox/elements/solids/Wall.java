package sandbox.elements.solids;

import sandbox.elements.ElementRegistry;


public class Wall extends ImmovableSolid {
    public Wall() {
        super(ElementRegistry.ID.WALL, "Wall", new int[]{130, 100, 100});
    }
    
}
