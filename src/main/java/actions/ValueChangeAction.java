package actions;

import engine.GameObject;
import engine.Window;
import org.joml.Vector2f;

import java.util.Objects;
import java.util.function.BiConsumer;

public class ValueChangeAction<T> implements EditorAction {

    private final String name;
    private final long objectId;
    private final BiConsumer<GameObject, T> setter; // setter bere objekt + hodnotu
    private final T oldValue;
    private final T newValue;

    public ValueChangeAction(
            String name,
            GameObject obj,
            BiConsumer<GameObject, T> setter,
            T oldValue,
            T newValue
    ) {
        this.name = name;
        this.objectId = obj.getId();
        this.setter = setter;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    private GameObject getObject() {
        System.out.println("ValueChangeAction: Retrieving object with ID " + objectId);
        return Objects.requireNonNull(Window.getEditorView()).getObjectById(objectId);
    }

    @Override
    public void execute() {
        GameObject go = getObject();
        if (go != null) setter.accept(go, newValue);
    }

    @Override
    public void undo() {
        GameObject go = getObject();
        if (go != null) setter.accept(go, oldValue);
        else System.out.println("ValueChangeAction: Object with ID " + objectId + " not found for undo.");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNameWithDetails() {
        return name + " (from " + oldValue + " to " + newValue + ")";
    }
}
