package engine;

import components.Sprite;
import components.SpriteRenderer;
import components.Spritesheet;
import org.joml.Vector2f;
import util.AssetPool;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_TAB;
import static util.Constants.WHITE;

public class EditorScene extends Scene {

    private GameObject obj1 = new GameObject("Player", new Transform(new Vector2f(100,100), new Vector2f(256,256)), 1);
    private GameObject obj2 = new GameObject("Player", new Transform(new Vector2f(100,100), new Vector2f(128,128)), 2);
    private Spritesheet spritesheet;

    public EditorScene() {
        System.out.println("Editor Scene Initialized");
    }

    @Override
    public void init() {
        loadResources();
        this.camera = new Camera(new Vector2f());

        this.spritesheet = AssetPool.getSpritesheet("spritesheet.png");

        obj1.addComponent(new SpriteRenderer(new Sprite(AssetPool.getTexture("texture.png"))));
        this.addGameObjectToScene(obj1);


        obj2.addComponent(new SpriteRenderer(spritesheet.getSprite(0)));
        this.addGameObjectToScene(obj2);

        GameObject obj3 = new GameObject("nikdo", new Transform(new Vector2f(150,100), new Vector2f(258,200)), 3);
        obj3.addComponent(new SpriteRenderer(new Sprite(AssetPool.getTexture("blendImage2.png"))));
        this.addGameObjectToScene(obj3);

    }

    private void loadResources() {
        AssetPool.getShader("vertexDefault.glsl", "fragmentDefault.glsl");


        AssetPool.addSpritesheet("spritesheet.png",
                new Spritesheet(AssetPool.getTexture("spritesheet.png"), 16, 16, 26, 0));
    }


    private int spriteIndex = 0;
    private float spriteFlipTime = 0.2f;
    private float spriteFlipTimeLeft = 0.0f;

    @Override
    public void update(float dt) {
        System.out.println("FPS: " + (1 / dt));

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

    }


}
