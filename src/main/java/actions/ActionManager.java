package actions;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class ActionManager {

    private final Deque<EditorAction> undoStack = new ArrayDeque<>();
    private final Deque<EditorAction> redoStack = new ArrayDeque<>();

    public void execute(EditorAction action) {
        action.execute();
        undoStack.push(action);
        redoStack.clear();
    }

    public void undo() {
        if (undoStack.isEmpty()) return;
        EditorAction action = undoStack.pop();
        action.undo();
        redoStack.push(action);
    }

    public void redo() {
        if (redoStack.isEmpty()) return;
        EditorAction action = redoStack.pop();
        action.redo();
        undoStack.push(action);
    }

    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }

    public Deque<EditorAction> getUndoStack() {
        return undoStack;
    }

    public Deque<EditorAction> getRedoStack() {
        return redoStack;
    }
}
