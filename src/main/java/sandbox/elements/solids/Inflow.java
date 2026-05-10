package sandbox.elements.solids;

import sandbox.core.World;
import sandbox.elements.Element;
import sandbox.elements.ElementRegistry;

public class Inflow extends ImmovableSolid {

    public Inflow() {
        super(ElementRegistry.ID.INFLOW, "Inflow", new int[]{220, 80, 80});
    }

    @Override
    public void onSpawn(World world, int x, int y) {
        world.setData(x, y, (byte) ElementRegistry.ID.EMPTY);
    }

    @Override
    public void onRemove(World world, int x, int y) {
        world.setData(x, y, (byte) ElementRegistry.ID.EMPTY);
    }

    @Override
    public void update(World world, int x, int y) {
        int sourceID = world.getData(x, y) & 0xFF;

        if (sourceID == ElementRegistry.ID.EMPTY) {
            outer:
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    int nid = world.get(x + dx, y + dy);
                    if (nid > 0 && nid != ElementRegistry.ID.INFLOW && nid != ElementRegistry.ID.OUTFLOW) {
                        sourceID = nid;
                        world.setData(x, y, (byte) sourceID);
                        break outer;
                    }
                }
            }
        }

        if (sourceID == ElementRegistry.ID.EMPTY) return;

        // Prefer spawning downward so gravity-driven elements flow naturally
        int[][] order = {{0,1},{-1,1},{1,1},{-1,0},{1,0},{0,-1},{-1,-1},{1,-1}};
        for (int[] d : order) {
            int tx = x + d[0], ty = y + d[1];
            if (world.isEmpty(tx, ty)) {
                world.set(tx, ty, sourceID);
                Element def = ElementRegistry.get(sourceID);
                if (def != null) def.onSpawn(world, tx, ty);
                return;
            }
        }
    }
}
