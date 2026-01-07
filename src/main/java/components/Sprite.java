package components;

import org.joml.Vector2f;
import render.Texture;

public class Sprite {

    private Texture texture;
    private Vector2f[] texCoords;
    private Vector2f[] originalTexCoords;

    public Sprite(Texture texture) {
        this.texture = texture;
        this.texCoords = new Vector2f[]{
                new Vector2f(0.0f, 1.0f), // top-left
                new Vector2f(1.0f, 1.0f), // top-right
                new Vector2f(1.0f, 0.0f), // bottom-right
                new Vector2f(0.0f, 0.0f)  // bottom-left
        };
        // deep copy pro original
        this.originalTexCoords = new Vector2f[texCoords.length];
        for (int i = 0; i < texCoords.length; i++) {
            this.originalTexCoords[i] = new Vector2f(texCoords[i]);
        }
    }

    public Sprite(Texture texture, Vector2f[] texCoords) {
        this.texture = texture;
        this.texCoords = new Vector2f[texCoords.length];
        this.originalTexCoords = new Vector2f[texCoords.length];
        for (int i = 0; i < texCoords.length; i++) {
            this.texCoords[i] = new Vector2f(texCoords[i]);
            this.originalTexCoords[i] = new Vector2f(texCoords[i]);
        }
    }

    public Texture getTexture() {
        return this.texture;
    }

    public Vector2f[] getTexCoords() {
        return this.texCoords;
    }

    public void setTexCoords(Vector2f[] coords) {
        this.texCoords = coords;
    }

    public Sprite copy() {
        Sprite s = new Sprite(this.texture);
        Vector2f[] copiedCoords = new Vector2f[this.texCoords.length];
        for (int i = 0; i < this.texCoords.length; i++) {
            copiedCoords[i] = new Vector2f(this.texCoords[i]);
        }
        s.setTexCoords(copiedCoords);
        return s;
    }

    /** Flip přes prohození souřadnic */
    public void flipHorizontally() {
        swap(0, 1); // top-left ↔ top-right
        swap(3, 2); // bottom-left ↔ bottom-right
    }

    public void flipVertically() {
        swap(0, 3); // top-left ↔ bottom-left
        swap(1, 2); // top-right ↔ bottom-right
    }

    private void swap(int i, int j) {
        Vector2f temp = texCoords[i];
        texCoords[i] = texCoords[j];
        texCoords[j] = temp;
    }

    /** Revert na původní souřadnice */
    public void revert() {
        for (int i = 0; i < texCoords.length; i++) {
            texCoords[i].set(originalTexCoords[i]);
        }
    }
}
