package gui;

import engine.Scene;
import engine.Window;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import project.ProjectManager;

import javax.swing.*;
import java.nio.file.Path;

import static org.lwjgl.glfw.GLFW.glfwGetCurrentContext;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static util.Constants.*;

public class ImGuiLayer {

    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    private boolean showDemoWindow = false;
    private boolean rightSidebarOpen = true;
    private boolean bottomSidebarOpen = true;

    public void init() {
        // Create context
        ImGui.createContext();


        initImGuiStyle(GUI_BUTTON, GUI_BUTTON_HOVER);
        roundImGuiStyle(GUI_ROUNDING);


        ImGuiIO io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.setIniFilename("assets/gui/imgui.ini"); // Disable saving .ini file
        io.getFonts().addFontFromFileTTF("assets/fonts/JetBrainsMono-Regular.ttf", 18);
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard); // Enable Keyboard Controls
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);

        // Init backend bindings
        long windowHandle = glfwGetCurrentContext();
        imGuiGlfw.init(windowHandle, true);
        imGuiGl3.init("#version 330");

        imGuiGlfw.newFrame();
        ImGui.newFrame();

        setupDockspace();
        ImGui.begin("Right sidebar");
        ImGui.text("Hello from ImGui!");
        ImGui.end();

        ImGui.begin("Bottom sidebar");
        ImGui.text("Hello from ImGui!");
        ImGui.end();


        // Example window
        if (showDemoWindow) {
            ImGui.showDemoWindow();
        }


        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());

        io.setIniFilename(null);
    }

    public void update(float dt, Scene currentScene) {
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        setupDockspace();


        if (rightSidebarOpen) {
            ImGui.begin("Right sidebar");
            if (ImGui.treeNode("Engine Info")) {
                ImGui.text("FPS: " + (int) (1f / dt));
                ImGui.text("Frame Time: " + (dt * 1000) + " ms");
                if (ImGui.button("Exit")) {
                    glfwSetWindowShouldClose(glfwGetCurrentContext(), true);
                }
                ImGui.treePop();
            }

            if (ImGui.treeNode("Scene objects")) {
                for (int i = 0; i < currentScene.getGameObjects().size(); i++) {
                    if (ImGui.selectable(currentScene.getGameObjects().get(i).getName(), currentScene.getGameObjects().get(i) == currentScene.getActiveGameObject())) {
                        currentScene.setActiveGameObject(currentScene.getGameObjects().get(i));
                    }
                }
                ImGui.treePop();
            }
            RightSidebar.render();
            ImGui.end();

        }

        if (bottomSidebarOpen) {
            ImGui.begin("Bottom sidebar");
            ImGui.text("Hello from ImGui!");
            ImGui.text("Delta Time: " + dt);
            ImGui.end();

        }

        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu("File")) {
                if (ImGui.menuItem("New")) {
                    ProjectManager pm = ProjectManager.get();

                    // 1) Nejprve okno pro zadání názvu projektu
                    String projectName = JOptionPane.showInputDialog(
                            null,
                            "Zadejte název projektu:",
                            "New Project",
                            JOptionPane.PLAIN_MESSAGE
                    );

                    // Pokud uživatel zrušil nebo nechal prázdné
                    if (projectName == null || projectName.trim().isEmpty()) {
                        System.out.println("Project creation cancelled.");
                        return;
                    }

                    projectName = projectName.trim();

                    // 2) Nyní se otevře JFileChooser pro výběr složky
                    JFileChooser chooser = new JFileChooser();
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    chooser.setCurrentDirectory(Path.of(".").toFile());

                    int result = chooser.showOpenDialog(null);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        Path selectedDir = chooser.getSelectedFile().toPath();

                        System.out.println("New project directory: " + selectedDir);
                        System.out.println("Project name: " + projectName);

                        pm.createNewProject(projectName, selectedDir);

                        Window.get().updateTitle();

                        Window.getScene().resetGameObjects();
                        Window.setCurrentScene(0);
                    } else {
                        System.out.println("Directory selection cancelled.");
                    }
                }

                if (ImGui.menuItem("Open")) {
                    // Akce pro "Open"
                }
                if (ImGui.menuItem("Save")) {
                    ProjectManager pm = ProjectManager.get();
                    if (pm.getCurrentProject() == null) {
                        // 1) Nejprve okno pro zadání názvu projektu
                        String projectName = JOptionPane.showInputDialog(
                                null,
                                "Zadejte název projektu:",
                                "New Project",
                                JOptionPane.PLAIN_MESSAGE
                        );

                        // Pokud uživatel zrušil nebo nechal prázdné
                        if (projectName == null || projectName.trim().isEmpty()) {
                            System.out.println("Project creation cancelled.");
                            return;
                        }

                        projectName = projectName.trim();

                        // 2) Nyní se otevře JFileChooser pro výběr složky
                        JFileChooser chooser = new JFileChooser();
                        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                        chooser.setCurrentDirectory(Path.of(".").toFile());

                        int result = chooser.showOpenDialog(null);
                        if (result == JFileChooser.APPROVE_OPTION) {
                            Path selectedDir = chooser.getSelectedFile().toPath();

                            System.out.println("New project directory: " + selectedDir);
                            System.out.println("Project name: " + projectName);

                            pm.createNewProject(projectName, selectedDir);

                            Window.get().updateTitle();

                        } else {
                            System.out.println("Directory selection cancelled.");
                        }
                    }

                    if (pm.getCurrentProject() != null) {
                        pm.saveProject();
                    }
                }

                ImGui.endMenu();
            }

            if (ImGui.beginMenu("Edit")) {
                ImGui.menuItem("Undo");
                ImGui.menuItem("Redo");
                ImGui.endMenu();
            }

            if (ImGui.beginMenu("View")) {
                if (ImGui.menuItem("Right Sidebar", "", rightSidebarOpen)) {
                    rightSidebarOpen = !rightSidebarOpen;
                }
                if (ImGui.menuItem("Bottom Sidebar", "", bottomSidebarOpen)) {
                    bottomSidebarOpen = !bottomSidebarOpen;
                }
                ImGui.endMenu();
            }

            ImGui.endMainMenuBar();
        }

        currentScene.sceneImgui();

        // Example window
        if (showDemoWindow) {
            ImGui.showDemoWindow();
        }


        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    public void destroy() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
    }

    private void initImGuiStyle(float[] baseColor, float[] hoverColor) {
        ImGui.styleColorsDark();
        ImGui.getStyle().setColor(ImGuiCol.WindowBg, GUI_BG[0], GUI_BG[1], GUI_BG[2], GUI_BG[3]);
        ImGui.getStyle().setColor(ImGuiCol.TitleBg, GUI_TITLE_BG[0], GUI_TITLE_BG[1], GUI_TITLE_BG[2], GUI_TITLE_BG[3]);
        ImGui.getStyle().setColor(ImGuiCol.TitleBgActive, GUI_TITLE_BG[0], GUI_TITLE_BG[1], GUI_TITLE_BG[2], GUI_TITLE_BG[3]);
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
        int windowFlags =  ImGuiWindowFlags.NoDocking;

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

        int dockspaceFlags = ImGuiDockNodeFlags.PassthruCentralNode;

        // Begin the invisible DockSpace window (no visible title bar or borders)
        ImGui.begin("DockSpaceRoot", new ImBoolean(true), windowFlags);

        // Pop the style variables
        ImGui.popStyleVar(3);

        // Create a DockSpace
        int dockspaceId = ImGui.getID("DockSpaceRoot");
        ImGui.dockSpace(dockspaceId, 0, 0, dockspaceFlags); // No resize for DockSpace

        // End the DockSpace window
        ImGui.end();
    }


}