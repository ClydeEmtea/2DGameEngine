package physics2d.components;

import engine.GameObject;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.collision.Manifold;
import org.jbox2d.dynamics.contacts.Contact;

public class ContactListener implements org.jbox2d.callbacks.ContactListener {

    @Override
    public void beginContact(Contact contact) {
        handle(contact, true);
    }

    @Override
    public void endContact(Contact contact) {
        handle(contact, false);
    }

    @Override
    public void preSolve(Contact contact, Manifold manifold) {}

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {}

    private void handle(Contact contact, boolean begin) {

        GameObject a = (GameObject) contact.getFixtureA().getBody().getUserData();
        GameObject b = (GameObject) contact.getFixtureB().getBody().getUserData();

        if (a == null || b == null) return;

        notify(a, b, begin);
        notify(b, a, begin);
    }

    private void notify(GameObject self, GameObject other, boolean begin) {

        CollisionComponent cc = self.getComponent(CollisionComponent.class);
        if (cc != null) {
            if (begin) cc.begin(other);
            else cc.end(other);
        }

        self.getAllScriptInstances().forEach(script -> {
            if (begin) script.onCollisionEnter(other);
            else script.onCollisionExit(other);
        });
    }
}