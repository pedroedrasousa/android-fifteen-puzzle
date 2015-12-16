package com.pedroedrasousa.engine.object3d;

import android.content.Context;
import android.opengl.GLES20;

import com.pedroedrasousa.engine.Renderer;
import com.pedroedrasousa.engine.UniqueColorFactory;
import com.pedroedrasousa.engine.math.Vec3;
import com.pedroedrasousa.engine.shader.ColorShader;
import com.pedroedrasousa.engine.shader.TangentSpaceShader;
import com.pedroedrasousa.engine.object3d.mesh.ColorMesh;
import com.pedroedrasousa.engine.object3d.mesh.TangentSpaceMesh;


public class PickableModel extends Model {
	
	private ColorMesh	boundingBoxMesh;
	private Renderer	mRenderer;
	
	public PickableModel(Renderer renderer) {
		super(renderer);
		mRenderer = renderer;
	}

	public ColorMesh getBoundingBoxMesh() {
		return boundingBoxMesh;
	}

	public void setMesh(VertexData vertexData, TangentSpaceShader shader) {
		mesh = new TangentSpaceMesh(mRenderer, vertexData, shader);
	}
	
	public void setBoundingBoxMesh(VertexData vertexData, ColorShader shader) {
		boundingBoxMesh = new ColorMesh(mRenderer, vertexData, shader, UniqueColorFactory.buildUniqueColor());
	}
	
	public void loadFromOBJ(Context context, TangentSpaceShader tangentSpaceShader, ColorShader colorShader, String assetName) {
		final MeshLoader ml = new MeshLoader();
		ml.loadFromObj(context, assetName);
		VertexData vertexData	= ml.getVertexData();
		VertexData bbVertexData	= ml.getBoundingBoxMesh();
		setMesh(vertexData, tangentSpaceShader);
		setBoundingBoxMesh(bbVertexData, colorShader);
	}

	public void renderBoundingBox() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, baseMap.getHandle());
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, normalMap.getHandle());
        boundingBoxMesh.render();
	}
	
	/**
	 * Indicates whether a color matches this models bounding box color or not.
	 * @param pickedColor The picked pixel color.
	 * @return True if the picked pixel color matches the model bounding box color.
	 */
	public boolean isPicked(Vec3 pickedColor) {
		if (boundingBoxMesh.getColor().equals(pickedColor, UniqueColorFactory.COLOR_ERROR_DELTA)) {
			return true;
		}
		return false;
	}
	
//	@Override
//	public void reload(Context context) {
//		super.reload(context);
//		mBoundingBoxMesh.reload();
//	}
}
