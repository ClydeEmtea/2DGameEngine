package components;

import engine.Component;
import engine.KeyListener;
import engine.MouseListener;
import engine.Window;
import imgui.ImGui;
import project.ProjectManager;
import scripts.Script;
import scripts.ScriptCompiler;
import scripts.ScriptLoader;
import scripts.ScriptUtils;

import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class ScriptComponent extends Component {

    private final String className;
    private Script scriptInstance;
    public boolean started = false;
    private final Path filePath;
    private WatchService watchService;

    public ScriptComponent(String className, Path filePath) {
        this.className = className;
        this.filePath = filePath;
        load();
        watchFileChanges();
    }

    private void watchFileChanges() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            filePath.getParent().register(watchService, ENTRY_MODIFY);

            Thread watcherThread = new Thread(() -> {
                while (true) {
                    try {
                        WatchKey key = watchService.take();
                        for (WatchEvent<?> event : key.pollEvents()) {
                            Path changed = (Path) event.context();
                            if (changed.equals(filePath.getFileName())) {
                                System.out.println("Script changed: " + filePath);
                                reloadScript();
                            }
                        }
                        key.reset();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            });
            watcherThread.setDaemon(true);
            watcherThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reloadScript() {
        // 1. Compile
        ScriptCompiler.compile(filePath);

        // 2. Reload
        Script newScript = ScriptLoader.loadScript(filePath.getParent(), className);
        if (newScript != null) {
            // 3. Copy environment a instance
            newScript.setEnvironment(this.gameObject, Window.get(), MouseListener.get(), KeyListener.get());
            this.scriptInstance = newScript;
            System.out.println("Script reloaded: " + className);
        }
    }

    public String getClassName() {
        return className;
    }

    public Script getScriptInstance() {
        return scriptInstance;
    }

    public Path getFilePath() {
        return filePath;
    }

    @Override
    public void update(float dt) {

    }

    @Override
    public void initScript() {
        scriptInstance.init();
        scriptInstance.setEnvironment(this.gameObject, Window.get(), MouseListener.get(), KeyListener.get());
    }

    @Override
    public void updateScript(float dt) {
        scriptInstance.update(dt);
    }

    public void load() {
        ScriptCompiler.compile(filePath);

        scriptInstance = ScriptLoader.loadScript(
                filePath.getParent(),
                className
        );

    }

    public void onAddedToGameObject() {
        scriptInstance.setEnvironment(this.gameObject, Window.get(), MouseListener.get(), KeyListener.get());
    }


    @Override
    public void imgui() {
        super.imgui();
        ImGui.sameLine();
        ImGui.text(": " + this.className);
        if (ImGui.button("Edit")) {
            System.out.println(this.filePath);
            ProjectManager.get().openInVSCode(this.filePath);
        }
        ImGui.sameLine();
        if (ImGui.button("Remove")) {
            this.gameObject.removeComponent(this);
        }

//        if (ImGui.button("init")) {
//            scriptInstance.init();
//        }

        if (scriptInstance == null) {
            ImGui.textDisabled("Script not loaded");
            return;
        }

        var fields = ScriptUtils.getExposedFields(scriptInstance);
        for (var field : fields) {
            try {
                field.setAccessible(true);
                String type = field.getType().getSimpleName();
                String name = field.getName();
                Object value = field.get(scriptInstance);

                String text = type + " " + name + " = " + String.valueOf(value);
                ImGui.text(text);

            } catch (IllegalAccessException e) {
                ImGui.textColored(1, 0, 0, 1, "Error reading field");
            }
        }

    }

}
