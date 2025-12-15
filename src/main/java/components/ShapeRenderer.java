package components;

import engine.Component;
import imgui.ImGui;
import org.joml.Vector2f;
import org.joml.Vector4f;
import util.Constants;

import java.util.ArrayList;
import java.util.List;


public class ShapeRenderer extends Component {

    private ShapeType shapeType = ShapeType.QUAD;
    private Vector4f color = new Vector4f(Constants.WHITE);
    private SpriteRenderer spriteRenderer;

    // pro polygon
    private List<Vector2f> points = new ArrayList<>();

    @Override
    public void start() {
        spriteRenderer = gameObject.getComponent(SpriteRenderer.class);

        if (spriteRenderer == null) {
            throw new RuntimeException();
        }

        applyShape();
    }

    private void applyShape() {
        if (spriteRenderer == null) return;

        switch (shapeType) {
            case QUAD -> {
                isColorOnly = true;
            }

            case TRIANGLE -> {
                isColorOnly = true;
                spriteRenderer.setColor(new Vector4f(1, 0, 0, 1));
            }

            case POLYGON -> {
                isColorOnly = true;
                spriteRenderer.setColor(new Vector4f(0, 1, 0, 1));
            }
        }
    }



    public ShapeType getShapeType() {
        return shapeType;
    }

    public void setShapeType(ShapeType type) {
        this.shapeType = type;
        applyShape();
    }


    public List<Vector2f> getPoints() {
        return points;
    }

    private void rebuildMesh() {
        // TODO:
        // QUAD      → klasický batch quad
        // TRIANGLE  → 3 vertexy
        // POLYGON   → triangulace
    }

    @Override
    public void update(float dt) {

    }

    @Override
    public void imgui() {
        if (ImGui.beginCombo("Shape Type", shapeType.name())) {
            for (ShapeType t : ShapeType.values()) {
                if (ImGui.selectable(t.name(), t == shapeType)) {
                    setShapeType(t);
                }
            }
            ImGui.endCombo();
        }

        float[] c = {color.x, color.y, color.z, color.w};
        if (ImGui.colorEdit4("Color", c)) {
            color.set(c);
        }

        if (shapeType == ShapeType.POLYGON) {
            polygonEditor();
        }
    }

    private void polygonEditor() {
        ImGui.text("Polygon Points");

        for (int i = 0; i < points.size(); i++) {
            Vector2f p = points.get(i);
            float[] v = {p.x, p.y};
            if (ImGui.dragFloat2("P" + i, v, 0.01f)) {
                p.set(v);
                rebuildMesh();
            }
        }

        if (ImGui.button("Add Point")) {
            points.add(new Vector2f(0, 0));
            rebuildMesh();
        }

        ImGui.sameLine();

        if (points.size() > 3 && ImGui.button("Remove Last")) {
            points.remove(points.size() - 1);
            rebuildMesh();
        }
    }
}
