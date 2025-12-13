package scripts;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.nio.file.Path;

public class ScriptCompiler {

    public static void compile(Path javaFile) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        if (compiler == null) {
            throw new RuntimeException(
                    "No Java compiler found. Are you running a JRE instead of a JDK?"
            );
        }

        int result = compiler.run(
                null,
                null,
                null,
                javaFile.toAbsolutePath().toString()
        );

        if (result != 0) {
            throw new RuntimeException("Script compilation failed");
        }
    }
}
