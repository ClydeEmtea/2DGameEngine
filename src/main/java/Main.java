import engine.*;
import org.lwjgl.glfw.GLFWErrorCallback;

import static org.lwjgl.glfw.GLFW.glfwTerminate;

public class Main {
    public static void main(String[] args) {
        try {
            Window window = Window.get();
            window.run();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Terminate GLFW and free the error callback
            GLFWErrorCallback.createPrint(System.err).free();
            glfwTerminate();
        }
    }
}