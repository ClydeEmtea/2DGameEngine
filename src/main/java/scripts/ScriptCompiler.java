package scripts;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.nio.file.Path;

public class ScriptCompiler {

    /**
     * Pokusí se zkompilovat java soubor.
     * @param javaFile cesta ke skriptu
     * @return true pokud kompilace proběhla v pořádku, false pokud nastala chyba
     */
    public static boolean compile(Path javaFile) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        if (compiler == null) {
            System.err.println("No Java compiler found. Are you running a JRE instead of a JDK?");
            return false;
        }

        int result = compiler.run(
                null, // System.in
                System.out, // System.out - výstup kompilátoru
                System.err, // System.err - chyby kompilátoru
                javaFile.toAbsolutePath().toString()
        );

        if (result != 0) {
            System.err.println("Script compilation failed: " + javaFile.getFileName());
            return false;
        }

        return true;
    }
}
