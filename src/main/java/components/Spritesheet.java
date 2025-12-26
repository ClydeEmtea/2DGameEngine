package components;

import org.joml.Vector2f;
import render.Texture;

import java.util.ArrayList;
import java.util.List;

public class Spritesheet {

    private Texture texture;

    private List<Sprite> sprites;

    public Spritesheet(Texture texture, int spriteWidth, int spriteHeight, int numSprites, int spacing) {
        this.sprites = new ArrayList<>();
        this.texture = texture;

        int curX = 0;
        int curY = 0;

        for (int i = 0; i < numSprites; i++) {
            float topY = (curY) / (float)texture.getHeight();
            float bottomY = (curY + spriteHeight) / (float)texture.getHeight();
            float leftX = curX / (float)texture.getWidth();
            float rightX = (curX + spriteWidth) / (float)texture.getWidth();

            Vector2f[] texCoords = {
                    new Vector2f(rightX, bottomY),  // bottom-right
                    new Vector2f(leftX,  bottomY),  // bottom-left
                    new Vector2f(leftX,  topY),     // top-left
                    new Vector2f(rightX, topY),     // top-right
            };

            Sprite sprite = new Sprite(this.texture, texCoords);
            this.sprites.add(sprite);

            curX += spriteWidth + spacing;
            if (curX >= texture.getWidth()) {
                curX = 0;
                curY += spriteHeight + spacing;
            }
        }
    }


    public Sprite getSprite(int index) {
        if (index < 0 || index >= sprites.size()) {
            throw new IndexOutOfBoundsException("Index out of bounds for spritesheet");
        }
        return sprites.get(index);
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public List<Sprite> getSprites() {
        return sprites;
    }

    public void setSprites(List<Sprite> sprites) {
        this.sprites = sprites;
    }
}
