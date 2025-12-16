package components;

import engine.GameObject;
import engine.View;
import engine.Transform;
import engine.Window;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.List;

public class Grid {
    private static final int CELL_SIZE = 100;
    private static List<Integer> xCoords;
    private static List<Integer> yCoords;

    private static int centerX;
    private static int centerY;
    private static int changing = 2;

    public static void initialize(View view) {
        float width = Window.get().getWidth();
        float height = Window.get().getHeight();
        System.out.println("Initializing grid with width: " + width + " height: " + height);
        xCoords = new java.util.ArrayList<>();
        yCoords = new java.util.ArrayList<>();

        for (float x = 0; x <= width * 2; x += CELL_SIZE) {
            GameObject line = makeLine(new Vector2f(x, -height), new Vector2f(2, height*2));
            view.addLine(line);
            xCoords.add((int) x);

        }

        for (float y = 0; y <= height * 2; y += CELL_SIZE) {
            GameObject line = makeLine(new Vector2f(0 - width, y), new Vector2f(width*2, 2));
            view.addLine(line);
            yCoords.add((int) y);
        }
    }

    public static void render(View view) {
        int cameraX = (int) view.camera().position.x;
        int cameraY = (int) view.camera().position.y;

        if (cameraX + CELL_SIZE * changing> centerX) {
            centerX += CELL_SIZE * changing;
            for (GameObject line : view.getGridLines()) {
                line.transform.position.x += CELL_SIZE * changing;
            }
        } else if (cameraX - CELL_SIZE * changing < centerX) {
            centerX -= CELL_SIZE * changing;
            for (GameObject line : view.getGridLines()) {
                line.transform.position.x -= CELL_SIZE * changing;
            }
        }
        if (cameraY + CELL_SIZE * changing > centerY) {
            centerY += CELL_SIZE * changing;
            for (GameObject line : view.getGridLines()) {
                line.transform.position.y += CELL_SIZE * changing;
            }
        } else if (cameraY - CELL_SIZE * changing < centerY) {
            centerY -= CELL_SIZE * changing;
            for (GameObject line : view.getGridLines()) {
                line.transform.position.y -= CELL_SIZE * changing;
            }
        }



    }

    private static GameObject makeLine(Vector2f pos, Vector2f size) {
        int rand = (int)(Math.random() * 100);
        GameObject line = new GameObject("GridLine",
                new Transform(new Vector2f(pos), new Vector2f(size)), -1*rand);
        line.addComponent(new SpriteRenderer(new Vector4f(0.3f, 0.3f, 0.3f, 1.0f)));
        return line;
    }
}
