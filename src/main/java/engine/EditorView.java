package engine;

import actions.MultiCreateGameObjectAction;
import actions.MultiDeleteGameObjectAction;
import components.*;
import gui.ObjectCreationWindow;
import gui.RightSidebar;
import imgui.ImGui;
import imgui.flag.ImGuiHoveredFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import org.joml.Vector2f;
import org.joml.Vector4f;
import physics2d.Physics2D;
import physics2d.components.Box2DCollider;
import physics2d.components.CapsuleCollider;
import physics2d.components.CircleCollider;
import physics2d.components.RigidBody2D;
import project.ProjectManager;
import render.Renderer;
import render.Texture;
import util.AssetPool;
import util.IdGenerator;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static util.Constants.DEFAULT_FRAGMENT_SHADER;
import static util.Constants.DEFAULT_VERTEX_SHADER;

public class EditorView extends View {

    public boolean showCreationWindow = false;

    private boolean dragging = false;
    private Vector2f dragOffset = new Vector2f();
    public boolean showHitboxes = true;

    private boolean selectionDragging = false;
    private Vector2f selectionStart = new Vector2f();
    private Vector2f selectionEnd = new Vector2f();


    private GameObject animovany;
    private Spritesheet spritesheet;
    private int spriteFlipTimeLeft = 100;
    private int spriteFlipTime = 100;
    private int spriteIndex = 0;

    private boolean draggingObjects = false;
    private final java.util.Map<GameObject, Vector2f> dragStartPositions = new java.util.HashMap<>();


    public EditorView() {
        System.out.println("Editor Scene Initialized");
    }

    @Override
    public void init() {
        for (Sound sound : AssetPool.getAllSounds()) {
            sound.stop();
        }
        loadResources();
        this.camera = new Camera(new Vector2f());
        this.physics2D = new Physics2D();
        this.gameObjects = new ArrayList<>();
        this.renderer = new Renderer();
        this.activeGameObject = null;
        this.activeGroup = null;

        List<GameObject> viewObjects;
        if (currentScene != null) {
            viewObjects = ProjectManager.get().loadSceneObjects(currentScene.getName());
        } else {
            viewObjects = ProjectManager.get().loadSceneObjects("MainScene");
        }
        if (viewObjects != null) {
            for (GameObject go : viewObjects) {
                this.addGameObjectToView(go);
            }
        }

        for (GameObject go : gameObjects) {
            for (Component c : go.getAllScripts()) {
                c.initScriptEditor();
            }
        }

        IdGenerator.resetCurrentIdFromObjects(this.gameObjects);
    }


    private void loadResources() {
        AssetPool.getShader(DEFAULT_VERTEX_SHADER, DEFAULT_FRAGMENT_SHADER);

    }


    @Override
    public void update(float dt) {

//        spriteFlipTimeLeft -= dt;
//        if (spriteFlipTimeLeft <= 0) {
//            spriteFlipTimeLeft = spriteFlipTime;
//            spriteIndex++;
//            if (spriteIndex > 3) {
//                spriteIndex = 1;
//            }
//            animovany.getComponent(SpriteRenderer.class).setSprite(spritesheet.getSprite(spriteIndex));
//        }

        if (ProjectManager.get().getCurrentProject() != null && false) {
            System.out.println(ProjectManager.get().getCurrentProject().getProjectPath());
            System.out.println(ProjectManager.get().getCurrentProject().getImagesPath());
        }

        Grid.render(this);


        this.renderer.render();

        for (GameObject go : this.gameObjects) {
            go.update(dt);
        }


        if (showHitboxes) {
            renderHitboxes();
        }

        keyboardHandles();

        movement();

        selectionHandles();

    }

    public GameObject getObjectById(long id) {
        for (GameObject go : gameObjects) {
            if (go.getId() == id) return go;
        }
        return null;
    }


    private void keyboardHandles() {
        if (KeyListener.isKeyTyped(GLFW_KEY_TAB)) {
            Window.setCurrentView(1);
        }

        if (KeyListener.isKeyPressed(GLFW_KEY_LEFT_CONTROL) && KeyListener.isKeyTyped(GLFW_KEY_S)) {
            if (ProjectManager.get().getCurrentProject() != null) {
                ProjectManager.get().saveProject();
                System.out.println("Project saved!");
            }
        }

        if (KeyListener.isKeyPressed(GLFW_KEY_LEFT_CONTROL) && KeyListener.isKeyTyped(GLFW_KEY_D)) {
            duplicateSelected();
        }
        if (KeyListener.isKeyPressed(GLFW_KEY_LEFT_CONTROL) && KeyListener.isKeyTyped(GLFW_KEY_Z)) {
            Window.getActionManager().undo();
        }
        if (KeyListener.isKeyPressed(GLFW_KEY_LEFT_CONTROL) && KeyListener.isKeyTyped(GLFW_KEY_Y)) {
            Window.getActionManager().redo();
        }

        if (KeyListener.isKeyPressed(GLFW_KEY_LEFT_CONTROL) && KeyListener.isKeyTyped(GLFW_KEY_A)) {
            RightSidebar.selectedObjects.clear();
            for (GameObject go : gameObjects) {
                RightSidebar.selectedObjects.add(go);
            }
            if (!RightSidebar.selectedObjects.isEmpty()) {
                setActiveGameObject(RightSidebar.selectedObjects.get(0));
            }
        }

        if (KeyListener.isKeyTyped(GLFW_KEY_DELETE)) {

            List<GameObject> toDelete = new ArrayList<>();

            toDelete.addAll(RightSidebar.selectedObjects);

            for (Group g : RightSidebar.selectedGroups) {
                toDelete.addAll(g.getObjects());
            }

            Window.getActionManager().execute(
                    new MultiDeleteGameObjectAction("Delete", toDelete)
            );

            RightSidebar.selectedObjects.clear();
            RightSidebar.selectedGroups.clear();
            activeGameObject = null;
            activeGroup = null;
        }

    }

    private void duplicateSelected() {
        List<GameObject> duplicates = new ArrayList<>();
        List<String> reservedNames = new ArrayList<>();
        for (GameObject go : RightSidebar.selectedObjects) {
            String name = generateDuplicateName(go.getName(), reservedNames);
            reservedNames.add(name);
            GameObject created = new GameObject(name, go.transform.copy(), go.getZIndex());
            created.transform.position.x += go.transform.scale.x;
            if (go.getShaperenderer() != null) {
                created.addComponent(new ShapeRenderer());
                created.getShaperenderer().setShapeType(go.getShapeType());
                List<Vector2f> ps = new ArrayList<>(go.getShaperenderer().getPoints());
                created.getShaperenderer().setPoints(ps);
            }
            SpriteRenderer goSr = go.getComponent(SpriteRenderer.class);
            if (goSr != null) {
                if (goSr.isColorOnly) {
                    created.addComponent(new SpriteRenderer(goSr.getColor()));
                } else {
                    Texture tex = goSr.getTexture();
                    if (tex != null) {
                        SpriteRenderer createdSr = new SpriteRenderer(new Sprite(tex));
                        createdSr.setColor(goSr.getColor());
                        created.addComponent(createdSr);
                    }
                }
            }

            for (Component c : go.getAllComponents()) {
                if (c instanceof ScriptComponent) {
                    ScriptComponent sc = new ScriptComponent(((ScriptComponent) c).getClassName(), ((ScriptComponent) c).getFilePath());
                    created.addComponent(sc);
                }
            }

            // RigidBody2D
            RigidBody2D rb = go.getComponent(RigidBody2D.class);
            if (rb != null) {
                RigidBody2D newRb = new RigidBody2D();
                // přenést další relevantní hodnoty jako velocity, acceleration atd.
                newRb.setVelocity(rb.getVelocity());
                newRb.setMass(rb.getMass());
                newRb.setBodyType(rb.getBodyType());
                newRb.setAngularDamping(rb.getAngularDamping());
                newRb.setContinuousCollision(rb.isContinuousCollision());
                newRb.setFixedRotation(rb.isFixedRotation());
                newRb.setLinearDamping(rb.getLinearDamping());
                newRb.setDensity(rb.getDensity());
                newRb.setFriction(rb.getFriction());
                newRb.setRestitution(rb.getRestitution());
                created.addComponent(newRb);
            }

            // Box2DCollider
            Box2DCollider bc = go.getComponent(Box2DCollider.class);
            if (bc != null) {
                Box2DCollider newBc = new Box2DCollider();
                newBc.setHalfSize(bc.getHalfSize());
                newBc.setOffset(bc.getOffset());
                created.addComponent(newBc);
            }

            // CircleCollider
            CircleCollider cc = go.getComponent(CircleCollider.class);
            if (cc != null) {
                CircleCollider newCc = new CircleCollider();
                newCc.setRadius(cc.getRadius());
                newCc.setOffset(cc.getOffset());
                created.addComponent(newCc);
            }

            // CapsuleCollider
            CapsuleCollider cap = go.getComponent(CapsuleCollider.class);
            if (cap != null) {
                CapsuleCollider newCap = new CapsuleCollider();
                newCap.setRadius(cap.getRadius());
                newCap.setHeight(cap.getHeight());
                newCap.setOffset(cap.getOffset());
                created.addComponent(newCap);
            }

            duplicates.add(created);
        }

        Window.getActionManager().execute(
                new MultiCreateGameObjectAction(
                        "Duplicate GameObject",
                        duplicates
                )
        );

        RightSidebar.selectedObjects.clear();
        RightSidebar.selectedObjects.addAll(duplicates);
        activeGameObject = duplicates.get(0);
    }

    private String generateDuplicateName(String originalName, List<String> reservedNames) {
        String baseName = stripIndex(originalName);
        int index = 1;

        String name;
        while (true) {
            name = baseName + " (" + index + ")";
            if (getObjectByName(name) == null && !reservedNames.contains(name)) {
                return name;
            }
            index++;
        }
    }

    private String stripIndex(String name) {
        return name.replaceAll("\\s*\\(\\d+\\)$", "");
    }



    private void movement() {
        float camSpeed = 0.04f * camera.getZoom();
        if (KeyListener.isKeyPressed(GLFW_KEY_W)) {
            if (ImGui.isAnyItemActive() || KeyListener.isKeyPressed(GLFW_KEY_LEFT_CONTROL)) {
                return;
            }
            camera.position.y += camSpeed;
        }
        if (KeyListener.isKeyPressed(GLFW_KEY_S)) {
            if (ImGui.isAnyItemActive() || KeyListener.isKeyPressed(GLFW_KEY_LEFT_CONTROL)) {
                return;
            }
            camera.position.y -= camSpeed;
        }
        if (KeyListener.isKeyPressed(GLFW_KEY_A)) {
            if (ImGui.isAnyItemActive() || KeyListener.isKeyPressed(GLFW_KEY_LEFT_CONTROL)) {
                return;
            }
            camera.position.x -= camSpeed;
        }
        if (KeyListener.isKeyPressed(GLFW_KEY_D)) {
            if (ImGui.isAnyItemActive() || KeyListener.isKeyPressed(GLFW_KEY_LEFT_CONTROL)) {
                return;
            }
            camera.position.x += camSpeed;
        }

        float itemSpeed = 0.005f * camera.getZoom();
        if (KeyListener.isKeyPressed(GLFW_KEY_UP)) {
            if (ImGui.isAnyItemActive() || (activeGroup == null && activeGameObject == null)) {
                return;
            }
            for (GameObject go : RightSidebar.selectedObjects) {
                go.transform.position.y += itemSpeed;
            }
        }
        if (KeyListener.isKeyPressed(GLFW_KEY_DOWN)) {
            if (ImGui.isAnyItemActive() || (activeGroup == null && activeGameObject == null)) {
                return;
            }
            for (GameObject go : RightSidebar.selectedObjects) {
                go.transform.position.y -= itemSpeed;
            }
        }
        if (KeyListener.isKeyPressed(GLFW_KEY_LEFT)) {
            if (ImGui.isAnyItemActive() || (activeGroup == null && activeGameObject == null)) {
                return;
            }
            for (GameObject go : RightSidebar.selectedObjects) {
                go.transform.position.x -= itemSpeed;
            }
        }
        if (KeyListener.isKeyPressed(GLFW_KEY_RIGHT)) {
            if (ImGui.isAnyItemActive() || (activeGroup == null && activeGameObject == null)) {
                return;
            }
            for (GameObject go : RightSidebar.selectedObjects) {
                go.transform.position.x += itemSpeed;
            }
        }

        if (MouseListener.isDragging() &&
                MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_2)) {

            Vector2f delta = MouseListener.getDelta();

            float unitsPerPixelX =
                    camera.getProjectionSize().x * camera.getZoom()
                            / Window.get().getWidth();

            float unitsPerPixelY =
                    camera.getProjectionSize().y * camera.getZoom()
                            / Window.get().getHeight();

            camera.position.x += delta.x * unitsPerPixelX;
            camera.position.y -= delta.y * unitsPerPixelY;
        }

        if (MouseListener.getScrollY() != 0) {

            if (ImGui.isAnyItemHovered() || ImGui.isAnyItemActive() ||
                    ImGui.isWindowHovered(ImGuiHoveredFlags.AnyWindow)) {
                return;
            }

            float zoomFactor = 1.1f;
            float scroll = MouseListener.getScrollY();

            Vector2f mouseWorldBefore =
                    camera.screenToWorld(MouseListener.getX(), MouseListener.getY());

            if (scroll > 0) {
                camera.setZoom(camera.getZoom() / zoomFactor);
            } else {
                camera.setZoom(camera.getZoom() * zoomFactor);
            }

            Vector2f mouseWorldAfter =
                    camera.screenToWorld(MouseListener.getX(), MouseListener.getY());

            camera.position.add(
                    mouseWorldBefore.x - mouseWorldAfter.x,
                    mouseWorldBefore.y - mouseWorldAfter.y
            );
        }
    }

    private void selectionHandles() {
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
                if (go.isLocked()) continue;
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
                    } else if (KeyListener.isKeyPressed(GLFW_KEY_LEFT_ALT)) {
                        RightSidebar.selectedObjects.remove(go);
                        if (activeGameObject.equals(go)) {
                            activeGameObject = null;
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
                    draggingObjects = true;
                    dragOffset.set(
                            mouseWorld.x - activeTransform.position.x,
                            mouseWorld.y - activeTransform.position.y
                    );

                    dragStartPositions.clear();
                    for (GameObject go : RightSidebar.selectedObjects) {
                        dragStartPositions.put(go, new Vector2f(go.transform.position));
                    }
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
            if (!MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_1) && draggingObjects) {

                draggingObjects = false;

                java.util.Map<GameObject, Vector2f> endPositions = new java.util.HashMap<>();

                boolean moved = false;

                for (GameObject go : dragStartPositions.keySet()) {
                    Vector2f start = dragStartPositions.get(go);
                    Vector2f end = new Vector2f(go.transform.position);
                    endPositions.put(go, end);

                    if (!start.equals(end)) {
                        moved = true;
                    }
                }

                if (moved) {
                    Window.getActionManager().execute(
                            new actions.MultiMoveAction(
                                    "Move Objects",
                                    new java.util.HashMap<>(dragStartPositions),
                                    endPositions
                            )
                    );
                }

                dragStartPositions.clear();
            }

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

            Vector2f start = new Vector2f(selectionStart);
            Vector2f end = new Vector2f(selectionEnd);

            boolean ctrl = KeyListener.isKeyPressed(GLFW_KEY_LEFT_CONTROL);
            boolean alt = KeyListener.isKeyPressed(GLFW_KEY_LEFT_ALT);


            if (!ctrl && !alt) {
                RightSidebar.selectedObjects.clear();
            }

            // rohy výběru
            Vector2f[] selectionCorners = new Vector2f[]{
                    new Vector2f(start.x, start.y),
                    new Vector2f(end.x, start.y),
                    new Vector2f(end.x, end.y),
                    new Vector2f(start.x, end.y)
            };

            for (GameObject go : gameObjects) {
                if (go.isLocked()) continue;
                Transform t = go.transform;
                Vector2f pos = t.position;
                Vector2f size = t.scale;
                float rotation = t.rotation;

                Vector2f[] corners = new Vector2f[]{
                        new Vector2f(-size.x/2, -size.y/2),
                        new Vector2f(size.x/2, -size.y/2),
                        new Vector2f(size.x/2, size.y/2),
                        new Vector2f(-size.x/2, size.y/2)
                };

                float cx = pos.x;
                float cy = pos.y;

                float cos = (float)Math.cos(rotation);
                float sin = (float)Math.sin(rotation);

                // transformace rohu do světových souřadnic
                for (int i = 0; i < corners.length; i++) {
                    float rx = corners[i].x * cos - corners[i].y * sin + cx;
                    float ry = corners[i].x * sin + corners[i].y * cos + cy;
                    corners[i].set(rx, ry);
                }

                boolean intersects = false;

                // 1) Test: nějaký roh objektu uvnitř výběru
                for (Vector2f corner : corners) {
                    if (pointInPolygon(corner, selectionCorners)) {
                        intersects = true;
                        break;
                    }
                }

                // 2) Test: nějaký roh výběru uvnitř objektu
                if (!intersects) {
                    for (Vector2f sc : selectionCorners) {
                        if (pointInPolygon(sc, corners)) {
                            intersects = true;
                            break;
                        }
                    }
                }

                // 3) Test: hrany se protínají
                if (!intersects) {
                    for (int i = 0; i < corners.length; i++) {
                        Vector2f a1 = corners[i];
                        Vector2f a2 = corners[(i + 1) % corners.length];

                        for (int j = 0; j < selectionCorners.length; j++) {
                            Vector2f b1 = selectionCorners[j];
                            Vector2f b2 = selectionCorners[(j + 1) % selectionCorners.length];

                            if (linesIntersect(a1, a2, b1, b2)) {
                                intersects = true;
                                break;
                            }
                        }
                        if (intersects) break;
                    }
                }

                if (intersects && !RightSidebar.selectedObjects.contains(go)) {
                    if (!alt)
                        RightSidebar.selectedObjects.add(go);
                    else RightSidebar.selectedObjects.remove(go);
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

    private boolean linesIntersect(Vector2f p1, Vector2f p2, Vector2f q1, Vector2f q2) {
        float s1x = p2.x - p1.x;
        float s1y = p2.y - p1.y;
        float s2x = q2.x - q1.x;
        float s2y = q2.y - q1.y;

        float s = (-s1y * (p1.x - q1.x) + s1x * (p1.y - q1.y)) / (-s2x * s1y + s1x * s2y);
        float t = ( s2x * (p1.y - q1.y) - s2y * (p1.x - q1.x)) / (-s2x * s1y + s1x * s2y);

        return s >= 0 && s <= 1 && t >= 0 && t <= 1;
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
        float cx = pos.x;
        float cy = pos.y;

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
//        if (ImGui.button("Add GameObject")) {
//            this.showCreationWindow = !this.showCreationWindow;
//        }

        if (this.showCreationWindow) {
            if (ObjectCreationWindow.imgui(this)) {
                this.showCreationWindow = false; // zavřít po vytvoření
            }
        }

        activeGameObjectImGui();
        if (ImGui.isMouseReleased(GLFW_MOUSE_BUTTON_2) && (!MouseListener.wasDragging())) {
            if (activeGameObject != null)
                ImGui.openPopup("ActiveGOContextMenu");
            else ImGui.openPopup("SceneContextMenu");
        }

        if (ImGui.beginPopup("SceneContextMenu")) {

            if (ImGui.menuItem("Add GameObject")) {

                GameObject go = createNewObjectWithoutAdding();

                go.transform.position.set(
                        camera.screenToWorld(
                                MouseListener.getX(),
                                MouseListener.getY()
                        )
                );

                Window.getActionManager().execute(
                        new MultiCreateGameObjectAction(
                                "Create GameObject",
                                List.of(go)
                        )
                );

                Window.getView().setActiveGameObject(go);
            }

            ImGui.endPopup();
        }



        if (ImGui.beginPopup("ActiveGOContextMenu")) {
            if (ImGui.menuItem("Set to background")) {
                activeGameObject.setZIndex(-100);
            }
            if (ImGui.menuItem("Delete")) {
                removeGameObject(activeGameObject);
                setActiveGameObject(null);
            }
            if (ImGui.menuItem("Duplicate")) {
                duplicateSelected();
            }
            ImGui.separator();
            if (ImGui.menuItem("Add Animation")) {
                activeGameObject.addComponent(new Animation());
            }
            ImGui.separator();
            if (ImGui.menuItem("Add RigidBody")) {
                if (activeGameObject.getComponent(RigidBody2D.class) == null) {
                    activeGameObject.addComponent(new RigidBody2D());
                }
            }

            if (ImGui.menuItem("Add Box Collider")) {
                if (activeGameObject.getComponent(Box2DCollider.class) == null && activeGameObject.getComponent(CircleCollider.class) == null) {
                    Box2DCollider collider = new Box2DCollider();
                    collider.setHalfSize(new Vector2f(activeGameObject.transform.scale));
//                    collider.setOrigin(new float[] {collider.getHalfSize().x, collider.getHalfSize().y});
                    activeGameObject.addComponent(collider);
                }
            }

            if (ImGui.menuItem("Add Circle Collider")) {
                if (activeGameObject.getComponent(CircleCollider.class) == null && activeGameObject.getComponent(Box2DCollider.class) == null) {
                    activeGameObject.addComponent(new CircleCollider());
                }
            }

            if (ImGui.menuItem("Add Capsule Collider")) {
                if (activeGameObject.getComponent(CircleCollider.class) == null && activeGameObject.getComponent(Box2DCollider.class) == null) {
                    activeGameObject.addComponent(new CapsuleCollider());
                }
            }
            ImGui.endPopup();
        }


        ImGui.end();
    }

    private boolean pointInRotatedRect(Vector2f point, Vector2f pos, Vector2f size, float rotation) {
        float cx = pos.x;
        float cy = pos.y;

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



