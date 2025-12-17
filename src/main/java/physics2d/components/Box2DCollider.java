package physics2d.components;

import imgui.ImGui;
import org.joml.Vector2f;
import org.joml.Vector4f;
import render.Renderer;

public class Box2DCollider extends Collider {
    private Vector2f halfSize = new Vector2f();

    public void setOrigin(float[] origin) {
        this.origin.x = origin[0];
        this.origin.y = origin[1];
    }

    private Vector2f origin = new Vector2f();


    public Vector2f getOrigin() {
        return this.origin;
    }


    public Vector2f getHalfSize() {
        return halfSize;
    }

    public void setHalfSize(Vector2f halfSize) {
        this.halfSize = halfSize;
    }

    @Override
    public void update(float dt) {
        Vector2f pos = this.gameObject.transform.position;
        float rot = this.gameObject.transform.rotation;

        Vector2f center = new Vector2f(pos).add(origin);

        Vector2f[] corners = {
                new Vector2f(-halfSize.x,  halfSize.y), // TL
                new Vector2f( halfSize.x,  halfSize.y), // TR
                new Vector2f( halfSize.x, -halfSize.y), // BR
                new Vector2f(-halfSize.x, -halfSize.y)  // BL
        };

        for (Vector2f v : corners) {
            // rotace
            float x = v.x * (float)Math.cos(rot) - v.y * (float)Math.sin(rot);
            float y = v.x * (float)Math.sin(rot) + v.y * (float)Math.cos(rot);
            v.set(x, y).add(center);
        }

        Renderer.beginLines(new Vector4f(1, 1, 1, 1));
        Renderer.drawLine(corners[0], corners[1]);
        Renderer.drawLine(corners[1], corners[2]);
        Renderer.drawLine(corners[2], corners[3]);
        Renderer.drawLine(corners[3], corners[0]);
        Renderer.endLines();
    }




    @Override
    public void imgui() {
        ImGui.text("Box2DCollider");

        // Half Size
        float[] half = { halfSize.x, halfSize.y };
        if (ImGui.dragFloat2("Half Size", half, 0.1f)) {
            halfSize.set(half[0], half[1]);
        }

        // Origin
        float[] orig = { origin.x, origin.y };
        if (ImGui.dragFloat2("Origin", orig, 0.1f)) {
            origin.set(orig[0], orig[1]);
        }
    }
}