package components;

import engine.Component;
import engine.Transform;
import org.joml.Vector2f;
import org.joml.Vector4f;
import render.Texture;

import static util.Constants.WHITE;

public class SpriteRenderer extends Component {

    private Vector4f color;
    private Sprite sprite;

    private Transform lastTransform;
    private boolean isDirty = true;

    public SpriteRenderer(Vector4f color) {
        this.color = color;
        this.sprite = new Sprite(null);

    }

    public SpriteRenderer(Sprite sprite) {
        this.color = WHITE;
        this.sprite = sprite;
    }

    @Override
    public void start() {
        this.lastTransform = gameObject.transform.copy();
    }

    @Override
    public void update(float dt) {
        if (!this.lastTransform.equals(this.gameObject.transform)) {
            this.gameObject.transform.copy(this.lastTransform);
            isDirty = true;
        }

    }

    public Vector4f getColor() {
        return color;
    }

    public Texture getTexture() {
        return sprite.getTexture();
    }

    public Vector2f[] getTexCoords() {
        return sprite.getTexCoords();
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
        this.isDirty = true;
    }

    public void setColor(Vector4f color) {
        if (!this.color.equals(color)) {
            this.color = color;
            this.isDirty = true;
        }
    }

    public boolean isDirty() {
        return isDirty;
    }
    public void setClean() {
        isDirty = false;
    }
}
