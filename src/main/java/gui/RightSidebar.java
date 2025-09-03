package gui;

import java.util.ArrayList;
import java.util.List;

public class RightSidebar {

    private static final List<Runnable> renderCallbacks = new ArrayList<>();

    private RightSidebar() {
    }

    public static void clearCallbacks() {
        renderCallbacks.clear();
    }

    public static void addCallback(Runnable callback) {
        renderCallbacks.add(callback);
    }

    public static void render() {
        for (Runnable callback : renderCallbacks) {
            callback.run();
        }
    }
}
