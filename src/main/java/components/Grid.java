package components;

import engine.GameObject;
import engine.Scene;
import engine.Transform;
import org.joml.Vector2f;
import org.joml.Vector4f;
import util.Constants;

import java.util.List;

public class Grid {
    private static final int CELL_SIZE = 100;
    private static List<Integer> xCoords;
    private static List<Integer> yCoords;

    private static int centerX;
    private static int centerY;

    public static void initialize(Scene scene) {
        float width = Constants.WIDTH;
        float height = Constants.HEIGHT;
        xCoords = new java.util.ArrayList<>();
        yCoords = new java.util.ArrayList<>();

        for (float x = 0; x <= width * 2; x += CELL_SIZE) {
            GameObject line = makeLine(new Vector2f(x, -height), new Vector2f(1, height*2));
            scene.addLine(line);
            xCoords.add((int) x);

        }

        for (float y = 0; y <= height * 2; y += CELL_SIZE) {
            GameObject line = makeLine(new Vector2f(0 - width, y), new Vector2f(width*2, 1));
            scene.addLine(line);
            yCoords.add((int) y);
        }
    }

    public static void render(Scene scene) {
        int cameraX = (int) scene.camera().position.x;
        int cameraY = (int) scene.camera().position.y;

        if (cameraX + CELL_SIZE * 5> centerX) {
            centerX += CELL_SIZE * 5;
            for (GameObject line : scene.getGridLines()) {
                line.transform.position.x += CELL_SIZE * 5;
            }
        } else if (cameraX - CELL_SIZE * 5 < centerX) {
            centerX -= CELL_SIZE * 5;
            for (GameObject line : scene.getGridLines()) {
                line.transform.position.x -= CELL_SIZE * 5;
            }
        }
        if (cameraY + CELL_SIZE * 5 > centerY) {
            centerY += CELL_SIZE * 5;
            for (GameObject line : scene.getGridLines()) {
                line.transform.position.y += CELL_SIZE * 5;
            }
        } else if (cameraY - CELL_SIZE * 5 < centerY) {
            centerY -= CELL_SIZE * 5;
            for (GameObject line : scene.getGridLines()) {
                line.transform.position.y -= CELL_SIZE * 5;
            }
        }



    }

    private static GameObject makeLine(Vector2f pos, Vector2f size) {
        GameObject line = new GameObject("GridLine",
                new Transform(new Vector2f(pos), new Vector2f(size)), -1);
        line.addComponent(new SpriteRenderer(new Vector4f(0.3f, 0.3f, 0.3f, 1.0f)));
        return line;
    }
}
