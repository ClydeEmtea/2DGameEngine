package util;

public interface Constants {
    int WIDTH = 1920;
    int HEIGHT = 1080;
    String TITLE = "Game Engine";

    int POSITION_SIZE = 2;
    int COLOR_SIZE = 4;
    int POSITION_OFFSET = 0;
    int COLOR_OFFSET = (POSITION_OFFSET + POSITION_SIZE) * Float.BYTES;
    int VERTEX_SIZE = 6;
    int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;
    int MAX_BATCH_SIZE = 1000;

    String SHADER_PATH = "assets/shaders/";
    String DEFAULT_VERTEX_SHADER = SHADER_PATH + "vertexDefault.glsl";
    String DEFAULT_FRAGMENT_SHADER = SHADER_PATH + "fragmentDefault.glsl";

    String TEXTURE_PATH = "assets/images/";
}
