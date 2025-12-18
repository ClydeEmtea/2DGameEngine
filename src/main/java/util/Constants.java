package util;

import org.joml.Vector4f;

public interface Constants {
    int WIDTH = 2560;
    int HEIGHT = 1440;
    String TITLE = "Game Engine";

    int POSITION_SIZE = 2;
    int COLOR_SIZE = 4;
    int POSITION_OFFSET = 0;
    int COLOR_OFFSET = POSITION_OFFSET + POSITION_SIZE * Float.BYTES;
    int MAX_BATCH_SIZE = 1000;
    int TEXCOORD_SIZE = 2;
    int TEX_ID_SIZE = 1;
    int ROUNDNESS_SIZE = 1;
    int MAX_VERTS_PER_SPRITE = 6;

    int VERTEX_SIZE = POSITION_SIZE + COLOR_SIZE + TEXCOORD_SIZE + TEX_ID_SIZE + ROUNDNESS_SIZE;
    int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

    int TEXCOORD_OFFSET = COLOR_OFFSET + COLOR_SIZE * Float.BYTES;
    int TEX_ID_OFFSET = TEXCOORD_OFFSET + TEXCOORD_SIZE * Float.BYTES;
    int ROUNDNESS_OFFSET = TEX_ID_OFFSET + TEX_ID_SIZE * Float.BYTES;

    String SHADER_PATH = "assets/shaders/";
    String DEFAULT_VERTEX_SHADER = "vertexDefault.glsl";
    String DEFAULT_FRAGMENT_SHADER = "fragmentDefault.glsl";

    final float EDITOR_SCALE = 100.0f;

    String TEXTURE_PATH = "assets/images/";

    Vector4f WHITE = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

    float[] BACKGROUND_COLOR = {0.07f, 0.07f, 0.07f, 1.0f};

    float[] GUI_BG =           {0.04f, 0.02f, 0.05f, 1.0f};  // hodně tmavě fialová (pozadí)
    float[] GUI_TITLE_BG =     {0.20f, 0.08f, 0.28f, 1.0f};  // tmavší purpurová pro titulky
    float[] GUI_BUTTON =       {0.25f, 0.12f, 0.32f, 1.0f};  // fialová s nádechem do vínova
    float[] GUI_BUTTON_HOVER = {0.35f, 0.18f, 0.45f, 1.0f};  // světlejší fialová pro hover


    float GUI_ROUNDING = 8.0f;


}
