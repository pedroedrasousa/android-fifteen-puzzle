package com.pedroedrasousa.engine;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.opengl.GLES20;

/**
 * OpenGL frame buffer.
 *
 * @author Pedro Edra Sousa
 */
public class FrameBuffer {

    //<editor-fold desc="Private attributes">
    private static final int BYTES_PER_FLOAT = 4;

    private int[] frameBuffer;
    private int[] depthBuffer;
    private int[] texture;
    private int width;
    private int height;

    private Renderer renderer;
    //</editor-fold>

    //<editor-fold desc="Constructors">

    /**
     * Creates a new instance of <code>FrameBuffer</code>.
     *
     * @param renderer
     * @param width
     * @param height
     */
    public FrameBuffer(Renderer renderer, int width, int height) {
        this.renderer = renderer;
        this.width = width;
        this.height = height;
        init(renderer, width, height);
    }
    //</editor-fold>

    public int getFrameBufferName() {
        return frameBuffer[0];
    }

    public int getDepthBufferName() {
        return depthBuffer[0];
    }

    public int getTextureName() {
        return texture[0];
    }

    public void bind() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, texture[0], 0);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, depthBuffer[0]);

        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            return;
        }

        GLES20.glViewport(0, 0, width, height);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }

    public void unbind() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
        GLES20.glViewport(0, 0, renderer.getViewportWidth(), renderer.getViewportHeight());
    }

    public void destroy() {
        GLES20.glDeleteFramebuffers(1, frameBuffer, 0);
        GLES20.glDeleteRenderbuffers(1, depthBuffer, 0);
    }

    //<editor-fold desc="Private methods">
    /**
     * Initializes OpenGl frame buffer.
     *
     * @param renderer
     * @param width Frame width in pixels.
     * @param height Frame height in pixels.
     */
    private void init(Renderer renderer, int width, int height) {

        frameBuffer = new int[1];
        depthBuffer = new int[1];
        texture = new int[1];

        // Generate
        GLES20.glGenFramebuffers(1, frameBuffer, 0);
        GLES20.glGenRenderbuffers(1, depthBuffer, 0);
        GLES20.glGenTextures(1, texture, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);

        // Create an empty buffer
        int[] buf = new int[width * height];
        Buffer pixels = ByteBuffer.allocateDirect(buf.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asIntBuffer();

        // Generate the textures
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixels);

        // Create the render buffer and bind a 16-bit depth buffer
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthBuffer[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height);
    }
    //</editor-fold>
}