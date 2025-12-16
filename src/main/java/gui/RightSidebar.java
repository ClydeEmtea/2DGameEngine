package gui;

import engine.GameObject;
import engine.Group;
import engine.View;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.flag.ImGuiWindowFlags;

import java.util.ArrayList;
import java.util.List;


public class RightSidebar {

    private static final List<Runnable> renderCallbacks = new ArrayList<>();
    public static List<GameObject> selectedObjects = new ArrayList<>();
    private static final List<Group> selectedGroups = new ArrayList<>();
    private static GameObject lastSelectedObject = null;
    private static Group lastSelectedGroup = null;

    private RightSidebar() {}

    static void activeGameObjectImGui(View currentView) {
        GameObject activeGameObject = currentView.getActiveGameObject();
        if (activeGameObject != null) {
            ImGui.dummy(0, 50);
            ImGui.separator();
            ImGui.dummy(0,15);
            ImGui.text(activeGameObject.getName());
            activeGameObject.imgui();
        }
    }

    static void activeGroupImGui(View currentView) {
        Group group = currentView.getActiveGroup();
        if (group != null) {
            ImGui.dummy(0, 50);
            ImGui.separator();
            ImGui.dummy(0,15);
            ImGui.text(group.getName());
            group.imgui();
        }
    }

    static void sceneObjectsImGui(View view) {
        drawGroup(view.getRoot(), view, true);

        if (!selectedObjects.isEmpty() || !selectedGroups.isEmpty()) {
            boolean ctrl = ImGui.getIO().getKeyCtrl();
            if (ctrl && ImGui.isKeyPressed(71)) { // Ctrl+G
                groupSelectedItems(view);

            }

            ImGui.separator();
            if (ImGui.button("Group Selected")) {
                groupSelectedItems(view);
            }
        }
    }

    private static void drawGroup(Group group, View view, boolean isRoot) {
        String displayName = isRoot ? "Scene" : group.getName();
        boolean groupSelected = selectedGroups.contains(group);

        // Zvýrazníme group pokud je vybraná
        int flags = ImGuiTreeNodeFlags.DefaultOpen | ImGuiTreeNodeFlags.OpenOnArrow;
        if (groupSelected) {
            flags |= ImGuiTreeNodeFlags.Selected;
        }

        boolean open = ImGui.treeNodeEx(String.valueOf(group), flags, displayName);

        // Klik na group mimo expand arrow
        if (ImGui.isItemClicked()) {
            boolean shift = ImGui.getIO().getKeyShift();
            boolean ctrl = ImGui.getIO().getKeyCtrl();

            if (shift && lastSelectedGroup != null) {
                if (!selectedGroups.contains(group))
                    selectedGroups.add(group);
                // Přidej všechny objekty této group do selection
                for (GameObject go : group.getAllObjectsRecursive()) {
                    if (!selectedObjects.contains(go)) selectedObjects.add(go);
                }
                for (Group g : group.getAllGroupsRecursive()) {
                    if (!selectedGroups.contains(g)) selectedGroups.add(g);
                }
            } else if (ctrl) {
                if (selectedGroups.contains(group))
                    selectedGroups.remove(group);
                else
                    selectedGroups.add(group);
                lastSelectedGroup = group;

                // Přidej všechny objekty této group do selection
                for (GameObject go : group.getAllObjectsRecursive()) {
                    if (!selectedObjects.contains(go)) selectedObjects.add(go);
                }
                for (Group g : group.getAllGroupsRecursive()) {
                    if (!selectedGroups.contains(g)) selectedGroups.add(g);
                }

            } else {
                selectedGroups.clear();
                selectedObjects.clear(); // clear objekty při vybrání group
                selectedGroups.add(group);
                lastSelectedGroup = group;

                // Přidej všechny objekty této group do selection
                selectedObjects.addAll(group.getAllObjectsRecursive());
                selectedGroups.addAll(group.getAllGroupsRecursive());
            }

            view.setActiveGroup(group);


        }



        if (!open) return;

        // Rekurze přes podsložky
        for (Group sub : group.getGroups()) {
            drawGroup(sub, view, false);
        }

        // Vypiš objekty v této skupině
        List<GameObject> objects = group.getObjects();
        for (int i = 0; i < objects.size(); i++) {
            GameObject go = objects.get(i);
            boolean objSelected = selectedObjects.contains(go);

            if (ImGui.selectable(go.getName(), objSelected)) {
                boolean shift = ImGui.getIO().getKeyShift();
                boolean ctrl = ImGui.getIO().getKeyCtrl();

                if (shift && lastSelectedObject != null && objects.contains(lastSelectedObject)) {
                    int lastIndex = objects.indexOf(lastSelectedObject);
                    int start = Math.min(lastIndex, i);
                    int end = Math.max(lastIndex, i);
                    for (int j = start; j <= end; j++) {
                        GameObject obj = objects.get(j);
                        if (!selectedObjects.contains(obj))
                            selectedObjects.add(obj);
                    }
                } else if (ctrl) {
                    if (selectedObjects.contains(go))
                        selectedObjects.remove(go);
                    else
                        selectedObjects.add(go);
                    lastSelectedObject = go;
                } else {
                    selectedObjects.clear();
                    selectedGroups.clear();
                    selectedObjects.add(go);
                    lastSelectedObject = go;
                }

                if (!selectedObjects.isEmpty()) {
                    view.setActiveGameObject(selectedObjects.get(0));
                } else {
                    view.setActiveGameObject(null);
                }
            }

        }

        ImGui.treePop();
    }




    private static void groupSelectedItems(View view) {
        if (selectedObjects.isEmpty() && selectedGroups.isEmpty()) return;

        Group newGroup = new Group();
        newGroup.setName("New Group");

        // Přidej vybrané objekty
        for (GameObject go : new ArrayList<>(selectedObjects)) {
            view.getRoot().remove(go);
            newGroup.add(go);
        }

        // Přidej vybrané groups
        for (Group g : new ArrayList<>(selectedGroups)) {
            view.getRoot().removeGroup(g);
            newGroup.addGroup(g);
        }

        view.getRoot().addGroup(newGroup);

        selectedObjects.clear();
        selectedGroups.clear();

        if (!newGroup.getObjects().isEmpty()) {
            selectedObjects.addAll(newGroup.getObjects());
            view.setActiveGameObject(newGroup.getObjects().get(0));

            view.setActiveGroup(newGroup);
            selectedGroups.add(newGroup);
        }

    }

    public static void syncSelectionWithActive(View view) {
        GameObject activeGO = view.getActiveGameObject();
        Group activeGroup = view.getActiveGroup();

        boolean ctrl = ImGui.getIO().getKeyCtrl();
        boolean shift = ImGui.getIO().getKeyShift();

        if (!ctrl && !shift) {
            selectedObjects.clear();
            selectedGroups.clear();

            if (activeGO != null) {
                selectedObjects.add(activeGO);
            } else if (activeGroup != null) {
                selectedGroups.add(activeGroup);
            }
        }
    }



    public static void clearCallbacks() {
        renderCallbacks.clear();
        selectedObjects.clear();
        selectedGroups.clear();
        lastSelectedObject = null;
        lastSelectedGroup = null;
    }

    public static void addCallback(Runnable callback) {
        renderCallbacks.add(callback);
    }

    public static void render(View currentView) {
        ImGui.begin("Right sidebar");
        for (Runnable callback : renderCallbacks) {
            callback.run();
        }

        ImGui.beginChild("RightSidebarScroll", 0, 500, true, ImGuiWindowFlags.HorizontalScrollbar);

        sceneObjectsImGui(currentView);

        ImGui.endChild();


        activeGroupImGui(currentView);
        activeGameObjectImGui(currentView);
        ImGui.end();
    }
}
