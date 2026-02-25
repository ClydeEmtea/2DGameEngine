package physics2d.components;

import engine.GameObject;
import engine.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CollisionComponent extends Component {

    private final Set<GameObject> touching = new HashSet<>();
    private final Set<GameObject> previousTouching = new HashSet<>();

    public void begin(GameObject other) {
        touching.add(other);
    }

    public void end(GameObject other) {
        touching.remove(other);
    }

    public void sync() {
        previousTouching.clear();
        previousTouching.addAll(touching);
    }

    public Set<GameObject> getEntered() {
        Set<GameObject> entered = new HashSet<>(touching);
        entered.removeAll(previousTouching);
        return entered;
    }

    public Set<GameObject> getExited() {
        Set<GameObject> exited = new HashSet<>(previousTouching);
        exited.removeAll(touching);
        return exited;
    }

    public boolean isTouching(GameObject other) {
        return touching.contains(other);
    }

    public boolean isTouching(String name) {
        return touching.stream().anyMatch(go -> go.getName().equals(name));
    }

    public boolean isColliding() {
        return !touching.isEmpty();
    }

    public Set<GameObject> getAllTouching() {
        return Collections.unmodifiableSet(touching);
    }

    public void clear() {
        touching.clear();
    }

    @Override
    public void update(float dt) {}
}