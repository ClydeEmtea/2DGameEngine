package engine;

import components.SpriteRenderer;
import imgui.ImGui;
import org.joml.Vector4f;
import project.ProjectManager;

import java.util.ArrayList;
import java.util.List;

public class GameObject {

    private String name;
    private List<Component> components;
    public Transform transform;
    private int zIndex;

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

    public void addComponent(Component c) {
        components.add(c);
        c.gameObject = this;
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
        for (Component c : components) {
            c.imgui();
        }
        // pole musí být mimo if, aby ImGui mohl měnit hodnoty
        float[] pos = { transform.position.x, transform.position.y };

// dragFloat2 změní obsah pole
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

        // Použij sliderInt místo dragInt pro omezení rozsahu
        int[] zIndexArr = { zIndex };
        if (ImGui.sliderInt("Z Index", zIndexArr, 0, 100)) {
            zIndex = zIndexArr[0];
        }
        // Refresh the whole renderer if zIndex changes
        if (ImGui.button("Apply Z Index Change")) {
            ProjectManager.get().saveProject();
            Window.setCurrentScene(0);
        }




    }
}
