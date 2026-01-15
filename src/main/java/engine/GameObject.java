package engine;

import actions.ValueChangeAction;
import components.*;
import gui.ImGuiLayer;
import gui.ImGuiUtils;
import imgui.ImGui;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import imgui.type.ImString;
import observers.Event;
import observers.EventSystem;
import observers.EventType;
import org.joml.Vector2f;
import org.joml.Vector4f;
import physics2d.components.Box2DCollider;
import physics2d.components.CapsuleCollider;
import physics2d.components.CircleCollider;
import physics2d.components.RigidBody2D;
import project.ProjectManager;
import util.AssetPool;
import util.HasId;
import util.IdGenerator;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static util.Constants.EDITOR_SCALE;

public class GameObject implements HasId {

    private String name;
    private List<Component> components;
    public Transform transform;
    private int zIndex;
    private String newScriptName = "";
    private Float editorRotationDeg = null;
    private ImString imName;
    private boolean locked = false;

    private long id;

    private Vector2f positionDragStart = new Vector2f();
    private Vector2f scaleDragStart = new Vector2f();
    private float roundnessDragStart;
    private float rotationDragStartDeg;
    private Vector4f colorDragStart;
    private int zIndexDragStart;


    public GameObject(String name) {
        this.name = name;
        this.imName = new ImString(name, 50);
        this.zIndex = 0;
        this.components = new ArrayList<>();
        this.transform = new Transform();
        this.id = IdGenerator.getNextId();
    }

    public GameObject(String name, Transform transform, int zIndex) {
        this.name = name;
        this.imName = new ImString(name, 50);
        this.zIndex = zIndex;
        this.components = new ArrayList<>();
        this.transform = transform;
        this.id = IdGenerator.getNextId();
    }

    public GameObject(String name, Transform transform, int zIndex, long id) {
        this.name = name;
        this.imName = new ImString(name, 50);
        this.zIndex = zIndex;
        this.components = new ArrayList<>();
        this.transform = transform;
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public <T extends Component> T getComponent(Class<T> componentClass) {
        for (Component c : components) {
            if (componentClass.isAssignableFrom(c.getClass())) {
                try {
                    return componentClass.cast(c);
                } catch (ClassCastException e) {
                    Window.addError(e.getMessage());
                    EventSystem.notify(null, new Event(EventType.ErrorEvent));
                    e.printStackTrace();
                    assert false : "Error: Class cast exception in getComponent";
                }
            }
        }
        return null;
    }

    public List<Component> getAllComponents() {
        return components;
    }

    public List<Component> getAllScripts() {
        List<Component> result = new ArrayList<>();
        for (Component c : components) {
            if (c instanceof ScriptComponent sc) {
                result.add(sc);
            }
        }
        return result;
    }

    public List<Component> getAllAnimations() {
        List<Component> result = new ArrayList<>();
        for (Component c : components) {
            if (c instanceof Animation a) {
                result.add(a);
            }
        }
        return result;
    }

    public void addComponent(Component c) {
        components.add(c);
        c.gameObject = this;

        if (c instanceof ScriptComponent sc) {
            sc.onAddedToGameObject();
        }
    }

    public <T extends Component> void removeComponent(Class<T> componentClass) {
        for (int i = 0; i < components.size(); i++) {
            if (componentClass.isAssignableFrom(components.get(i).getClass())) {
                components.remove(i);
                return;
            }
        }
    }

    public void removeComponent(Component c) {
        if (c == null) return;

        components.remove(c);
        c.gameObject = null; // Odpojení reference na GameObject
    }

    public void update(float dt) {
        for (Component c : components) {
            c.update(dt);
        }
    }

    public void start() {
        for (Component c : components) {
            c.start();
        }
    }

    public ShapeType getShapeType() {
        if (getComponent(ShapeRenderer.class) == null) {
            addComponent(new ShapeRenderer());
        }
        return getComponent(ShapeRenderer.class).getShapeType();
    }

    public ShapeRenderer getShaperenderer() {
        return getComponent(ShapeRenderer.class);
    }

    public Animation getAnimation(String name) {
        for (Component c : components) {
            if (c instanceof Animation a) {
                if (a.getAnimationName().equals(name)) {
                    return a;
                }
            }
        }
        return null;
    }

    public boolean isAnimationPlaying(String name) {
        Animation anim = getAnimation(name);
        if (anim != null) {
            return anim.isPlaying();
        }
        return false;
    }

    public boolean isAnyAnimationPlaying() {
        for (Component c : components) {
            if (c instanceof Animation a) {
                if (a.isPlaying()) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getZIndex() {
        return zIndex;
    }

    public void setZIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    public float getRotation() {
        return transform.rotation;
    }

    public void imgui() {
// --- Name ---
        ImGui.inputText("##Name", imName);
        ImGui.sameLine();

        if (ImGuiUtils.lightBlueButton("Submit")) {
            if (imName.isNotEmpty()) {
                String oldName = getName();
                String newName = imName.get();

                if (!oldName.equals(newName)) {
                    setName(newName);

                    Window.getActionManager().execute(
                            new ValueChangeAction<>(
                                    "Change Name",
                                    this,
                                    GameObject::setName,
                                    oldName,
                                    newName
                            )
                    );
                }
            }
        }


        ImGui.beginGroup();

        ImGui.invisibleButton("##GO_DROP_TARGET", ImGui.getContentRegionAvailX(), 50);

        if (ImGui.beginDragDropTarget()) {
            Object payload = ImGui.acceptDragDropPayload("ASSET_FILE");

            if (payload != null)
                onAssetDropped((String) payload);

            ImGui.endDragDropTarget();
        }

        ImGui.endGroup();


        float[] pos = {
                transform.position.x * EDITOR_SCALE,
                transform.position.y * EDITOR_SCALE
        };

        ImGui.dragFloat2("Position", pos, 1.0f);

// --- START DRAG ---
        if (ImGui.isItemActivated()) {
            positionDragStart.set(transform.position);
        }

// --- LIVE UPDATE ---
        if (ImGui.isItemEdited()) {
            transform.position.x = pos[0] / EDITOR_SCALE;
            transform.position.y = pos[1] / EDITOR_SCALE;
        }

// --- END DRAG → CREATE ACTION ---
        if (ImGui.isItemDeactivatedAfterEdit()) {

            Vector2f oldValue = new Vector2f(positionDragStart);
            Vector2f newValue = new Vector2f(transform.position);

            Window.getActionManager().execute(
                    new ValueChangeAction<>(
                            "Change Position",
                            this,
                            (go, v) -> go.transform.position.set(v),
                            oldValue,
                            newValue
                    )
            );
        }


        // =========================
        // SCALE (editor units)
        // =========================
        float[] scale = {
                transform.scale.x * EDITOR_SCALE,
                transform.scale.y * EDITOR_SCALE
        };

        ImGui.dragFloat2("Scale", scale, 1.0f);

// --- START DRAG ---
        if (ImGui.isItemActivated()) {
            scaleDragStart.set(transform.scale);
        }

// --- LIVE UPDATE ---
        if (ImGui.isItemEdited()) {

            boolean keepAspect = ImGui.getIO().getKeyCtrl();

            if (keepAspect) {
                float ratio = scaleDragStart.x / scaleDragStart.y;

                float dx = Math.abs(scale[0] - scaleDragStart.x * EDITOR_SCALE);
                float dy = Math.abs(scale[1] - scaleDragStart.y * EDITOR_SCALE);

                if (dx > dy) {
                    scale[1] = scale[0] / ratio;
                } else {
                    scale[0] = scale[1] * ratio;
                }
            }

            transform.scale.x = scale[0] / EDITOR_SCALE;
            transform.scale.y = scale[1] / EDITOR_SCALE;
        }

// --- END DRAG → ACTION ---
        if (ImGui.isItemDeactivatedAfterEdit()) {

            Vector2f oldValue = new Vector2f(scaleDragStart);
            Vector2f newValue = new Vector2f(transform.scale);

            Window.getActionManager().execute(
                    new ValueChangeAction<>(
                            "Change Scale",
                            this,
                            (go, v) -> go.transform.scale.set(v),
                            oldValue,
                            newValue
                    )
            );
        }



        float[] roundness = { transform.roundness };

        ImGui.dragFloat("Roundness", roundness, 0.005f, 0.0f, 0.5f);

// --- START ---
        if (ImGui.isItemActivated()) {
            roundnessDragStart = transform.roundness;
        }

// --- LIVE ---
        if (ImGui.isItemEdited()) {
            transform.roundness = roundness[0];
        }

// --- END → ACTION ---
        if (ImGui.isItemDeactivatedAfterEdit()) {

            Window.getActionManager().execute(
                    new ValueChangeAction<>(
                            "Change Roundness",
                            this,
                            (go, v) -> go.transform.roundness = v,
                            roundnessDragStart,
                            transform.roundness
                    )
            );
        }


        float rotationDeg = (float) Math.toDegrees(-transform.rotation);
        float[] rot = { rotationDeg };

        float speed = ImGui.getIO().getKeyCtrl() ? 0.1f : 1.0f;

        ImGui.dragFloat("Rotation", rot, speed, -180.0f, 180.0f);

// --- START ---
        if (ImGui.isItemActivated()) {
            rotationDragStartDeg = rotationDeg;
        }

// --- LIVE ---
        if (ImGui.isItemEdited()) {
            transform.rotation = (float) Math.toRadians(-rot[0]);
        }

// --- END → ACTION ---
        if (ImGui.isItemDeactivatedAfterEdit()) {

            float oldRad = (float) Math.toRadians(-rotationDragStartDeg);
            float newRad = transform.rotation;

            Window.getActionManager().execute(
                    new ValueChangeAction<>(
                            "Change Rotation",
                            this,
                            (go, v) -> go.transform.rotation = v,
                            oldRad,
                            newRad
                    )
            );
        }



        if (ImGui.button("Reset rotation")) {
            editorRotationDeg = 0.0f;
            transform.rotation = 0.0f;
        }

        ImGui.dummy(0,20);


        SpriteRenderer sr = getComponent(SpriteRenderer.class);

        float[] color = {
                sr.getColor().x,
                sr.getColor().y,
                sr.getColor().z,
                sr.getColor().w
        };

// --- START ---
        if (ImGui.colorEdit4("Color", color)) {

            // live update
            sr.setColor(new Vector4f(
                    color[0],
                    color[1],
                    color[2],
                    color[3]
            ));
        }

// --- START DRAG ---
        if (ImGui.isItemActivated()) {
            colorDragStart = new Vector4f(sr.getColor());
        }

// --- END → ACTION ---
        if (ImGui.isItemDeactivatedAfterEdit()) {

            Vector4f oldValue = new Vector4f(colorDragStart);
            Vector4f newValue = new Vector4f(sr.getColor());

            Window.getActionManager().execute(
                    new ValueChangeAction<>(
                            "Change Color",
                            this,
                            GameObject::setColor,
                            oldValue,
                            newValue
                    )
            );

        }


        int[] zIndexArr = { zIndex };

// --- START ---
        if (ImGui.sliderInt("Z Index", zIndexArr, -100, 100)) {
            this.zIndex = zIndexArr[0];
        }

// --- START DRAG ---
        if (ImGui.isItemActivated()) {
            zIndexDragStart = zIndex;
        }

// --- END → ACTION ---
        if (ImGui.isItemDeactivatedAfterEdit()) {

            int oldValue = zIndexDragStart;
            int newValue = zIndex;

            Window.getActionManager().execute(
                    new ValueChangeAction<>(
                            "Change Z Index",
                            this,
                            (go, v)-> go.zIndex = v,
                            oldValue,
                            newValue
                    )
            );
        }


        if (ImGui.checkbox("Lock GameObject", locked)) {
            boolean oldValue = locked;
            locked = !locked;

            Window.getActionManager().execute(
                    new ValueChangeAction<>(
                            "Toggle Lock",
                            this,
                            (go, v) -> go.locked = v,
                            oldValue,
                            locked
                    )
            );
        }




        ImGui.dummy(0,20);




        if (ImGuiUtils.redButton("Remove object")) {
            Window.getView().removeGameObject(this);
            Window.getView().setActiveGameObject(null);
        }





        ImGui.dummy(0,20);

        ImGui.text("Add Script");

        ImString buffer = new ImString(newScriptName, 64);
        if (ImGui.inputText("##ScriptName", buffer)) {
            newScriptName = buffer.get().trim();
        }

        ImGui.sameLine();

        if (ImGuiUtils.lightBlueButton("Create")) {
            if (!newScriptName.isBlank()) {
                try {
                    var pm = ProjectManager.get();
                    var scriptPath = pm.createNewScript(newScriptName);

                    String className = newScriptName;
                    this.addComponent(new components.ScriptComponent(className, scriptPath));

                    pm.openInVSCode(scriptPath);

                    newScriptName = "";

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            ProjectManager.get().saveProject();
        }


        ImGui.dummy(0,10);
        ImGui.separator();
        ImGui.dummy(0,10);


        List<Component> componentsSnapshot = new ArrayList<>(getAllComponents());

        for (Component component : componentsSnapshot) {
            if (component instanceof SpriteRenderer) {
                ImGui.dummy(0, 10);
                component.imgui();
                ImGui.dummy(0,10);
                ImGui.separator();
                ImGui.dummy(0,10);
            }
        }


        for (Component component : componentsSnapshot) {
            if (component instanceof ScriptComponent) {
                ImGui.dummy(0, 10);
                component.imgui();
                ImGui.dummy(0,10);
                ImGui.separator();
                ImGui.dummy(0,10);
            }
        }
        for (Component component : componentsSnapshot) {
            if (component instanceof Animation) {
                ImGui.dummy(0, 10);
                component.imgui();
                if (ImGuiUtils.redButton("Remove animation")) {
                    this.removeComponent(component);
                }
                ImGui.dummy(0,10);
                ImGui.separator();
                ImGui.dummy(0,10);
            }
        }

        for (Component component : componentsSnapshot) {
            if (component instanceof RigidBody2D) {
                ImGui.dummy(0, 10);
                component.imgui();
                                if (ImGuiUtils.redButton("Remove rigid body")) {
                    this.removeComponent(component);
                }
                ImGui.dummy(0,10);
                ImGui.separator();
                ImGui.dummy(0,10);
            }
        }

        for (Component component : componentsSnapshot) {
            if (component instanceof Box2DCollider) {
                ImGui.dummy(0, 10);
                component.imgui();
                if (ImGuiUtils.redButton("Remove box collider")) {
                    this.removeComponent(component);
                }
                ImGui.dummy(0,10);
                ImGui.separator();
                ImGui.dummy(0,10);
            }
        }

        for (Component component : componentsSnapshot) {
            if (component instanceof CircleCollider) {
                ImGui.dummy(0, 10);
                component.imgui();
                if (ImGuiUtils.redButton("Remove circle collider")) {
                    this.removeComponent(component);
                }
                ImGui.dummy(0,10);
                ImGui.separator();
                ImGui.dummy(0,10);
            }
        }

        for (Component component : componentsSnapshot) {
            if (component instanceof CapsuleCollider) {
                ImGui.dummy(0, 10);
                component.imgui();
                if (ImGuiUtils.redButton("Remove capsule collider")) {
                    this.removeComponent(component);
                }
                ImGui.dummy(0,10);
                ImGui.separator();
                ImGui.dummy(0,10);
            }
        }

        for (Component component : componentsSnapshot) {
            if (component instanceof ShapeRenderer) {
                ImGui.dummy(0, 10);
                component.imgui();
            }
        }





    }

    private void onAssetDropped(String path) {
        String filename = Paths.get(path).getFileName().toString();

        String fullPath = path;

        if (fullPath.endsWith(".png") || fullPath.endsWith(".jpg") || fullPath.endsWith(".jpeg")) {

            String imagesPath = String.valueOf(ProjectManager.get()
                    .getCurrentProject()
                    .getImagesPath());

            // normalizace separátorů
            fullPath = fullPath.replace("\\", "/");
            imagesPath = imagesPath.replace("\\", "/");

            if (fullPath.startsWith(imagesPath)) {

                String relativePath = fullPath.substring(imagesPath.length());
                if (relativePath.startsWith("/"))
                    relativePath = relativePath.substring(1);

                SpriteRenderer sr = getComponent(SpriteRenderer.class);

                Sprite newSprite = new Sprite(AssetPool.getTexture(relativePath));


                if (sr == null) {

                    sr = new SpriteRenderer(
                            new Sprite(AssetPool.getTexture(relativePath))
                    );
                    addComponent(sr);
                } else {
                    // změna sprite – s undo/redo
                    Sprite oldSprite = sr.getSprite();

                    Window.getActionManager().execute(
                            new ValueChangeAction<>(
                                    "Change Sprite",
                                    this,
                                    (go, sprite) -> go.getComponent(SpriteRenderer.class).setSprite(sprite),
                                    oldSprite,
                                    newSprite
                            )
                    );
                }

                System.out.println("Texture assigned: " + relativePath);
            }
        }


        else if (path.endsWith(".java")) {
            int dot = filename.lastIndexOf('.');

            String className = (dot == -1)
                    ? filename
                    : filename.substring(0, dot);

            addComponent(new ScriptComponent(className, ProjectManager.get().getScriptPath(className)));
            System.out.println("Script added: " + className);
        }
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void setColor(Vector4f color) {
        SpriteRenderer sr = getComponent(SpriteRenderer.class);
        if (sr != null) {
            sr.setColor(color);
        }
    }
}
