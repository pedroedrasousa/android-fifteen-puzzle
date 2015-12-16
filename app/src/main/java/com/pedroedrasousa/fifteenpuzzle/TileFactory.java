package com.pedroedrasousa.fifteenpuzzle;

import java.util.Vector;

import com.pedroedrasousa.engine.Renderer;
import com.pedroedrasousa.engine.Texture;
import com.pedroedrasousa.engine.object3d.VertexData;
import com.pedroedrasousa.engine.shader.ColorShader;
import com.pedroedrasousa.engine.shader.TangentSpaceShader;

public class TileFactory {
	
	private static Renderer					mRenderer;
	
	private static VertexData				mMeshVertexData;
	private static VertexData				mBBMeshVertexData;
	
	private static Vector<Texture>			mBaseMapTexture;
	private static Vector<Texture>			mNormalMapTexture;
	
    private static TangentSpaceShader		mMeshShader;
    private static ColorShader				mBBMeshShader;
	
	static {
		mBaseMapTexture		= new Vector<Texture>();
		mNormalMapTexture	= new Vector<Texture>();
	}
		
	public static Tile buildTile(int number, int nbrBoardSquaresX, int nbrBoardSquaresY) {
		
		int posX = (number - 1) % nbrBoardSquaresX;
		int posY = (number - 1) / nbrBoardSquaresX;

		Tile t = new Tile(mRenderer, posX, posY, nbrBoardSquaresX, nbrBoardSquaresY);
		
		t.setMesh(mMeshVertexData, mMeshShader);
		t.setBoundingBoxMesh(mBBMeshVertexData, mBBMeshShader);
		
		t.setBaseMap(mBaseMapTexture.get(number - 1));
		t.setNormalMap(mNormalMapTexture.get(number - 1));
		
		t.setNumber(number);
    	
    	return t;
	}
	
	public static void setRenderer(Renderer renderer) {
		mRenderer = renderer;
	}
	
	public static VertexData getMesh() {
		return mMeshVertexData;
	}

	public static VertexData getBbmesh() {
		return mBBMeshVertexData;
	}

	public static void setMesh(VertexData mesh) {
		mMeshVertexData = mesh;
	}

	public static void setBBMesh(VertexData bbmesh) {
		mBBMeshVertexData = bbmesh;
	}

	public static void setMeshShader(TangentSpaceShader shader) {
		mMeshShader = shader;
	}

	public static void setBBMeshShader(ColorShader shader) {
		mBBMeshShader = shader;
	}

	public static TangentSpaceShader getmMeshShader() {
		return mMeshShader;
	}

	public static ColorShader getmBBMeshShader() {
		return mBBMeshShader;
	}

	public static void setBaseMapTexture(int number, Texture texture) {
		mBaseMapTexture.add(number - 1, texture);
	}

	public static void setNormalMapTexture(int number, Texture texture) {
		mNormalMapTexture.add(number - 1, texture);
	}
}
