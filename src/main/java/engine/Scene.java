package engine;

import components.Sprite;
import components.SpriteRenderer;
import imgui.ImGui;
import render.Renderer;
import render.Texture;

import java.util.ArrayList;
import java.util.List;

public abstract class Scene {

    public Renderer renderer = new Renderer();
    protected Camera camera;
    private boolean isRunning = false;


    protected List<GameObject> gameObjects = new ArrayList<>();
    protected GameObject activeGameObject = null;
    protected GameObject selectedGameObjectIcon = null;

    public Scene() {

    }

    public void init() {

    }
    public void start() {
        for (GameObject gameObject : gameObjects) {
            gameObject.start();
            this.renderer.add(gameObject);
        }
        isRunning = true;
    }


    public void addGameObjectToScene(GameObject gameObject) {
        if (!isRunning) {
            gameObjects.add(gameObject);
        } else {
            gameObjects.add(gameObject);
            gameObject.start();
            this.renderer.add(gameObject);
        }
    }

    public void addLine(GameObject line) {}

    public void removeGameObject(GameObject gameObject) {
        if (this.gameObjects.remove(gameObject)) {
            renderer.remove(gameObject);
        }
    }


    public abstract void update(float dt);

    public Camera camera() {
        return this.camera;
    }

    public void sceneImgui() {
//        if (activeGameObject != null) {
//            ImGui.setNextWindowSize(400, 200);
//            ImGui.begin(activeGameObject.getName());
//            activeGameObject.imgui();
//            ImGui.end();
//        }

        imgui();
    }

    public void imgui() {

    }
    public List<GameObject> getGameObjects() {
        return gameObjects;
    }

    public void resetGameObjects() {
        this.gameObjects = new ArrayList<>();
        this.renderer = new Renderer();
    }

    public GameObject getActiveGameObject() {
        return activeGameObject;
    }

    public List<GameObject> getGridLines() {return null;}

    public void setActiveGameObject(GameObject activeGameObject) {
        this.activeGameObject = activeGameObject;
    }
}
