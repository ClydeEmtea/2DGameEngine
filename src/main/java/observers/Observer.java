package observers;

import engine.GameObject;

public interface Observer {
    void onNotify(GameObject gameObject, Event event);
}
