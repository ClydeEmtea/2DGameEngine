package project;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import components.Sprite;
import components.SpriteRenderer;
import engine.GameObject;
import engine.Scene;
import engine.Transform;
import engine.Window;
import org.joml.Vector2f;
import org.joml.Vector4f;
import render.Texture;
import util.AssetPool;

import java.io.File;
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
            Files.createDirectories(currentProject.getProjectPath());
            Files.createDirectories(currentProject.getAssetsPath());
            Files.createDirectories(currentProject.getScenesPath());
            Files.createDirectories(currentProject.getScriptsPath());
            Files.createDirectories(currentProject.getImagesPath());
            Files.createDirectories(currentProject.getShadersPath());
            Files.createDirectories(currentProject.getAudioPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Make config file and add default Scene
        try {
            Path configPath = currentProject.getConfigPath();
            if (!Files.exists(configPath)) {
                Files.createFile(configPath);
            }
            // Add default scene
            Files.createFile(currentProject.getScenesPath().resolve("MainScene.json"));
        } catch (IOException e) {
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

    public void openProject(String path) {
        try {
            String json = Files.readString(Path.of(path));
            ProjectConfig cfg = gson.fromJson(json, ProjectConfig.class);
            this.currentProject = new Project(cfg.projectName, Path.of(cfg.projectPath));
            Window.get().updateTitle();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<GameObject> loadSceneObjects(String scene) {
        if (currentProject == null) {
            return null;
        }
        Path file = scenesDir().resolve(scene + ".json");
        if (!Files.exists(file)) {
            return null;
        }

        try {
            String json = Files.readString(file);
            SceneData sceneData = gson.fromJson(json, SceneData.class);
            List<GameObject> gameObjects = new ArrayList<>();

            if (sceneData == null || sceneData.objects == null) {
                return gameObjects; // Return empty list if no objects
            }

            for (GameObjectData god : sceneData.objects) {
                GameObject go;
                float x = god.posX;
                float y = god.posY;
                float scaleX = god.scaleX;
                float scaleY = god.scaleY;
                int zIndex = god.zIndex;

                SpriteRenderer sr = null;

                if (god.colorOnly) {
                    sr = new SpriteRenderer(new Vector4f(god.r, god.g, god.b, god.a));
                } else {
                    if (god.texturePath != null) {
                        String fileName = new File(god.texturePath).getName();
                        System.out.println("Loading texture: " + fileName);
                        Texture tex = AssetPool.getTexture(fileName);
                        sr = new SpriteRenderer(new Sprite(tex));
                    }
                }
                go = new GameObject(god.name, new Transform(new Vector2f(x, y), new Vector2f(scaleX, scaleY)), zIndex);
                go.addComponent(sr);
                gameObjects.add(go);
            }

            return gameObjects;

        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
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
            god.zIndex = go.getZIndex();

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
    int zIndex;

    boolean colorOnly;
    String texturePath;  // null pokud není sprite

    float r, g, b, a; // color fallback
}
