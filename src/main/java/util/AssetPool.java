package util;

import components.Spritesheet;
import render.Shader;
import render.Texture;

import java.util.HashMap;
import java.util.Map;

import static util.Constants.SHADER_PATH;
import static util.Constants.TEXTURE_PATH;

public class AssetPool {
    private static Map<String, Shader> shaders = new HashMap<>();
    private static Map<String, Texture> textures = new HashMap<>();
    private static Map<String, Spritesheet> spriteSheets = new HashMap<>();

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
        System.out.println(TEXTURE_PATH + fileName);
        if (AssetPool.textures.containsKey(fileName)) {
            return AssetPool.textures.get(fileName);
        } else {
            Texture texture = new Texture(TEXTURE_PATH + fileName);
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

}
