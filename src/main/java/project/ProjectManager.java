package project;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import components.*;
import engine.*;
import observers.Event;
import observers.EventSystem;
import observers.EventType;
import org.joml.Vector2f;
import org.joml.Vector4f;
import physics2d.components.Box2DCollider;
import physics2d.components.CapsuleCollider;
import physics2d.components.CircleCollider;
import physics2d.components.RigidBody2D;
import physics2d.enums.BodyType;
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

    public void createNewScene(String name) {
        if (currentProject == null) return;

        Path sceneFile = scenesDir().resolve(name + ".json");

        try {
            if (!Files.exists(sceneFile)) {
                Files.createFile(sceneFile);
            }

            ProjectConfig cfg;
            if (Files.exists(config())) {
                String json = Files.readString(config());
                cfg = gson.fromJson(json, ProjectConfig.class);
            } else {
                cfg = new ProjectConfig();
                cfg.projectName = currentProject.getName();
                cfg.projectPath = currentProject.getProjectPath().toString();
                cfg.scenes = new ArrayList<>();
            }

            if (cfg.scenes == null) {
                cfg.scenes = new ArrayList<>();
            } else {
                cfg.scenes = new ArrayList<>(cfg.scenes);
            }

            if (!cfg.scenes.contains(name)) {
                cfg.scenes.add(name);
            }

            Files.writeString(config(), gson.toJson(cfg));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public List<String> getScenes() {
        if (currentProject == null) {
            return List.of();
        }

        try {
            if (!Files.exists(config())) {
                return List.of();
            }

            String json = Files.readString(config());
            ProjectConfig cfg = gson.fromJson(json, ProjectConfig.class);

            if (cfg == null || cfg.scenes == null) {
                return List.of();
            }

            return cfg.scenes;

        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
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
        System.out.println("Loading scene " + file.toAbsolutePath());
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
            SpriteRenderer sr = new SpriteRenderer(new Sprite(tex));
            sr.setColor(new Vector4f(god.r, god.g, god.b, god.a));
            go.addComponent(sr);
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

        // RigidBody2D
        if (god.rigidBody != null) {
            RigidBody2D rb = new RigidBody2D();
            rb.setVelocity(new Vector2f(god.rigidBody.velocity[0], god.rigidBody.velocity[1]));
            rb.setMass(god.rigidBody.mass);
            rb.setAngularDamping(god.rigidBody.angularDamping);
            rb.setLinearDamping(god.rigidBody.linearDamping);
            rb.setFixedRotation(god.rigidBody.fixedRotation);
            rb.setContinuousCollision(god.rigidBody.continuousCollision);
            rb.setBodyType(BodyType.valueOf(god.rigidBody.bodyType));
            go.addComponent(rb);
        }

        // Box2DCollider
        if (god.boxCollider != null) {
            Box2DCollider bc = new Box2DCollider();
            bc.setHalfSize(new Vector2f(god.boxCollider.halfSize[0], god.boxCollider.halfSize[1]));
            bc.setOffset(new Vector2f(god.boxCollider.offset[0],god.boxCollider.offset[1]));
            go.addComponent(bc);
        }

        // CircleCollider
        if (god.circleCollider != null) {
            CircleCollider cc = new CircleCollider();
            cc.setRadius(god.circleCollider.radius);
            cc.setOffset(new Vector2f(
                    god.circleCollider.offset[0],
                    god.circleCollider.offset[1]
            ));
            go.addComponent(cc);
        }

        if (god.capsuleCollider != null) {
            CapsuleCollider cc = new CapsuleCollider();
            cc.setRadius(god.capsuleCollider.radius);
            cc.setHeight(god.capsuleCollider.height);
            cc.setOffset(new Vector2f(
                    god.capsuleCollider.offset[0],
                    god.capsuleCollider.offset[1]
            ));
            go.addComponent(cc);
        }



        // Animations
        if (god.animations != null) {
            for (AnimationData ad : god.animations) {
                Animation anim = new Animation();
                anim.setAnimationName(ad.name);
                anim.setInterval(ad.interval);
                anim.setLoop(ad.loop);

                for (SpriteFrameData fd : ad.frames) {
                    Texture tex = AssetPool.getTexture(
                            new File(fd.texturePath).getName()
                    );

                    Vector2f[] texCoords = new Vector2f[]{
                            new Vector2f(fd.x,           fd.y),         // top-left
                            new Vector2f(fd.x + fd.w,    fd.y),         // top-right
                            new Vector2f(fd.x + fd.w,    fd.y + fd.h),  // bottom-right
                            new Vector2f(fd.x,           fd.y + fd.h)   // bottom-left
                    };
                    Sprite sprite = new Sprite(tex, texCoords);

                    anim.addSprite(sprite);
                }


                go.addComponent(anim);

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

    public void saveScene(String scene) {
        if (currentProject == null) {
            return;
        }

        Path file = scenesDir().resolve(scene + ".json");
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
            }
            Vector4f color = spriteRenderer.getColor();
            god.r = color.x;
            god.g = color.y;
            god.b = color.z;
            god.a = color.w;

        }

        god.scripts = new ArrayList<>();
        for (Component c : go.getAllScripts()) {
            if (c instanceof ScriptComponent sc) {
                god.scripts.add(sc.getClassName());
            }
        }

        ShapeRenderer shapeRenderer = go.getComponent(ShapeRenderer.class);
        if (shapeRenderer != null) {
            god.shapeType = shapeRenderer.getShapeType().name();
            god.shapePoints = new ArrayList<>();
            for (Vector2f p : shapeRenderer.getPoints()) {
                god.shapePoints.add(new float[]{p.x, p.y});
            }
        }
        // RigidBody2D
        RigidBody2D rigidBody = go.getComponent(RigidBody2D.class);
        if (rigidBody != null) {
            // Ukládáme všechny hodnoty kromě rawBody a bodyType
            god.rigidBody = new RigidBodyData();
            god.rigidBody.velocity = new float[]{rigidBody.getVelocity().x, rigidBody.getVelocity().y};
            god.rigidBody.mass = rigidBody.getMass();
            god.rigidBody.angularDamping = rigidBody.getAngularDamping();
            god.rigidBody.linearDamping = rigidBody.getLinearDamping();
            god.rigidBody.fixedRotation = rigidBody.isFixedRotation();
            god.rigidBody.continuousCollision = rigidBody.isContinuousCollision();
            god.rigidBody.bodyType = String.valueOf(rigidBody.getBodyType());
        }

        // Box2DCollider
        Box2DCollider collider = go.getComponent(Box2DCollider.class);
        if (collider != null) {
            god.boxCollider = new BoxColliderData();
            god.boxCollider.halfSize = new float[]{collider.getHalfSize().x, collider.getHalfSize().y};
            god.boxCollider.offset = new float[]{collider.getOffset().x, collider.getOffset().y};
        }

        // CircleCollider
        CircleCollider cc = go.getComponent(CircleCollider.class);
        if (cc != null) {
            god.circleCollider = new CircleColliderData();
            god.circleCollider.radius = cc.getRadius();
            god.circleCollider.offset = new float[]{
                    cc.getOffset().x,
                    cc.getOffset().y
            };
        }

        CapsuleCollider cap = go.getComponent(CapsuleCollider.class);
        if (cap != null) {
            god.capsuleCollider = new CapsuleColliderData();
            god.capsuleCollider.radius = cap.getRadius();
            god.capsuleCollider.height = cap.getHeight();
            god.capsuleCollider.offset = new float[]{
                    cap.getOffset().x,
                    cap.getOffset().y
            };
        }




        // Animations
        List<Component> animations = go.getAllAnimations();
        god.animations = new ArrayList<>();
        if (!animations.isEmpty()) {

            for (Component c : animations) {
                Animation a = (Animation) c;

                AnimationData ad = new AnimationData();
                ad.name = a.getAnimationName();
                ad.interval = a.getInterval();
                ad.loop = a.isLoop();
                ad.frames = new ArrayList<>();

                for (Sprite s : a.getSprites()) {
                    SpriteFrameData fd = new SpriteFrameData();
                    fd.texturePath = s.getTexture().getFilePath();
                    fd.x = s.getTexCoords()[0].x;
                    fd.y = s.getTexCoords()[0].y;
                    fd.w = s.getTexCoords()[2].x - fd.x;
                    fd.h = s.getTexCoords()[2].y - fd.y;
                    ad.frames.add(fd);
                }


                god.animations.add(ad);
            }
        }


        return god;
    }


    public void saveProject() {
        saveScene(Window.getView().currentScene.getName());
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
import physics2d.*;
import physics2d.components.*;
import java.util.*;
import static org.lwjgl.glfw.GLFW.*;
import org.joml.Vector2f;
import org.jbox2d.common.Vec2;

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
            Window.addError(e.getMessage());
            EventSystem.notify(null, new Event(EventType.ErrorEvent));
            assert false : e.getMessage();
        }
        return null;
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

    RigidBodyData rigidBody;
    BoxColliderData boxCollider;
    CircleColliderData circleCollider;
    CapsuleColliderData capsuleCollider;

    List<AnimationData> animations;
}

class GroupData {
    String name;
    List<GameObjectData> objects = new ArrayList<>();
    List<GroupData> groups = new ArrayList<>();
}

class RigidBodyData {
    float[] velocity;
    float mass;
    float angularDamping;
    float linearDamping;
    boolean fixedRotation;
    boolean continuousCollision;
    String bodyType;
}

class BoxColliderData {
    float[] halfSize;
    float[] offset;
}

class CircleColliderData {
    float radius;
    float[] offset;
}

class CapsuleColliderData {
    float radius;
    float height;
    float[] offset;
}



class AnimationData {
    String name;
    float interval;
    boolean loop;
    List<SpriteFrameData> frames;
}

class SpriteFrameData {
    String texturePath;
    float x, y, w, h;
}
