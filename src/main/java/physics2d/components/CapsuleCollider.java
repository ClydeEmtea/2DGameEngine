package physics2d.components;

import engine.Component;
import engine.Window;
import imgui.ImGui;
import org.joml.Vector2f;
import org.joml.Vector4f;
import render.Renderer;

public class CapsuleCollider extends Collider {

    private float radius = 0.25f;
    private float height = 1.0f;

    public float getRadius() {
        return radius;
    }

    public float getHeight() {
        return height;
    }

    public void setRadius(float radius) {
        this.radius = Math.max(0.01f, radius);
    }

    public void setHeight(float height) {
        this.height = Math.max(radius * 2f, height);
    }

    @Override
    public void update(float dt) {
        if (Window.getView().isGame) return;

        Vector2f center = new Vector2f(this.gameObject.transform.position)
                .add(this.getOffset());

        Renderer.drawCapsule(
                center,
                radius,
                height,
                this.gameObject.transform.rotation,
                new Vector4f(1, 1, 1, 1),
                32
        );
    }


    @Override
    public void imgui() {
        super.imgui();

        ImGui.text("CapsuleCollider");

        float[] r = { radius };
        if (ImGui.dragFloat("Radius", r, 0.01f, 0.01f, 10.0f)) {
            setRadius(r[0]);
        }

        float[] h = { height };
        if (ImGui.dragFloat("Height", h, 0.01f, radius * 2f, 20.0f)) {
            setHeight(h[0]);
        }

        float[] off = { getOffset().x, getOffset().y };
        if (ImGui.dragFloat2("Offset", off, 0.01f)) {
            setOffset(new Vector2f(off[0], off[1]));
        }

        if (ImGui.button("Reset offset")) {
            setOffset(new Vector2f(0, 0));
        }
    }
}
