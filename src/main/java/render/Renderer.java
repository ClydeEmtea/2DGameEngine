package render;

import components.SpriteRenderer;
import engine.GameObject;
import engine.Window;
import org.joml.Vector2f;
import org.joml.Vector4f;
import util.AssetPool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static util.Constants.MAX_BATCH_SIZE;

public class Renderer {

    private List<RenderBatch> renderBatches;
    private static Shader debugShader;
    private final List<SpriteRenderer> rebatchQueue = new ArrayList<>();



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
            RenderBatch newBatch = new RenderBatch(MAX_BATCH_SIZE, sprite.gameObject.getZIndex(), this);
            newBatch.start();
            renderBatches.add(newBatch);
            newBatch.addSprite(sprite);
            Collections.sort(renderBatches);
        }
    }


    public void render() {
        for (int i = 0; i < renderBatches.size(); i++) {
            RenderBatch batch = renderBatches.get(i);
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


    public static void drawLine(Vector2f corner, Vector2f corner1) {
        glVertex2f(corner.x, corner.y);
        glVertex2f(corner1.x, corner1.y);
    }

    // ==================================================
    // Add Box2D methods
    // ==================================================
    public static void addBox2D(Vector2f center, Vector2f dimensions, float rotation) {
        // TODO: ADD CONSTANTS FOR COMMON COLORS
        addBox2D(center, dimensions, rotation, new Vector4f(0, 1, 0, 1), 1);
    }

    public static void addBox2D(Vector2f center, Vector2f dimensions, float rotation, Vector4f color) {
        addBox2D(center, dimensions, rotation, color, 1);
    }

    public static void addBox2D(Vector2f center, Vector2f dimensions, float rotation,
                                Vector4f color, int lifetime) {
        Vector2f min = new Vector2f(center).sub(new Vector2f(dimensions).mul(0.5f));
        Vector2f max = new Vector2f(center).add(new Vector2f(dimensions).mul(0.5f));

        Vector2f[] vertices = {
                new Vector2f(min.x, min.y), new Vector2f(min.x, max.y),
                new Vector2f(max.x, max.y), new Vector2f(max.x, min.y)
        };

        if (rotation != 0.0f) {
            for (Vector2f vert : vertices) {
                rotate(vert, rotation, center);
            }
        }

        beginLines(color);

        drawLine(vertices[0], vertices[1]);
        drawLine(vertices[0], vertices[3]);
        drawLine(vertices[1], vertices[2]);
        drawLine(vertices[2], vertices[3]);

        endLines();
    }

    public static void rotate(Vector2f vec, float angleDeg, Vector2f origin) {
        float x = vec.x - origin.x;
        float y = vec.y - origin.y;

        float cos = (float)Math.cos((angleDeg));
        float sin = (float)Math.sin((angleDeg));

        float xPrime = (x * cos) - (y * sin);
        float yPrime = (x * sin) + (y * cos);

        xPrime += origin.x;
        yPrime += origin.y;

        vec.x = xPrime;
        vec.y = yPrime;
    }

    public void remove(GameObject go) {
        for (RenderBatch batch : renderBatches) {
            batch.destroyIfExists(go);
        }
    }

    public void requestRebatch(SpriteRenderer sprite) {
        rebatchQueue.add(sprite);
    }


}
