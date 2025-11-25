package project;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import components.SpriteRenderer;
import engine.GameObject;
import engine.Scene;
import engine.Window;
import org.joml.Vector4f;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ProjectManager {
    private static ProjectManager instance = null;

    private Project currentProject;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private ProjectManager() {}

    public static ProjectManager get() {
        if (instance == null) {
            instance = new ProjectManager();
        }
        return instance;
    }

    public Project getCurrentProject() {
        return currentProject;
    }

    private Path config() {
        return currentProject.getConfigPath();
    }

    private Path scenesDir() {
        return currentProject.getScenesPath();
    }

    public void createNewProject(String name, Path path) {
        this.currentProject = new Project(name, path);

        // Make directories for the new project
        try {
            java.nio.file.Files.createDirectories(currentProject.getProjectPath());
            java.nio.file.Files.createDirectories(currentProject.getAssetsPath());
            java.nio.file.Files.createDirectories(currentProject.getScenesPath());
            java.nio.file.Files.createDirectories(currentProject.getScriptsPath());
            java.nio.file.Files.createDirectories(currentProject.getTexturesPath());
            java.nio.file.Files.createDirectories(currentProject.getShadersPath());
            java.nio.file.Files.createDirectories(currentProject.getAudioPath());
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        // Make config file and add default Scene
        try {
            java.nio.file.Path configPath = currentProject.getConfigPath();
            if (!java.nio.file.Files.exists(configPath)) {
                java.nio.file.Files.createFile(configPath);
            }
            // Add default scene
            java.nio.file.Files.createFile(currentProject.getScenesPath().resolve("MainScene.json"));
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        // Adds MainScene to config file
        try {
            ProjectConfig cfg = new ProjectConfig();
            cfg.projectName = currentProject.getName();
            cfg.projectPath = currentProject.getProjectPath().toString();
            cfg.scenes = List.of("MainScene");

            Files.writeString(config(), gson.toJson(cfg));

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void createNewProjectWithScenes(String name, String path) {
        // Implement project creation with default scenes logic here
    }

    public void openProject(Project project) {
        this.currentProject = project;
    }

    public void saveProject() {
        if (currentProject == null) {
            return;
        }

        Path file = scenesDir().resolve("MainScene.json");
        Scene scene = Window.getScene();
        List<GameObject> gameObjects = scene.getGameObjects();

        SceneData sceneData = new SceneData();
        sceneData.objects = new ArrayList<>();

        for (GameObject go : gameObjects) {
            GameObjectData god = new GameObjectData();
            god.name = go.getName();
            god.posX = go.transform.position.x;
            god.posY = go.transform.position.y;
            god.scaleX = go.transform.scale.x;
            god.scaleY = go.transform.scale.y;

            SpriteRenderer spriteRenderer = go.getComponent(SpriteRenderer.class);
            if (spriteRenderer != null) {

                if (spriteRenderer.getTexture() != null) {
                    god.texturePath = spriteRenderer.getTexture().getFilePath();
                    god.colorOnly = false;
                } else {
                    god.texturePath = null;
                    god.colorOnly = true;

                    Vector4f color = spriteRenderer.getColor();
                    god.r = color.x;
                    god.g = color.y;
                    god.b = color.z;
                    god.a = color.w;

                }
            }
            sceneData.objects.add(god);
        }

        try {
            String json = gson.toJson(sceneData);
            Files.writeString(file, json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class SceneData {
    List<GameObjectData> objects;
}

class GameObjectData {
    String name;
    float posX, posY;
    float scaleX, scaleY;

    boolean colorOnly;
    String texturePath;  // null pokud není sprite

    float r, g, b, a; // color fallback
}
