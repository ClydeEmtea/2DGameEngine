package physics2d.components;

import actions.ComponentValueChangeAction;
import engine.Window;
import imgui.ImGui;
import org.joml.Vector2f;
import org.joml.Vector4f;
import render.Renderer;

public class Box2DCollider extends Collider {
    private Vector2f halfSize = new Vector2f();
    private Vector2f halfSizeDragStart = new Vector2f();
    private Vector2f offsetDragStart = new Vector2f();


    public Vector2f getHalfSize() {
        return halfSize;
    }

    public void setHalfSize(Vector2f halfSize) {
        this.halfSize = halfSize;
    }

    @Override
    public void update(float dt) {
        Vector2f center = new Vector2f(this.gameObject.transform.position).add(this.getOffset());
        if (!Window.getView().isGame)
            Renderer.addBox2D(center, this.halfSize, this.gameObject.transform.rotation, new Vector4f(1,1,1,1));
    }


    @Override
    public void imgui() {
        super.imgui();

        ImGui.text("Box2DCollider");

        final float EDITOR_SCALE = 100.0f;

// --- Half Size ---
        float[] half = {
                halfSize.x * EDITOR_SCALE,
                halfSize.y * EDITOR_SCALE
        };

        ImGui.dragFloat2("Half Size", half, 1.0f);

        if (ImGui.isItemActivated()) {
            halfSizeDragStart.set(halfSize);
        }

        if (ImGui.isItemEdited()) {
            halfSize.set(
                    half[0] / EDITOR_SCALE,
                    half[1] / EDITOR_SCALE
            );
        }

        if (ImGui.isItemDeactivatedAfterEdit()) {
            Vector2f oldHalf = new Vector2f(halfSizeDragStart);
            Vector2f newHalf = new Vector2f(halfSize);

            Window.getActionManager().execute(
                    new ComponentValueChangeAction<>(
                            "Change BoxCollider Half Size",
                            gameObject,
                            Box2DCollider.class,
                            Box2DCollider::setHalfSize,
                            oldHalf,
                            newHalf
                    )
            );
        }


        if (ImGui.button("Scale to object")) {
            Vector2f oldHalf = new Vector2f(halfSize);
            Vector2f newHalf = new Vector2f(gameObject.transform.scale);

            halfSize.set(newHalf);

            Window.getActionManager().execute(
                    new ComponentValueChangeAction<>(
                            "Scale Collider To Object",
                            gameObject,
                            Box2DCollider.class,
                            Box2DCollider::setHalfSize,
                            oldHalf,
                            newHalf
                    )
            );
        }



// --- Offset ---
        float[] offset = { getOffset().x, getOffset().y };

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
                            Box2DCollider.class,
                            Box2DCollider::setOffset,
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
                            Box2DCollider.class,
                            Box2DCollider::setOffset,
                            oldOffset,
                            newOffset
                    )
            );
        }

    }
}