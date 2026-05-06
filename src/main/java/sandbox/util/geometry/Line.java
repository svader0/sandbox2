package sandbox.util.geometry;

public class Line {
    // Applies some function to each point along a line between (x0, y0) and (x1, y1) using
    // Bresenham's line algorithm.
    public static void applyForEach(int x0, int y0, int x1, int y1, java.util.function.BiConsumer<Integer, Integer> func) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            func.accept(x0, y0); // Apply the function to the current point

            if (x0 == x1 && y0 == y1) break;
            int err2 = err * 2;
            if (err2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (err2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
    }
}