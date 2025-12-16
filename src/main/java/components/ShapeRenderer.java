package components;

import engine.Component;
import engine.Transform;
import imgui.ImGui;
import org.joml.Vector2f;
import org.joml.Vector4f;
import util.Constants;
import java.util.ArrayList;
import java.util.List;

public class ShapeRenderer extends Component {

    private ShapeType shapeType = ShapeType.DEFAULT; // Default = standardní quad
    private SpriteRenderer spriteRenderer;
    private List<Vector2f> points = new ArrayList<>();

    @Override
    public void start() {
        spriteRenderer = gameObject.getComponent(SpriteRenderer.class);
        if (spriteRenderer == null) {
            throw new RuntimeException("ShapeRenderer requires a SpriteRenderer on the same GameObject");
        }
        applyShape();
    }

    private void applyShape() {
        if (spriteRenderer == null) return;

        switch (shapeType) {
            case DEFAULT -> spriteRenderer.setCustomVertices(null);
            case TRIANGLE -> {
                if (points.size() != 3) {
                    points.clear();
                    points.add(new Vector2f(0, 0));
                    points.add(new Vector2f(1, 0));
                    points.add(new Vector2f(0.5f, 1));
                }
                spriteRenderer.setCustomVertices(points.toArray(new Vector2f[0]));
            }
            case QUAD -> {
                if (points.size() != 4) {
                    points.clear();
                    points.add(new Vector2f(0, 0)); // 0
                    points.add(new Vector2f(1, 0)); // 1
                    points.add(new Vector2f(1, 1)); // 2
                    points.add(new Vector2f(0, 1)); // 3
                }

                Vector2f[] quadVerts = new Vector2f[] {
                        points.get(0), points.get(1), points.get(2),
                        points.get(0), points.get(2), points.get(3)
                };

                spriteRenderer.setCustomVertices(quadVerts);
            }

        }
    }


    public ShapeType getShapeType() {
        return shapeType;
    }

    public void setShapeType(ShapeType type) {
        this.shapeType = type;
        if (type == ShapeType.DEFAULT) {
            applyShape();
        } else if (type == ShapeType.TRIANGLE) {
            if (points.size() != 3) {
                applyShape();
            }
        } else {
            if (points.size() != 4) {
                applyShape();
            }
        }
    }

    public List<Vector2f> getPoints() {
        return points;
    }

    public boolean containsPoint(Vector2f worldPoint) {
        Transform t = gameObject.transform;

        // transform all local points to world space
        List<Vector2f> worldPoints = new ArrayList<>();

        for (Vector2f lp : points) {
            // local (0..1) -> object space
            float x = lp.x * t.scale.x;
            float y = lp.y * t.scale.y;

            // rotate around center
            float cx = t.scale.x * 0.5f;
            float cy = t.scale.y * 0.5f;

            float dx = x - cx;
            float dy = y - cy;

            float cos = (float) Math.cos(t.rotation);
            float sin = (float) Math.sin(t.rotation);

            float rx = dx * cos - dy * sin;
            float ry = dx * sin + dy * cos;

            // back to world
            worldPoints.add(new Vector2f(
                    rx + cx + t.position.x,
                    ry + cy + t.position.y
            ));
        }

        if (shapeType == ShapeType.TRIANGLE) {
            return pointInTriangle(
                    worldPoint,
                    worldPoints.get(0),
                    worldPoints.get(1),
                    worldPoints.get(2)
            );
        }

        if (shapeType == ShapeType.QUAD) {
            return pointInQuad(
                    worldPoint,
                    worldPoints.get(0),
                    worldPoints.get(1),
                    worldPoints.get(2),
                    worldPoints.get(3)
            );
        }

        return false;
    }


    public static boolean pointInTriangle(
            Vector2f p,
            Vector2f a,
            Vector2f b,
            Vector2f c
    ) {
        Vector2f v0 = new Vector2f(c).sub(a);
        Vector2f v1 = new Vector2f(b).sub(a);
        Vector2f v2 = new Vector2f(p).sub(a);

        float dot00 = v0.dot(v0);
        float dot01 = v0.dot(v1);
        float dot02 = v0.dot(v2);
        float dot11 = v1.dot(v1);
        float dot12 = v1.dot(v2);

        float denom = dot00 * dot11 - dot01 * dot01;
        if (denom == 0.0f) return false;

        float invDenom = 1.0f / denom;
        float u = (dot11 * dot02 - dot01 * dot12) * invDenom;
        float v = (dot00 * dot12 - dot01 * dot02) * invDenom;

        return (u >= 0) && (v >= 0) && (u + v <= 1);
    }
    public static boolean pointInQuad(
            Vector2f p,
            Vector2f a,
            Vector2f b,
            Vector2f c,
            Vector2f d
    ) {
        return pointInTriangle(p, a, b, c) ||
                pointInTriangle(p, a, c, d);
    }


    public void setPoints(List<Vector2f> points) {
        this.points = points;
        rebuildMesh();
    }

    @Override
    public void update(float dt) {
        // nic zatím
    }

    private void rebuildMesh() {
        if (spriteRenderer == null) return;

        if (shapeType == ShapeType.QUAD && points.size() == 4) {
            Vector2f[] verts = new Vector2f[] {
                    points.get(0), points.get(1), points.get(2),
                    points.get(0), points.get(2), points.get(3)
            };
            spriteRenderer.setCustomVertices(verts);
            return;
        }

        // TRIANGLE
        spriteRenderer.setCustomVertices(points.toArray(new Vector2f[0]));
    }


    @Override
    public void imgui() {
        ImGui.text("Shape Type");
        ImGui.sameLine();
        if (ImGui.beginCombo("##Shape Type", shapeType.name())) {
            for (ShapeType t : ShapeType.values()) {
                if (ImGui.selectable(t.name(), t == shapeType)) {
                    setShapeType(t);
                }
            }
            ImGui.endCombo();
        }
        if (shapeType == ShapeType.TRIANGLE) {
            for (int i = 0; i < 3; i++) {
                Vector2f p = points.get(i);
                float[] v = {p.x, p.y};
                if (ImGui.dragFloat2("P" + i, v, 0.01f)) {
                    p.set(v);
                    rebuildMesh();
                }
            }
        }
        if (shapeType == ShapeType.QUAD) {
            for (int i = 0; i < 4; i++) {
                Vector2f p = points.get(i);
                float[] v = {p.x, p.y};
                if (ImGui.dragFloat2("P" + i, v, 0.01f)) {
                    p.set(v);
                    rebuildMesh();
                }
            }
        }

    }

}
