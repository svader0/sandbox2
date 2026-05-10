package sandbox.elements.liquids;

import sandbox.core.World;
import sandbox.elements.Element;
import sandbox.elements.ElementRegistry;

public class Lava extends Liquid {

    private static final int[][] NEIGHBORS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    public Lava() {
        super(ElementRegistry.ID.LAVA, "Lava", new int[]{220, 90, 30}, 200, 2);
    }

    @Override
    public void onSpawn(World world, int x, int y) {
        super.onSpawn(world, x, y);
        world.setTemp(x, y, (byte) 100);
    }

    @Override
    protected boolean preUpdateReact(World world, int x, int y) {
        // Flicker: re-roll shade some frames so lava looks restless.
        if (Math.random() < 0.05) world.setShade(x, y, (byte)((Math.random() * 60) - 30));

        for (int[] d : NEIGHBORS) {
            int nx = x + d[0], ny = y + d[1];
            if (!world.inBounds(nx, ny)) continue;
            if (world.get(nx, ny) == ElementRegistry.ID.WATER) {
                world.set(nx, ny, ElementRegistry.ID.STEAM);
                Element steam = ElementRegistry.get(ElementRegistry.ID.STEAM);
                if (steam != null) steam.onSpawn(world, nx, ny);
                world.set(x, y, ElementRegistry.ID.STONE);
                Element stone = ElementRegistry.get(ElementRegistry.ID.STONE);
                if (stone != null) stone.onSpawn(world, x, y);
                return true;
            }
        }
        return false;
    }
}
