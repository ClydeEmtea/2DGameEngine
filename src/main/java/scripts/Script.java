package scripts;

import engine.GameObject;
import engine.KeyListener;
import engine.MouseListener;
import engine.Window;

public interface Script {
    void init();
    void update(float dt);
    void setEnvironment(GameObject go, Window window, MouseListener mouseListener, KeyListener keyListener);
}

