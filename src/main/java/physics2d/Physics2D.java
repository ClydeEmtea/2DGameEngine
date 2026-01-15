package physics2d;

import engine.GameObject;
import engine.Transform;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.joml.Vector2f;
import physics2d.components.Box2DCollider;
import physics2d.components.CapsuleCollider;
import physics2d.components.CircleCollider;
import physics2d.components.RigidBody2D;

public class Physics2D {
    private Vec2 gravity = new Vec2(0, -10.0f);

    public World getWorld() {
        return world;
    }

    private World world = new World(gravity);

    private float physicsTime = 0.0f;
//    private float physicsTimeStep = 1.0f / 60.0f;
    private float physicsTimeStep = 1.0f / 100.0f;
    private int velocityIterations = 16;
    private int positionIterations = 6;

    public void add(GameObject go) {
        RigidBody2D rb = go.getComponent(RigidBody2D.class);
        if (rb != null && rb.getRawBody() == null) {
            Transform transform = go.transform;

            BodyDef bodyDef = new BodyDef();
            bodyDef.angle = transform.rotation;
            bodyDef.position.set(transform.position.x, transform.position.y);
            bodyDef.angularDamping = rb.getAngularDamping();
            bodyDef.linearDamping = rb.getLinearDamping();
            bodyDef.fixedRotation = rb.isFixedRotation();
            bodyDef.bullet = rb.isContinuousCollision();

            switch (rb.getBodyType()) {
                case Kinematic -> bodyDef.type = BodyType.KINEMATIC;
                case Static -> bodyDef.type = BodyType.STATIC;
                case Dynamic -> bodyDef.type = BodyType.DYNAMIC;
            }

            Body body = this.world.createBody(bodyDef);
            rb.setRawBody(body);

            CircleCollider cc = go.getComponent(CircleCollider.class);
            if (cc != null) {
                CircleShape shape = new CircleShape();
                shape.m_radius = cc.getRadius();
                shape.m_p.set(cc.getOffset().x, cc.getOffset().y);

                FixtureDef fd = new FixtureDef();
                fd.shape = shape;
                fd.density = rb.getDensity();
                fd.friction = rb.getFriction();
                fd.restitution = rb.getRestitution();

                body.createFixture(fd);
            }

            Box2DCollider bc = go.getComponent(Box2DCollider.class);
            if (bc != null) {
                PolygonShape shape = new PolygonShape();
                Vector2f hs = new Vector2f(bc.getHalfSize()).mul(0.5f);
                Vector2f off = bc.getOffset();
                shape.setAsBox(hs.x, hs.y, new Vec2(off.x, off.y), 0);

                FixtureDef fd = new FixtureDef();
                fd.shape = shape;
                fd.density = rb.getDensity();
                fd.friction = rb.getFriction();
                fd.restitution = rb.getRestitution();

                System.out.println("Density: " + fd.density + " Friction: " + fd.friction + " Restitution: " + fd.restitution);

                body.createFixture(fd);
            }

            CapsuleCollider cap = go.getComponent(CapsuleCollider.class);
            if (cap != null) {

                float r = cap.getRadius();
                float h = cap.getHeight();
                float halfStraight = (h / 2f) - r;

                Vector2f off = cap.getOffset();

                // horní kruh
                CircleShape top = new CircleShape();
                top.m_radius = r;
                top.m_p.set(off.x, off.y + halfStraight);
                FixtureDef topFD = new FixtureDef();
                topFD.shape = top;
                topFD.density = rb.getDensity();
                topFD.friction = rb.getFriction();
                topFD.restitution = rb.getRestitution();
                body.createFixture(topFD);

                // dolní kruh
                CircleShape bottom = new CircleShape();
                bottom.m_radius = r;
                bottom.m_p.set(off.x, off.y - halfStraight);
                FixtureDef bottomFD = new FixtureDef();
                bottomFD.shape = bottom;
                bottomFD.density = rb.getDensity();
                bottomFD.friction = rb.getFriction();
                bottomFD.restitution = rb.getRestitution();
                body.createFixture(bottomFD);

                // střední box
                PolygonShape box = new PolygonShape();
                box.setAsBox(r, halfStraight, new Vec2(off.x, off.y), 0);
                FixtureDef boxFD = new FixtureDef();
                boxFD.shape = box;
                boxFD.density = rb.getDensity();
                boxFD.friction = rb.getFriction();
                boxFD.restitution = rb.getRestitution();
                body.createFixture(boxFD);
            }



        }
    }

    public void update(float dt) {
        physicsTime += dt;
        if (physicsTime >= 0) {
            physicsTime -= physicsTimeStep;
            world.step(physicsTimeStep, velocityIterations, positionIterations);
        }
    }

    public void destroyGameObject(GameObject go) {
        RigidBody2D rb = go.getComponent(RigidBody2D.class);
        if (rb != null) {
            if (rb.getRawBody() != null) {
                world.destroyBody(rb.getRawBody());
                rb.setRawBody(null);
            }
        }
    }
}