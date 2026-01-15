package actions;

import engine.GameObject;
import engine.Window;
import engine.Group;

import java.util.ArrayList;
import java.util.List;

public class MultiDeleteGameObjectAction implements EditorAction {

    private final String name;
    private final List<DeletedObject> deletedObjects = new ArrayList<>();

    public MultiDeleteGameObjectAction(String name, List<GameObject> gameObjects) {
        this.name = name;

        for (GameObject go : gameObjects) {
            deletedObjects.add(
                    new DeletedObject(go, go.getParentGroup())
            );
        }
    }

    @Override
    public void execute() {
        var view = Window.getView();

        for (DeletedObject entry : deletedObjects) {
            GameObject go = entry.gameObject;

            view.removeGameObject(go);
        }

        if (deletedObjects.stream().anyMatch(e -> e.gameObject == view.getActiveGameObject())) {
            view.setActiveGameObject(null);
        }
    }

    @Override
    public void undo() {
        var view = Window.getView();

        for (DeletedObject entry : deletedObjects) {
            view.addGameObjectToView(entry.gameObject);
            if (entry.parentGroup != null) {
                entry.parentGroup.add(entry.gameObject);
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNameWithDetails() {
        return name + " (" + deletedObjects.size() + " objects)";
    }

    private static class DeletedObject {
        GameObject gameObject;
        Group parentGroup;

        DeletedObject(GameObject gameObject, Group parentGroup) {
            this.gameObject = gameObject;
            this.parentGroup = parentGroup;
        }
    }
}


