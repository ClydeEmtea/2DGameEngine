package physics2d.components;

import engine.Component;
import engine.Window;
import imgui.ImGui;
import org.joml.Vector2f;
import org.joml.Vector4f;
import render.Renderer;

public class CircleCollider extends Collider {
    private float radius = 1f;

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    @Override
    public void update(float dt) {
        Vector2f center = new Vector2f(this.gameObject.transform.position).add(this.getOffset());
        if (!Window.getView().isGame)
            Renderer.drawCircle(center, radius, new Vector4f(1,1,1,1), 32);

    }


    @Override
    public void imgui() {
        super.imgui();

        ImGui.text("CircleCollider");

        float[] r = { radius };
        if (ImGui.dragFloat("Radius", r, 0.01f, 0.01f, 100.0f)) {
            radius = Math.max(0.01f, r[0]);
        }


        float[] offset = {getOffset().x, getOffset().y};
        if (ImGui.dragFloat2("Offset", offset, 0.01f)) {
            setOffset(new Vector2f(offset[0], offset[1]));
        }
        if (ImGui.button("Reset offset")) {
            setOffset(new Vector2f(0,0));
        }
    }
}