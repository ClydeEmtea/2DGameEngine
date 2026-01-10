package render;

import components.SpriteRenderer;
import engine.GameObject;
import engine.Transform;
import engine.Window;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector4f;
import util.AssetPool;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static util.Constants.*;

public class RenderBatch implements Comparable<RenderBatch> {
    private SpriteRenderer[] sprites;
    private int numSprites;
    private boolean hasRoom;
    private float[] vertexArray;
    private int[] texSlots = {0, 1, 2, 3, 4, 5, 6, 7};

    private int vaoID, vboID, eboID;
    private int maxBatchSize;
    private Shader shader;
    private List<Texture> textures;
    private int zIndex;

    private Renderer renderer;

    private boolean isDirty = false;

    public RenderBatch(int maxBatchSize, int zIndex, Renderer renderer) {
        this.zIndex = zIndex;
        shader = AssetPool.getShader(DEFAULT_VERTEX_SHADER, DEFAULT_FRAGMENT_SHADER);
        this.maxBatchSize = maxBatchSize;
        this.sprites = new SpriteRenderer[maxBatchSize];
        this.vertexArray = new float[maxBatchSize * MAX_VERTS_PER_SPRITE * VERTEX_SIZE];
        this.numSprites = 0;
        this.hasRoom = true;
        this.textures = new ArrayList<>();
        this.renderer = renderer;
    }

    public void start() {
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, vertexArray.length * Float.BYTES, GL_DYNAMIC_DRAW);

        eboID = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);

        glVertexAttribPointer(0, POSITION_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, POSITION_OFFSET);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, COLOR_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, COLOR_OFFSET);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, TEXCOORD_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEXCOORD_OFFSET);
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(3, TEX_ID_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_ID_OFFSET);
        glEnableVertexAttribArray(3);
        glVertexAttribPointer(4, ROUNDNESS_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, ROUNDNESS_OFFSET);
        glEnableVertexAttribArray(4);
    }

    public void render() {
        boolean rebuffer = false;
        for (int i = 0; i < numSprites; i++) {
            SpriteRenderer sprite = sprites[i];
            if (sprites[i].isDirty()) {
                loadVertexProperties(i);
                updateEBO();
                sprites[i].setClean();
                rebuffer = true;
            }
            if (sprite.gameObject.getZIndex() != this.zIndex) {
                destroyIfExists(sprite.gameObject);
                renderer.add(sprite.gameObject);
                sprite.gameObject.start();
                System.out.println("Textura: " + sprite.getTexture());
                i --;
            }


        }
        if (rebuffer) {
            glBindBuffer(GL_ARRAY_BUFFER, vboID);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertexArray);
        }
        if (isDirty) {
            glBindBuffer(GL_ARRAY_BUFFER, vboID);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertexArray);
            isDirty = false;
        }

        shader.use();
        shader.uploadMat4f("uProjection", Window.getView().camera().getProjectionMatrix());
        shader.uploadMat4f("uView", Window.getView().camera().getViewMatrix());

        for (int i = 0; i < textures.size(); i++) {
            glActiveTexture(GL_TEXTURE0 + i + 1);
            textures.get(i).bind();
        }
        shader.uploadIntArray("uTextures", texSlots);

        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        int indexCount = 0;
        for (int i = 0; i < numSprites; i++) {
            Vector2f[] verts = sprites[i].getCustomVertices();
            indexCount += (verts != null && verts.length == 3) ? 3 : 6;
        }

        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);

        for (Texture texture : textures) texture.unbind();
        shader.detach();
    }

    public void addSprite(SpriteRenderer sprite) {
        sprites[numSprites] = sprite;
        numSprites++;

        if (sprite.getTexture() != null && !textures.contains(sprite.getTexture()))
            textures.add(sprite.getTexture());

        loadVertexProperties(numSprites - 1);
        updateEBO(); // důležité pro trojúhelníky
        rebuildTextures();

        if (numSprites >= maxBatchSize) hasRoom = false;

        isDirty = true;
    }



    private void loadVertexProperties(int index) {
        SpriteRenderer sprite = sprites[index];
        Vector2f[] verts = sprite.getCustomVertices();
        if (verts == null || verts.length == 0) {
            verts = new Vector2f[]{
                    new Vector2f(-0.5f, -0.5f),
                    new Vector2f( 0.5f, -0.5f),
                    new Vector2f( 0.5f,  0.5f),
                    new Vector2f(-0.5f,  0.5f)
            };
        }


        int offset = 0;
        for (int i = 0; i < index; i++) {
            Vector2f[] prevVerts = sprites[i].getCustomVertices();
            offset += (prevVerts != null ? prevVerts.length : 4) * VERTEX_SIZE;

        }

        Transform t = sprite.gameObject.transform;
        Vector4f color = sprite.getColor();

        int texID = 0;
        if (sprite.getTexture() != null) {
            for (int i = 0; i < textures.size(); i++)
                if (textures.get(i).getFilePath().equals(sprite.getTexture().getFilePath()))
                    texID = i + 1;
        }

        Vector2f[] texCoords = sprite.getTexCoords();
        if (texCoords == null || texCoords.length < verts.length) {
            if (verts.length == 6) {
                texCoords = new Vector2f[]{
                        new Vector2f(0, 1),
                        new Vector2f(1, 1),
                        new Vector2f(1, 0),

                        new Vector2f(0, 1),
                        new Vector2f(1, 0),
                        new Vector2f(0, 0)
                };
            } else {
                texCoords = new Vector2f[]{
                        new Vector2f(0, 1),
                        new Vector2f(1, 1),
                        new Vector2f(1, 0),
                        new Vector2f(0, 0)
                };
            }
        }


        float cx = t.position.x;
        float cy = t.position.y;
        float cos = (float) Math.cos(t.rotation);
        float sin = (float) Math.sin(t.rotation);

        for (int i = 0; i < verts.length; i++) {
            float localX = verts[i].x * t.scale.x;
            float localY = verts[i].y * t.scale.y;


            // rotace kolem středu (0,0)
            float rx = localX * cos - localY * sin;
            float ry = localX * sin + localY * cos;

            vertexArray[offset]     = rx + cx;
            vertexArray[offset + 1] = ry + cy;
            vertexArray[offset + 2] = color.x;
            vertexArray[offset + 3] = color.y;
            vertexArray[offset + 4] = color.z;
            vertexArray[offset + 5] = color.w;
            vertexArray[offset + 6] = texCoords[i].x;
            vertexArray[offset + 7] = texCoords[i].y;
            vertexArray[offset + 8] = texID;
            vertexArray[offset + 9] = t.roundness;

            offset += VERTEX_SIZE;
        }

    }



    private void updateEBO() {
        List<Integer> indicesList = new ArrayList<>();
        int vertexOffset = 0;

        for (int i = 0; i < numSprites; i++) {
            SpriteRenderer sprite = sprites[i];
            Vector2f[] verts = sprite.getCustomVertices();
            int vertCount = (verts != null) ? verts.length : 4;

            if (vertCount == 3) {
                // Trojúhelník
                indicesList.add(vertexOffset);
                indicesList.add(vertexOffset + 1);
                indicesList.add(vertexOffset + 2);
            } else if (vertCount >= 4) {
                // Quad => dva trojúhelníky
                indicesList.add(vertexOffset);
                indicesList.add(vertexOffset + 1);
                indicesList.add(vertexOffset + 2);

                indicesList.add(vertexOffset);
                indicesList.add(vertexOffset + 2);
                indicesList.add(vertexOffset + 3);
            }

            vertexOffset += vertCount;
        }

        int[] indices = indicesList.stream().mapToInt(Integer::intValue).toArray();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
    }


    public boolean hasRoom() { return hasRoom; }
    public boolean hasTextureRoom() {
        return textures.size() < 8;
    }

    public boolean hasTexture(Texture texture) { return textures.contains(texture); }
    public int getZIndex() { return zIndex; }

    @Override
    public int compareTo(@NotNull RenderBatch o) { return Integer.compare(this.zIndex, o.zIndex); }

    public void removeSprite(SpriteRenderer sprite) {
        for (int i = 0; i < numSprites; i++) {
            if (sprites[i] == sprite) {
                for (int j = i; j < numSprites - 1; j++)
                    sprites[j] = sprites[j + 1];
                sprites[numSprites - 1] = null;
                numSprites--;
                hasRoom = true;
                updateEBO(); // důležité po odstranění
                break;
            }
        }
    }

    private void rebuildTextures() {
        textures.clear();
        for (int i = 0; i < numSprites; i++) {
            Texture t = sprites[i].getTexture();
            if (t != null && !textures.contains(t)) {
                textures.add(t);
            }
        }
    }


    public boolean destroyIfExists(GameObject go) {
        SpriteRenderer sprite = go.getComponent(SpriteRenderer.class);
        for (int i=0; i < numSprites; i++) {
            if (sprites[i] == sprite) {
                for (int j=i; j < numSprites - 1; j++) {
                    sprites[j] = sprites[j + 1];
                    sprites[j].setDirty();
                }
                sprites[numSprites - 1] = null;
                numSprites--;
                hasRoom = true;
                System.out.println("nicim: " + go);
                rebuildTextures();
                updateEBO();
                isDirty = true;

                return true;
            }
        }

        return false;
    }
}
