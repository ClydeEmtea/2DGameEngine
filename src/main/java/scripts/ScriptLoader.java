package scripts;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

public class ScriptLoader {

    public static Script loadScript(
            Path scriptsDir,
            String className
    ) {
        try {
            URL[] urls = { scriptsDir.toUri().toURL() };

            URLClassLoader loader =
                    new URLClassLoader(urls, Script.class.getClassLoader());

            Class<?> clazz = loader.loadClass(className);

            if (!Script.class.isAssignableFrom(clazz)) {
                throw new RuntimeException("Class does not implement Script");
            }

            return (Script) clazz
                    .getDeclaredConstructor()
                    .newInstance();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
