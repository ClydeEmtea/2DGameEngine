package render;

import components.SpriteRenderer;
import engine.GameObject;

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

    public void add(SpriteRenderer spriteRenderer) {
        boolean added = false;
        for (RenderBatch batch : renderBatches) {
            if (batch.hasRoom()) {
                batch.addSprite(spriteRenderer);
                added = true;
                break;
            }
        }
        if (!added) {
            RenderBatch newBatch = new RenderBatch(MAX_BATCH_SIZE);
            newBatch.start();
            renderBatches.add(newBatch);
            newBatch.addSprite(spriteRenderer);
        }
    }

    public void render() {
        for (RenderBatch batch : renderBatches) {
            batch.render();
        }
    }
}
