package sandbox.elements.solids;

import sandbox.core.World;
import sandbox.elements.ElementRegistry;

public class Outflow extends ImmovableSolid {

    public Outflow() {
        super(ElementRegistry.ID.OUTFLOW, "Outflow", new int[]{60, 100, 220});
    }

    @Override
    public void update(World world, int x, int y) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                int tx = x + dx, ty = y + dy;
                int id = world.get(tx, ty);
                if (id > 0
                        && id != ElementRegistry.ID.INFLOW
                        && id != ElementRegistry.ID.OUTFLOW
                        && id != ElementRegistry.ID.WALL
                        && id != ElementRegistry.ID.STONE) {
                    world.set(tx, ty, ElementRegistry.ID.EMPTY);
                }
            }
        }
    }
}
