package engine;

import imgui.ImGui;
import org.joml.Vector2f;
import project.ProjectManager;
import util.AssetPool;

import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_TAB;

public class GameScene extends Scene {

    public GameScene() {
        // Initialize the game scene
        System.out.println("Game Scene Initialized");
    }

    @Override
    public void init() {
        loadResources();
        this.camera = new Camera(new Vector2f());
        List<GameObject> sceneObjects = ProjectManager.get().loadSceneObjects("MainScene");
        if (sceneObjects != null) {
            for (GameObject go : sceneObjects) {
                this.addGameObjectToScene(go);
            }
        }

        for (GameObject go : gameObjects) {
            for (Component c : go.getAllScripts()) {
                c.initScript();
            }
        }

    }

    private void loadResources() {
        AssetPool.getShader("vertexDefault.glsl", "fragmentDefault.glsl");

    }

    @Override
    public void update(float dt) {

        for (GameObject go : this.gameObjects) {
            go.update(dt);
            for (Component c : go.getAllScripts()) {
                c.updateScript(dt);
            }
        }

        this.renderer.render();


        if (KeyListener.isKeyTyped(GLFW_KEY_TAB)) {
            Window.setCurrentScene(0);
        }
    }

    @Override
    public void imgui() {


    }
}
