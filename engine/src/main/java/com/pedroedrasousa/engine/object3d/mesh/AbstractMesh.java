package com.pedroedrasousa.engine.object3d.mesh;

import com.pedroedrasousa.engine.Renderer;
import com.pedroedrasousa.engine.Texture;
import com.pedroedrasousa.engine.object3d.VBOData;
import com.pedroedrasousa.engine.object3d.VertexData;
import com.pedroedrasousa.engine.shader.Shader;

public abstract class AbstractMesh {

	private static final String	TAG	= Texture.class.getSimpleName();

	protected VertexData	vertexData;
	protected VBOData		vboData;		// Will assume null if not supported.
	protected Shader		shaderProgram;
	protected int			renderMode;

	public AbstractMesh(Renderer renderer, VertexData vertexData, Shader shaderProgram, boolean useVBOs) {
		this.vertexData = vertexData;
		this.shaderProgram = shaderProgram;
		if (useVBOs && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
			vboData = new VBOData(renderer, vertexData);
		}
	}

	/**
	 * Use VBOs by default if supported and argument useVBOs is omitted.
	 * @param vertexData
	 * @param shaderProgram
	 */
	public AbstractMesh(Renderer renderer, VertexData vertexData, Shader shaderProgram) {
		this(renderer, vertexData, shaderProgram, true);
	}

	public int getRenderMode() {
		return renderMode;
	}

	public void setRenderMode(int renderMode) {
		this.renderMode = renderMode;
	}

	public VBOData getVBOData() {
		return vboData;
	}

	public VertexData getMesh() {
		return vertexData;
	}

	public void setMesh(VertexData vertexData) {
		this.vertexData = vertexData;
	}

	public Shader getShaderProgram() {
		return shaderProgram;
	}

	public void setShaderProgram(Shader shaderProgram) {
		this.shaderProgram = shaderProgram;
	}

	public void reload() {
		if (vboData != null)
			vboData.loadVBOVertexData();
	}

	public abstract void render();
}
