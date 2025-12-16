package render;

import components.SpriteRenderer;
import engine.GameObject;
import engine.Window;
import org.joml.Vector4f;
import util.AssetPool;

import java.util.Collections;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static util.Constants.MAX_BATCH_SIZE;

public class Renderer {

    private List<RenderBatch> renderBatches;
    private static Shader debugShader;


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

    public static void beginLines(Vector4f color) {
        if (debugShader == null) {
            debugShader = AssetPool.getShader("vertexDebug.glsl", "fragmentDebug.glsl");
        }
        debugShader.use();
        debugShader.uploadMat4f("uProjection", Window.getView().camera().getProjectionMatrix());
        debugShader.uploadMat4f("uView", Window.getView().camera().getViewMatrix());
        debugShader.uploadVec4f("uColor", color);

        glLineWidth(1.5f);
        glBegin(GL_LINES);
    }

    public static void endLines() {
        glEnd();
        debugShader.detach();
    }



    public static void drawLine(float x1, float y1, float x2, float y2) {
        glVertex2f(x1, y1);
        glVertex2f(x2, y2);
    }

    public void remove(GameObject go) {
        for (RenderBatch batch : renderBatches) {
            batch.removeSprite(go.getComponent(SpriteRenderer.class));
        }
    }

}
