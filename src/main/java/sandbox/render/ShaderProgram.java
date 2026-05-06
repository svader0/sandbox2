package sandbox.render;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.lwjgl.opengl.GL33.*;

public class ShaderProgram {
    private final int programId;

    public ShaderProgram(String vertResource, String fragResource) {
        int vert = compileShader(GL_VERTEX_SHADER,   loadResource(vertResource));
        int frag = compileShader(GL_FRAGMENT_SHADER, loadResource(fragResource));

        programId = glCreateProgram();
        glAttachShader(programId, vert);
        glAttachShader(programId, frag);
        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE)
            throw new RuntimeException("Shader link error:\n" + glGetProgramInfoLog(programId));

        // Once linked, the individual shader objects aren't needed
        glDeleteShader(vert);
        glDeleteShader(frag);
    }

    private int compileShader(int type, String source) {
        int id = glCreateShader(type);
        glShaderSource(id, source);
        glCompileShader(id);

        if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE)
            throw new RuntimeException("Shader compile error:\n" + glGetShaderInfoLog(id));

        return id;
    }

    private String loadResource(String path) {
        try (InputStream in = getClass().getResourceAsStream(path)) {
            if (in == null)
                throw new RuntimeException("Shader resource not found: " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load shader: " + path, e);
        }
    }

    public void bind()   { glUseProgram(programId); }
    public void unbind() { glUseProgram(0); }

    public int getUniform(String name) {
        int loc = glGetUniformLocation(programId, name);
        if (loc == -1)
            System.err.println("Warning: uniform '" + name + "' not found");
        return loc;
    }

    public void destroy() { glDeleteProgram(programId); }
}