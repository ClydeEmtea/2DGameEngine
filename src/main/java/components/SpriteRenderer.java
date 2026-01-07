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
    private ImBoolean flippedX = new  ImBoolean(false);
    private ImBoolean flippedY = new ImBoolean(false);

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
        Vector2f[] coords = new Vector2f[sprite.getTexCoords().length];
        for (int i = 0; i < coords.length; i++) {
            coords[i] = new Vector2f(sprite.getTexCoords()[i]);
        }
        return coords;
    }


    public void setSprite(String filename) {
        Window.getView().renderer.remove(this.gameObject);
        this.color = WHITE;
        this.sprite = new Sprite(AssetPool.getTexture(filename));
        sprite.revert();
        if (flipX) sprite.flipHorizontally();
        if (flipY) sprite.flipVertically();
        this.setDirty();
        this.isColorOnly = false;
        Window.getView().renderer.add(this.gameObject);
    }

    public void setSprite(Sprite sprite) {
        Window.getView().renderer.remove(this.gameObject);
        this.color = WHITE;
        this.sprite = sprite;
        sprite.revert();
        if (flipX) sprite.flipHorizontally();
        if (flipY) sprite.flipVertically();
        this.setDirty();
        this.isColorOnly = false;
        Window.getView().renderer.add(this.gameObject);
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

        // --- CHECKBOXY ---
        ImGui.checkbox("Flip X", flippedX);
        ImGui.checkbox("Flip Y", flippedY);

        // --- SYNCHRONIZACE STAVU ---
        if (flippedX.get() != flipX) {
            flipX = flippedX.get();
            flip(true, false);
        }

        if (flippedY.get() != flipY) {
            flipY = flippedY.get();
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
        if (sprite == null) return;

        if (horizontal) {
            sprite.flipHorizontally();
        }

        if (vertical) {
            sprite.flipVertically();
        }

        setDirty();
    }



    public void setFlip(boolean horizontal, boolean vertical) {
        // horizontální flip
        if (flipX != horizontal) {
            flipX = horizontal;
            sprite.flipHorizontally(); // swap pouze pokud se stav mění
            setDirty();
        }

        // vertikální flip
        if (flipY != vertical) {
            flipY = vertical;
            sprite.flipVertically(); // swap pouze pokud se stav mění
            setDirty();
        }

        // synchronizace ImGui checkboxů
        flippedX.set(flipX);
        flippedY.set(flipY);
    }


    /**
     * Rychlé getter metody pro skript
     */
    public boolean getFlipX() { return flipX; }
    public boolean getFlipY() { return flipY; }

    public Sprite getSprite() {
        return sprite;
    }
}
