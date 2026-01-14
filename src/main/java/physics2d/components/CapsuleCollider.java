package physics2d.components;

import actions.ComponentValueChangeAction;
import engine.Component;
import engine.Window;
import imgui.ImGui;
import org.joml.Vector2f;
import org.joml.Vector4f;
import render.Renderer;

public class CapsuleCollider extends Collider {

    private float radius = 0.25f;
    private float height = 1.0f;

    private Vector2f offsetDragStart = new Vector2f();
    private float radiusDragStart;
    private float heightDragStart;

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
        ImGui.dragFloat("Radius", r, 0.01f, 0.01f, 10.0f);

        if (ImGui.isItemActivated()) {
            radiusDragStart = radius;
        }

        if (ImGui.isItemEdited()) {
            setRadius(r[0]);
        }

        if (ImGui.isItemDeactivatedAfterEdit()) {
            float oldRadius = radiusDragStart;
            float newRadius = radius;
            Window.getActionManager().execute(
                    new ComponentValueChangeAction<>(
                            "Change Collider Radius",
                            gameObject,
                            CapsuleCollider.class,
                            CapsuleCollider::setRadius,
                            oldRadius,
                            newRadius
                    )
            );
        }

        float[] h = { height };
        ImGui.dragFloat("Height", h, 0.01f, radius * 2f, 20.0f);

        if (ImGui.isItemActivated()) {
            heightDragStart = height;
        }
        if (ImGui.isItemEdited()) {
            setHeight(h[0]);
        }
        if (ImGui.isItemDeactivatedAfterEdit()) {
            float oldHeight = heightDragStart;
            float newHeight = height;
            Window.getActionManager().execute(
                    new ComponentValueChangeAction<>(
                            "Change Collider Height",
                            gameObject,
                            CapsuleCollider.class,
                            CapsuleCollider::setHeight,
                            oldHeight,
                            newHeight
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
                            CapsuleCollider.class,
                            CapsuleCollider::setOffset,
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
                            CapsuleCollider.class,
                            CapsuleCollider::setOffset,
                            oldOffset,
                            newOffset
                    )
            );
        }
    }
}
