package engine;

import imgui.ImGui;

public abstract class Component {

    public GameObject gameObject = null;
    protected boolean isColorOnly = false;

    public abstract void update(float dt);

    public void start() {}

    public void initScript() {}

    public void imgui() {
        ImGui.text("Component: " + this.getClass().getSimpleName());
    }

    public boolean isColorOnly() {
        return isColorOnly;
    }

    public void initScriptEditor() {};

    public void updateScript(float dt) {}
}
