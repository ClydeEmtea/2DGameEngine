package project;

import engine.View;

import java.nio.file.Path;
import java.util.List;

public class Project {
    private String name;
    private final Path projectPath;      // Kořenová složka projektu
    private List<View> views;

    protected Project(String name, Path projectPath, List<View> views) {
        this.name = name;
        this.projectPath = projectPath;
        this.views = views;
    }

    protected Project(String name, Path projectPath) {
        this.name = name;
        this.projectPath = projectPath;
    }


    public Path getProjectPath() {
        return projectPath;
    }

    public Path getAssetsPath() {
        return projectPath.resolve("assets");
    }

    public Path getScenesPath() {
        return projectPath.resolve("scenes");
    }

    public Path getScriptsPath() {
        return projectPath.resolve("assets/scripts");
    }

    public Path getImagesPath() {
        return projectPath.resolve("assets/images");
    }

    public Path getShadersPath() {
        return projectPath.resolve("assets/shaders");
    }

    public Path getAudioPath() {
        return projectPath.resolve("assets/audio");
    }

    public Path getConfigPath() {
        return projectPath.resolve("project.json");
    }

    public List<View> getScenes() {
        return views;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addScene(View view) {
        this.views.add(view);
    }
}
