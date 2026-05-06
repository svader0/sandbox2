package sandbox.util.geometry;

import java.util.function.BiConsumer;

public class Circle {
    public static void applyForEach(int cx, int cy, int r, BiConsumer<Integer, Integer> fn) {
        for (int dy = -r; dy <= r; dy++)
            for (int dx = -r; dx <= r; dx++)
                if (dx * dx + dy * dy <= r * r)
                    fn.accept(cx + dx, cy + dy);
    }

    public static void applyForEachOnBorder(int cx, int cy, int r, BiConsumer<Integer, Integer> fn) {
        int r2 = r * r, r1 = (r - 1) * (r - 1);
        for (int dy = -r; dy <= r; dy++)
            for (int dx = -r; dx <= r; dx++) {
                int d2 = dx * dx + dy * dy;
                if (d2 <= r2 && d2 >= r1)
                    fn.accept(cx + dx, cy + dy);
            }
    }
}
