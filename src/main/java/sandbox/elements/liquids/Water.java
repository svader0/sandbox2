package sandbox.elements.liquids;

import sandbox.core.World;
import sandbox.elements.ElementRegistry;

public class Water extends Liquid {
    public Water() {
        super(ElementRegistry.ID.WATER, "Water", new int[]{30, 100, 255}, 5, 6);
    }

    @Override
    protected void liquidInteractions(World world, int x, int y) {
        // TODO: Evaporate if above fire, freeze if below ice, etc.
    }
}