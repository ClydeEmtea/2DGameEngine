package util;

public class IdGenerator {
    private static long currentId = 0;

    public static synchronized long getNextId() {
        return currentId++;
    }

    public static synchronized void setCurrentId(long newId) {
        currentId = newId;
    }

    public static void resetCurrentIdFromObjects(Iterable<? extends HasId> objects) {
        long maxId = 0;
        for (HasId obj : objects) {
            if (obj.getId() > maxId) maxId = obj.getId();
        }
        setCurrentId(maxId + 1);
    }
}
