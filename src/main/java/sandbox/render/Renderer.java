package sandbox.render;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.GL33.*;

public class Renderer {
    private ShaderProgram shader;
    private int vao, vbo, ebo;
    private int textureId;
    private int simWidth, simHeight;

    public void init(int simWidth, int simHeight) {
        this.simWidth  = simWidth;
        this.simHeight = simHeight;

        shader = new ShaderProgram("/shaders/render.vert", "/shaders/render.frag");

        setupQuad();
        setupTexture();
    }

    private void setupQuad() {
        // Two triangles covering the entire screen, with UV coords
        float[] vertices = {
            // position    // texcoord
            -1f,  1f,      0f, 0f,   // top-left
             1f,  1f,      1f, 0f,   // top-right
             1f, -1f,      1f, 1f,   // bottom-right
            -1f, -1f,      0f, 1f,   // bottom-left
        };
        int[] indices = { 0, 1, 2,  2, 3, 0 };

        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        ebo = glGenBuffers();

        glBindVertexArray(vao);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer vbuf = stack.floats(vertices);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, vbuf, GL_STATIC_DRAW);

            var ibuf = stack.ints(indices);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, ibuf, GL_STATIC_DRAW);
        }

        int stride = 4 * Float.BYTES;
        // position (location 0)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);
        // texcoord (location 1)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 2L * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindVertexArray(0);
    }

    private void setupTexture() {
        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);

        // Nearest-neighbor — we want crisp pixels, not blurry interpolation
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        // Allocate empty texture the size of the sim grid
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8,
                     simWidth, simHeight, 0,
                     GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);

        glBindTexture(GL_TEXTURE_2D, 0);
    }

    // Called every frame with raw RGBA pixel data from the world
    public void uploadPixels(ByteBuffer pixels) {
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0,
                        simWidth, simHeight,
                        GL_RGBA, GL_UNSIGNED_BYTE, pixels);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void draw() {
        glClear(GL_COLOR_BUFFER_BIT);

        shader.bind();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glUniform1i(shader.getUniform("uTexture"), 0);

        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);

        shader.unbind();
    }

    public void destroy() {
        shader.destroy();
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
        glDeleteTextures(textureId);
    }
}