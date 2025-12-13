package scripts;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ScriptUtils {

    public static List<Field> getExposedFields(Script script) {
        List<Field> result = new ArrayList<>();

        Class<?> clazz = script.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Exposed.class)) {
                field.setAccessible(true);
                result.add(field);
            }
        }
        return result;
    }
}
