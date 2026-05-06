package sandbox.particle.defs;

import sandbox.core.World;
import sandbox.particle.Element;

// Rises and disperses — steam, smoke, fire etc.
public abstract class Gas extends Element {
    public final int lifetime; // how many ticks before it disappears

    protected Gas(int id, String name, int[] color, int lifetime) {
        super(id, name, color);
        this.lifetime = lifetime;
    }

    @Override
    public void onSpawn(World world, int x, int y) {
        world.setData(x, y, (byte) lifetime);
    }

    @Override
    public void update(World world, int x, int y) {
        int life = Byte.toUnsignedInt(world.getData(x, y));
        if (life == 0) {
            world.set(x, y, 0);
            return;
        }
        world.setData(x, y, (byte)(life - 1));
    }
}