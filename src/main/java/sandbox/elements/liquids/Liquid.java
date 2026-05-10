package sandbox.elements.liquids;

import sandbox.core.World;
import sandbox.elements.Element;
import sandbox.elements.ElementRegistry;
import sandbox.elements.gasses.Gas;

// Falls under gravity, spreads horizontally, stratifies by density.
// Per-cell state: velY (signed) accumulates fall speed; FLAG_FLOW_DIR persists last
// horizontal preference so settled fluid doesn't jitter back and forth.
public abstract class Liquid extends Element {

    private static final int GRAVITY = 1;
    private static final int MAX_VY  = 6;

    public final int density;
    public final int dispersionRate;

    private final int[][] colorCache = new int[256][3];

    protected Liquid(int id, String name, int[] color, int density, int dispersionRate) {
        super(id, name, color);
        this.density        = density;
        this.dispersionRate = dispersionRate;
        for (int i = 0; i < 256; i++) {
            int v = (byte) i;
            colorCache[i][0] = clamp255(color[0] + v / 4);
            colorCache[i][1] = clamp255(color[1] + v / 4);
            colorCache[i][2] = clamp255(color[2] + v / 4);
        }
    }

    @Override
    public void onSpawn(World world, int x, int y) {
        world.setShade(x, y, (byte)((Math.random() * 30) - 15));
        world.setFlowDir(x, y, Math.random() < 0.5 ? -1 : 1);
    }

    @Override
    public int[] getColor(byte shade) {
        return colorCache[shade & 0xFF];
    }

    @Override
    public void update(World world, int x, int y) {
        if (preUpdateReact(world, x, y)) return;

        int cx = x, cy = y;
        int vy = world.getVy(cx, cy);
        vy = Math.min(vy + GRAVITY, MAX_VY);

        // 1. Multi-step vertical fall through empty/gas/lighter-liquid.
        for (int step = 0; step < vy; step++) {
            if (canFlowInto(world, cx, cy + 1)) {
                world.swap(cx, cy, cx, cy + 1);
                cy++;
            } else {
                vy = 0;
                break;
            }
        }

        // 2. If blocked vertically, sweep horizontally up to dispersionRate, preferring
        //    the cached flow direction. If the scan finds a cell with empty space below,
        //    move into that cell AND drop into it the same frame (one-frame cascade).
        //    Diagonal-down at distance 1 is just a drop-on-the-first-step case.
        if (cy == y) {
            int dir = world.getFlowDir(cx, cy);
            long packed = scanSpread(world, cx, cy, dir);
            int reach = unpackReach(packed);
            if (reach == 0) {
                dir = -dir;
                packed = scanSpread(world, cx, cy, dir);
                reach = unpackReach(packed);
                world.setFlowDir(cx, cy, dir);
            }
            if (reach > 0) {
                boolean drop = unpackDrop(packed);
                int nx = cx + dir * reach;
                int ny = cy + (drop ? 1 : 0);
                world.swap(cx, cy, nx, ny);
                world.markMoved(nx, ny);
                cx = nx;
                cy = ny;
            }
        }

        world.setVy(cx, cy, vy);
        onMoved(world, x, y, cx, cy);
    }

    // Returns true if target (tx, ty) accepts inflow: empty, gas, or strictly lighter liquid.
    protected boolean canFlowInto(World world, int tx, int ty) {
        if (!world.inBounds(tx, ty)) return false;
        int id = world.get(tx, ty);
        if (id == ElementRegistry.ID.EMPTY) return true;
        Element other = ElementRegistry.get(id);
        if (other instanceof Gas) return true;
        if (other instanceof Liquid) return ((Liquid) other).density < this.density;
        return false;
    }

    // Pressure-aware horizontal scan.
    //
    // Cells under column pressure (same liquid directly above) may TUNNEL through
    // same-liquid neighbors up to `dispersionRate * 4` cells, modelling the way a
    // pressurized fluid pushes laterally toward any open slot. Without pressure
    // (lone surface cells, top-row of a column), tunneling is disabled and only
    // ordinary dispersion applies. This stops flat pools from teleporting cells
    // around while letting tall columns equalize quickly.
    //
    // Returns reach + drop-available flag packed into a long: bit 0 = drop, bits 1+ = reach.
    private long scanSpread(World world, int cx, int cy, int dir) {
        boolean pressured = world.inBounds(cx, cy - 1) && world.get(cx, cy - 1) == this.id;
        int budget = pressured ? dispersionRate * 4 : dispersionRate;

        int reach = 0;
        boolean drop = false;
        for (int n = 1; n <= budget; n++) {
            int tx = cx + dir * n;
            if (!world.inBounds(tx, cy)) break;
            int id = world.get(tx, cy);
            if (id == this.id) {
                if (pressured) continue;   // tunnel through column-mate
                else break;                // no pressure: same-liquid blocks
            }
            if (!canFlowInto(world, tx, cy)) break;
            reach = n;
            if (canFlowInto(world, tx, cy + 1)) { drop = true; break; }
        }
        return ((long) reach << 1) | (drop ? 1L : 0L);
    }

    private static int     unpackReach(long p) { return (int) (p >> 1); }
    private static boolean unpackDrop (long p) { return (p & 1L) != 0; }

    // Subclass hook: react to neighbors BEFORE moving. Return true to abort movement
    // (e.g. lava convertedto stone). Default: no-op.
    protected boolean preUpdateReact(World world, int x, int y) { return false; }

    // Subclass hook: called after movement settles, with original and new coords.
    protected void onMoved(World world, int fromX, int fromY, int toX, int toY) {}

    private static int clamp255(int v) { return v < 0 ? 0 : (v > 255 ? 255 : v); }
}
