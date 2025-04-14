package engine;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import util.Constants;

import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private int width, height;
    private final String title;
    private long glfwWindow;

    private static Window window = null;

    private static Scene currentScene = null;

    private Window() {
        this.width = Constants.WIDTH;
        this.height = Constants.HEIGHT;
        this.title = Constants.TITLE;
    }

    public static void setCurrentScene(int scene) {
        switch (scene) {
            case 0 -> currentScene = new EditorScene();
            case 1 -> currentScene = new GameScene();
            default -> throw new IllegalArgumentException("Invalid scene index: " + scene);
        }
        currentScene.init();
        currentScene.start();
    }

    public static Window get() {
        if (Window.window == null) {
            Window.window = new Window();
        }
        return Window.window;
    }

    public static Scene getScene() {
        get();
        return currentScene;
    }

    public void run() {
        System.out.println("LWJGL " + Version.getVersion() + "!");

        init();
        loop();

        // Free the memory
        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }

    public void init() {
        // Set up an error callback

        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE); // the window will be maximized after creation

        // Create the window
        glfwWindow = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);
        if ( glfwWindow == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Set up a key callback
        glfwSetCursorPosCallback(glfwWindow, MouseListener::mousePosCallback);
        glfwSetMouseButtonCallback(glfwWindow, MouseListener::mouseButtonCallback);
        glfwSetScrollCallback(glfwWindow, MouseListener::mouseScrollCallback);

        glfwSetKeyCallback(glfwWindow, KeyListener::keyCallback);


        // Make the OpenGL context current
        glfwMakeContextCurrent(glfwWindow);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(glfwWindow);

        // This line is critical for LWJGL's interoperation with GLFW's OpenGL context,
        // or any context that is managed externally. LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL bindings available for use.
        GL.createCapabilities();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        // glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

        Window.setCurrentScene(0);
    }

    public void loop() {
        double beginTime = glfwGetTime();
        double endTime;
        float deltaTime = 0.0f;

        while (!glfwWindowShouldClose(glfwWindow)) {
            // Poll for window events
            glfwPollEvents();
            // Clear the framebuffer
            glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            currentScene.update(deltaTime);

            // Swap the color buffers
            glfwSwapBuffers(glfwWindow);

            endTime = glfwGetTime();
            deltaTime = (float) (endTime - beginTime);
            beginTime = endTime;

            KeyListener.endFrame();
            MouseListener.endFrame();

        }
    }
}
