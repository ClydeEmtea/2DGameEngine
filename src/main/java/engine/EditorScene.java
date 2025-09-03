package engine;

import components.Sprite;
import components.SpriteRenderer;
import components.Spritesheet;
import gui.ObjectCreationWindow;
import gui.RightSidebar;
import imgui.ImGui;
import org.joml.Vector2f;
import org.joml.Vector4f;
import util.AssetPool;

import static org.lwjgl.glfw.GLFW.*;
import static util.Constants.WHITE;

public class EditorScene extends Scene {

    private GameObject obj1 = new GameObject("Player", new Transform(new Vector2f(100,100), new Vector2f(256,256)), 1);
    private GameObject obj2 = new GameObject("Pla", new Transform(new Vector2f(100,100), new Vector2f(128,128)), 2);
    private Spritesheet spritesheet;

    private boolean showCreationWindow = false;

    public EditorScene() {
        System.out.println("Editor Scene Initialized");
    }

    @Override
    public void init() {
        loadResources();
        this.camera = new Camera(new Vector2f());

        this.spritesheet = AssetPool.getSpritesheet("C:\\Users\\EmTea\\IdeaProjects\\2DGameEngine\\assets\\images\\spritesheet.png");

        obj1.addComponent(new SpriteRenderer(new Sprite(AssetPool.getTexture("C:\\Users\\EmTea\\IdeaProjects\\2DGameEngine\\assets\\images\\texture.png"))));
        this.addGameObjectToScene(obj1);


        obj2.addComponent(new SpriteRenderer(spritesheet.getSprite(0)));
        this.addGameObjectToScene(obj2);

        GameObject obj3 = new GameObject("nikdo", new Transform(new Vector2f(150,100), new Vector2f(258,200)), 3);
        obj3.addComponent(new SpriteRenderer(new Sprite(AssetPool.getTexture("C:\\Users\\EmTea\\IdeaProjects\\2DGameEngine\\assets\\images\\blendImage2.png"))));
        this.addGameObjectToScene(obj3);

        GameObject obj4 = new GameObject("White", new Transform(new Vector2f(400,100), new Vector2f(258,200)), 4);
        obj4.addComponent(new SpriteRenderer(new Vector4f(1,1,1,1)));
        this.addGameObjectToScene(obj4);



        RightSidebar.clearCallbacks();
        RightSidebar.addCallback(() -> {
            imgui.ImGui.text("Custom Content!");
            imgui.ImGui.button("Click Me");
        });

    }

    private void loadResources() {
        AssetPool.getShader("vertexDefault.glsl", "fragmentDefault.glsl");


        AssetPool.addSpritesheet("C:\\Users\\EmTea\\IdeaProjects\\2DGameEngine\\assets\\images\\spritesheet.png",
                new Spritesheet(AssetPool.getTexture("C:\\Users\\EmTea\\IdeaProjects\\2DGameEngine\\assets\\images\\spritesheet.png"), 16, 16, 26, 0));
    }


    private int spriteIndex = 0;
    private float spriteFlipTime = 0.2f;
    private float spriteFlipTimeLeft = 0.0f;

    @Override
    public void update(float dt) {

        spriteFlipTimeLeft -= dt;
        if (spriteFlipTimeLeft <= 0) {
            spriteFlipTimeLeft = spriteFlipTime;
            spriteIndex++;
            if (spriteIndex > 3) {
                spriteIndex = 1;
            }
            obj2.getComponent(SpriteRenderer.class).setSprite(spritesheet.getSprite(spriteIndex));
        }

        obj2.transform.position.x += 100 * dt;

        for (GameObject go : this.gameObjects) {
            go.update(dt);
        }

        this.renderer.render();

        if (KeyListener.isKeyTyped(GLFW_KEY_TAB)) {
            Window.setCurrentScene(1);
        }


        if (MouseListener.isDragging() && MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_2)) {
            Vector2f delta = MouseListener.getDelta();
            float zoom = this.camera.getZoom();
            delta.mul(1.0f / zoom);
            this.camera.position.x += delta.x;
            this.camera.position.y -= delta.y;
        }

        if (MouseListener.getScrollY() != 0) {
            float zoom = this.camera.getZoom();
            zoom += MouseListener.getScrollY() * 0.1f;
            this.camera.setZoom(zoom);
        }

    }

    @Override
    public void imgui() {
        ImGui.begin("Scene");
        if (ImGui.button("Add GameObject")) {
            this.showCreationWindow = !this.showCreationWindow;
        }

        if (this.showCreationWindow) {
            ObjectCreationWindow.imgui(this);
        }


        ImGui.end();

    }


}
