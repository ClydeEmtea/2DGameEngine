package render;

import components.SpriteRenderer;
import engine.GameObject;

import java.util.Collections;
import java.util.List;

import static util.Constants.MAX_BATCH_SIZE;

public class Renderer {

    private List<RenderBatch> renderBatches;

    public Renderer() {
        renderBatches = new java.util.ArrayList<>();
    }

    public void add(GameObject go) {
        SpriteRenderer spriteRenderer = go.getComponent(SpriteRenderer.class);
        if (spriteRenderer != null) {
            add(spriteRenderer);
        }
    }

    public void add(SpriteRenderer sprite) {
        boolean added = false;
        for (RenderBatch batch : renderBatches) {
            if (batch.hasRoom() && batch.getZIndex() == sprite.gameObject.getZIndex()) {
                Texture texture = sprite.getTexture();
                if (texture == null || (batch.hasTexture(texture) || batch.hasTextureRoom())) {
                    batch.addSprite(sprite);
                    added = true;
                    break;
                }
            }
        }
        if (!added) {
            RenderBatch newBatch = new RenderBatch(MAX_BATCH_SIZE, sprite.gameObject.getZIndex());
            newBatch.start();
            renderBatches.add(newBatch);
            newBatch.addSprite(sprite);
            Collections.sort(renderBatches);
        }
    }


    public void render() {
        for (RenderBatch batch : renderBatches) {
            batch.render();
        }
    }
}
