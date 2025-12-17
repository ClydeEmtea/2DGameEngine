package util;

import components.Spritesheet;
import engine.Sound;
import project.ProjectManager;
import render.Shader;
import render.Texture;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static util.Constants.SHADER_PATH;
import static util.Constants.TEXTURE_PATH;

public class AssetPool {
    private static Map<String, Shader> shaders = new HashMap<>();
    private static Map<String, Texture> textures = new HashMap<>();
    private static Map<String, Spritesheet> spriteSheets = new HashMap<>();
    private static Map<String, Sound> sounds = new HashMap<>();

    public static Shader getShader(String vertexName, String fragmentName) {
        String shaderKey = vertexName + fragmentName;
        if (AssetPool.shaders.containsKey(shaderKey)) {
            return AssetPool.shaders.get(shaderKey);
        } else {
            Shader shader = new Shader(SHADER_PATH + vertexName, SHADER_PATH + fragmentName);
            shader.compile();
            AssetPool.shaders.put(shaderKey, shader);
            return shader;
        }
    }

    public static Texture getTexture(String fileName) {
//        System.out.println((ProjectManager.get().getCurrentProject().getImagesPath() + "\\" + fileName).replace("\\", "/"));
        if (AssetPool.textures.containsKey(fileName)) {
            return AssetPool.textures.get(fileName);
        } else {
            Texture texture = new Texture((ProjectManager.get().getCurrentProject().getImagesPath() + "\\" + fileName).replace("\\", "/"));
            System.out.println("ano");
            AssetPool.textures.put(fileName, texture);
            return texture;
        }

    }

    public static void addSpritesheet(String name, Spritesheet spritesheet) {

        if (spriteSheets.containsKey(name)) {
            assert false: "Spritesheet with name " + name + " already exists.";
        }
        spriteSheets.put(name, spritesheet);
    }

    public static Spritesheet getSpritesheet(String name) {
        if (spriteSheets.containsKey(name)) {
            return spriteSheets.get(name);
        } else {
            assert false : "Spritesheet with name " + name + " does not exist.";
        }
        return null;
    }

    public static Sound getSound(String fileName, boolean loops) {
        if (AssetPool.sounds.containsKey(fileName)) {
            return AssetPool.sounds.get(fileName);
        } else {
            Sound sound = new Sound((ProjectManager.get().getCurrentProject().getAudioPath() + "\\" + fileName).replace("\\", "/"), loops);
            System.out.println("ano, zvuk");
            AssetPool.sounds.put(fileName, sound);
            return sound;
        }
    }

    public static Sound addSound(String fileName, boolean loops) {
        if (AssetPool.sounds.containsKey(fileName)) {
            return AssetPool.sounds.get(fileName);
        } else {
            Sound sound = new Sound((ProjectManager.get().getCurrentProject().getAudioPath() + "\\" + fileName).replace("\\", "/"), loops);
            System.out.println("ano, zvuk");
            AssetPool.sounds.put(fileName, sound);
            return sound;
        }
    }

    public static Collection<Sound> getAllSounds() {
        return sounds.values();
    }

}
