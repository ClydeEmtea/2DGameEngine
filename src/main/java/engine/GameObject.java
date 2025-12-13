package engine;

import components.ScriptComponent;
import components.SpriteRenderer;
import imgui.ImGui;
import imgui.type.ImString;
import org.joml.Vector2f;
import org.joml.Vector4f;
import project.ProjectManager;

import java.util.ArrayList;
import java.util.List;

public class GameObject {

    private String name;
    private List<Component> components;
    public Transform transform;
    private int zIndex;
    private String newScriptName = "";

    public GameObject(String name) {
        this.name = name;
        this.zIndex = 0;
        this.components = new ArrayList<>();
        this.transform = new Transform();
    }

    public GameObject(String name, Transform transform, int zIndex) {
        this.name = name;
        this.zIndex = zIndex;
        this.components = new ArrayList<>();
        this.transform = transform;
    }

    public String getName() {
        return name;
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

    public int getZIndex() {
        return zIndex;
    }

    public void setZIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    public void imgui() {
        float[] pos = { transform.position.x, transform.position.y };

        if (ImGui.dragFloat2("Position", pos, 0.5f)) {
            transform.position.x = pos[0];
            transform.position.y = pos[1];
        }

        float[] scale = { transform.scale.x, transform.scale.y };
        if (ImGui.dragFloat2("Scale", scale, 0.5f)) {
            transform.scale.x = scale[0];
            transform.scale.y = scale[1];
        }

        Component c = this.components.get(0);
        if (c instanceof SpriteRenderer sr && sr.isColorOnly()) {
            float[] color = {
                    sr.getColor().x,
                    sr.getColor().y,
                    sr.getColor().z,
                    sr.getColor().w
            };

            if (ImGui.colorEdit4("Color", color)) {
                sr.setColor(new Vector4f(color[0], color[1], color[2], color[3]));
            }
        }

        int[] zIndexArr = { zIndex };
        if (ImGui.sliderInt("Z Index", zIndexArr, 0, 100)) {
            zIndex = zIndexArr[0];
        }
        if (ImGui.button("Apply Z Index Change")) {
            ProjectManager.get().saveProject();
            Vector2f camPos = Window.getScene().camera.position;
            float camZoom = Window.getScene().camera.getZoom();
            Window.setCurrentScene(0);
            Window.getScene().camera.position = camPos;
            Window.getScene().camera.setZoom(camZoom);
            Window.getScene().setActiveGameObject(this);
        }

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
        for (Component component : getAllComponents()) {
            ImGui.dummy(0, 10);
            component.imgui();
        }


    }
}
