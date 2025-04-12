package engine;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class KeyListener {
    private static KeyListener instance;
    private boolean[] keyPressed = new boolean[350];
    private boolean[] keyPressedLastFrame = new boolean[350];

    private KeyListener() {
    }

    public static KeyListener get() {
        if (KeyListener.instance == null) {
            KeyListener.instance = new KeyListener();
        }
        return KeyListener.instance;
    }

    public static void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (action == GLFW_PRESS) {
            get().keyPressed[key] = true;
        } else if (action == GLFW_RELEASE) {
            get().keyPressed[key] = false;
        }
    }

    public static boolean isKeyPressed(int keyCode) {
        return get().keyPressed[keyCode];
    }

    public static boolean isKeyTyped(int keyCode) {
        return get().keyPressed[keyCode] && !get().keyPressedLastFrame[keyCode];
    }

    public static void endFrame() {
        for (int i = 0; i < get().keyPressed.length; i++) {
            get().keyPressedLastFrame[i] = get().keyPressed[i];
        }
    }

}
