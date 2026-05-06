package sandbox.elements.liquids;

import sandbox.core.World;
import sandbox.elements.Element;

// Flows and spreads — water, lava, acid etc.
public abstract class Liquid extends Element {
    public final int density;   // higher sinks through lower
    public int dispersionRate;

    protected Liquid(int id, String name, int[] color, int density, int dispersionRate) {
        super(id, name, color);
        this.density        = density;
        this.dispersionRate = dispersionRate;
    }

    @Override
    public void update(World world, int x, int y) {
        // Try to fall down
        if (world.isEmpty(x, y + 1)) {
            world.swap(x, y, x, y + 1);
            return;
        }

        boolean goLeft;
        if (world.isEmpty(x - 1, y + 1) && world.isEmpty(x + 1, y + 1)) {
            goLeft = Math.random() > 0.5;
        } else if (world.isEmpty(x - 1, y + 1)) {
            goLeft = true;
        } else if (world.isEmpty(x + 1, y + 1)) {
            goLeft = false;
        } else {
            goLeft = Math.random() > 0.5; // no downward path, but still randomize left/right flow
        }

        for (int i = 1; i <= dispersionRate; i++) {
            int dx = goLeft ? -i : i;
            if (world.isEmpty(x + dx, y)) {
                world.swap(x, y, x + dx, y);
                return;
            }
        }


        liquidInteractions(world, x, y);
    }

    // Subclasses override this for reactions
    protected void liquidInteractions(World world, int x, int y) {}
}