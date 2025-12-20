package components;

import engine.Component;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.stb.STBEasyFont;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBEasyFont.stb_easy_font_print;

public class FontRenderer extends Component {

    private String text = "Hello Editor";
    private Vector4f color = new Vector4f(1, 1, 1, 1);

    public void setText(String text) {
        this.text = text;
    }

    public void setColor(Vector4f color) {
        this.color.set(color);
    }

    @Override
    public void update(float dt) {
        renderText();
    }

    private void renderText() {
        if (text == null || text.isEmpty()) return;

        Vector2f pos = gameObject.transform.position;

        glDisable(GL_TEXTURE_2D);
        glColor4f(color.x, color.y, color.z, color.w);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer buffer = stack.malloc(1024 * 16);
            int num_quads;

            num_quads = stb_easy_font_print(pos.x, pos.y, text, null, buffer);

            glEnableClientState(GL_VERTEX_ARRAY);
            glVertexPointer(2, GL_FLOAT, 16, buffer);
            glDrawArrays(GL_QUADS, 0, num_quads*4);
            glDisableClientState(GL_VERTEX_ARRAY);
        }

        glEnable(GL_TEXTURE_2D);
    }

}
