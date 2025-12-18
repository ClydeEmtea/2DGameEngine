package engine;

import org.joml.Vector2f;
import physics2d.Physics2D;
import project.ProjectManager;
import util.AssetPool;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_TAB;
import static util.Constants.DEFAULT_FRAGMENT_SHADER;
import static util.Constants.DEFAULT_VERTEX_SHADER;

public class GameView extends View {

    public GameView() {
        // Initialize the game scene
        System.out.println("Game Scene Initialized");
    }

    @Override
    public void init() {
        loadResources();
        this.camera = new Camera(new Vector2f());
        this.physics2D = new Physics2D();
        List<GameObject> sceneObjects = ProjectManager.get().loadSceneObjects("MainScene");
        if (sceneObjects != null) {
            for (GameObject go : sceneObjects) {
                this.addGameObjectToView(go);
            }
        }

        for (GameObject go : gameObjects) {
            for (Component c : go.getAllScripts()) {
                c.initScript();
            }
        }

    }

    private void loadResources() {
        AssetPool.getShader(DEFAULT_VERTEX_SHADER, DEFAULT_FRAGMENT_SHADER);

    }

    @Override
    public void update(float dt) {

        for (GameObject go : new ArrayList<>(this.gameObjects)) {
            go.update(dt);
            for (Component c : go.getAllScripts()) {
                try {
                    c.updateScript(dt);
                } catch (Exception e) {
                    System.err.println("Script error in " + c.getClass() + ": " + e);
                    e.printStackTrace();
                }
            }
        }


        try {
            this.physics2D.update(dt);
        } catch (Exception e) {
            assert false : "Physics update failed";
        }

        this.renderer.render();


        if (KeyListener.isKeyTyped(GLFW_KEY_TAB)) {
            Window.setCurrentView(0);
        }
    }

    @Override
    public void imgui(float dt) {


    }
}
