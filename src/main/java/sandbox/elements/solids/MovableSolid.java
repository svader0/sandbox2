package sandbox.elements.solids;

import sandbox.core.World;
import sandbox.elements.Element;
import sandbox.elements.ElementRegistry;
import sandbox.elements.liquids.Liquid;

// Falls and piles — sand, salt, gunpowder etc.
public abstract class MovableSolid extends Element {

    private static final int   GRAVITY    = 1;
    private static final int   MAX_VY     = 6;
    private static final int   MAX_VX     = 5;
    private static final float SPLASH_MAX = 0.7f;

    // Probability (0–1) that a passing particle dislodges this one from rest.
    protected final float inertialResistance;

    // 0 = frictionless (slides forever), 1 = instant stop.
    protected final float friction;

    // pre-computes all the possible shade variations to avoid doing the math every frame
    private final int[][] colorCache = new int[256][3];

    protected MovableSolid(int id, String name, int[] color, float inertialResistance, float friction) {
        super(id, name, color);
        this.inertialResistance = inertialResistance;
        this.friction = friction;
        for (int i = 0; i < 256; i++) {
            int v = (byte) i;
            colorCache[i][0] = Math.max(0, Math.min(255, color[0] + v));
            colorCache[i][1] = Math.max(0, Math.min(255, color[1] + v));
            colorCache[i][2] = Math.max(0, Math.min(255, color[2] + v));
        }
    }

    @Override
    public void onSpawn(World world, int x, int y) {
        world.setShade(x, y, (byte)((Math.random() * 40) - 20));
        world.setFalling(x, y, true);
    }

    @Override
    public int[] getColor(byte shade) {
        return colorCache[shade & 0xFF];
    }

    @Override
    public void update(World world, int x, int y) {
        boolean wasFalling = world.isFalling(x, y);
        int cx = x, cy = y;

        if (!wasFalling) {
            if (canFallInto(world, cx, cy + 1)) {
                world.swap(cx, cy, cx, cy + 1);
                cy++;
                world.setFalling(cx, cy, true);
                world.setVy(cx, cy, 1);
            }
            return;
        }

        int vy = Math.min(world.getVy(x, y) + GRAVITY, MAX_VY);
        int vx = world.getVx(x, y);
        boolean moved = false;

        // Multi-step vertical fall — wake up neighbors at each level passed
        for (int step = 0; step < vy; step++) {
            if (canFallInto(world, cx, cy + 1)) {
                world.swap(cx, cy, cx, cy + 1);
                cy++;
                tryDislodge(world, cx - 1, cy);
                tryDislodge(world, cx + 1, cy);
                moved = true;
            } else {
                // Impact: wake up neighbors adjacent to the collision point
                tryDislodge(world, cx - 1, cy + 1);
                tryDislodge(world, cx + 1, cy + 1);

                int   impactVy = vy - step;
                float splashF  = impactVy * (float)(Math.random() * SPLASH_MAX);
                int   splash   = (int) splashF;
                if (Math.random() < splashF - splash) splash++;
                if (splash > 0) {
                    int dir = (Math.random() > 0.5) ? 1 : -1;
                    vx = clamp(vx + dir * splash, -MAX_VX, MAX_VX);
                }
                vy = 0;
                break;
            }
        }

        // Diagonal settle — slide distance is derived from inertialResistance so the
        // single parameter controls both dislodge probability and angle of repose.
        // Low resistance → slides far to find a drop → shallow pile.
        // High resistance → only checks immediate diagonal → steep pile.
        if (vy == 0 && vx == 0 && Math.random() >= inertialResistance) {
            int maxSlide = Math.max(1, Math.round((1f - friction) * 8));
            boolean goLeft = Math.random() > 0.5;
            int d1 = goLeft ? -1 : 1;
            int slide = findSlide(world, cx, cy, d1, maxSlide);
            if (slide == 0) slide = findSlide(world, cx, cy, -d1, maxSlide);
            if (slide != 0) {
                world.swap(cx, cy, cx + slide, cy + 1);
                cx += slide; cy++;
                tryDislodge(world, cx - 1, cy);
                tryDislodge(world, cx + 1, cy);
                moved = true;
            }
        }

        // Horizontal slide
        if (vx != 0) {
            int dir   = vx > 0 ? 1 : -1;
            int steps = Math.abs(vx);
            for (int step = 0; step < steps; step++) {
                int nx = cx + dir;
                if (world.isEmpty(nx, cy)) {
                    tryDislodge(world, nx, cy + 1);
                    world.swap(cx, cy, nx, cy);
                    cx = nx;
                    moved = true;
                } else {
                    vx = 0;
                    break;
                }
            }
            float fVx = vx * (1f - friction);
            int   sign = fVx >= 0 ? 1 : -1;
            int   mag  = (int) Math.abs(fVx);
            if (Math.random() < Math.abs(fVx) - mag) mag++;
            vx = sign * mag;

            if (cx != x || cy != y) world.markMoved(cx, cy);
        }

        world.setFalling(cx, cy, moved);
        world.setVy(cx, cy, vy);
        world.setVx(cx, cy, vx);
    }

    // Scans up to maxSlide cells sideways at height cy for a position with an open
    // drop below. Requires all intermediate cells at cy to be clear (no wall hopping).
    private int findSlide(World world, int cx, int cy, int dir, int maxSlide) {
        for (int n = 1; n <= maxSlide; n++) {
            if (n > 1 && !world.isEmpty(cx + dir * (n - 1), cy)) return 0;
            if (canFallInto(world, cx + dir * n, cy + 1)) return dir * n;
        }
        return 0;
    }

    private void tryDislodge(World world, int x, int y) {
        if (!world.inBounds(x, y)) return;
        Element def = ElementRegistry.get(world.get(x, y));
        if (!(def instanceof MovableSolid)) return;
        if (world.isFalling(x, y)) return;
        if (Math.random() < ((MovableSolid) def).inertialResistance) return;
        world.setFalling(x, y, true);
    }

    private boolean canFallInto(World world, int x, int y) {
        if (world.isEmpty(x, y)) return true;
        Element def = ElementRegistry.get(world.get(x, y));
        return def instanceof Liquid && ((Liquid) def).density < 10;
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
