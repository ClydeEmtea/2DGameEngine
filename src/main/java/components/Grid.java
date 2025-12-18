package components;

import engine.View;
import engine.Window;
import org.joml.Vector2f;
import org.joml.Vector4f;
import render.Renderer;

public class Grid {

    private static final float CELL_SIZE = 0.25f;
    private static final int HALF_EXTENT = 100;

    public static void render(View view) {
        Vector2f camPos = view.camera().position;
        if (view.camera().getZoom() > 5.0f) return;

        // Zarovnání gridu na násobky CELL_SIZE
        float startX = (float)Math.floor(camPos.x / CELL_SIZE) * CELL_SIZE;
        float startY = (float)Math.floor(camPos.y / CELL_SIZE) * CELL_SIZE;

        Renderer.beginLines(new Vector4f(0.3f, 0.3f, 0.3f, 0.5f));

        for (int i = -HALF_EXTENT; i <= HALF_EXTENT; i++) {
            float x = startX + i * CELL_SIZE;
            Renderer.drawLine(
                    x, startY - HALF_EXTENT * CELL_SIZE,
                    x, startY + HALF_EXTENT * CELL_SIZE
            );
        }

        for (int i = -HALF_EXTENT; i <= HALF_EXTENT; i++) {
            float y = startY + i * CELL_SIZE;
            Renderer.drawLine(
                    startX - HALF_EXTENT * CELL_SIZE, y,
                    startX + HALF_EXTENT * CELL_SIZE, y
            );
        }

        Renderer.endLines();
    }
}
