package actions;

import engine.GameObject;
import engine.Window;

import java.util.ArrayList;
import java.util.List;

public class MultiCreateGameObjectAction implements EditorAction {

    private final String name;
    private final List<GameObject> gameObjects;

    public MultiCreateGameObjectAction(String name, List<GameObject> gameObjects) {
        this.name = name;
        this.gameObjects = new ArrayList<>(gameObjects);
    }

    @Override
    public void execute() {
        var view = Window.getView();
        for (GameObject go : gameObjects) {
            view.addGameObjectToView(go);
        }
    }

    @Override
    public void undo() {
        var view = Window.getView();
        for (GameObject go : gameObjects) {
            view.removeGameObject(go);
        }

        if (gameObjects.contains(view.getActiveGameObject())) {
            view.setActiveGameObject(null);
        }
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNameWithDetails() {
        return name + " (" + gameObjects.size() + " objects)";
    }
}
