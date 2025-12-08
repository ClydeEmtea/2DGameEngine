package engine;

import components.*;
import gui.ObjectCreationWindow;
import imgui.ImGui;
import imgui.flag.ImGuiHoveredFlags;
import org.joml.Vector2f;
import project.ProjectManager;
import render.Renderer;
import util.AssetPool;
import util.Constants;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class EditorScene extends Scene {

    public boolean showCreationWindow = false;
    protected List<GameObject> gridLines = new ArrayList<>();

    private boolean dragging = false;
    private Vector2f dragOffset = new Vector2f();



    public EditorScene() {
        System.out.println("Editor Scene Initialized");
    }

    @Override
    public void init() {
        loadResources();
        this.camera = new Camera(new Vector2f());
        Grid.initialize(this);
        for (GameObject line : gridLines) {
            line.start();
            this.renderer.add(line);
        }
        List<GameObject> sceneObjects = ProjectManager.get().loadSceneObjects("MainScene");
        if (sceneObjects != null) {
            for (GameObject go : sceneObjects) {
                this.addGameObjectToScene(go);
            }
        }

    }

    private void loadResources() {
        AssetPool.getShader("vertexDefault.glsl", "fragmentDefault.glsl");

    }


    @Override
    public void update(float dt) {

        if (ProjectManager.get().getCurrentProject() != null && false) {
            System.out.println(ProjectManager.get().getCurrentProject().getProjectPath());
            System.out.println(ProjectManager.get().getCurrentProject().getImagesPath());
        }

        Grid.render(this);
        for (GameObject line : gridLines) {
            line.update(dt);
        }

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

            float oldZoom = this.camera.getZoom();
            float newZoom = oldZoom + MouseListener.getScrollY() * 0.1f;

            newZoom = Math.max(0.1f, Math.min(newZoom, 10f));

            float cxBefore = camera.screenToWorld(Window.get().getWidth()/ 4f,Window.get().getHeight() / 4f).x;
            float cyBefore = camera.screenToWorld(Window.get().getWidth()/ 4f, Window.get().getHeight() / 4f).y;

            camera.setZoom(newZoom);

            float cxAfter = camera.screenToWorld(Window.get().getWidth()/ 4f,Window.get().getHeight() / 4f).x;
            float cyAfter = camera.screenToWorld(Window.get().getWidth()/ 4f, Window.get().getHeight() / 4f).y;

            camera.position.x += (cxBefore - cxAfter);
            camera.position.y += (cyBefore - cyAfter);
        }

        if (MouseListener.mouseButtonClicked(GLFW_MOUSE_BUTTON_1)) {
            if (ImGui.isAnyItemHovered() || ImGui.isAnyItemActive() || ImGui.isWindowHovered(ImGuiHoveredFlags.AnyWindow)) {
                return;
            }
            Vector2f mouseWorld = new Vector2f (this.camera().screenToWorld(MouseListener.getX(), MouseListener.getY()));
            boolean found = false;
            for (GameObject go : this.gameObjects) {
                if (go.getComponent(SpriteRenderer.class) != null) {
                    Vector2f pos = go.transform.position;
                    Vector2f size = go.transform.scale;
                    if (mouseWorld.x >= pos.x && mouseWorld.x <= pos.x + size.x &&
                            mouseWorld.y >= pos.y && mouseWorld.y <= pos.y + size.y) {
                        setActiveGameObject(go);
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                setActiveGameObject(null);
            }
        }

        if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_1) && this.getActiveGameObject() != null) {
            if (ImGui.isAnyItemHovered() || ImGui.isAnyItemActive() || ImGui.isWindowHovered(ImGuiHoveredFlags.AnyWindow)) {
                return;
            }
            Vector2f mouseWorld = this.camera().screenToWorld(MouseListener.getX(), MouseListener.getY());
            Vector2f goPos = this.getActiveGameObject().transform.position;
            Vector2f goSize = this.getActiveGameObject().transform.scale;

            if (!dragging) {
                if (mouseWorld.x >= goPos.x && mouseWorld.x <= goPos.x + goSize.x &&
                        mouseWorld.y >= goPos.y && mouseWorld.y <= goPos.y + goSize.y) {

                    dragging = true;

                    dragOffset.set(mouseWorld.x - goPos.x, mouseWorld.y - goPos.y);
                }
            }

            if (dragging) {
                this.getActiveGameObject().transform.position.set(
                        mouseWorld.x - dragOffset.x,
                        mouseWorld.y - dragOffset.y
                );
            }

        } else {
            dragging = false;
        }

    }

    @Override
    public void addLine(GameObject line) {
         gridLines.add(line);
         line.start();
         this.renderer.add(line);
    }

    public void removeLine(GameObject line) {
        gridLines.remove(line);
        this.renderer = new Renderer(); // TODO: Optimize this
        for (GameObject go : gameObjects) {
            this.renderer.add(go);
        }
        for (GameObject go : gridLines) {
            this.renderer.add(go);
        }
    }

    @Override
    public List<GameObject> getGridLines() {
        return this.gridLines;
    }

    @Override
    public void imgui() {
        ImGui.begin("Scene");
        if (ImGui.button("Add GameObject")) {
            this.showCreationWindow = !this.showCreationWindow;
        }

        if (this.showCreationWindow) {
            if (ObjectCreationWindow.imgui(this)) {
                this.showCreationWindow = false; // zavřít po vytvoření
            }
        }

        ImGui.end();
    }



}


//this.spritesheet = AssetPool.getSpritesheet("C:\\Users\\EmTea\\IdeaProjects\\2DGameEngine\\assets\\images\\spritesheet.png");
//
//        obj1.addComponent(new SpriteRenderer(new Sprite(AssetPool.getTexture("C:\\Users\\EmTea\\IdeaProjects\\2DGameEngine\\assets\\images\\texture.png"))));
//        this.addGameObjectToScene(obj1);
//
//
//        obj2.addComponent(new SpriteRenderer(spritesheet.getSprite(0)));
//        this.addGameObjectToScene(obj2);
//
//        GameObject obj3 = new GameObject("nikdo", new Transform(new Vector2f(150,100), new Vector2f(258,200)), 3);
//        obj3.addComponent(new SpriteRenderer(new Sprite(AssetPool.getTexture("C:\\Users\\EmTea\\IdeaProjects\\2DGameEngine\\assets\\images\\blendImage2.png"))));
//        this.addGameObjectToScene(obj3);
//
//        GameObject obj4 = new GameObject("White", new Transform(new Vector2f(400,100), new Vector2f(258,200)), 4);
//        obj4.addComponent(new SpriteRenderer(new Vector4f(1,1,1,1)));
//        this.addGameObjectToScene(obj4);

//        spriteFlipTimeLeft -= dt;
//        if (spriteFlipTimeLeft <= 0) {
//            spriteFlipTimeLeft = spriteFlipTime;
//            spriteIndex++;
//            if (spriteIndex > 3) {
//                spriteIndex = 1;
//            }
//            obj2.getComponent(SpriteRenderer.class).setSprite(spritesheet.getSprite(spriteIndex));
//        }
//
//        obj2.transform.position.x += 100 * dt;