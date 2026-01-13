package actions;

import engine.Component;
import engine.GameObject;
import engine.Window;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.Objects;
import java.util.function.BiConsumer;

public class ComponentValueChangeAction<T extends Component, V> implements EditorAction {

    private final String name;
    private final long objectId;
    private final Class<T> componentClass;
    private final BiConsumer<T, V> setter; // setter bere komponentu + hodnotu
    private final V oldValue;
    private final V newValue;

    public ComponentValueChangeAction(
            String name,
            GameObject obj,
            Class<T> componentClass,
            BiConsumer<T, V> setter,
            V oldValue,
            V newValue
    ) {
        this.name = name;
        this.objectId = obj.getId();
        this.componentClass = componentClass;
        this.setter = setter;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    private T getComponent() {
        GameObject go = Objects.requireNonNull(Window.getEditorView()).getObjectById(objectId);
        if (go == null) return null;
        return go.getComponent(componentClass);
    }

    @Override
    public void execute() {
        T comp = getComponent();
        if (comp != null) setter.accept(comp, newValue);
    }

    @Override
    public void undo() {
        T comp = getComponent();
        if (comp != null) setter.accept(comp, oldValue);
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
