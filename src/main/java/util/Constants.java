package util;

import org.joml.Vector4f;

public interface Constants {
    int WIDTH = 1920;
    int HEIGHT = 1080;
    String TITLE = "Game Engine";

    int POSITION_SIZE = 2;
    int COLOR_SIZE = 4;
    int POSITION_OFFSET = 0;
    int COLOR_OFFSET = POSITION_OFFSET + POSITION_SIZE * Float.BYTES;
    int VERTEX_SIZE = 9;
    int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;
    int MAX_BATCH_SIZE = 1000;
    int TEXCOORD_SIZE = 2;
    int TEX_ID_SIZE = 1;
    int TEXCOORD_OFFSET = COLOR_OFFSET + COLOR_SIZE * Float.BYTES;
    int TEX_ID_OFFSET = TEXCOORD_OFFSET + TEXCOORD_SIZE * Float.BYTES;

    String SHADER_PATH = "assets/shaders/";
    String DEFAULT_VERTEX_SHADER = SHADER_PATH + "vertexDefault.glsl";
    String DEFAULT_FRAGMENT_SHADER = SHADER_PATH + "fragmentDefault.glsl";

    String TEXTURE_PATH = "assets/images/";

    Vector4f WHITE = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
}
