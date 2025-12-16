package engine;

import components.*;
import gui.ObjectCreationWindow;
import gui.RightSidebar;
import imgui.ImGui;
import imgui.flag.ImGuiHoveredFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import org.joml.Vector2f;
import org.joml.Vector4f;
import project.ProjectManager;
import render.Renderer;
import util.AssetPool;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class EditorView extends View {

    public boolean showCreationWindow = false;
    protected List<GameObject> gridLines = new ArrayList<>();

    private boolean dragging = false;
    private Vector2f dragOffset = new Vector2f();
    public boolean showHitboxes = true;

    private boolean selectionDragging = false;
    private Vector2f selectionStart = new Vector2f();
    private Vector2f selectionEnd = new Vector2f();


    public EditorView() {
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
        List<GameObject> viewObjects = ProjectManager.get().loadSceneObjects("MainScene");
        if (viewObjects != null) {
            for (GameObject go : viewObjects) {
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

        if (showHitboxes) {
            renderHitboxes();
        }

        if (KeyListener.isKeyTyped(GLFW_KEY_TAB)) {
            Window.setCurrentView(1);
        }

        if (KeyListener.isKeyPressed(GLFW_KEY_LEFT_CONTROL) && KeyListener.isKeyTyped(GLFW_KEY_S)) {
            if (ProjectManager.get().getCurrentProject() != null) {
                ProjectManager.get().saveProject();
                System.out.println("Project saved!");
            }
        }


        if (MouseListener.isDragging() && MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_2)) {
            Vector2f delta = MouseListener.getDelta();
            float zoom = this.camera.getZoom();
            delta.mul(1.0f / zoom);
            this.camera.position.x += delta.x;
            this.camera.position.y -= delta.y;
        }

        if (MouseListener.getScrollY() != 0) {

            if (ImGui.isAnyItemHovered() || ImGui.isAnyItemActive() ||
                    ImGui.isWindowHovered(ImGuiHoveredFlags.AnyWindow)) {
                return;
            }

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
            if (ImGui.isAnyItemHovered() || ImGui.isAnyItemActive() ||
                    ImGui.isWindowHovered(ImGuiHoveredFlags.AnyWindow)) {
                return;
            }

            Vector2f mouseWorld = camera().screenToWorld(
                    MouseListener.getX(),
                    MouseListener.getY()
            );

            boolean found = false;

            for (GameObject go : gameObjects) {
                SpriteRenderer sr = go.getComponent(SpriteRenderer.class);
                if (sr == null) continue;

                Transform t = go.transform;
                ShapeType shape = go.getShapeType();

                boolean contains = (shape == ShapeType.DEFAULT) ?
                        pointInRotatedRect(mouseWorld, t.position, t.scale, t.rotation) :
                        go.getShaperenderer().containsPoint(mouseWorld);

                if (contains) {
                    setActiveGameObject(go);
                    found = true;

                    if (KeyListener.isKeyPressed(GLFW_KEY_LEFT_CONTROL)) {
                        // Ctrl = přidat do selection, ale nevymazat předchozí
                        if (!RightSidebar.selectedObjects.contains(go)) {
                            RightSidebar.selectedObjects.add(go);
                        }
                    } else {
                        // Normální klik = jen tento objekt
                        RightSidebar.syncSelectionWithActive(this);
                    }

                    break;
                }
            }

            if (!found) {
                setActiveGameObject(null);
                RightSidebar.syncSelectionWithActive(this);
            }
        }




        if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_1)
                && getActiveGameObject() != null) {

            if (ImGui.isAnyItemHovered() || ImGui.isAnyItemActive() ||
                    ImGui.isWindowHovered(ImGuiHoveredFlags.AnyWindow)) {
                return;
            }

            Vector2f mouseWorld = camera().screenToWorld(
                    MouseListener.getX(),
                    MouseListener.getY()
            );

            Transform activeTransform = getActiveGameObject().transform;
            ShapeType shape = getActiveGameObject().getShapeType();

            if (!dragging) {
                boolean contains = (shape == ShapeType.DEFAULT) ?
                        pointInRotatedRect(mouseWorld, activeTransform.position, activeTransform.scale, activeTransform.rotation) :
                        getActiveGameObject().getShaperenderer().containsPoint(mouseWorld);

                if (contains) {
                    dragging = true;
                    dragOffset.set(mouseWorld.x - activeTransform.position.x, mouseWorld.y - activeTransform.position.y);
                }
            }

            if (dragging) {
                Vector2f oldPosition = new Vector2f(activeTransform.position); // pevná kopie
                activeTransform.position.set(mouseWorld.x - dragOffset.x, mouseWorld.y - dragOffset.y);
                Vector2f diff = new Vector2f(activeTransform.position).sub(oldPosition); // rozdíl aktivního objektu

                // Pohyb všech ostatních vybraných objektů
                for (GameObject go : RightSidebar.selectedObjects) {
                    if (go != getActiveGameObject()) {
                        go.transform.position.add(diff.x, diff.y);
                    }
                }
            }

        } else {
            dragging = false;
        }

        if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_1)) {
            if (ImGui.isAnyItemHovered() || ImGui.isAnyItemActive() ||
                    ImGui.isWindowHovered(ImGuiHoveredFlags.AnyWindow)) {
                return;
            }

            Vector2f mouseWorld = camera.screenToWorld(MouseListener.getX(), MouseListener.getY());

            if (getActiveGameObject() == null) {
                if (!selectionDragging) {
                    selectionDragging = true;
                    selectionStart.set(mouseWorld);
                    selectionEnd.set(mouseWorld);
                } else {
                    selectionEnd.set(mouseWorld);
                }
            }
        }


        if (!MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_1) && selectionDragging) {
            selectionDragging = false;

            Vector2f min = new Vector2f(
                    Math.min(selectionStart.x, selectionEnd.x),
                    Math.min(selectionStart.y, selectionEnd.y)
            );
            Vector2f max = new Vector2f(
                    Math.max(selectionStart.x, selectionEnd.x),
                    Math.max(selectionStart.y, selectionEnd.y)
            );

            boolean ctrl = KeyListener.isKeyPressed(GLFW_KEY_LEFT_CONTROL);

            if (!ctrl) {
                RightSidebar.selectedObjects.clear();
            }

            for (GameObject go : gameObjects) {
                Vector2f pos = go.transform.position;
                Vector2f size = go.transform.scale;

                if (pos.x + size.x > min.x && pos.x < max.x &&
                        pos.y + size.y > min.y && pos.y < max.y) {
                    if (!RightSidebar.selectedObjects.contains(go)) {
                        RightSidebar.selectedObjects.add(go);
                    }
                }
            }

            if (!RightSidebar.selectedObjects.isEmpty()) {
                setActiveGameObject(RightSidebar.selectedObjects.get(0));
            } else {
                setActiveGameObject(null);
            }
        }


        if (selectionDragging) {
            Vector2f min = new Vector2f(
                    Math.min(selectionStart.x, selectionEnd.x),
                    Math.min(selectionStart.y, selectionEnd.y)
            );
            Vector2f max = new Vector2f(
                    Math.max(selectionStart.x, selectionEnd.x),
                    Math.max(selectionStart.y, selectionEnd.y)
            );

            Renderer.beginLines(new Vector4f(0, 0.7f, 1, 1)); // modrý
            Renderer.drawLine(min.x, min.y, max.x, min.y);
            Renderer.drawLine(max.x, min.y, max.x, max.y);
            Renderer.drawLine(max.x, max.y, min.x, max.y);
            Renderer.drawLine(min.x, max.y, min.x, min.y);
            Renderer.endLines();
        }




    }

    private void renderHitboxes() {
        glDisable(GL_TEXTURE_2D);
        glLineWidth(1.5f);

        for (GameObject go : gameObjects) {
            if (activeGameObject == go || (activeGroup != null && activeGroup.contains(go)) || RightSidebar.selectedObjects.contains(go)) {
                Transform t = go.transform;

                if (go.getShapeType() == ShapeType.DEFAULT) {
                    drawRotatedRect(t.position, t.scale, t.rotation);
                } else {
                    ShapeRenderer sr = go.getShaperenderer();
                    if (sr == null) continue;

                    List<Vector2f> pts = sr.getWorldPoints();
                    if (pts.size() < 2) continue;

                    Renderer.beginLines(new Vector4f(0f, 1f, 0f, 1f));

                    for (int i = 0; i < pts.size(); i++) {
                        Vector2f a = pts.get(i);
                        Vector2f b = pts.get((i + 1) % pts.size());
                        Renderer.drawLine(a.x, a.y, b.x, b.y);
                    }

                    Renderer.endLines();
                }
            }
        }

        glEnable(GL_TEXTURE_2D);
    }

    private void drawRotatedRect(Vector2f pos, Vector2f size, float rotation) {
        float cx = pos.x + size.x * 0.5f;
        float cy = pos.y + size.y * 0.5f;

        Vector2f[] corners = {
                new Vector2f(-size.x/2, -size.y/2),
                new Vector2f( size.x/2, -size.y/2),
                new Vector2f( size.x/2,  size.y/2),
                new Vector2f(-size.x/2,  size.y/2)
        };

        float cos = (float) Math.cos(rotation);
        float sin = (float) Math.sin(rotation);

        Vector2f[] world = new Vector2f[4];
        for (int i = 0; i < 4; i++) {
            float rx = corners[i].x * cos - corners[i].y * sin;
            float ry = corners[i].x * sin + corners[i].y * cos;
            world[i] = new Vector2f(cx + rx, cy + ry);
        }

        Renderer.beginLines(new Vector4f(0, 1, 0, 1));

        for (int i = 0; i < 4; i++) {
            Vector2f a = world[i];
            Vector2f b = world[(i + 1) % 4];
            Renderer.drawLine(a.x, a.y, b.x, b.y);
        }

        Renderer.endLines();
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
    public void imgui(float dt) {
        ImGui.begin("Scene");
        if (ImGui.treeNodeEx("Engine Info", ImGuiTreeNodeFlags.DefaultOpen)) {
            ImGui.text("FPS: " + (int) (1f / dt));
            ImGui.text("Frame Time: " + (dt * 1000) + " ms");
            if (ImGui.button("Exit")) {
                glfwSetWindowShouldClose(glfwGetCurrentContext(), true);
            }
            ImGui.treePop();
        }
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

    private boolean pointInRotatedRect(Vector2f point, Vector2f pos, Vector2f size, float rotation) {
        float cx = pos.x + size.x * 0.5f;
        float cy = pos.y + size.y * 0.5f;

        Vector2f[] corners = {
                new Vector2f(-size.x/2, -size.y/2),
                new Vector2f(size.x/2, -size.y/2),
                new Vector2f(size.x/2, size.y/2),
                new Vector2f(-size.x/2, size.y/2)
        };

        float cos = (float) Math.cos(rotation);
        float sin = (float) Math.sin(rotation);

        Vector2f[] world = new Vector2f[4];
        for (int i = 0; i < 4; i++) {
            float rx = corners[i].x * cos - corners[i].y * sin;
            float ry = corners[i].x * sin + corners[i].y * cos;
            world[i] = new Vector2f(cx + rx, cy + ry);
        }

        return pointInPolygon(point, world);
    }
    private boolean pointInPolygon(Vector2f point, Vector2f[] polygon) {
        boolean result = false;
        int j = polygon.length - 1;
        for (int i = 0; i < polygon.length; i++) {
            if ((polygon[i].y > point.y) != (polygon[j].y > point.y) &&
                    (point.x < (polygon[j].x - polygon[i].x) * (point.y - polygon[i].y) /
                            (polygon[j].y - polygon[i].y) + polygon[i].x)) {
                result = !result;
            }
            j = i;
        }
        return result;
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