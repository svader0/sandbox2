package sandbox.core;

import java.nio.ByteBuffer;
import org.lwjgl.system.MemoryUtil;

import sandbox.particle.ElementRegistry;

public class World {
    public final int width, height;

    // Flat array of pre-allocated Cell objects, indexed by (y * width + x).
    // We avoid allocations during the simulation step by reusing Cell objects and swapping references.
    private final Cell[] grid;

    private final ByteBuffer pixels;
    private static final int[] COLOR_EMPTY     = {25, 25, 25};

    public World(int width, int height) {
        this.width  = width;
        this.height = height;
        grid = new Cell[width * height];
        for (int i = 0; i < grid.length; i++) grid[i] = new Cell();
        pixels = MemoryUtil.memAlloc(width * height * 4);
    }

    // ------------------------------------------------------------------
    // Grid access
    // ------------------------------------------------------------------

    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    // Returns the element type ID at (x, y), or 0 (EMPTY) if out of bounds or empty.
    public int get(int x, int y) {
        if (!inBounds(x, y)) return -1;
        Cell c = grid[y * width + x];
        return c.type == null ? ElementRegistry.ID.EMPTY : c.type.id;
    }

    // Sets the cell type by ID. Passing EMPTY (0) resets all fields.
    public void set(int x, int y, int typeId) {
        if (!inBounds(x, y)) return;
        Cell c = grid[y * width + x];
        if (typeId == ElementRegistry.ID.EMPTY) {
            c.reset();
        } else {
            c.type = ElementRegistry.get(typeId);
        }
    }

    public boolean isEmpty(int x, int y) {
        return inBounds(x, y) && grid[y * width + x].type == null;
    }

    // Moves a particle by swapping the Cell references at two grid positions.
    // All state (velocity, data, shade, …) travels with the Cell object.
    public void swap(int ax, int ay, int bx, int by) {
        if (!inBounds(ax, ay) || !inBounds(bx, by)) return;
        int ai = ay * width + ax, bi = by * width + bx;
        Cell tmp = grid[ai]; grid[ai] = grid[bi]; grid[bi] = tmp;
    }

    // ------------------------------------------------------------------
    // Flags (bit-packed booleans inside Cell.flags)
    // ------------------------------------------------------------------

    public static final int FLAG_IGNITED = 0; // bit 0
    public static final int FLAG_MOVED   = 1; // bit 1 — cleared each step; prevents double-processing lateral slides
    public static final int FLAG_FALLING = 2; // bit 2 — persists; true if the particle moved last frame

    public boolean getFlag(int x, int y, int bit) {
        if (!inBounds(x, y)) return false;
        return (grid[y * width + x].flags & (1 << bit)) != 0;
    }

    public void setFlag(int x, int y, int bit, boolean val) {
        if (!inBounds(x, y)) return;
        Cell c = grid[y * width + x];
        c.flags = val ? (byte)(c.flags | (1 << bit)) : (byte)(c.flags & ~(1 << bit));
    }

    public boolean isIgnited(int x, int y)          { return getFlag(x, y, FLAG_IGNITED); }
    public void setIgnited(int x, int y, boolean v) { setFlag(x, y, FLAG_IGNITED, v); }
    public void markMoved(int x, int y)             { setFlag(x, y, FLAG_MOVED, true); }
    public boolean isFalling(int x, int y)          { return getFlag(x, y, FLAG_FALLING); }
    public void setFalling(int x, int y, boolean v) { setFlag(x, y, FLAG_FALLING, v); }

    // ------------------------------------------------------------------
    // Per-cell data accessors (delegate to Cell fields)
    // ------------------------------------------------------------------

    public byte getData(int x, int y)          { return inBounds(x, y) ? grid[y*width+x].data  : 0; }
    public void setData(int x, int y, byte d)  { if (inBounds(x, y)) grid[y*width+x].data  = d; }

    public byte getShade(int x, int y)         { return inBounds(x, y) ? grid[y*width+x].shade : 0; }
    public void setShade(int x, int y, byte s) { if (inBounds(x, y)) grid[y*width+x].shade = s; }

    public byte getTemp(int x, int y)          { return inBounds(x, y) ? grid[y*width+x].temp  : 0; }
    public void setTemp(int x, int y, byte t)  { if (inBounds(x, y)) grid[y*width+x].temp  = t; }

    public int  getVx(int x, int y)            { return inBounds(x, y) ? grid[y*width+x].velX          : 0; } // byte auto-sign-extends to int
    public void setVx(int x, int y, int v)     { if (inBounds(x, y)) grid[y*width+x].velX = (byte) v; }
    public int  getVy(int x, int y)            { return inBounds(x, y) ? grid[y*width+x].velY & 0xFF   : 0; } // unsigned
    public void setVy(int x, int y, int v)     { if (inBounds(x, y)) grid[y*width+x].velY = (byte) v; }

    // ------------------------------------------------------------------
    // Simulation step
    // ------------------------------------------------------------------

    public void step() {
        // Clear FLAG_MOVED before the sweep so lateral slides don't get processed twice.
        int mask = ~(1 << FLAG_MOVED);
        for (Cell c : grid) c.flags = (byte)(c.flags & mask);

        for (int y = height - 1; y >= 0; y--) {
            boolean leftToRight = (y & 1) == 0;
            int xStart = leftToRight ? 0 : width - 1;
            int xEnd   = leftToRight ? width : -1;
            int xStep  = leftToRight ? 1 : -1;

            for (int x = xStart; x != xEnd; x += xStep) {
                Cell c = grid[y * width + x];
                if (c.type == null) continue;
                if ((c.flags & (1 << FLAG_MOVED)) != 0) continue;
                c.type.update(this, x, y);
            }
        }
    }

    // ------------------------------------------------------------------
    // Pixel buffer for renderer
    // ------------------------------------------------------------------

    public ByteBuffer buildPixels() {
        pixels.clear();
        for (Cell c : grid) {
            int[] color = c.type != null ? c.type.getColor(c.shade) : COLOR_EMPTY;
            pixels.put((byte) color[0]);
            pixels.put((byte) color[1]);
            pixels.put((byte) color[2]);
            pixels.put((byte) 255);
        }
        pixels.flip();
        return pixels;
    }

    public void clear() {
        for (Cell c : grid) c.reset();
    }

    public void destroy() {
        MemoryUtil.memFree(pixels);
    }
}
