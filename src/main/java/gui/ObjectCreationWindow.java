package gui;

import components.ShapeRenderer;
import components.Sprite;
import components.SpriteRenderer;
import engine.GameObject;
import engine.Transform;
import engine.View;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImString;
import org.joml.Vector2f;
import org.joml.Vector4f;
import project.ProjectManager;
import render.Texture;
import util.AssetPool;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class ObjectCreationWindow {

    private static ImString name = new ImString("New Game Object", 128);
    private static boolean useSprite = false;
    private static float[] color = {1, 1, 1, 1};

    private static String selectedFileName = "";

    public static boolean imgui(View view) {
        boolean created = false;
        ImGui.setNextWindowSize(400, 200);
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
                    String fileName = file.getName();
                    String lower = fileName.toLowerCase();

                    System.out.println(fileName);

                    if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {

                        // Cílová složka v projektu
                        Path texturesPath = ProjectManager.get()
                                .getCurrentProject()
                                .getImagesPath();

                        File dest = new File(texturesPath.toFile(), fileName);

                        try {
                            java.nio.file.Files.copy(
                                    file.toPath(),
                                    dest.toPath(),
                                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
                            );
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // !!! Ukládáme pouze název souboru, NE celou cestu
                        selectedFileName = fileName;

                    } else {
                        selectedFileName = "";
                        System.out.println("Only PNG or JPG allowed");
                    }
                }
            }

            if (!selectedFileName.isEmpty()) {
                ImGui.text("Selected: " + selectedFileName);
            } else {
                ImGui.text("No file selected");
            }
        }

        if (ImGui.button("Create")) {

            // Unikátní jméno
            String baseName = name.get();
            String finalName = baseName;
            Set<String> existingNames = new HashSet<>();

            for (GameObject go : view.getGameObjects()) {
                existingNames.add(go.getName());
            }

            int counter = 1;
            while (existingNames.contains(finalName)) {
                finalName = baseName + " (" + counter + ")";
                counter++;
            }

            GameObject go;

            if (useSprite && !selectedFileName.isEmpty()) {

                System.out.println("Loading texture: " + selectedFileName);
                Texture tex = AssetPool.getTexture(selectedFileName); // -> assets/images/<name>

                int width = tex.getWidth();
                int height = tex.getHeight();

                go = new GameObject(finalName,
                        new Transform(new Vector2f(400, 300), new Vector2f(width, height)), 0);

                System.out.println("Creating object with sprite: " + selectedFileName +
                        " (" + width + "x" + height + ")");

                Sprite sprite = new Sprite(tex);
                go.addComponent(new SpriteRenderer(sprite));

            } else {
                go = new GameObject(finalName,
                        new Transform(new Vector2f(400, 300), new Vector2f(128, 128)), 0);

                go.addComponent(new SpriteRenderer(
                        new Vector4f(color[0], color[1], color[2], color[3])
                ));
            }
            go.addComponent(new ShapeRenderer());

            view.addGameObjectToView(go);

            // Reset
            name.set("New Game Object");
            useSprite = false;
            color[0] = color[1] = color[2] = color[3] = 1;
            selectedFileName = "";

            created = true;
        }

        ImGui.end();
        return created;
    }
}
