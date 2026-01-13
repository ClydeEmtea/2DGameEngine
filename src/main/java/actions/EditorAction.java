package actions;

public interface EditorAction {
    void execute();
    void undo();
    default void redo() { execute(); }
    String getName();
    default String getNameWithDetails() { return getName(); }
}

