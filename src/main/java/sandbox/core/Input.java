package sandbox.core;

import static org.lwjgl.glfw.GLFW.*;

public class Input {
    private final long window;
    private final boolean[] justPressed = new boolean[GLFW_KEY_LAST + 1];
    private double mouseX, mouseY;

    public Input(long windowHandle) {
        this.window = windowHandle;
        glfwSetKeyCallback(windowHandle, (win, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS && key >= 0 && key <= GLFW_KEY_LAST)
                justPressed[key] = true;
        });
        glfwSetCursorPosCallback(windowHandle, (win, x, y) -> {
            mouseX = x;
            mouseY = y;
        });
    }

    public boolean wasPressed(int key) {
        if (key < 0 || key > GLFW_KEY_LAST) return false;
        boolean v = justPressed[key];
        justPressed[key] = false;
        return v;
    }

    public boolean isMouseDown(int button) {
        return glfwGetMouseButton(window, button) == GLFW_PRESS;
    }

    public double getMouseX() { return mouseX; }
    public double getMouseY() { return mouseY; }
}
