package engine;

import components.*;
import imgui.ImGui;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import imgui.type.ImString;
import org.joml.Vector2f;
import org.joml.Vector4f;
import physics2d.components.Box2DCollider;
import physics2d.components.CircleCollider;
import physics2d.components.RigidBody2D;
import project.ProjectManager;
import util.AssetPool;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static util.Constants.EDITOR_SCALE;

public class GameObject {

    private String name;
    private List<Component> components;
    public Transform transform;
    private int zIndex;
    private String newScriptName = "";
    private Float editorRotationDeg = null;
    private ImString imName;


    public GameObject(String name) {
        this.name = name;
        this.imName = new ImString(name, 50);
        this.zIndex = 0;
        this.components = new ArrayList<>();
        this.transform = new Transform();
    }

    public GameObject(String name, Transform transform, int zIndex) {
        this.name = name;
        this.imName = new ImString(name, 50);
        this.zIndex = zIndex;
        this.components = new ArrayList<>();
        this.transform = transform;
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
        return getComponent(ShapeRenderer.class).getShapeType();
    }

    public ShapeRenderer getShaperenderer() {
        return getComponent(ShapeRenderer.class);
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
        ImGui.inputText("##Name", imName);
        ImGui.sameLine();
        if (ImGui.button("Submit")) {
            if (imName.isNotEmpty()) setName(String.valueOf(imName));
        }

        ImGui.beginGroup();

        ImGui.invisibleButton("##GO_DROP_TARGET", ImGui.getContentRegionAvailX(), 50);

        if (ImGui.beginDragDropTarget()) {
            Object payload = ImGui.acceptDragDropPayload("ASSET_FILE");
            if (payload != null) {
                onAssetDropped((String) payload);
            }
            ImGui.endDragDropTarget();
        }

        ImGui.endGroup();
        float[] pos = {
                transform.position.x * EDITOR_SCALE,
                transform.position.y * EDITOR_SCALE
        };

        if (ImGui.dragFloat2("Position", pos, 1.0f)) {
            transform.position.x = pos[0] / EDITOR_SCALE;
            transform.position.y = pos[1] / EDITOR_SCALE;
        }

        // =========================
        // SCALE (editor units)
        // =========================
        float[] scale = {
                transform.scale.x * EDITOR_SCALE,
                transform.scale.y * EDITOR_SCALE
        };

        if (ImGui.dragFloat2("Scale", scale, 1.0f)) {

            boolean keepAspect = ImGui.getIO().getKeyCtrl();

            if (keepAspect) {
                float ratio = transform.scale.x / transform.scale.y;
                if (Math.abs(scale[0] - transform.scale.x * EDITOR_SCALE)
                        > Math.abs(scale[1] - transform.scale.y * EDITOR_SCALE)) {
                    scale[1] = scale[0] / ratio;
                } else {
                    scale[0] = scale[1] * ratio;
                }
            }

            transform.scale.x = scale[0] / EDITOR_SCALE;
            transform.scale.y = scale[1] / EDITOR_SCALE;

        }


        float[] roundness = { transform.roundness };
        if (ImGui.dragFloat("Roundness", roundness, 0.005f, 0.0f, 0.5f)) {
            transform.roundness = roundness[0];
        }

        if (editorRotationDeg == null) {
            editorRotationDeg = (float) Math.toDegrees(transform.rotation);
        }

        float speed = ImGui.getIO().getKeyCtrl() ? 0.1f : 1.0f;

        float[] rot = { editorRotationDeg };

        if (ImGui.dragFloat("Rotation", rot, speed, -180.0f, 180.0f)) {
            editorRotationDeg = rot[0];
            transform.rotation = (float) Math.toRadians(-editorRotationDeg);
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

        if (ImGui.colorEdit4("Color", color)) {
            sr.setColor(new Vector4f(color[0], color[1], color[2], color[3]));
        }

        int[] zIndexArr = { zIndex };
        if (ImGui.sliderInt("Z Index", zIndexArr, 0, 100)) {
            this.zIndex = zIndexArr[0];
        }


        ImGui.dummy(0,20);


        ImVec4 oldButton = ImGui.getStyle().getColor(ImGuiCol.Button);
        ImVec4 oldHover = ImGui.getStyle().getColor(ImGuiCol.ButtonHovered);
        ImVec4 oldActive = ImGui.getStyle().getColor(ImGuiCol.ButtonActive);

        ImGui.getStyle().setColor(ImGuiCol.Button, 0.6f, 0.2f, 0.2f, 1.0f);
        ImGui.getStyle().setColor(ImGuiCol.ButtonHovered, 0.7f, 0.3f, 0.4f, 1.0f);
        ImGui.getStyle().setColor(ImGuiCol.ButtonActive, 0.8f, 0.1f, 0.2f, 1.0f);

        if (ImGui.button("Remove object")) {
            Window.getView().removeGameObject(this);
            Window.getView().setActiveGameObject(null);
        }

        ImGui.getStyle().setColor(ImGuiCol.Button, oldButton.x, oldButton.y, oldButton.z, oldButton.w);
        ImGui.getStyle().setColor(ImGuiCol.ButtonHovered, oldHover.x, oldHover.y, oldHover.z, oldHover.w);
        ImGui.getStyle().setColor(ImGuiCol.ButtonActive, oldActive.x, oldActive.y, oldActive.z, oldActive.w);



        ImGui.dummy(0,20);

        ImGui.text("Add Script");

        ImString buffer = new ImString(newScriptName, 64);
        if (ImGui.inputText("##ScriptName", buffer)) {
            newScriptName = buffer.get().trim();
        }

        ImGui.sameLine();

        if (ImGui.button("Create")) {
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


        ImGui.separator();

        List<Component> componentsSnapshot = new ArrayList<>(getAllComponents());

        for (Component component : componentsSnapshot) {
            if (component instanceof SpriteRenderer) {
                ImGui.dummy(0, 10);
                component.imgui();
            }
        }

        for (Component component : componentsSnapshot) {
            if (component instanceof ScriptComponent) {
                ImGui.dummy(0, 10);
                component.imgui();
            }
        }

        for (Component component : componentsSnapshot) {
            if (component instanceof RigidBody2D) {
                ImGui.dummy(0, 10);
                component.imgui();
                                if (ImGui.button("Remove rigid body")) {
                    this.removeComponent(component);
                }
            }
        }

        for (Component component : componentsSnapshot) {
            if (component instanceof Box2DCollider) {
                ImGui.dummy(0, 10);
                component.imgui();
                if (ImGui.button("Remove box collider")) {
                    this.removeComponent(component);
                }
            }
        }

        for (Component component : componentsSnapshot) {
            if (component instanceof CircleCollider) {
                ImGui.dummy(0, 10);
                component.imgui();
                if (ImGui.button("Remove circle collider")) {
                    this.removeComponent(component);
                }
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

        if (path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg")) {

            SpriteRenderer sr = getComponent(SpriteRenderer.class);

            if (sr == null) {
                sr = new SpriteRenderer(new Sprite(AssetPool.getTexture(filename)));
                addComponent(sr);
            } else {
                sr.setSprite(filename);
            }

            System.out.println("Texture assigned: " + path);
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

}
