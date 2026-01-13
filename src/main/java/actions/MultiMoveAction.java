package actions;

import engine.GameObject;
import engine.Window;
import org.joml.Vector2f;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MultiMoveAction implements EditorAction {

    private final String name;
    private final Map<Long, Vector2f> oldPositions;
    private final Map<Long, Vector2f> newPositions;

    public MultiMoveAction(
            String name,
            Map<GameObject, Vector2f> oldPositions,
            Map<GameObject, Vector2f> newPositions
    ) {
        this.name = name;
        this.oldPositions = new HashMap<>();
        this.newPositions = new HashMap<>();

        for (var entry : oldPositions.entrySet()) {
            this.oldPositions.put(entry.getKey().getId(), new Vector2f(entry.getValue()));
        }
        for (var entry : newPositions.entrySet()) {
            this.newPositions.put(entry.getKey().getId(), new Vector2f(entry.getValue()));
        }
    }

    @Override
    public void execute() {
        for (var entry : newPositions.entrySet()) {
            GameObject go = Objects.requireNonNull(Window.getEditorView()).getObjectById(entry.getKey());
            if (go != null) go.transform.position.set(entry.getValue());
        }
    }

    @Override
    public void undo() {
        for (var entry : oldPositions.entrySet()) {
            GameObject go = Objects.requireNonNull(Window.getEditorView()).getObjectById(entry.getKey());
            if (go != null) go.transform.position.set(entry.getValue());
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNameWithDetails() {
        return name + " (" + oldPositions.size() + " objects)";
    }
}
