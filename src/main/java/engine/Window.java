package engine;

import gui.ImGuiLayer;
import observers.Event;
import observers.EventSystem;
import observers.EventType;
import observers.Observer;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.opengl.GL;
import project.ProjectManager;
import util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window implements Observer {

    private int width, height;
    private final String title;
    private long glfwWindow;

    private static Window window = null;

    private static View currentView = null;

    private ImGuiLayer imGuiLayer;

    private long audioContext;
    private long audioDevice;

    private static List<String> errors = new ArrayList<>();

    private Window() {
        this.width = Constants.WIDTH;
        this.height = Constants.HEIGHT;
        ProjectManager projectManager = ProjectManager.get();
        String projectName = (projectManager.getCurrentProject() != null) ? projectManager.getCurrentProject().getName() : "Unnamed Project";
        this.title = Constants.TITLE + " - " + projectName;
        EventSystem.addObserver(this);
    }

    public static void setCurrentView(int scene) {
        Scene curScene = null;
        if (currentView != null && currentView.currentScene != null) curScene = currentView.currentScene;
        switch (scene) {
            case 0 -> {
                EventSystem.notify(null, new Event(EventType.StopPlay));
                currentView = new EditorView();
                ImGuiLayer.showLayers();
                getView().isGame = false;
            }
            case 1 -> {
                EventSystem.notify(null, new Event(EventType.StartPlay));
                currentView = new GameView();
                ImGuiLayer.hideLayers();
                getView().isGame = true;
            }
            default -> throw new IllegalArgumentException("Invalid scene index: " + scene);
        }
        for (String name : ProjectManager.get().getScenes()) {
            Scene s = new Scene(name);
            if (curScene != null && s.getName().equals(curScene.getName())) {
                currentView.currentScene = s;
                System.out.println("i just did that");
            }
            if (curScene == null && s.getName().equals("MainScene")) {
                currentView.currentScene = s;
                System.out.println("i just did that");
            }
            currentView.scenes.add(s);
        }
        currentView.init();
        currentView.start();
    }

    public static void setScene(Scene scene) {
        if (currentView.currentScene.getName().equals(scene.getName())) return;
        ProjectManager.get().saveProject();
        if (currentView.isGame) currentView = new GameView();
        else currentView = new EditorView();
        for (String name : ProjectManager.get().getScenes()) {
            Scene s = new Scene(name);
            currentView.scenes.add(s);
        }
        currentView.currentScene = scene;
        currentView.init();
        currentView.start();
    }

    public static Scene createNewScene(String name) {
        Scene s = new Scene(name);
        currentView.scenes.add(s);
        setScene(s);
        return s;
    }

    public static Window get() {
        if (Window.window == null) {
            Window.window = new Window();
        }
        return Window.window;
    }

    public static View getView() {
        get();
        return currentView;
    }

    public static List<String> getErrors() {
        return errors;
    }

    public static void addError(String error) {
        errors.add(error);
    }

    public static void clearErrors() {
        errors = new ArrayList<>();
    }

    public long getGlfwWindow() {
        return glfwWindow;
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

        alcDestroyContext(audioContext);
        alcCloseDevice(audioDevice);

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

            if (Window.getView() != null && Window.getView().camera != null) {
                Window.getView().camera.adjustProjection();
            }
        });


        // Make the OpenGL context current
        glfwMakeContextCurrent(glfwWindow);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(glfwWindow);

        // Initialize the audio device
        String defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
        audioDevice = alcOpenDevice(defaultDeviceName);

        int[] attributes = {0};
        audioContext = alcCreateContext(audioDevice,attributes);
        alcMakeContextCurrent(audioContext);

        ALCCapabilities alcCapabilities = ALC.createCapabilities(audioDevice);
        ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);

        if (!alCapabilities.OpenAL10) {
            assert false : "OpenAL 10 is not supported";
        }

        // This line is critical for LWJGL's interoperation with GLFW's OpenGL context,
        // or any context that is managed externally. LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL bindings available for use.
        GL.createCapabilities();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        // glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

        // Aktualizace reálné velikosti okna a framebufferu
        int[] fbWidth = new int[1];
        int[] fbHeight = new int[1];
        glfwGetFramebufferSize(glfwWindow, fbWidth, fbHeight);
        this.width = fbWidth[0];
        this.height = fbHeight[0];

        ProjectManager.get().openProject("C:/Users/EmTea/Desktop/jrbu/project.json");

        Window.setCurrentView(0);

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

            currentView.update(deltaTime);

            imGuiLayer.update(deltaTime, currentView);

            // Swap the color buffers
            glfwSwapBuffers(glfwWindow);

            endTime = glfwGetTime();
            deltaTime = (float) (endTime - beginTime);
            beginTime = endTime;

            KeyListener.endFrame();
            MouseListener.endFrame();

        }
    }

    @Override
    public void onNotify(GameObject gameObject, Event event) {
        if (event.type == EventType.StartPlay) {
            ProjectManager.get().saveProject();
        } else if (event.type == EventType.StopPlay) {
            System.out.println("stopping");
        }
    }
}
