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

import java.nio.file.Path;

public class ScriptComponent extends Component {

    private final String className;
    private Script scriptInstance;
    public boolean started = false;
    private final Path filePath;

    public ScriptComponent(String className, Path filePath) {
        this.className = className;
        this.filePath = filePath;
        load();
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

//        if (ImGui.button("init")) {
//            scriptInstance.init();
//        }

        if (scriptInstance == null) {
            ImGui.textDisabled("Script not loaded");
            return;
        }

        for (var field : ScriptUtils.getExposedFields(scriptInstance)) {
            try {
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
