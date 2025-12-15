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
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static util.Constants.*;

public class RenderBatch implements Comparable<RenderBatch> {
    // Vertex
    // ========
    // Position             Color                       TexCoords       TexID
    // float, float       float, float, float, float    float, float    float

    private SpriteRenderer[] sprites;
    private int numSprites;
    private boolean hasRoom;
    private float[] vertexArray;
    private int[] texSlots = {0, 1, 2, 3, 4, 5, 6, 7};

    private int vaoID, vboID;
    private int maxBatchSize;
    private Shader shader;

    private List<Texture> textures;
    private int zIndex;

    public RenderBatch(int maxBatchSize, int zIndex) {
        this.zIndex = zIndex;
        shader = AssetPool.getShader("vertexDefault.glsl", "fragmentDefault.glsl");
        this.maxBatchSize = maxBatchSize;
        this.sprites = new SpriteRenderer[maxBatchSize];

        vertexArray = new float[maxBatchSize * 4 * VERTEX_SIZE];
        this.numSprites = 0;
        this.hasRoom = true;

        this.textures = new ArrayList<>();
    }

    public void start() {
        // Generate and bind the VAO
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        // Allocate space for vertices
        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, (long) vertexArray.length * Float.BYTES, GL_DYNAMIC_DRAW);

        // Create and upload index buffer
        int eboID = glGenBuffers();
        int[] indices = generateIndices();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        // Enable the buffer attribute pointers
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
            if (sprites[i].isDirty()) {
                loadVertexProperties(i);
                sprites[i].setClean();
                rebuffer = true;
            }
        }

        if (rebuffer) {
            glBindBuffer(GL_ARRAY_BUFFER, vboID);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertexArray);
        }
        // Use the shader
        shader.use();
        shader.uploadMat4f("uProjection", Window.getScene().camera().getProjectionMatrix());
        shader.uploadMat4f("uView", Window.getScene().camera().getViewMatrix());

        for (int i=0; i < textures.size(); i++) {
            glActiveTexture(GL_TEXTURE0 + i + 1);
            textures.get(i).bind();
        }
        shader.uploadIntArray("uTextures", texSlots);

        // Bind the VAO
        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        // Draw the sprites
        glDrawElements(GL_TRIANGLES, numSprites * 6, GL_UNSIGNED_INT, 0);

//        if (Window.getScene().getActiveGameObject() != null &&
//                Window.getScene().getActiveGameObject().getComponent(SpriteRenderer.class) != null) {
//
//            Vector2f pos = Window.getScene().getActiveGameObject().transform.position;
//            Vector2f size = Window.getScene().getActiveGameObject().transform.scale;
//
//            glColor3f(0.95f, 0.6f, 0.05f);
//            glLineWidth(4f);
//
//            glBegin(GL_LINE_LOOP);
//            glVertex2f(pos.x, pos.y);
//            glVertex2f(pos.x + size.x, pos.y);
//            glVertex2f(pos.x + size.x, pos.y + size.y);
//            glVertex2f(pos.x, pos.y + size.y);
//            glEnd();
//        }

        // Disable the VAO
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);

        for (Texture texture : textures) {
            texture.unbind();
        }



        shader.detach();
    }

    public void addSprite(SpriteRenderer sprite) {
        // Get index and add renderObject
        int index = this.numSprites;
        this.sprites[index] = sprite;
        this.numSprites++;

        if (sprite.getTexture() != null) {
            if (!textures.contains(sprite.getTexture())) {
                textures.add(sprite.getTexture());
            }
        }

        // Add properties to local vertices array
        loadVertexProperties(index);

        if (numSprites >= this.maxBatchSize) {
            this.hasRoom = false;
        }
    }

    private void loadVertexProperties(int index) {
        SpriteRenderer sprite = this.sprites[index];
        int offset = index * 4 * VERTEX_SIZE;

        Transform t = sprite.gameObject.transform;
        Vector4f color = sprite.getColor();
        Vector2f[] texCoords = sprite.getTexCoords();

        // Texture ID
        int texID = 0;
        if (sprite.getTexture() != null) {
            for (int i = 0; i < textures.size(); i++) {
                if (textures.get(i).getFilePath().equals(sprite.getTexture().getFilePath())) {
                    texID = i + 1;
                    break;
                }
            }
        }

        // Pivot = střed sprite (ale position zůstává levý dolní roh!)
        float cx = t.position.x + t.scale.x * 0.5f;
        float cy = t.position.y + t.scale.y * 0.5f;

        float cos = (float) Math.cos(t.rotation);
        float sin = (float) Math.sin(t.rotation);

        float xAdd = 1.0f;
        float yAdd = 1.0f;

        for (int i = 0; i < 4; i++) {

            if (i == 1) yAdd = 0.0f;
            else if (i == 2) xAdd = 0.0f;
            else if (i == 3) yAdd = 1.0f;

            // původní (nerotovaná) pozice vrcholu
            float x = t.position.x + xAdd * t.scale.x;
            float y = t.position.y + yAdd * t.scale.y;

            // posun do pivotu
            float dx = x - cx;
            float dy = y - cy;

            // rotace
            float rx = dx * cos - dy * sin;
            float ry = dx * sin + dy * cos;

            // návrat zpět
            vertexArray[offset]     = rx + cx;
            vertexArray[offset + 1] = ry + cy;

            // color
            vertexArray[offset + 2] = color.x;
            vertexArray[offset + 3] = color.y;
            vertexArray[offset + 4] = color.z;
            vertexArray[offset + 5] = color.w;

            // tex coords
            vertexArray[offset + 6] = texCoords[i].x;
            vertexArray[offset + 7] = texCoords[i].y;

            // tex id
            vertexArray[offset + 8] = texID;

            // roundness
            vertexArray[offset + 9] = t.roundness;

            offset += VERTEX_SIZE;
        }
    }


    private int[] generateIndices() {
        int[] indices = new int[maxBatchSize * 6];
        for (int i = 0; i < maxBatchSize; i++) {
            loadElementIndices(indices, i);
        }
        return indices;
    }

    private void loadElementIndices(int[] indices, int index) {
        int offsetArrayIndex = index * 6;
        int offset = index * 4;
        indices[offsetArrayIndex] = offset + 3;
        indices[offsetArrayIndex + 1] = offset + 2;
        indices[offsetArrayIndex + 2] = offset;

        indices[offsetArrayIndex + 3] = offset;
        indices[offsetArrayIndex + 4] = offset + 2;
        indices[offsetArrayIndex + 5] = offset + 1;
    }

    public boolean hasRoom() {
        return this.hasRoom;
    }

    public boolean hasTextureRoom() {
        return this.sprites.length < 8;
    }

    public boolean hasTexture(Texture texture) {
        return this.textures.contains(texture);
    }

    public int getZIndex() {
        return zIndex;
    }


    @Override
    public int compareTo(@NotNull RenderBatch o) {
        return Integer.compare(this.zIndex, o.zIndex);
    }

    public void removeSprite(SpriteRenderer sprite) {
        for (int i = 0; i < numSprites; i++) {
            if (sprites[i] == sprite) {
                for (int j = i; j < numSprites - 1; j++) {
                    sprites[j] = sprites[j + 1];
                }
                sprites[numSprites - 1] = null;
                numSprites--;
                hasRoom = true;
                break;
            }
        }
    }

}
