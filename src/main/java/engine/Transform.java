package engine;

import org.joml.Vector2f;

public class Transform {

    public Vector2f position;
    public Vector2f scale;
    public float rotation; // radians

    public Transform() {
        init(new Vector2f(), new Vector2f(1, 1), 0.0f);
    }

    public Transform(Vector2f position) {
        init(position, new Vector2f(1, 1), 0.0f);
    }

    public Transform(Vector2f position, Vector2f scale) {
        init(position, scale, 0.0f);
    }

    public Transform(Vector2f position, Vector2f scale, float rotation) {
        init(position, scale, rotation);
    }

    public void init(Vector2f position, Vector2f scale, float rotation) {
        this.position = position;
        this.scale = scale;
        this.rotation = rotation;
    }

    public Transform copy() {
        return new Transform(
                new Vector2f(this.position),
                new Vector2f(this.scale),
                this.rotation
        );
    }

    public void copy(Transform other) {
        other.position.set(this.position);
        other.scale.set(this.scale);
        other.rotation = this.rotation;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Transform t)) return false;
        return position.equals(t.position)
                && scale.equals(t.scale)
                && rotation == t.rotation;
    }
}
