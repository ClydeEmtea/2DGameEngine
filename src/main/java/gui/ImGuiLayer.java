package gui;

import engine.Scene;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;

import static org.lwjgl.glfw.GLFW.glfwGetCurrentContext;

import static util.Constants.*;

public class ImGuiLayer {

    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    private boolean showDemoWindow = true;

    public void init() {
        // Create context
        ImGui.createContext();

        ImGui.styleColorsDark();
        ImGui.getStyle().setColor(ImGuiCol.WindowBg, GUI_BG[0], GUI_BG[1], GUI_BG[2], GUI_BG[3]);
        ImGui.getStyle().setColor(ImGuiCol.TitleBg, GUI_TITLE_BG[0], GUI_TITLE_BG[1], GUI_TITLE_BG[2], GUI_TITLE_BG[3]);
        ImGui.getStyle().setColor(ImGuiCol.TitleBgActive, GUI_TITLE_BG[0], GUI_TITLE_BG[1], GUI_TITLE_BG[2], GUI_TITLE_BG[3]);
        tintImGuiStyle(GUI_BUTTON, GUI_BUTTON_HOVER);
        roundImGuiStyle(GUI_ROUNDING);


        ImGuiIO io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.setIniFilename(null); // Disable saving .ini file
        io.getFonts().addFontFromFileTTF("assets/fonts/JetBrainsMono-Regular.ttf", 18);
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard); // Enable Keyboard Controls
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);

        // Init backend bindings
        long windowHandle = glfwGetCurrentContext();
        imGuiGlfw.init(windowHandle, true);
        imGuiGl3.init("#version 330");
    }

    public void update(float dt, Scene currentScene) {
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        setupDockspace();

        dockRightWindow();
        dockTopWindow();

        currentScene.sceneImgui();

        // Example window
        if (showDemoWindow) {
            ImGui.showDemoWindow();
        }

        ImGui.begin("My ImGui Window");
        ImGui.text("Hello from ImGui!");
        if (ImGui.button("Toggle Demo")) {
            showDemoWindow = !showDemoWindow;
        }
        ImGui.end();

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    public void destroy() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
    }

    private void tintImGuiStyle(float[] baseColor, float[] hoverColor) {
        int[] keys = new int[]{
                ImGuiCol.Button, ImGuiCol.FrameBg, ImGuiCol.SliderGrab, ImGuiCol.ResizeGrip, ImGuiCol.Tab,
                ImGuiCol.ScrollbarGrab, ImGuiCol.CheckMark, ImGuiCol.Border, ImGuiCol.TabActive,
                ImGuiCol.TabUnfocused, ImGuiCol.TabUnfocusedActive, ImGuiCol.ScrollbarBg, ImGuiCol.ScrollbarGrabHovered,
                ImGuiCol.ScrollbarGrabActive, ImGuiCol.ResizeGripActive, ImGuiCol.CheckMark, ImGuiCol.BorderShadow,
                ImGuiCol.Separator, ImGuiCol.SeparatorHovered, ImGuiCol.SeparatorActive,

                // New: sections and headers
                ImGuiCol.Header,
                ImGuiCol.HeaderActive,
        };

        int[] hoverKeys = new int[]{
                ImGuiCol.ButtonHovered, ImGuiCol.FrameBgHovered, ImGuiCol.SliderGrabActive,
                ImGuiCol.ResizeGripHovered, ImGuiCol.TabHovered, ImGuiCol.ScrollbarGrabActive,

                // New: section hovers
                ImGuiCol.HeaderHovered,
                ImGuiCol.TitleBgCollapsed
        };

        for (int key : keys) {
            ImGui.getStyle().setColor(key, baseColor[0], baseColor[1], baseColor[2], baseColor[3]);
        }

        for (int key : hoverKeys) {
            ImGui.getStyle().setColor(key, hoverColor[0], hoverColor[1], hoverColor[2], hoverColor[3]);
        }
    }

    private void roundImGuiStyle(float rounding) {
        ImGui.getStyle().setFrameRounding(rounding);
        ImGui.getStyle().setGrabRounding(rounding);
        ImGui.getStyle().setTabRounding(rounding);
        ImGui.getStyle().setPopupRounding(rounding);
        ImGui.getStyle().setScrollbarRounding(rounding);
        ImGui.getStyle().setWindowRounding(rounding);
    }

    private void setupDockspace() {
        int windowFlags = ImGuiWindowFlags.MenuBar | ImGuiWindowFlags.NoDocking;

        // Set DockSpace size and position
        ImGui.setNextWindowPos(0, 0);
        ImGui.setNextWindowSize(ImGui.getIO().getDisplaySizeX(), ImGui.getIO().getDisplaySizeY());
        ImGui.setNextWindowBgAlpha(0.0f); // Transparent background
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0.0f); // No rounding
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0.0f); // No border
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0, 0); // No padding


        windowFlags |= ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse |
                ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoNavFocus |
                ImGuiWindowFlags.NoBackground | ImGuiWindowFlags.NoSavedSettings |
                ImGuiWindowFlags.NoDocking | ImGuiWindowFlags.NoScrollWithMouse |
                ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoDecoration;

        int dockspaceFlags = ImGuiDockNodeFlags.PassthruCentralNode | ImGuiDockNodeFlags.NoResize;

        // Begin the invisible DockSpace window (no visible title bar or borders)
        ImGui.begin("DockSpaceRoot", new ImBoolean(true), windowFlags);

        // Pop the style variables
        ImGui.popStyleVar(3);

        // Create a DockSpace
        int dockspaceId = ImGui.getID("MyDockSpace");
        ImGui.dockSpace(dockspaceId, 0, 0, dockspaceFlags); // No resize for DockSpace

        // End the DockSpace window
        ImGui.end();
    }

    private void dockRightWindow() {
        // Prepare the window to dock on the right side
        ImGui.setNextWindowDockID(ImGui.getID("MyDockSpace"), ImGuiDockNodeFlags.NoResize);

        // Set the window's position to the right side of the screen
        ImGui.setNextWindowPos(ImGui.getIO().getDisplaySizeX() - 400, 200);
        ImGui.setNextWindowSize(400, ImGui.getIO().getDisplaySizeY());

        // Create the window
        ImGui.begin("Docked Right Window", new ImBoolean(true), ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoDocking);
        ImGui.text("This window is docked to the right!");
        ImGui.end();
    }

    private void dockTopWindow() {
        // Prepare the window to dock on the top
        ImGui.setNextWindowDockID(ImGui.getID("MyDockSpace"), ImGuiDockNodeFlags.NoResize);

        // Set the window's position to the top of the screen
        ImGui.setNextWindowPos(0, 0);  // Position at the top
        ImGui.setNextWindowSize(ImGui.getIO().getDisplaySizeX(), 200);  // Set height to 200 pixels, adjust as necessary

        // Create the window
        ImGui.begin("Docked Top Window", new ImBoolean(true), ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoDocking);
        ImGui.text("This window is docked to the top!");
        ImGui.end();
    }


}