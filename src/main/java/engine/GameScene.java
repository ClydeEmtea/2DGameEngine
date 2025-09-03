package engine;

import components.Sprite;
import components.SpriteRenderer;
import imgui.ImGui;
import org.joml.Vector2f;
import util.AssetPool;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_TAB;

public class GameScene extends Scene {

    public GameScene() {
        // Initialize the game scene
        System.out.println("Game Scene Initialized");
    }

    @Override
    public void update(float dt) {


        if (KeyListener.isKeyTyped(GLFW_KEY_TAB)) {
            Window.setCurrentScene(0);
        }
    }

    @Override
    public void imgui() {
        ImGui.begin("Scene");
        ImGui.text("Game Scene");
        ImGui.end();

    }
}
