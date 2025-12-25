package physics2d.components;

import engine.Window;
import imgui.ImGui;
import org.joml.Vector2f;
import org.joml.Vector4f;
import render.Renderer;

public class Box2DCollider extends Collider {
    private Vector2f halfSize = new Vector2f();

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
        ImGui.text("Box2DCollider");

        final float EDITOR_SCALE = 100.0f;

        float[] half = {
                halfSize.x * EDITOR_SCALE,
                halfSize.y * EDITOR_SCALE
        };

        if (ImGui.dragFloat2("Half Size", half, 1.0f)) {
            halfSize.set(
                    half[0] / EDITOR_SCALE,
                    half[1] / EDITOR_SCALE
            );
        }

        if (ImGui.button("Scale to object")) {
            this.setHalfSize(this.gameObject.transform.scale);
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