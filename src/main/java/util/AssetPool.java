package util;

import render.Shader;
import render.Texture;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static util.Constants.SHADER_PATH;
import static util.Constants.TEXTURE_PATH;

public class AssetPool {
    private static Map<String, Shader> shaders = new HashMap<>();
    private static Map<String, Texture> textures = new HashMap<>();

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
        if (AssetPool.textures.containsKey(fileName)) {
            return AssetPool.textures.get(fileName);
        } else {
            Texture texture = new Texture(TEXTURE_PATH + fileName);
            AssetPool.textures.put(fileName, texture);
            return texture;
        }

    }

}
