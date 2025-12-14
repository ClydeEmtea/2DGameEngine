package engine;

import components.GridRenderer;
import gui.ImGuiLayer;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import project.ProjectManager;
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

    private ImGuiLayer imGuiLayer;

    private Window() {
        this.width = Constants.WIDTH;
        this.height = Constants.HEIGHT;
        ProjectManager projectManager = ProjectManager.get();
        String projectName = (projectManager.getCurrentProject() != null) ? projectManager.getCurrentProject().getName() : "Unnamed Project";
        this.title = Constants.TITLE + " - " + projectName;
    }

    public static void setCurrentScene(int scene) {
        switch (scene) {
            case 0 -> {
                currentScene = new EditorScene();
                ImGuiLayer.showLayers();
            }
            case 1 -> {
                currentScene = new GameScene();
                ImGuiLayer.hideLayers();
            }
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

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void updateTitle() {
        String projectName = "Unnamed Project";
        ProjectManager pm = ProjectManager.get();

        if (pm.getCurrentProject() != null) {
            projectName = pm.getCurrentProject().getName();
        }

        glfwSetWindowTitle(this.glfwWindow, Constants.TITLE + " - " + projectName);
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
        imGuiLayer.destroy();
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

        glfwSetFramebufferSizeCallback(glfwWindow, (window, w, h) -> {
            this.width = w;
            this.height = h;

            glViewport(0, 0, w, h);

            if (Window.getScene() != null && Window.getScene().camera != null) {
                Window.getScene().camera.adjustProjection();
            }
        });


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

        ProjectManager.get().openProject("C:/Users/EmTea/Desktop/jrbu/project.json");

        Window.setCurrentScene(0);

        imGuiLayer = new ImGuiLayer();
        imGuiLayer.init();
    }

    public void loop() {
        double beginTime = glfwGetTime();
        double endTime;
        float deltaTime = 0.0f;

        while (!glfwWindowShouldClose(glfwWindow)) {
            // Poll for window events
            glfwPollEvents();
            // Clear the framebuffer

            // Background color
            glClearColor(Constants.BACKGROUND_COLOR[0], Constants.BACKGROUND_COLOR[1], Constants.BACKGROUND_COLOR[2], Constants.BACKGROUND_COLOR[3]);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            currentScene.update(deltaTime);

            imGuiLayer.update(deltaTime, currentScene);

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
