package physics2d.components;

import engine.Component;
import imgui.ImGui;

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

    }


    @Override
    public void imgui() {
        ImGui.text("CircleCollider");

        float[] r = { radius };
        if (ImGui.dragFloat("Radius", r, 0.1f, 0.0f, 100.0f)) {
            radius = r[0];
        }
    }
}