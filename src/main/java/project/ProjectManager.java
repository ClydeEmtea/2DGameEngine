package project;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import components.*;
import engine.*;
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

    public List<GameObject> loadSceneObjectsasdf(String scene) {
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
                float rotation = god.rotation;
                float roundness = god.roundness;
                int zIndex = god.zIndex;

                go = new GameObject(god.name, new Transform(new Vector2f(x, y), new Vector2f(scaleX, scaleY), rotation, roundness), zIndex);

                // SpriteRenderer
                SpriteRenderer sr = null;
                if (god.colorOnly) {
                    sr = new SpriteRenderer(new Vector4f(god.r, god.g, god.b, god.a));
                } else if (god.texturePath != null) {
                    String fileName = new File(god.texturePath).getName();
                    Texture tex = AssetPool.getTexture(fileName);
                    sr = new SpriteRenderer(new Sprite(tex));
                }
                if (sr != null) go.addComponent(sr);

                // ShapeRenderer
                ShapeRenderer shape = null;
                if (god.shapeType != null) {
                    ShapeType type = ShapeType.valueOf(god.shapeType);
                    shape = new ShapeRenderer();
                    shape.setShapeType(type);

                    if (god.shapePoints != null && !god.shapePoints.isEmpty()) {
                        List<Vector2f> points = new ArrayList<>();
                        for (float[] arr : god.shapePoints) {
                            points.add(new Vector2f(arr[0], arr[1]));
                        }
                        shape.setPoints(points);
                        for (Vector2f poi : points) {
                            System.out.println(poi);
                        }
                    }

                    go.addComponent(shape);
                }


                // Scripts
                if (god.scripts != null) {
                    for (String scriptClass : god.scripts) {
                        Path p = getScriptPath(scriptClass);
                        if (p != null) {
                            ScriptComponent sc = new ScriptComponent(scriptClass, p);
                            go.addComponent(sc);
                        }
                    }
                }

                gameObjects.add(go);
            }


            return gameObjects;

        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }

    public List<GameObject> loadSceneObjects(String scene) {
        if (currentProject == null) return null;

        Path file = scenesDir().resolve(scene + ".json");
        if (!Files.exists(file)) return null;
        Group root = Window.getView().getRoot();

        try {
            String json = Files.readString(file);
            SceneData sceneData = gson.fromJson(json, SceneData.class);

            root.getObjects().clear();
            root.getGroups().clear();

            if (sceneData == null) return null;

            // Root-level objekty
            if (sceneData.objects != null) {
                for (GameObjectData god : sceneData.objects) {
                    root.add(dataToGameObject(god));
                }
            }

            // Root-level groups
            if (sceneData.groups != null) {
                for (GroupData gd : sceneData.groups) {
                    root.addGroup(dataToGroup(gd));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return root.getAllObjectsRecursive();
    }

    private GameObject dataToGameObject(GameObjectData god) {
        GameObject go = new GameObject(
                god.name,
                new Transform(
                        new Vector2f(god.posX, god.posY),
                        new Vector2f(god.scaleX, god.scaleY),
                        god.rotation,
                        god.roundness
                ),
                god.zIndex
        );

        // SpriteRenderer
        if (god.colorOnly) {
            go.addComponent(new SpriteRenderer(new Vector4f(god.r, god.g, god.b, god.a)));
        } else if (god.texturePath != null) {
            Texture tex = AssetPool.getTexture(new File(god.texturePath).getName());
            go.addComponent(new SpriteRenderer(new Sprite(tex)));
        }

        // ShapeRenderer
        if (god.shapeType != null) {
            ShapeRenderer shape = new ShapeRenderer();
            shape.setShapeType(ShapeType.valueOf(god.shapeType));

            if (god.shapePoints != null && !god.shapePoints.isEmpty()) {
                List<Vector2f> points = new ArrayList<>();
                for (float[] arr : god.shapePoints) {
                    points.add(new Vector2f(arr[0], arr[1]));
                }
                shape.setPoints(points);
            }
            go.addComponent(shape);
        }

        // Scripts
        if (god.scripts != null) {
            for (String scriptClass : god.scripts) {
                Path p = getScriptPath(scriptClass);
                if (p != null) {
                    go.addComponent(new ScriptComponent(scriptClass, p));
                }
            }
        }

        return go;
    }

    private Group dataToGroup(GroupData gd) {
        Group group = new Group(gd.name);

        // Přidáme všechny objekty
        for (GameObjectData god : gd.objects) {
            group.add(dataToGameObject(god));
        }

        // Rekurzivně přidáme všechny podskupiny
        for (GroupData sub : gd.groups) {
            group.addGroup(dataToGroup(sub));
        }

        return group;
    }


    public void saveProject() {
        if (currentProject == null) {
            return;
        }

        Path file = scenesDir().resolve("MainScene.json");
        View view = Window.getView();
        Group root = view.getRoot();

        SceneData sceneData = new SceneData();
        sceneData.objects = new ArrayList<>();
        sceneData.groups = new ArrayList<>();

        for (GameObject go : root.getObjects()) {
            sceneData.objects.add(gameObjectToData(go));
        }

        for (Group g : root.getGroups()) {
            sceneData.groups.add(groupToData(g));
        }

        try {
            String json = gson.toJson(sceneData);
            Files.writeString(file, json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private GameObjectData gameObjectToData(GameObject go) {
        GameObjectData god = new GameObjectData();
        god.name = go.getName();
        god.posX = go.transform.position.x;
        god.posY = go.transform.position.y;
        god.scaleX = go.transform.scale.x;
        god.scaleY = go.transform.scale.y;
        god.rotation = go.transform.rotation;
        god.roundness = go.transform.roundness;
        god.zIndex = go.getZIndex();
        god.shapeType = String.valueOf(go.getShapeType());

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

        god.scripts = new ArrayList<>();
        ScriptComponent sc = go.getComponent(ScriptComponent.class);
        if (sc != null) {
            god.scripts.add(sc.getClassName());
        }

        ShapeRenderer shapeRenderer = go.getComponent(ShapeRenderer.class);
        if (shapeRenderer != null) {
            god.shapeType = shapeRenderer.getShapeType().name();
            god.shapePoints = new ArrayList<>();
            for (Vector2f p : shapeRenderer.getPoints()) {
                god.shapePoints.add(new float[]{p.x, p.y});
            }
        }

        return god;
    }

    private GroupData groupToData(Group g) {
        GroupData gd = new GroupData();
        gd.name = g.getName();

        // Uložíme všechny objekty v group
        for (GameObject go : g.getObjects()) {
            gd.objects.add(gameObjectToData(go));
        }

        // Rekurzivně uložíme všechny podskupiny
        for (Group sub : g.getGroups()) {
            gd.groups.add(groupToData(sub));
        }

        return gd;
    }


    private static final String SCRIPT_TEMPLATE = """
import scripts.Script;
import scripts.Exposed;
import engine.*;
import components.*;
import render.*;
import util.*;
import java.util.*;
import static org.lwjgl.glfw.GLFW.*;

public class %s implements Script {
    // Environment - DO NOT EDIT
    private GameObject thisGameObject;
    private Window window;
    private MouseListener mouseListener;
    private KeyListener keyListener;
    private Camera camera;
    private View view;
    // ------------
    
    // Add variables here
    
    
    
    
    @Override
    public void init() {
        // called once
    }

    @Override
    public void update(float dt) {
        // called every frame
    }
    
    @Override
    public void setEnvironment(GameObject go, Window window, MouseListener mouseListener, KeyListener keyListener) {
        // sets environment - DO NOT EDIT
        this.thisGameObject = go;
        this.window = window;
        this.mouseListener = mouseListener;
        this.keyListener = keyListener;
        this.camera = this.window.getView().camera();
        this.view = this.window.getView();
    }
}
""";


    public Path createNewScript(String scriptName) {
        if (currentProject == null) {
            throw new IllegalStateException("No project open");
        }

        // validace názvu
        if (!scriptName.matches("[A-Z][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("Invalid Java class name");
        }

        Path scriptPath = currentProject.getScriptsPath()
                .resolve(scriptName + ".java");

        if (Files.exists(scriptPath)) {
            throw new RuntimeException("Script already exists");
        }

        try {
            String content = SCRIPT_TEMPLATE.formatted(scriptName);
            Files.writeString(scriptPath, content);
            return scriptPath;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Path getScriptPath(String scriptName) {
        if (currentProject == null) {
            throw new IllegalStateException("No project open");
        }

        // validace názvu
        if (!scriptName.matches("[A-Z][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("Invalid Java class name");
        }

        Path scriptPath = currentProject.getScriptsPath()
                .resolve(scriptName + ".java");

        if (Files.exists(scriptPath)) {
            return scriptPath;
        }
        return null;
    }

    public void openInVSCode(Path file) {
        try {
            new ProcessBuilder(
                    "cmd", "/c", "code", file.toAbsolutePath().toString()
            ).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

class SceneData {
    List<GameObjectData> objects;
    List<GroupData> groups;
}

class GameObjectData {
    String name;
    float posX, posY;
    float scaleX, scaleY;
    float rotation;
    float roundness;
    int zIndex;

    boolean colorOnly;
    String texturePath;  // null pokud není sprite
    String shapeType;
    List<float[]> shapePoints;

    float r, g, b, a; // color fallback
    ArrayList<String> scripts;
}

class GroupData {
    String name;
    List<GameObjectData> objects = new ArrayList<>();
    List<GroupData> groups = new ArrayList<>();
}

