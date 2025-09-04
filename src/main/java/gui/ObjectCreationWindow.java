package gui;

import components.Sprite;
import components.SpriteRenderer;
import engine.GameObject;
import engine.Transform;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImString;
import org.joml.Vector2f;
import org.joml.Vector4f;
import render.Texture;
import util.AssetPool;

import javax.swing.*;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class ObjectCreationWindow {

    private static ImString name = new ImString("New Game Object", 128);
    private static boolean useSprite = false;
    private static float[] color = {1, 1, 1, 1};

    private static String selectedFilePath = "";

    public static void imgui(engine.Scene scene) {
        ImGui.begin("Create Object");

        ImGui.inputText("Name", name, ImGuiInputTextFlags.EnterReturnsTrue | ImGuiInputTextFlags.AutoSelectAll);
        ImGui.text(name.toString());

        if (!useSprite)
            ImGui.colorEdit4("Color", color);

        if (ImGui.checkbox("Use Sprite", useSprite)) {
            useSprite = !useSprite;
        }

        if (useSprite) {
            if (ImGui.button("Browse Texture")) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                chooser.setCurrentDirectory(new File("assets/images"));

                chooser.setAcceptAllFileFilterUsed(false);
                chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                        "Image files", "png", "jpg", "jpeg"));
                int result = chooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    String filePath = file.getAbsolutePath();
                    String lower = filePath.toLowerCase();
                    // Jen .png nebo .jpg
                    if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                        selectedFilePath = filePath;
                    } else {
                        selectedFilePath = "";
                        System.out.println("Only PNG or JPG allowed");
                    }
                }
            }

            if (!selectedFilePath.isEmpty()) {
                ImGui.text("Selected: " + selectedFilePath);
            } else {
                ImGui.text("No file selected");
            }
        }

        if (ImGui.button("Create")) {
            // Zkontrolujeme duplicity jmen
            String baseName = name.get();
            String finalName = baseName;
            Set<String> existingNames = new HashSet<>();
            for (GameObject go : scene.getGameObjects()) {
                existingNames.add(go.getName());
            }

            int counter = 1;
            while (existingNames.contains(finalName)) {
                finalName = baseName + " (" + counter + ")";
                counter++;
            }

            GameObject go;

            if (useSprite && !selectedFilePath.isEmpty()) {
                Texture tex = AssetPool.getTexture(selectedFilePath);
                int width = tex.getWidth();
                int height = tex.getHeight();
                go = new GameObject(finalName,
                        new Transform(new Vector2f(400, 300), new Vector2f(width, height)), 0);

                System.out.println("Creating object with sprite: " + selectedFilePath + " (" + width + "x" + height + ")");
                Sprite sprite = new Sprite(tex);
                go.addComponent(new SpriteRenderer(sprite));
            } else {
                go = new GameObject(finalName,
                        new Transform(new Vector2f(400, 300), new Vector2f(128, 128)), 0);
                go.addComponent(new SpriteRenderer(new Vector4f(color[0], color[1], color[2], color[3])));
            }

            scene.addGameObjectToScene(go);

            // Reset fields
            name.set("New Game Object");
            useSprite = false;
            color[0] = 1;
            color[1] = 1;
            color[2] = 1;
            color[3] = 1;
            selectedFilePath = "";
        }

        ImGui.end();
    }
}
