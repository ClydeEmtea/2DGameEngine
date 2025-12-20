package render;

import engine.Window;
import observers.Event;
import observers.EventSystem;
import observers.EventType;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;

public class Shader {

    private int programID;
    private String vertexSource;
    private String fragmentSource;
    private final String fileVertex;
    private final String fileFragment;

    private boolean beingUsed = false;

    public Shader (String filePathVertex, String filePathFragment) {
        this.fileVertex = filePathVertex;
        this.fileFragment = filePathFragment;

        try {
            vertexSource = new String(Files.readAllBytes(Paths.get(fileVertex)));
            fragmentSource = new String(Files.readAllBytes(Paths.get(fileFragment)));
        } catch (IOException e) {
            Window.addError(e.getMessage());
            EventSystem.notify(null, new Event(EventType.ErrorEvent));
            e.printStackTrace();
            assert false : "Error: Could not read shader file";
        }

        System.out.println("Vertex Shader Source: " + vertexSource);
        System.out.println("Fragment Shader Source: " + fragmentSource);

    }

    // Compile and link shaders
    public void compile() {
        int vertexID, fragmentID;
        // Load and compile vertex shader
        vertexID = glCreateShader(GL_VERTEX_SHADER);
        // Pass the shader source to the GPU
        glShaderSource(vertexID, vertexSource);
        glCompileShader(vertexID);

        // Check for compilation errors
        int success = glGetShaderi(vertexID, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(vertexID, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: " + fileVertex + " vertex shader compilation failed");
            System.out.println("ERROR: " + glGetShaderInfoLog(vertexID, len));
            assert false : "Vertex shader compilation failed";
        }

        // Load and compile fragment shader
        fragmentID = glCreateShader(GL_FRAGMENT_SHADER);
        // Pass the shader source to the GPU
        glShaderSource(fragmentID, fragmentSource);
        glCompileShader(fragmentID);
        // Check for compilation errors
        success = glGetShaderi(fragmentID, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(fragmentID, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: " + fileFragment + " fragment shader compilation failed");
            System.out.println("ERROR: " + glGetShaderInfoLog(fragmentID, len));
            assert false : "Fragment shader compilation failed";
        }

        // Create shader program
        programID = glCreateProgram();
        // Attach shaders to the program
        glAttachShader(programID, vertexID);
        glAttachShader(programID, fragmentID);
        // Link the program
        glLinkProgram(programID);
        // Check for linking errors
        success = glGetProgrami(programID, GL_LINK_STATUS);
        if (success == GL_FALSE) {
            int len = glGetProgrami(programID, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: " + fileVertex + fileFragment + " linking of shaders failed");
            System.out.println("ERROR: " + glGetProgramInfoLog(programID, len));
            assert false : "Shader program linking failed";
        }
    }

    public void use() {
        if (beingUsed) {
            return;
        }
        // Bind the shader program
        glUseProgram(programID);
        beingUsed = true;

    }

    public void detach() {
        if (!beingUsed) {
            return;
        }
        // Detach the shader program
        glUseProgram(0);
        beingUsed = false;

    }

    public void uploadMat4f(String name, Matrix4f matrix) {
        int location = glGetUniformLocation(programID, name);
        use();
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        matrix.get(buffer);
        glUniformMatrix4fv(location, false, buffer);
    }

    public void uploadMat3f(String name, Matrix4f matrix) {
        int location = glGetUniformLocation(programID, name);
        use();
        FloatBuffer buffer = BufferUtils.createFloatBuffer(9);
        matrix.get(buffer);
        glUniformMatrix3fv(location, false, buffer);
    }

    public void uploadVec4f(String name, Vector4f vector) {
        int location = glGetUniformLocation(programID, name);
        use();
        glUniform4f(location, vector.x, vector.y, vector.z, vector.w);
    }

    public void uploadVec3f(String name, float x, float y, float z) {
        int location = glGetUniformLocation(programID, name);
        use();
        glUniform3f(location, x, y, z);
    }

    public void uploadVec2f(String name, float x, float y) {
        int location = glGetUniformLocation(programID, name);
        use();
        glUniform2f(location, x, y);
    }

    public void uploadFloat(String name, float value) {
        int location = glGetUniformLocation(programID, name);
        use();
        glUniform1f(location, value);
    }

    public void uploadInt(String name, int value) {
        int location = glGetUniformLocation(programID, name);
        use();
        glUniform1i(location, value);
    }

    public void uploadTexture(String name, int slot) {
        int location = glGetUniformLocation(programID, name);
        use();
        glUniform1i(location, slot);
    }

    public void uploadIntArray(String name, int[] array) {
        int location = glGetUniformLocation(programID, name);
        use();
        glUniform1iv(location, array);
    }
}
