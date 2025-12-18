package physics2d.components;

import engine.Component;
import engine.Window;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import org.jbox2d.dynamics.Body;
import org.joml.Vector2f;
import physics2d.enums.BodyType;

public class RigidBody2D extends Component {
    private Vector2f velocity = new Vector2f();
    private float angularDamping = 0.8f;
    private float linearDamping = 0.9f;
    private float mass = 0;
    private BodyType bodyType = BodyType.Dynamic;

    private boolean fixedRotation = false;
    private boolean continuousCollision = true;

    private transient Body rawBody = null;

    @Override
    public void update(float dt) {
        if (!Window.getView().isGame) return;
        if (rawBody != null) {
            this.gameObject.transform.position.set(
                    rawBody.getPosition().x, rawBody.getPosition().y
            );
            this.gameObject.transform.rotation = rawBody.getAngle();
        }
    }

    public Vector2f getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2f velocity) {
        this.velocity = velocity;
    }

    public float getAngularDamping() {
        return angularDamping;
    }

    public void setAngularDamping(float angularDamping) {
        this.angularDamping = angularDamping;
    }

    public float getLinearDamping() {
        return linearDamping;
    }

    public void setLinearDamping(float linearDamping) {
        this.linearDamping = linearDamping;
    }

    public float getMass() {
        return mass;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public BodyType getBodyType() {
        return bodyType;
    }

    public void setBodyType(BodyType bodyType) {
        this.bodyType = bodyType;
    }

    public boolean isFixedRotation() {
        return fixedRotation;
    }

    public void setFixedRotation(boolean fixedRotation) {
        this.fixedRotation = fixedRotation;
    }

    public boolean isContinuousCollision() {
        return continuousCollision;
    }

    public void setContinuousCollision(boolean continuousCollision) {
        this.continuousCollision = continuousCollision;
    }

    public Body getRawBody() {
        return rawBody;
    }

    public void setRawBody(Body rawBody) {
        this.rawBody = rawBody;
    }

    @Override
    public void imgui() {
        ImGui.text("RigidBody2D");

        // Velocity
        float[] vel = { velocity.x, velocity.y };
        if (ImGui.dragFloat2("Velocity", vel, 0.1f)) {
            velocity.set(vel[0], vel[1]);
        }

        // Mass
        float[] m = { mass };
        if (ImGui.dragFloat("Mass", m, 0.1f, 0.0f, 1000.0f)) {
            mass = m[0];
        }

        // Angular Damping
        float[] angDamp = { angularDamping };
        if (ImGui.dragFloat("Angular Damping", angDamp, 0.01f, 0.0f, 10.0f)) {
            angularDamping = angDamp[0];
        }

        // Linear Damping
        float[] linDamp = { linearDamping };
        if (ImGui.dragFloat("Linear Damping", linDamp, 0.01f, 0.0f, 10.0f)) {
            linearDamping = linDamp[0];
        }

        // Fixed Rotation
        ImBoolean fixedRot = new ImBoolean(fixedRotation);
        if (ImGui.checkbox("Fixed Rotation", fixedRot)) {
            fixedRotation = fixedRot.get();
        }

        // Continuous Collision
        ImBoolean contColl = new ImBoolean(continuousCollision);
        if (ImGui.checkbox("Continuous Collision", contColl)) {
            continuousCollision = contColl.get();
        }

        String[] types = { "Static", "Dynamic", "Kinematic" };
        ImInt current = new ImInt(bodyType.ordinal());
        if (ImGui.combo("Body Type", current, types, types.length)) {
            bodyType = BodyType.values()[current.get()];
        }

    }

}
