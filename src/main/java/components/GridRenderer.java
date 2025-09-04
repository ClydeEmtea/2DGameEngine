package components;

import engine.Camera;
import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL11.*;

public class GridRenderer {
    private static float cellSize = 100;

    public GridRenderer(float cellSize) {
        GridRenderer.cellSize = cellSize;
    }

    public static void render(Camera camera, int windowWidth, int windowHeight) {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        glOrtho(camera.position.x,
                camera.position.x + windowWidth / camera.getZoom(),
                camera.position.y,
                camera.position.y + windowHeight / camera.getZoom(),
                -1.0f, 1.0f);


        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glColor3f(0.3f, 0.3f, 0.3f);
        glLineWidth(1.0f);

        float left = camera.position.x;
        float bottom = camera.position.y;
        float right = left + windowWidth / camera.getZoom();
        float top = bottom + windowHeight / camera.getZoom();

        glBegin(GL_LINES);

        for (float x = (float)Math.floor(left/cellSize)*cellSize; x < right; x += cellSize) {
            glVertex2f(x, bottom);
            glVertex2f(x, top);
        }

        for (float y = (float)Math.floor(bottom/cellSize)*cellSize; y < top; y += cellSize) {
            glVertex2f(left, y);
            glVertex2f(right, y);
        }

        glEnd();

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
    }

}
