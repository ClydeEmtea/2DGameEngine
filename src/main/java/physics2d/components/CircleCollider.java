package physics2d.components;

import actions.ComponentValueChangeAction;
import engine.Component;
import engine.Window;
import imgui.ImGui;
import org.joml.Vector2f;
import org.joml.Vector4f;
import render.Renderer;

public class CircleCollider extends Collider {
    private float radius = 1f;
    private float radiusDragStart;
    private Vector2f offsetDragStart = new Vector2f();

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
        ImGui.dragFloat("Radius", r, 0.01f, 0.01f, 100.0f);
        if (ImGui.isItemActivated()) {
            radiusDragStart = radius;
        }
        if (ImGui.isItemEdited()) {
            radius = r[0];
        }
        if (ImGui.isItemDeactivatedAfterEdit()) {
            float oldRadius = radiusDragStart;
            float newRadius = radius;
            Window.getActionManager().execute(
                    new ComponentValueChangeAction<>(
                            "Change Collider Radius",
                            gameObject,
                            CircleCollider.class,
                            CircleCollider::setRadius,
                            oldRadius,
                            newRadius
                    )
            );
            }


        float[] offset = {getOffset().x, getOffset().y};
        ImGui.dragFloat2("Offset", offset, 0.01f);

        if (ImGui.isItemActivated()) {
            offsetDragStart.set(getOffset());
        }

        if (ImGui.isItemEdited()) {
            setOffset(new Vector2f(offset[0], offset[1]));
        }
        if (ImGui.isItemDeactivatedAfterEdit()) {
            Vector2f oldOffset = new Vector2f(offsetDragStart);
            Vector2f newOffset = new Vector2f(getOffset());
            Window.getActionManager().execute(
                    new ComponentValueChangeAction<>(
                            "Change Collider Offset",
                            gameObject,
                            CircleCollider.class,
                            CircleCollider::setOffset,
                            oldOffset,
                            newOffset
                    )
            );
        }

        if (ImGui.button("Reset offset")) {
            Vector2f oldOffset = new Vector2f(getOffset());
            Vector2f newOffset = new Vector2f(0, 0);

            setOffset(newOffset);

            Window.getActionManager().execute(
                    new ComponentValueChangeAction<>(
                            "Reset Collider Offset",
                            gameObject,
                            CircleCollider.class,
                            CircleCollider::setOffset,
                            oldOffset,
                            newOffset
                    )
            );
        }
    }
}