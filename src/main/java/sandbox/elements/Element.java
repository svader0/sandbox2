package sandbox.elements;

import sandbox.core.World;

public abstract class Element {
    public final int    id;
    public final String name;
    public final int[]  color; // RGB as a 3-length array

    public int flamabilityResistance = 100;

    protected Element(int id, String name, int[] color) {
        this.id    = id;
        this.name  = name;
        this.color = color;
    }

    public abstract void update(World world, int x, int y);

    // Override to return a per-cell color variant using the cell's data[] value.
    // Default ignores cellData and returns the base color.
    public int[] getColor(byte cellData) { return color; }

    public boolean receiveHeat(World world, int x, int y, int amount) {
        if (world.isIgnited(x, y)) {
            return false; // already ignited! can't be even hotter.
        }

        this.flamabilityResistance -= (int)(amount * Math.random()); // add some randomness to flamability

        if (this.flamabilityResistance <= 0) {
            world.setIgnited(x, y, true);
            return true; // ignited!
        }
        return false; // not ignited yet
        
    }

    // Called when the user places this element; override to set initial data[] values etc.
    public void onSpawn(World world, int x, int y) {}

    // Called when this element is removed (replaced by another); override to e.g. spawn new elements based on cell data.
    public void onRemove(World world, int x, int y) {}

    // Optional — override to add neighbor reaction logic
    public void onNeighbor(World world, int x, int y, int nx, int ny) {}
}