package sandbox.elements.solids;

import sandbox.core.World;
import sandbox.elements.Element;

public class ImmovableSolid extends Element {
    public ImmovableSolid(int id, String name, int[] color) {
        super(id, name, color);
    }

    @Override
    public void update(World world, int x, int y) {
        // immovable solid — do nothing
    }
    
}
