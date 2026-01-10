package components;

import engine.Component;
import engine.Transform;
import imgui.ImGui;
import org.joml.Vector2f;
import org.joml.Vector4f;
import util.Constants;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

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
                    points.add(new Vector2f(-0.5f, -0.5f));
                    points.add(new Vector2f( 0.5f, -0.5f));
                    points.add(new Vector2f( 0.0f,  0.5f));

                }
                spriteRenderer.setCustomVertices(points.toArray(new Vector2f[0]));
            }
            case QUAD -> {
                if (points.size() != 4) {
                    points.clear();
                    points.add(new Vector2f(-0.5f, -0.5f));
                    points.add(new Vector2f( 0.5f, -0.5f));
                    points.add(new Vector2f( 0.5f,  0.5f));
                    points.add(new Vector2f(-0.5f,  0.5f));

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

        // world → local
        Vector2f p = new Vector2f(worldPoint).sub(t.position);

        float cos = (float) Math.cos(-t.rotation);
        float sin = (float) Math.sin(-t.rotation);

        float lx = p.x * cos - p.y * sin;
        float ly = p.x * sin + p.y * cos;

        // scale
        lx /= t.scale.x;
        ly /= t.scale.y;

        return pointInPolygon(new Vector2f(lx, ly), points);
    }

    private boolean pointInPolygon(Vector2f p, List<Vector2f> poly) {
        boolean inside = false;
        for (int i = 0, j = poly.size() - 1; i < poly.size(); j = i++) {
            if ((poly.get(i).y > p.y) != (poly.get(j).y > p.y) &&
                    p.x < (poly.get(j).x - poly.get(i).x) *
                            (p.y - poly.get(i).y) /
                            (poly.get(j).y - poly.get(i).y) + poly.get(i).x) {
                inside = !inside;
            }
        }
        return inside;
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
        super.imgui();

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


    public List<Vector2f> getWorldPoints() {
        Transform t = gameObject.transform;
        List<Vector2f> world = new ArrayList<>();

        float cos = (float) Math.cos(t.rotation);
        float sin = (float) Math.sin(t.rotation);

        for (Vector2f lp : points) {
            float x = lp.x * t.scale.x;
            float y = lp.y * t.scale.y;

            float rx = x * cos - y * sin;
            float ry = x * sin + y * cos;

            world.add(new Vector2f(
                    rx + t.position.x,
                    ry + t.position.y
            ));
        }
        return world;
    }



}
