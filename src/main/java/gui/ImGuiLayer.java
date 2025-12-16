package gui;

import engine.View;
import engine.Window;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import org.lwjgl.glfw.GLFWDropCallback;
import project.ProjectManager;
import render.Texture;
import util.AssetPool;

import javax.swing.*;
import java.io.File;
import java.nio.file.Path;

import static org.lwjgl.glfw.GLFW.*;
import static util.Constants.*;

public class ImGuiLayer {

    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    private boolean showDemoWindow = false;
    private static boolean rightSidebarOpen = true;
    private static boolean bottomSidebarOpen = true;

    private Path currentDirectory;
    private static final float TILE_SIZE = 96f;
    private static final float TILE_PADDING = 16f;


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

        // Drag and drop
        glfwSetDropCallback(windowHandle, (window, count, names) -> {
            for (int i = 0; i < count; i++) {
                String path = GLFWDropCallback.getName(names, i);
                Path source = Path.of(path);
                String lower = path.toLowerCase();

                if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                    Path imagesDir = ProjectManager.get().getCurrentProject().getImagesPath();

                    Path target = imagesDir.resolve(source.getFileName());

                    try {
                        java.nio.file.Files.copy(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Copied " + source + " to " + target);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        imGuiGlfw.newFrame();
        ImGui.newFrame();

        setupDockspace();


        // Example window
        if (showDemoWindow) {
            ImGui.showDemoWindow();
        }


        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());

        io.setIniFilename(null);

        currentDirectory = ProjectManager.get()
                .getCurrentProject()
                .getImagesPath();

    }

    public void update(float dt, View currentView) {
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        setupDockspace();


        if (rightSidebarOpen) {
            RightSidebar.render(currentView);

        }

        if (bottomSidebarOpen) {
            ImGui.begin("Asset Pool");

            ImGui.columns(2, "asset_browser",true);
            ImGui.setColumnWidth(0, 200);

            drawDirectoryList(ProjectManager.get().getCurrentProject().getAssetsPath());

            ImGui.nextColumn();

            drawDirectoryContent(currentDirectory);

            ImGui.columns(1);
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

                        Window.getView().resetGameObjects();
                        Window.setCurrentView(0);
                    } else {
                        System.out.println("Directory selection cancelled.");
                    }
                }

                if (ImGui.menuItem("Open")) {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    chooser.setCurrentDirectory(Path.of(".").toFile());
                    chooser.setDialogTitle("Select project.json");
                    chooser.setAcceptAllFileFilterUsed(false);
                    chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Project JSON", "json"));

                    int result = chooser.showOpenDialog(null);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        Path selectedFile = chooser.getSelectedFile().toPath();

                        // Otevření projektu přes ProjectManager
                        ProjectManager.get().openProject(selectedFile.toString());

                        // Aktualizace okna a scény
                        Window.get().updateTitle();
                        Window.getView().resetGameObjects();
                        Window.setCurrentView(0);
                        System.out.println("Project opened: " + selectedFile);
                    } else {
                        System.out.println("Open project cancelled.");
                    }

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

        currentView.viewImgui(dt);

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

    private void drawDirectoryList(Path root) {
        if (root == null || !root.toFile().exists()) return;

        File[] files = root.toFile().listFiles(File::isDirectory);
        if (files == null) return;

        for (File dir : files) {
            boolean selected = dir.toPath().equals(currentDirectory);
            if (ImGui.selectable(dir.getName(), selected)) {
                currentDirectory = dir.toPath();
            }
        }
    }

    private void drawDirectoryContent(Path dir) {
        if (dir == null || !dir.toFile().exists()) return;

        File[] files = dir.toFile().listFiles();
        if (files == null) return;

        float panelWidth = ImGui.getContentRegionAvailX();
        float cellSize = TILE_SIZE + TILE_PADDING;
        int columns = Math.max(1, (int)(panelWidth / cellSize));

        int i = 0;

        for (File file : files) {
            String name = file.getName();

            // Skip .class files
            if (name.endsWith(".class")) continue;

            ImGui.pushID(file.getAbsolutePath());
            ImGui.beginGroup();

            if (file.isDirectory()) {
                drawFolderPreview();
                if (ImGui.isItemClicked()) {
                    currentDirectory = file.toPath();
                }
            } else {
                drawFilePreview(file);

                // Double click behavior
                if (ImGui.isItemHovered() && ImGui.isMouseDoubleClicked(0)) {
                    try {
                        if (name.endsWith(".java")) {
                            // Open in VSCode
                            ProjectManager.get().openInVSCode(file.toPath());
                        } else {
                            // Open in system default program
                            if (java.awt.Desktop.isDesktopSupported()) {
                                java.awt.Desktop.getDesktop().open(file);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            ImGui.textWrapped(name);
            ImGui.endGroup();
            ImGui.popID();

            i++;
            if (i % columns != 0) {
                ImGui.sameLine();
            }
        }
    }


    private void drawFolderPreview() {
        ImGui.button("a", TILE_SIZE, TILE_SIZE);
    }
    private void drawFilePreview(File file) {
        Texture texture = AssetPool.getTexture(file.getName());
        if (texture == null) return;

        ImGui.beginGroup();

        // interaktivní plocha
        ImGui.invisibleButton("##drag", TILE_SIZE, TILE_SIZE);

        // pozice buttonu
        float x = ImGui.getItemRectMinX();
        float y = ImGui.getItemRectMinY();

        // vykreslení obrázku NAD buttonem
        ImGui.getWindowDrawList().addImage(
                texture.getId(),
                x, y,
                x + TILE_SIZE, y + TILE_SIZE
        );

        // === DRAG SOURCE ===
        if (ImGui.beginDragDropSource()) {

            ImGui.setDragDropPayload(
                    "ASSET_FILE",
                    file.getAbsolutePath()
            );

            ImGui.text(file.getName());
            ImGui.image(texture.getId(), 48, 48);

            ImGui.endDragDropSource();
        }

        ImGui.endGroup();
    }

    public static void hideLayers() {
        bottomSidebarOpen = false;
        rightSidebarOpen = false;

    }

    public static void showLayers() {
        bottomSidebarOpen = true;
        rightSidebarOpen = true;
    }




}