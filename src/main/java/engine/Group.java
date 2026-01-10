package engine;

import gui.ImGuiUtils;
import imgui.ImGui;
import imgui.type.ImString;

import java.util.ArrayList;
import java.util.List;

public class Group {

    private String name;
    private ImString imName = new ImString("Group name", 50);
    private final List<GameObject> objects = new ArrayList<>();
    private final List<Group> groups = new ArrayList<>();
    private float[][] positionOffsets = null;

    public Group(String name) {
        this.name = name;
    }

    public Group() {
        this.name = "Group";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /* ---------- GAME OBJECTS ---------- */

    public void add(GameObject go) {
        if (!objects.contains(go)) {
            objects.add(go);
        }
    }

    public void remove(GameObject go) {
        objects.remove(go);
    }

    public List<GameObject> getObjects() {
        return objects;
    }

    /* ---------- SUBGROUPS ---------- */

    public void addGroup(Group group) {
        groups.add(group);
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void removeGroup(Group group) {
        groups.remove(group);
    }

    public void moveTo(float x, float y) {
        if (objects.isEmpty()) return;

        float avgX = (float) objects.stream().mapToDouble(o -> o.transform.position.x).average().orElse(0);
        float avgY = (float) objects.stream().mapToDouble(o -> o.transform.position.y).average().orElse(0);

        float deltaX = x - avgX;
        float deltaY = y - avgY;

        // Posun všech objektů
        for (GameObject go : objects) {
            go.transform.position.x += deltaX;
            go.transform.position.y += deltaY;
        }

        // Rekurzivně posun subgroups
        for (Group g : groups) {
            g.moveTo(g.getCenterX() + deltaX, g.getCenterY() + deltaY);
        }
    }

    public void moveBy(float x, float y) {
        if (objects.isEmpty()) return;
        for (GameObject go : objects) {
            go.transform.position.x += x;
            go.transform.position.y += y;
        }
    }

    public float getCenterX() {
        return (float) objects.stream().mapToDouble(o -> o.transform.position.x).average().orElse(0);
    }

    public float getCenterY() {
        return (float) objects.stream().mapToDouble(o -> o.transform.position.y).average().orElse(0);
    }

    public boolean contains(GameObject go) {
        return objects.contains(go);
    }

    public void imgui() {
        ImGui.inputText("##Name", imName);
        ImGui.sameLine();
        if (ImGuiUtils.lightBlueButton("Submit")) {
            if (imName.isNotEmpty()) setName(String.valueOf(imName));
        }

        if (!objects.isEmpty()) {
            float[] pos = new float[]{getCenterX(), getCenterY()};
            if (ImGui.dragFloat2("Position", pos, 0.1f)) {
                moveTo(pos[0], pos[1]);
            }


            // ---------- BUTTONS ----------
            if (ImGui.button("Ungroup")) {
                for (GameObject go : new ArrayList<>(objects)) {
                    remove(go);
                    Window.getView().root.add(go);
                }
                for (Group group : new ArrayList<>(groups)) {
                    removeGroup(group);
                    Window.getView().root.addGroup(group);
                }
                Window.getView().root.removeGroup(this);
            }
        }
    }
    public List<GameObject> getAllObjectsRecursive() {
        List<GameObject> all = new ArrayList<>(objects);
        for (Group g : groups) {
            all.addAll(g.getAllObjectsRecursive());
        }
        return all;
    }

    public List<Group> getAllGroupsRecursive() {
        List<Group> all = new ArrayList<>(groups);
        for (Group g : groups) {
            all.addAll(g.getAllGroupsRecursive());
        }
        return all;
    }

    public boolean containsRecursively(GameObject go) {
        // Zkontrolujeme, jestli je objekt přímo v této group
        if (objects.contains(go)) {
            return true;
        }

        // Rekurzivně projdeme všechny podskupiny
        for (Group g : groups) {
            if (g.containsRecursively(go)) {
                return true;
            }
        }

        // Nenašli jsme
        return false;
    }
    public Group findParentOf(GameObject go) {
        if (objects.contains(go)) return this;

        for (Group g : groups) {
            Group found = g.findParentOf(go);
            if (found != null) return found;
        }
        return null;
    }
    public Group findParentOfGroup(Group g) {
        if (groups.contains(g)) return this;

        for (Group sub : groups) {
            Group found = sub.findParentOfGroup(g);
            if (found != null) return found;
        }
        return null;
    }
    public boolean containsGroupRecursive(Group target) {
        if (groups.contains(target)) return true;
        for (Group g : groups) {
            if (g.containsGroupRecursive(target)) return true;
        }
        return false;
    }




}