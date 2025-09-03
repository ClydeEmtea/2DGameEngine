package engine;

public abstract class Component {

    public GameObject gameObject = null;
    protected boolean isColorOnly = false;

    public abstract void update(float dt);

    public void start() {}

    public void imgui() {

    }

    public boolean isColorOnly() {
        return isColorOnly;
    }
}
