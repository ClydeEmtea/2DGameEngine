package components;

import engine.Component;
import engine.Transform;
import engine.Window;
import imgui.ImGui;
import imgui.type.ImBoolean;
import org.joml.Vector2f;
import org.joml.Vector4f;
import render.Texture;
import util.AssetPool;

import static util.Constants.WHITE;

public class SpriteRenderer extends Component {

    private Vector4f color;
    private Sprite sprite;

    private Transform lastTransform;
    private boolean isDirty = true;

    private boolean flipX = false;
    private boolean flipY = false;


    private Vector2f[] customVertices = null;

    public SpriteRenderer(Vector4f color) {
        this.color = color;
        this.sprite = new Sprite(null);
        this.isColorOnly = true;

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
            setDirty();
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

    public void setSprite(String filename) {
        Window.getView().renderer.remove(this.gameObject);
        this.color = WHITE;
        this.sprite = new Sprite(AssetPool.getTexture(filename));
        this.setDirty();
        this.isColorOnly = false;
        Window.getView().renderer.add(this.gameObject);
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
        this.isDirty = true;
    }

    public void removeSprite() {
        this.sprite = new Sprite(null);
        this.color = WHITE;
        this.isColorOnly = true;
        this.setDirty();
    }

    public void setColor(Vector4f color) {
        if (!this.color.equals(color)) {
            this.color = color;
            this.setDirty();
        }
    }

    public boolean isDirty() {
        return isDirty;
    }
    public void setClean() {
        isDirty = false;
    }
    public void setDirty() {
        this.isDirty = true;
    }


    @Override
    public void imgui() {
        super.imgui();
        Texture tex = this.getTexture();
        if (tex != null) {
            int texId = tex.getId();
            ImGui.image(texId, 100,100);
        }
        if (!isColorOnly) {
            if (ImGui.button("Remove sprite")) {
                removeSprite();
            }
        }

        if (ImGui.button("Flip horizontally")) {
            flip(true, false);
        }
        if (ImGui.button("Flip vertically")) {
            flip(false, true);
        }


    }

    public void setCustomVertices(Vector2f[] vertices) {
        this.customVertices = vertices;
        this.setDirty();
    }

    public Vector2f[] getCustomVertices() {
        return customVertices;
    }

    public void flip(boolean horizontal, boolean vertical) {
        this.flipX = horizontal;
        this.flipY = vertical;

        if (sprite == null) return;

        Vector2f[] original = sprite.getTexCoords();
        if (original == null) return;

        Vector2f[] flipped = new Vector2f[original.length];
        for (int i = 0; i < original.length; i++) {
            float u = original[i].x;
            float v = original[i].y;

            if (flipX) u = 1 - u;
            if (flipY) v = 1 - v;

            flipped[i] = new Vector2f(u, v);
        }

        sprite.setTexCoords(flipped);
        setDirty();
    }


    // Můžeš přidat i rychlé getter funkce:
    public boolean isFlippedX() { return flipX; }
    public boolean isFlippedY() { return flipY; }
}
