package sandbox;

import sandbox.core.Input;
import sandbox.core.Window;
import sandbox.core.World;
import sandbox.elements.Element;
import sandbox.elements.ElementRegistry;
import sandbox.elements.gasses.Smoke;
import sandbox.elements.gasses.Steam;
import sandbox.elements.liquids.Lava;
import sandbox.elements.liquids.Oil;
import sandbox.elements.liquids.Water;
import sandbox.elements.solids.*;

import java.nio.ByteBuffer;

import sandbox.render.Renderer;
import sandbox.util.geometry.Line;
import sandbox.util.geometry.Circle;

import static org.lwjgl.glfw.GLFW.*;

public class Main {
    static final int SIM_W = 800, SIM_H = 500, SCALE = 2;

    public static void main(String[] args) {
        Window   window   = new Window(SIM_W * SCALE, SIM_H * SCALE, "Particle Sandbox");
        Renderer renderer = new Renderer();
        World    world    = new World(SIM_W, SIM_H);
        renderer.init(SIM_W, SIM_H);

        ElementRegistry.register(new Sand());
        ElementRegistry.register(new Water());
        ElementRegistry.register(new Smoke());
        ElementRegistry.register(new Stone());
        ElementRegistry.register(new Wall());
        ElementRegistry.register(new Inflow());
        ElementRegistry.register(new Outflow());
        ElementRegistry.register(new Oil());
        ElementRegistry.register(new Lava());
        ElementRegistry.register(new Steam());

        Input input           = new Input(window.getHandle());
        int   selectedElement = ElementRegistry.ID.SAND;
        int   brushRadius     = 2;
        int   prevMx          = (int)(input.getMouseX() / SCALE);
        int   prevMy          = (int)(input.getMouseY() / SCALE);

        while (!window.shouldClose()) {
            window.pollEvents();

            // Element selection: keys 1–9
            for (int k = GLFW_KEY_1; k <= GLFW_KEY_9; k++)
                if (input.wasPressed(k)) selectedElement = k - GLFW_KEY_1 + 1;

            // Brush size: = / numpad+ to grow, - / numpad- to shrink
            if (input.wasPressed(GLFW_KEY_EQUAL) || input.wasPressed(GLFW_KEY_KP_ADD))
                brushRadius++;
            if (input.wasPressed(GLFW_KEY_MINUS) || input.wasPressed(GLFW_KEY_KP_SUBTRACT))
                brushRadius = Math.max(1, brushRadius - 1);

            // Reset
            if (input.wasPressed(GLFW_KEY_R)) world.clear();

            // Mouse painting — interpolate between last and current sim position
            int mx = (int)(input.getMouseX() / SCALE);
            int my = (int)(input.getMouseY() / SCALE);
            final int element = selectedElement, radius = brushRadius;
            if (input.isMouseDown(GLFW_MOUSE_BUTTON_LEFT))
                Line.applyForEach(prevMx, prevMy, mx, my, (x, y) -> paintCircle(world, x, y, radius, element));
            if (input.isMouseDown(GLFW_MOUSE_BUTTON_RIGHT)) {
                Line.applyForEach(prevMx, prevMy, mx, my, (x, y) -> paintCircle(world, x, y, radius, ElementRegistry.ID.EMPTY));
            }
            prevMx = mx;
            prevMy = my;

            world.step();
            ByteBuffer pixels = world.buildPixels();
            outlineBrush(pixels, SIM_W, SIM_H, mx, my, brushRadius);
            renderer.uploadPixels(pixels);
            renderer.draw();
            window.swapBuffers();
        }

        world.destroy();
        renderer.destroy();
        window.destroy();
    }

    static void paintCircle(World world, int cx, int cy, int r, int typeId) {
        Element def = ElementRegistry.get(typeId);
        Circle.applyForEach(cx, cy, r, (x, y) -> {
            world.set(x, y, typeId);
            if (def != null) def.onSpawn(world, x, y);
        });
    }

    static void outlineBrush(ByteBuffer pixels, int width, int height, int cx, int cy, int r) {
        Circle.applyForEachOnBorder(cx, cy, r, (x, y) -> {
            if (x >= 0 && x < width && y >= 0 && y < height) {
                int i = (y * width + x) * 4;
                pixels.put(i,     (byte) 255);
                pixels.put(i + 1, (byte) 255);
                pixels.put(i + 2, (byte) 255);
                pixels.put(i + 3, (byte) 255);
            }
        });
    }
}
