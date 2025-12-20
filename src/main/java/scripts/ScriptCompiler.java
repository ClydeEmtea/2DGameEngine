package scripts;

import engine.Window;
import observers.Event;
import observers.EventSystem;
import observers.EventType;

import javax.tools.*;
import java.nio.file.Path;
import java.util.Arrays;

public class ScriptCompiler {

    public static boolean compile(Path javaFile) {

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            Window.addError("No Java compiler found (JDK required)");
            EventSystem.notify(null, new Event(EventType.ErrorEvent));
            return false;
        }

        DiagnosticCollector<JavaFileObject> diagnostics =
                new DiagnosticCollector<>();

        StandardJavaFileManager fileManager =
                compiler.getStandardFileManager(diagnostics, null, null);

        Iterable<? extends JavaFileObject> compilationUnits =
                fileManager.getJavaFileObjectsFromFiles(
                        Arrays.asList(javaFile.toFile())
                );

        JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                fileManager,
                diagnostics,
                null,
                null,
                compilationUnits
        );

        boolean success = task.call();

        if (!success) {
            for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {

                String msg =
                        d.getSource().getName() +
                                ":" + d.getLineNumber() +
                                ": " + d.getKind() +
                                ": " + d.getMessage(null);

                Window.addError(msg);
            }

            EventSystem.notify(null, new Event(EventType.ErrorEvent));
        }

        try {
            fileManager.close();
        } catch (Exception ignored) {}

        return success;
    }
}
