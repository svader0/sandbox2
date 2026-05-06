package sandbox.core;

import sandbox.particle.Element;

// Holds all mutable state for one grid position.
// Adding a new per-cell property means adding a field here and nowhere else.
public class Cell {
    public Element type;  // null = empty
    public byte data;
    public byte shade;
    public byte flags;
    public byte temp;
    public byte velX;
    public byte velY;

    void reset() {
        type  = null;
        data  = shade = flags = temp = velX = velY = 0;
    }
}
