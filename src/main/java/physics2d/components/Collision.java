package physics2d.components;

import engine.GameObject;

public class Collision {
    public final GameObject self;
    public final GameObject other;

    public Collision(GameObject self, GameObject other) {
        this.self = self;
        this.other = other;
    }
}
