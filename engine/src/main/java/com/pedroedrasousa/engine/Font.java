package com.pedroedrasousa.engine;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.pedroedrasousa.engine.math.Vec2;
import com.pedroedrasousa.engine.math.Vec3;
import com.pedroedrasousa.engine.math.Vec4;
import com.pedroedrasousa.engine.shader.TextureShaderImpl;

/**
 * Font renderer.
 *
 * @author Pedro Edra Sousa
 */
public class Font implements RendererObserver {

    //<editor-fold desc="Private attributes">
    private final float TAB_FACTOR = 4.0f;
    private final float SPACE_FACTOR = 0.25f;
    private final float LINE_FACTOR = 0.8f;

    private final int NBR_H_CHARS = 16;
    private final int NBR_V_CHARS = 16;

    private final float SPACE_BETWEEN_CHARS_FACTOR = 0.8f;

    private float newLineSpaceFactor = LINE_FACTOR;

    private Vec2 scaleFactor = new Vec2(1.0f);
    private float widthScaleFactor;

    private int hInterval;
    private int vInterval;

    private Texture texture;

    /**
     * Pixel where the character starts in its bitmap space.
     */
    private float[] charPxStart = new float[NBR_H_CHARS * NBR_V_CHARS];;

    /**
     * Pixel where the character ends in its bitmap space.
     */
    private float[] charPxEnd = new float[NBR_H_CHARS * NBR_V_CHARS];;

    /**
     * Every char will use the same vertex coordinates
     */
    private FloatBuffer vertexBuffer;

    /**
     * Texture coordinates vary per char.
     */
    private FloatBuffer[] texCoordBuffer = new FloatBuffer[NBR_H_CHARS * NBR_V_CHARS];

    private TextureShaderImpl shaderProgram;

    private float[] mvpMatrix = new float[16];
    private float[] mvMatrix = new float[16];

    private Renderer renderer;

    private Vec4 color = new Vec4(1.0f);

    private float orthoWidth;
    private float orthoHeight;

    private Vec2 stringPos = new Vec2();

    private boolean depthTestCap;
    private boolean blendCap;
    //</editor-fold>

    //<editor-fold desc="Constructors">
    /**
     * Creates a new instance of <code>Font</code>
     *
     * @param renderer
     * @param assetName
     * @param scaleFactor
     */
    public Font(Renderer renderer, String assetName, float scaleFactor) {
        this.renderer = renderer;
        init(assetName, scaleFactor);
    }
    //</editor-fold>

    public float getVInterval() {
        return vInterval * 0.75f;
    }

    public float getHInterval() {
        return hInterval;
    }

    public Vec2 getScaleFactor() {
        return scaleFactor;
    }

    /**
     * Change font scale while maintaning the aspect ratio.
     *
     * @param factor Width factor relative to viewport.
     */
    public void setScaleFactor(float factor) {
        widthScaleFactor = factor;
        float viewportAspectRatio = renderer.getViewportWidth() / Math.max((float) renderer.getViewportHeight(), 0.01f);
        viewportAspectRatio = viewportAspectRatio / (orthoWidth / orthoHeight);
        scaleFactor.assign(widthScaleFactor, widthScaleFactor * viewportAspectRatio);
    }

    /**
     * @param x
     * @param y
     */
    public void setScaleFactor(float x, float y) {
        if (y == -1) {
            setScaleFactor(x);
        } else {
            widthScaleFactor = -1;
            scaleFactor.assign(x, y);
        }
    }

    public void setScaleFactor(Vec2 size) {
        setScaleFactor(size.x, size.y);
    }

    public void setPos(float x, float y) {
        stringPos.assign(x, y);
        buildModelMatrix();
    }

    public void setColor(Vec4 color) {
        this.color = color;
        shaderProgram.uniform4f("uColorFactor", this.color.x, this.color.y, this.color.z, this.color.w);
    }


    public void setColor(float r, float g, float b, float a) {
        setColor(new Vec4(r, g, b, a));
    }

    public void setNewLineSpaceFactor(float newLineSpaceFactor) {
        this.newLineSpaceFactor = newLineSpaceFactor;
    }

    public void resetNewLineSpaceFactor() {
        newLineSpaceFactor = LINE_FACTOR;
    }

    /**
     * Prepare OpenGL to render fonts.
     *
     * @param w
     * @param h
     */
    public void enable(float w, float h) {

        // Store OpenGL states to be restored when done with font rendering
        depthTestCap = GLES20.glIsEnabled(GLES20.GL_DEPTH_TEST);
        blendCap = GLES20.glIsEnabled(GLES20.GL_BLEND);

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        Matrix.orthoM(mvpMatrix, 0, 0, w, h, 0, 0, 100);

        shaderProgram.enable();

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture.getHandle());
        shaderProgram.uniform1i("uTexture", 0);
        shaderProgram.setVertexPosAttribPointer(2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        shaderProgram.uniformMatrix4fv("uMVPMatrix", 1, false, mvpMatrix, 0);
        shaderProgram.uniform4f("uColorFactor", color.x, color.y, color.z, color.w);

        orthoWidth = w;
        orthoHeight = h;

        setScaleFactor(widthScaleFactor);
    }

    /**
     * Restores OpenGL when done with font rendering.
     */
    public void disable() {

        if (depthTestCap) {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        } else {
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        }

        if (blendCap) {
            GLES20.glEnable(GLES20.GL_BLEND);
        } else {
            GLES20.glDisable(GLES20.GL_BLEND);
        }

        shaderProgram.disable();
    }

    /**
     * Renders a <code>String</code> using the specified color.
     *
     * @param strText
     * @param color
     */
    public void print(String strText, Vec3 color) {
        shaderProgram.uniform3f("uColorFactor", color.x, color.y, color.z);
        print(strText);
        shaderProgram.uniform3f("uColorFactor", this.color.x, this.color.y, this.color.z);
    }

    /**
     * Renders a <code>String</code> in the position specified by x and y using the specified color.
     *
     * @param x
     * @param y
     * @param strText
     * @param color
     */
    public void print(float x, float y, String strText, Vec4 color) {
        shaderProgram.uniform4f("uColorFactor", color.x, color.y, color.z, color.w);
        print(x, y, strText);
        shaderProgram.uniform4f("uColorFactor", this.color.x, this.color.y, this.color.z, this.color.w);
    }

    /**
     * Renders a <code>String</code> in the position specified by x and y.
     *
     * @param x
     * @param y
     * @param strText
     */
    public void print(float x, float y, String strText) {
        setPos(x, y);
        print(strText);
    }

    /**
     * Renders the specified <code>String</code>.
     *
     * @param strText
     */
    public void print(String strText) {
        if (strText == null)
            return;

        int len = strText.length();

        // Loop through every char in the string.
        for (int i = 0; i < len; i++) {
            char c = strText.charAt(i);

            if (c == ' ') {
                Matrix.translateM(mvMatrix, 0, SPACE_FACTOR, 0, 0);
            } else if (c == '\n') {
                // Translate after the matrix has been scaled so that translation values will be affected by the scale factor.
                mvMatrix[12] = stringPos.x;
                Matrix.translateM(mvMatrix, 0, 0.0f, newLineSpaceFactor, 0.0f);
            } else if (c == '\t') {
                Matrix.translateM(mvMatrix, 0, hInterval * TAB_FACTOR, 0, 0);
            } else if (c == '\\' && strText.charAt(i + 1) == '#') {
                String hexColor = strText.substring(i + 1, i + 8);
                Vec3 color = Vec3.hex2Vec3(hexColor);
                shaderProgram.uniform4f("uColorFactor", color.x, color.y, color.z, 1.0f);
                i += 7;
                continue;
            } else if (c == '\\' && strText.charAt(i + 1) == 's') {    // Change size.
                int strEndPos = strText.indexOf(']', i + 3);
                String strSize = strText.substring(i + 3, strEndPos);
                Vec2 size;

                if (strSize.contains(",")) {
                    // Assume that the string is composed by 2 floats.
                    size = new Vec2(strSize);
                } else {
                    // Assume that the string is a float.
                    float f = Float.parseFloat(strSize);
                    size = new Vec2(f, -1.0f);
                }

                // Undo previous scale factor.
                Matrix.scaleM(mvMatrix, 0, 1.0f / scaleFactor.x, 1.0f / scaleFactor.y, 0.0f);
                // Apply new scale factor.
                setScaleFactor(size);
                Matrix.scaleM(mvMatrix, 0, scaleFactor.x, scaleFactor.y, 0.0f);

                i += strEndPos - i;
                continue;
            } else {
                Matrix.translateM(mvMatrix, 0, -charPxStart[c] * SPACE_BETWEEN_CHARS_FACTOR, 0.0f, 0.0f);        // Translate to compensate char left margin.
                shaderProgram.setTexCoordsAttribPointer(2, GLES20.GL_FLOAT, false, 0, texCoordBuffer[c]);        // Set the texture coordinates according to the char.
                shaderProgram.uniformMatrix4fv("uMVMatrix", 1, false, mvMatrix, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
                Matrix.translateM(mvMatrix, 0, charPxEnd[c] * SPACE_BETWEEN_CHARS_FACTOR, 0.0f, 0.0f);            // Translate matrix to the end of the char.
            }
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Do nothing
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (widthScaleFactor != -1) {
            setScaleFactor(widthScaleFactor);
        }
    }

    private void buildModelMatrix() {
        Matrix.setIdentityM(mvMatrix, 0);
        Matrix.translateM(mvMatrix, 0, stringPos.x, stringPos.y, 0);
        Matrix.scaleM(mvMatrix, 0, scaleFactor.x, scaleFactor.y, 0.0f);
    }

    private void init(String assetName, float scaleFactor) {
        setScaleFactor(scaleFactor);

        // Create the shader program and get the handlers
        shaderProgram = new TextureShaderImpl(renderer, R.raw.font_vert, R.raw.font_frag);

        // Load the font texture
        texture = new Texture(renderer);
        texture.loadFromAsset(assetName);

        InputStream is = null;
        Bitmap bitmap = null;

        try {
            is = renderer.getActivity().getAssets().open(assetName);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            is = null;
        }

        // Get the width and height, in pixels, of the chars.
        hInterval = bitmap.getWidth() / NBR_H_CHARS;
        vInterval = bitmap.getHeight() / NBR_V_CHARS;

        // Build an array with vertex coordinates
        // Every letter will use the same vertex coordinates

        float[] vertices = new float[8];

        vertices[0] = 0.0f;
        vertices[1] = 1.0f;
        vertices[2] = 1.0f;
        vertices[3] = 1.0f;
        vertices[4] = 0.0f;
        vertices[5] = 0.0f;
        vertices[6] = 1.0f;
        vertices[7] = 0.0f;

        ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuf.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);


        float[] texture = new float[NBR_H_CHARS * NBR_V_CHARS * 8];

        int index = 0;
        int buffIndex = 0;

        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {

                float u1 = (float) x * 1.0f / 16.0f;
                float v1 = (float) y * 1.0f / 16.0f;

                float u2 = u1 + 1.0f / 16.0f;
                float v2 = v1 + 1.0f / 16.0f;

                texture[index] = u1;
                texture[index + 1] = v2;
                texture[index + 2] = u2;
                texture[index + 3] = v2;
                texture[index + 4] = u1;
                texture[index + 5] = v1;
                texture[index + 6] = u2;
                texture[index + 7] = v1;

                byteBuf = ByteBuffer.allocateDirect(texture.length * 4);
                byteBuf.order(ByteOrder.nativeOrder());
                texCoordBuffer[buffIndex] = byteBuf.asFloatBuffer();
                texCoordBuffer[buffIndex].put(texture);
                texCoordBuffer[buffIndex].position(0);

                // Get the character width in pixels
                int maxX = 0;
                int minX = hInterval;

                for (int yDelta = 0; yDelta < hInterval - 1; yDelta++) {
                    for (int xDelta = 0; xDelta < hInterval - 1; xDelta++) {
                        int color = bitmap.getPixel(x * hInterval + xDelta, y * vInterval + yDelta);
                        if (Color.alpha(color) != 0) {
                            if (xDelta > maxX) {
                                maxX = xDelta;
                            }
                            if (xDelta < minX) {
                                minX = xDelta;
                            }
                        }
                    }
                }

                charPxStart[buffIndex] = minX / (float) hInterval;
                charPxEnd[buffIndex] = maxX / (float) vInterval;

                buffIndex++;
            }
        }
    }
}
