package engine;

import gui.ImGuiLayer;
import imgui.ImGui;

public abstract class Component {

    public GameObject gameObject = null;
    protected boolean isColorOnly = false;

    public abstract void update(float dt);

    public void start() {}

    public void initScript() {}

    public void imgui() {
        ImGui.pushFont(ImGuiLayer.boldFont);
        ImGui.text("Component: " + this.getClass().getSimpleName());
        ImGui.popFont();
        ImGui.dummy(0,5);
    }

    public boolean isColorOnly() {
        return isColorOnly;
    }

    public void initScriptEditor() {};

    public void updateScript(float dt) {}

    public Component copy() {
        return this;
    }
}
