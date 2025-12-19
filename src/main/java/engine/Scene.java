package engine;

import project.ProjectManager;

public class Scene {
    private String name;

    public Scene(String name) {
        this.name = name;
        if (!ProjectManager.get().getScenes().contains(name))
            ProjectManager.get().createNewScene(name);
    }

    public String getName() {
        return name;
    }

}
