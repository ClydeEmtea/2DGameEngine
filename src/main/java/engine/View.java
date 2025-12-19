package engine;

import components.ShapeRenderer;
import components.SpriteRenderer;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import org.joml.Vector2f;
import org.joml.Vector4f;
import physics2d.Physics2D;
import project.ProjectManager;
import render.Renderer;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class View {

    public Renderer renderer = new Renderer();
    protected Camera camera;
    private boolean isRunning = false;
    public boolean isGame = false;

    protected List<GameObject> gameObjects = new ArrayList<>();
    protected Group root = new Group("Root");
    protected GameObject activeGameObject = null;
    protected Group activeGroup = null;
    protected Physics2D physics2D;

    public List<Scene> scenes = new ArrayList<>();
    public Scene currentScene = null;

    public View() {

    }

    public void init() {

    }
    public void start() {

        for (GameObject gameObject : gameObjects) {
            gameObject.start();
            this.renderer.add(gameObject);
            this.physics2D.add(gameObject);
        }
        isRunning = true;
    }


    public void addGameObjectToView(GameObject go) {
        gameObjects.add(go);

        if (isRunning) {
            go.start();
            this.renderer.add(go);
            this.physics2D.add(go);
        }
        if (!root.containsRecursively(go))
            root.add(go);
    }


    public void addLine(GameObject line) {}

    public void removeGameObject(GameObject gameObject) {
        if (this.gameObjects.remove(gameObject)) {
            this.renderer.remove(gameObject);
            this.physics2D.destroyGameObject(gameObject);
            removeFromGroups(root, gameObject);
        }
    }
    private void removeFromGroups(Group group, GameObject go) {
        group.remove(go);
        for (Group g : group.getGroups()) {
            removeFromGroups(g, go);
        }
    }



    public void update(float dt) {};

    public Camera camera() {
        return this.camera;
    }

    public void viewImgui(float dt) {


        imgui(dt);
    }

    public void imgui(float dt) {

    }
    public List<GameObject> getGameObjects() {
        return gameObjects;
    }

    public GameObject getObjectByName(String name) {
        for (GameObject go : gameObjects) {
            if (go.getName().equals(name)) {
                return go;
            }
        }
        return null; // nenalezeno
    }

    public GameObject createNewObject() {
        // Základní jméno
        String baseName = "GameObject";
        String name = baseName;
        int index = 1;

        // Zajistíme unikátní jméno
        while (getObjectByName(name) != null) {
            name = baseName + " (" + index + ")";
            index++;
        }

        // Vytvoření transformace
        Transform transform = new Transform(new Vector2f(0, 0), new Vector2f(0.1f, 0.1f), 0);

        // Vytvoření GameObjectu
        GameObject go = new GameObject(name, transform, 50);
        go.addComponent(new SpriteRenderer(
                new Vector4f(1, 1, 1, 1)
        ));

        go.addComponent(new ShapeRenderer());

        // Přidání do seznamu a root
        addGameObjectToView(go);

        return go;
    }

    public Group getGroupByName(String name) {
        return getGroupByNameRecursive(root, name);
    }

    private Group getGroupByNameRecursive(Group group, String name) {
        if (group.getName().equals(name)) {
            return group;
        }

        for (Group g : group.getGroups()) {
            Group found = getGroupByNameRecursive(g, name);
            if (found != null) {
                return found;
            }
        }

        return null; // nenalezeno
    }




    public void resetGameObjects() {
        this.gameObjects = new ArrayList<>();
        this.renderer = new Renderer();
        this.root = new Group("Root");
    }

    public GameObject getActiveGameObject() {
        return activeGameObject;
    }

    public Group getActiveGroup() {
        return activeGroup;
    }

    public void setActiveGameObject(GameObject activeGameObject) {
        this.activeGroup = null;
        this.activeGameObject = activeGameObject;
    }
    public void setActiveGroup(Group group) {
        this.activeGameObject = null;
        this.activeGroup = group;
    }
    public Group getRoot() {
        return root;
    }
    public Group findParentGroup(GameObject go) {
        return root.findParentOf(go);
    }
    public Group findParentGroup(Group target) {
        return root.findParentOfGroup(target);
    }

    void activeGameObjectImGui() {
        GameObject activeGameObject = this.getActiveGameObject();
        if (activeGameObject != null) {
            ImGui.dummy(0, 50);
            ImGui.separator();
            ImGui.dummy(0,15);
            ImGui.text(activeGameObject.getName());
            activeGameObject.imgui();

        }
    }


}
