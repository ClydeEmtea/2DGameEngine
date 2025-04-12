package render;

import components.SpriteRenderer;
import engine.Window;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static util.Constants.*;

public class RenderBatch {
    // Vertex
    // ========
    // Position             Color
    // float, float       float, float, float, float

    private SpriteRenderer[] sprites;
    private int numSprites;
    private boolean hasRoom;
    private float[] vertexArray;

    private int vaoID, vboID;
    private int maxBatchSize;
    private Shader shader;

    public RenderBatch(int maxBatchSize) {
        shader = new Shader(DEFAULT_VERTEX_SHADER, DEFAULT_FRAGMENT_SHADER);
        shader.compile();
        this.maxBatchSize = maxBatchSize;
        this.sprites = new SpriteRenderer[maxBatchSize];

        vertexArray = new float[maxBatchSize * 4 * VERTEX_SIZE];
        this.numSprites = 0;
        this.hasRoom = true;
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
    }

    public void render() {
        // for now rebuffer the whole array every frame
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexArray);

        // Use the shader
        shader.use();
        shader.uploadMat4f("uProjection", Window.getScene().camera().getProjectionMatrix());
        shader.uploadMat4f("uView", Window.getScene().camera().getViewMatrix());

        // Bind the VAO
        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        // Draw the sprites
        glDrawElements(GL_TRIANGLES, numSprites * 6, GL_UNSIGNED_INT, 0);

        // Disable the VAO
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);
        shader.detach();
    }

    public void addSprite(SpriteRenderer sprite) {
        int index = this.numSprites;
        this.sprites[index] = sprite;
        this.numSprites++;

        loadVertexProperties(index);

        if (this.numSprites >= this.maxBatchSize) {
            this.hasRoom = false;
        }

    }

    private void loadVertexProperties(int index) {
        SpriteRenderer sprite = this.sprites[index];
        int offset = index * 4 * VERTEX_SIZE;

        Vector4f color = sprite.getColor();

        float xAdd = 1.0f;
        float yAdd = 1.0f;
        for (int i = 0; i < 4; i++) {
            if (i == 1) {
                yAdd = 0.0f;
            }
            // Top right
            else if (i == 2) {
                xAdd = 0.0f;
            }
            // Top left
            else if (i == 3) {
                yAdd = 1.0f;
            }

            vertexArray[offset] = sprite.gameObject.transform.position.x + xAdd * sprite.gameObject.transform.scale.x;
            vertexArray[offset + 1] = sprite.gameObject.transform.position.y + yAdd * sprite.gameObject.transform.scale.y;

            vertexArray[offset + 2] = color.x;
            vertexArray[offset + 3] = color.y;
            vertexArray[offset + 4] = color.z;
            vertexArray[offset + 5] = color.w;

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
}
