package engine;

import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class MouseListener {
    private static MouseListener instance;
    private double ScrollX, ScrollY;
    private double xPos, yPos, lastX, lastY;
    private boolean mouseButtonPressed[] = new boolean[3];
    private boolean isDragging;

    private MouseListener() {
        ScrollX = 0.0;
        ScrollY = 0.0;
        xPos = 0.0;
        yPos = 0.0;
        lastX = 0.0;
        lastY = 0.0;
    }

    public static MouseListener get() {
        if (MouseListener.instance == null) {
            MouseListener.instance = new MouseListener();
        }
        return MouseListener.instance;
    }

    public static void mousePosCallback(long window, double xpos, double ypos) {
        get().lastX = get().xPos;
        get().lastY = get().yPos;
        get().xPos = xpos;
        get().yPos = ypos;
        get().isDragging = mouseButtonDown(0) || mouseButtonDown(1) || mouseButtonDown(2);
    }

    public static void mouseButtonCallback(long window, int button, int action, int mods) {
        if (action == GLFW_PRESS) {
            if (button < get().mouseButtonPressed.length) {
                get().mouseButtonPressed[button] = true;
            }
        } else if (action == GLFW_RELEASE) {
            if (button < get().mouseButtonPressed.length) {
                get().mouseButtonPressed[button] = false;
                get().isDragging = false;
            }
        }
    }

    public static void mouseScrollCallback(long window, double xOffset, double yOffset) {
        get().ScrollX = xOffset;
        get().ScrollY = yOffset;
    }

    public static void endFrame() {
        get().ScrollX = 0;
        get().ScrollY = 0;
        get().lastX = get().xPos;
        get().lastY = get().yPos;
    }

    public static float getX() {
        return (float) get().xPos;
    }
    public static float getY() {
        return (float) get().yPos;
    }
    public static float getDx() {
        return (float) (get().lastX - get().xPos);
    }
    public static float getDy() {
        return (float) (get().lastY - get().yPos);
    }
    public static float getScrollX() {
        return (float) get().ScrollX;
    }
    public static float getScrollY() {
        return (float) get().ScrollY;
    }
    public static boolean isDragging() {
        return get().isDragging;
    }
    public static boolean mouseButtonDown(int button) {
        if (button < get().mouseButtonPressed.length) {
            return get().mouseButtonPressed[button];
        }
        return false;
    }

    public static Vector2f getDelta() {
        return new Vector2f(getDx(), getDy());
    }
}
