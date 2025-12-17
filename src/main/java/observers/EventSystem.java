package observers;

import engine.GameObject;

import java.util.ArrayList;
import java.util.List;

public class EventSystem {
    private static List<Observer> observers = new ArrayList<>();

    public static void addObserver(Observer observer) {
        observers.add(observer);
    }

    public static void notify(GameObject gameObject, Event event) {
        for (Observer observer : observers) {
            observer.onNotify(gameObject, event);
        }
    }
}
