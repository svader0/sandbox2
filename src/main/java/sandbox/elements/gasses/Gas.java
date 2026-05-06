package sandbox.elements.gasses;

import sandbox.core.World;
import sandbox.elements.Element;
import sandbox.elements.ElementRegistry;

// Rises and disperses — steam, smoke, fire etc.
public abstract class Gas extends Element {
    public final int lifetime;

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
        // Velocity-based rise + spread behavior inspired by the provided CellularAutomaton.step()
        final int GRAVITY = 1;     // gas 'gravity' is upwards (we subtract)
        final int MAX_VY  = 6;
        final int MAX_VX  = 5;
        final float DAMPING = 0.9f;

        int life = Byte.toUnsignedInt(world.getData(x, y));
        if (life == 0) {
            world.set(x, y, 0);
            return;
        }

        // Read stored velocities. Vx is signed via getVx(); Vy stored as byte but getVy() returns unsigned,
        // so convert to signed range manually.
        int vx = world.getVx(x, y); // signed
        int vyu = world.getVy(x, y); // unsigned 0..255
        int vy = (vyu & 0x80) != 0 ? vyu - 256 : vyu; // signed vy (-128..127)

        // Apply 'anti-gravity' (gas rises)
        vy = Math.max(vy - GRAVITY, -MAX_VY);
        // Occasionally give a burst of upward velocity when reaching terminal rise
        if (vy == -MAX_VY && Math.random() > 0.7) {
            vy = -Math.max(1, MAX_VY / 2);
        }

        // Horizontal damping
        vx = (int)Math.round(vx * DAMPING);

        // Compute integer movement steps from velocities
        int absVx = Math.abs(vx);
        int absVy = Math.abs(vy);
        boolean xIsLarger = absVx > absVy;
        int upper = Math.max(absVx, absVy);
        int lower = Math.min(absVx, absVy);
        float slope = (lower == 0 || upper == 0) ? 0f : ((float)(lower + 1) / (upper + 1));

        int lastValidX = x, lastValidY = y;
        boolean moved = false;

        for (int i = 1; i <= Math.max(1, upper); i++) {
            int smaller = (int)Math.floor(i * slope);
            int xInc, yInc;
            if (xIsLarger) {
                xInc = i;
                yInc = smaller;
            } else {
                yInc = i;
                xInc = smaller;
            }

            int modY = y + (yInc * (vy < 0 ? -1 : 1));
            int modX = x + (xInc * (vx < 0 ? -1 : 1));

            if (!world.inBounds(modX, modY)) {
                // reached edge — remove particle
                world.set(x, y, ElementRegistry.ID.EMPTY);
                return;
            }

            int nid = world.get(modX, modY);
            if (nid == ElementRegistry.ID.EMPTY) {
                // move there
                world.swap(x, y, modX, modY);
                lastValidX = modX; lastValidY = modY;
                moved = true;
                break;
            }

            if (nid == this.id) {
                // same kind of gas — keep searching
                lastValidX = modX; lastValidY = modY;
                continue;
            }

            // Occupied by some other element — stop movement here
            break;
        }

        // Update life and velocities at destination/current cell
        if (moved) {
            world.setVx(lastValidX, lastValidY, Math.max(-MAX_VX, Math.min(MAX_VX, vx)));
            world.setVy(lastValidX, lastValidY, vy);
            world.markMoved(lastValidX, lastValidY);
        } else {
            // Not moved: small random lateral diffusion
            if (Math.random() < 0.25) {
                int dx = Math.random() > 0.5 ? 1 : -1;
                if (world.isEmpty(x + dx, y)) {
                    world.swap(x, y, x + dx, y);
                    world.markMoved(x + dx, y);
                    world.setVx(x + dx, y, dx);
                    world.setVy(x + dx, y, 0);
                    moved = true;
                }
            }
        }

        // Apply lifespan decrement
        world.setData(moved ? lastValidX : x, moved ? lastValidY : y, (byte)(life - 1));
    }
}