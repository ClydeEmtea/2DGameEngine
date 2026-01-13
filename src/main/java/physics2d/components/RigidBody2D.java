package physics2d.components;

import engine.Component;
import engine.Window;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import org.jbox2d.dynamics.Body;
import org.joml.Vector2f;
import physics2d.enums.BodyType;
import actions.ComponentValueChangeAction;

public class RigidBody2D extends Component {
    private Vector2f velocity = new Vector2f();
    private float angularDamping = 0.0f;
    private float linearDamping = 0.0f;
    private float mass = 0;
    private BodyType bodyType = BodyType.Dynamic;

    private boolean fixedRotation = false;
    private boolean continuousCollision = true;

    private transient Body rawBody = null;

    private float density = 1.0f;
    private float friction = 0.3f;
    private float restitution = 0.0f;

    private Vector2f velocityDragStart = new Vector2f();
    private float massDragStart;
    private float angularDampingDragStart;
    private float linearDampingDragStart;
    private float densityDragStart;
    private float frictionDragStart;
    private float restitutionDragStart;


    public float getDensity() { return density; }
    public float getFriction() { return friction; }
    public float getRestitution() { return restitution; }

    public void setDensity(float density) { this.density = density; }
    public void setFriction(float friction) { this.friction = friction; }
    public void setRestitution(float restitution) { this.restitution = restitution; }

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
        super.imgui();

        ImGui.text("RigidBody2D");

// --- Velocity ---
        float[] vel = { velocity.x, velocity.y };
        ImGui.dragFloat2("Velocity", vel, 0.1f);
        if (ImGui.isItemActivated()) {
            velocityDragStart.set(velocity);
        }
        if (ImGui.isItemEdited()) {
            velocity.set(vel[0], vel[1]);
        }
        if (ImGui.isItemDeactivatedAfterEdit()) {
            Vector2f oldVel = new Vector2f(velocityDragStart);
            Vector2f newVel = new Vector2f(velocity);
            Window.getActionManager().execute(
                    new ComponentValueChangeAction<>(
                            "Change Velocity",
                            gameObject,
                            RigidBody2D.class,
                            RigidBody2D::setVelocity,
                            oldVel,
                            newVel
                    )
            );
        }


        // --- Mass ---
        float[] m = { mass };
        ImGui.dragFloat("Mass", m, 1.0f, 0.0f, 10000.0f);

        if (ImGui.isItemActivated()) massDragStart = mass;
        if (ImGui.isItemEdited()) mass = m[0];
        if (ImGui.isItemDeactivatedAfterEdit()) {
            Window.getActionManager().execute(
                    new ComponentValueChangeAction<>(
                            "Change Mass",
                            gameObject,
                            RigidBody2D.class,
                            RigidBody2D::setMass,
                            massDragStart,
                            mass
                    )
            );
        }


        // --- Angular Damping ---
        float[] angDamp = { angularDamping };
        ImGui.dragFloat("Angular Damping", angDamp, 0.01f, 0.0f, 1.0f);

        if (ImGui.isItemActivated()) {
            angularDampingDragStart = angularDamping;
        }

        if (ImGui.isItemEdited()) {
            angularDamping = angDamp[0];
        }

        if (ImGui.isItemDeactivatedAfterEdit()) {
            Window.getActionManager().execute(
                    new ComponentValueChangeAction<>(
                            "Change Angular Damping",
                            gameObject,
                            RigidBody2D.class,
                            RigidBody2D::setAngularDamping,
                            angularDampingDragStart,
                            angularDamping
                    )
            );
        }

// --- Linear Damping ---
        float[] linDamp = { linearDamping };
        ImGui.dragFloat("Linear Damping", linDamp, 0.01f, 0.0f, 1.0f);

        if (ImGui.isItemActivated()) {
            linearDampingDragStart = linearDamping;
        }

        if (ImGui.isItemEdited()) {
            linearDamping = linDamp[0];
        }

        if (ImGui.isItemDeactivatedAfterEdit()) {
            Window.getActionManager().execute(
                    new ComponentValueChangeAction<>(
                            "Change Linear Damping",
                            gameObject,
                            RigidBody2D.class,
                            RigidBody2D::setLinearDamping,
                            linearDampingDragStart,
                            linearDamping
                    )
            );
        }

        // --- Fixed Rotation ---
        ImBoolean fixedRot = new ImBoolean(fixedRotation);
        if (ImGui.checkbox("Fixed Rotation", fixedRot)) {
            boolean oldVal = fixedRotation;
            fixedRotation = fixedRot.get();
            Window.getActionManager().execute(
                    new ComponentValueChangeAction<>(
                            "Toggle Fixed Rotation",
                            gameObject,
                            RigidBody2D.class,
                            RigidBody2D::setFixedRotation,
                            oldVal,
                            fixedRotation
                    )
            );
        }

        // --- Continuous Collision ---
        ImBoolean contColl = new ImBoolean(continuousCollision);
        if (ImGui.checkbox("Continuous Collision", contColl)) {
            boolean oldVal = continuousCollision;
            continuousCollision = contColl.get();
            Window.getActionManager().execute(
                    new ComponentValueChangeAction<>(
                            "Toggle Continuous Collision",
                            gameObject,
                            RigidBody2D.class,
                            RigidBody2D::setContinuousCollision,
                            oldVal,
                            continuousCollision
                    )
            );
        }

        // --- Body Type ---
        String[] types = { "Static", "Dynamic", "Kinematic" };
        ImInt current = new ImInt(bodyType.ordinal());
        if (ImGui.combo("Body Type", current, types, types.length)) {
            BodyType oldVal = bodyType;
            bodyType = BodyType.values()[current.get()];
            Window.getActionManager().execute(
                    new ComponentValueChangeAction<>(
                            "Change Body Type",
                            gameObject,
                            RigidBody2D.class,
                            RigidBody2D::setBodyType,
                            oldVal,
                            bodyType
                    )
            );
        }

        // --- Material Properties ---
        ImGui.text("Material Properties:");

        // --- Density ---
        float[] d = { density };
        ImGui.dragFloat("Density", d, 0.01f, 0f, 100f);

        if (ImGui.isItemActivated()) {
            densityDragStart = density;
        }

        if (ImGui.isItemEdited()) {
            density = d[0];
        }

        if (ImGui.isItemDeactivatedAfterEdit()) {
            Window.getActionManager().execute(
                    new ComponentValueChangeAction<>(
                            "Change Density",
                            gameObject,
                            RigidBody2D.class,
                            RigidBody2D::setDensity,
                            densityDragStart,
                            density
                    )
            );
        }

// --- Friction ---
        float[] f = { friction };
        ImGui.dragFloat("Friction", f, 0.01f, 0f, 1f);

        if (ImGui.isItemActivated()) {
            frictionDragStart = friction;
        }

        if (ImGui.isItemEdited()) {
            friction = f[0];
        }

        if (ImGui.isItemDeactivatedAfterEdit()) {
            Window.getActionManager().execute(
                    new ComponentValueChangeAction<>(
                            "Change Friction",
                            gameObject,
                            RigidBody2D.class,
                            RigidBody2D::setFriction,
                            frictionDragStart,
                            friction
                    )
            );
        }

// --- Restitution ---
        float[] r = { restitution };
        ImGui.dragFloat("Restitution", r, 0.01f, 0f, 1f);

        if (ImGui.isItemActivated()) {
            restitutionDragStart = restitution;
        }

        if (ImGui.isItemEdited()) {
            restitution = r[0];
        }

        if (ImGui.isItemDeactivatedAfterEdit()) {
            Window.getActionManager().execute(
                    new ComponentValueChangeAction<>(
                            "Change Restitution",
                            gameObject,
                            RigidBody2D.class,
                            RigidBody2D::setRestitution,
                            restitutionDragStart,
                            restitution
                    )
            );
        }

    }
}
