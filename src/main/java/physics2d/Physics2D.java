package physics2d;

import engine.GameObject;
import engine.Transform;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.joml.Vector2f;
import physics2d.components.*;

public class Physics2D {
    private Vec2 gravity = new Vec2(0, -10.0f);

    public World getWorld() {
        return world;
    }

    private World world = new World(gravity);

    private final ContactListener contactListener = new ContactListener();

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

            body.setUserData(go);

            CircleCollider cc = go.getComponent(CircleCollider.class);
            if (cc != null) {
                CircleShape shape = new CircleShape();
                shape.m_radius = cc.getRadius();
                shape.m_p.set(cc.getOffset().x, cc.getOffset().y);

                createFixtureDefCircle(rb, body, shape);
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
                fd.isSensor = rb.isSensor();

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
                createFixtureDefCircle(rb, body, top);

                // dolní kruh
                CircleShape bottom = new CircleShape();
                bottom.m_radius = r;
                bottom.m_p.set(off.x, off.y - halfStraight);
                createFixtureDefCircle(rb, body, bottom);

                // střední box
                PolygonShape box = new PolygonShape();
                box.setAsBox(r, halfStraight, new Vec2(off.x, off.y), 0);
                FixtureDef boxFD = new FixtureDef();
                boxFD.shape = box;
                boxFD.density = rb.getDensity();
                boxFD.friction = rb.getFriction();
                boxFD.restitution = rb.getRestitution();
                boxFD.isSensor = rb.isSensor();
                body.createFixture(boxFD);
            }



        }
    }

    private void createFixtureDefCircle(RigidBody2D rb, Body body, CircleShape shape) {
        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        fd.density = rb.getDensity();
        fd.friction = rb.getFriction();
        fd.restitution = rb.getRestitution();
        fd.isSensor = rb.isSensor();

        body.createFixture(fd);
    }

    private void clearAllCollisions() {
        for (Body b = world.getBodyList(); b != null; b = b.getNext()) {
            GameObject go = (GameObject) b.getUserData();
            if (go == null) continue;

            CollisionComponent cc = go.getComponent(CollisionComponent.class);
            if (cc != null) {
                cc.clear();
            }
        }
    }

    private void addCollision(GameObject self, GameObject other) {
        CollisionComponent cc = self.getComponent(CollisionComponent.class);
        if (cc != null) {
            cc.begin(other);
        }
    }

    public void update(float dt) {
        physicsTime += dt;
        if (physicsTime >= physicsTimeStep) {
            physicsTime -= physicsTimeStep;

            for (Body b = world.getBodyList(); b != null; b = b.getNext()) {
                GameObject go = (GameObject) b.getUserData();
                if (go == null) continue;

                CollisionComponent cc = go.getComponent(CollisionComponent.class);
                if (cc != null) {
                    cc.sync();
                    cc.clear();
                }
            }

            world.step(physicsTimeStep, velocityIterations, positionIterations);

            for (var c = world.getContactList(); c != null; c = c.getNext()) {
                if (!c.isTouching()) continue;

                GameObject a = (GameObject) c.getFixtureA().getBody().getUserData();
                GameObject b = (GameObject) c.getFixtureB().getBody().getUserData();

                if (a == null || b == null) continue;

                addCollision(a, b);
                addCollision(b, a);
            }

            for (Body b = world.getBodyList(); b != null; b = b.getNext()) {
                GameObject go = (GameObject) b.getUserData();
                if (go == null) continue;

                CollisionComponent cc = go.getComponent(CollisionComponent.class);
                if (cc == null) continue;

                for (GameObject entered : cc.getEntered()) {
                    go.getAllScriptInstances().forEach(s -> s.onCollisionEnter(entered));
                }

                for (GameObject exited : cc.getExited()) {
                    go.getAllScriptInstances().forEach(s -> s.onCollisionExit(exited));
                }
            }
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