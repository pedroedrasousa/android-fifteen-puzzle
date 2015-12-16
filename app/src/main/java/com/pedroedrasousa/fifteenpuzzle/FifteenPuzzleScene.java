package com.pedroedrasousa.fifteenpuzzle;

import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.gms.analytics.HitBuilders;
import com.pedroedrasousa.engine.EngineActivity;
import com.pedroedrasousa.engine.FrameBuffer;
import com.pedroedrasousa.engine.Renderer;
import com.pedroedrasousa.engine.RendererObserver;
import com.pedroedrasousa.engine.Scene;
import com.pedroedrasousa.engine.Texture;
import com.pedroedrasousa.engine.Timer;
import com.pedroedrasousa.engine.UniqueColorFactory;
import com.pedroedrasousa.engine.Utils;
import com.pedroedrasousa.engine.math.Vec2;
import com.pedroedrasousa.engine.math.Vec3;
import com.pedroedrasousa.engine.math.Vec4;
import com.pedroedrasousa.engine.shader.ColorShader;
import com.pedroedrasousa.engine.shader.ColorShaderImpl;
import com.pedroedrasousa.engine.shader.Shader;
import com.pedroedrasousa.engine.shader.SimpleShaderProg;
import com.pedroedrasousa.engine.shader.TangentSpaceShader;
import com.pedroedrasousa.engine.shader.TangentSpaceShaderImpl;
import com.pedroedrasousa.engine.Font;
import com.pedroedrasousa.engine.object3d.MeshLoader;
import com.pedroedrasousa.engine.object3d.VertexData;
import com.pedroedrasousa.engine.object3d.mesh.SimpleMesh;
import com.pedroedrasousa.engine.gui.*;
import com.pedroedrasousa.engine.gui.GuiElement.OnClickListener;

@SuppressWarnings("rawtypes")
public class FifteenPuzzleScene implements Scene, RendererObserver, FifteenPuzzleBoard, SensorEventListener {

	public final static String LEVEL_FILE		= "levels.txt";

	private final static int SHUFFLE_ITERATIONS = 60;

	public static final int GAMESTATUS_INPROGRESS			= 0;
	public static final int GAMESTATUS_SHUFFLING			= 1;
	public static final int GAMESTATUS_ANIMATION			= 2;
	public static final int GAMESTATUS_WINANIMATION			= 3;
	public static final int GAMESTATUS_STOPPED				= 4;
	public static final int GAMESTATUS_SOLVED				= 5;
	public static final int GAMESTATUS_LIMITEDMOVES			= 6;
	public static final int GAMESTATUS_MAX_MOVES_REACHED	= 7;

	/**
	 * v1 id was simply "challenge". The String change is a dirty way of distinguishing the 1st version
	 * preferences that, in case of an update keep stored in the device.
	 */
	public static final String GAME_ID_CHALLENGE			= "challenge_v2";

	private EngineActivity		mActivity;

	private String		mGameId1;
	private String		mGameId2;

    //<editor-fold desc="Threads">
    /**
     * Thread used to randomly shuffle the game tiles.
     */
    private Thread shufflerThread;

    /**
     * Thread used to animate the game tiles.
     */
	private Thread animationThread;
    //</editor-fold>

    /**
     * Picked color in the off-screen buffer.
     */
	private Vec3 pickedColor = new Vec3();

    /**
     * Geometry vertex data used in the tiles.
     */
	private VertexData	mTileVertexData;

    /**
     * Bounding box geometry vertex data used in the tiles.
     */
	private VertexData tileBBVertexData;

    /**
     * Off-screen plane used to get the z-buffer depth value.
     */
	private SimpleMesh plane;

	// Matrices

    //<editor-fold desc="Matrices">
    /**
     * Projection matrix.
     */
	private float[] projMatrix = new float[16];

    /**
     * View matrix.
     */
	private float[] vMatrix = new float[16];

    /**
     * Model view matrix.
     */
	private float[] mvMatrix = new float[16];

    /**
     * Model view projection matrix.
     */
	private float[] mvpMatrix = new float[16];
    //</editor-fold>

    //<editor-fold desc="Camera">
    /**
     * Specifies the position of the camera eye point.
     */
	private Vec3 cameraEye = new Vec3(0.0f, 16.0f, 1.5f);

    /**
     * Specifies the position of the camera reference point.
     */
	private Vec3 cameraCenter = new Vec3(0.0f, 0.0f, 0.0f);

    /**
     * Specifies the direction of the camera up vector.
     */
	private Vec3 cameraUp = new Vec3(0.0f, 1.0f, 0.0f);
    //</editor-fold>

	private Vec3 mLightPos;

    //<editor-fold desc="Shader programs">
	private TangentSpaceShader lightingShader;
	private Shader depth2ColorShader;
	private ColorShader simpleColorShader;
    //</editor-fold>

	private Timer				mTimer;
	private Font				mFont;
	//private FrameRateControler	mFramRateCtl;
	private GameBoard			mBoard;

	private Vector<Tile> mTiles = new Vector<Tile>();
	private Tile mSelectedTile;
	private Tile mCubeDown;
	private Tile mCubeUp;
	private Tile mCubeLeft;
	private Tile mCubeRight;

	private int mCurrentSize;

    private float mTouchPrevX;
	private float mTouchPrevY;

	private Vec3 mGyroscopeValues	= new Vec3();
	private Vec3 mCameraRot			= new Vec3();
	private Vec3 mCameraTargetRot	= new Vec3();

	private int mNbrMoves;
	private int mNbrAllowedMoves;

	private int mGameState;

	//private Toast mToast;

	// World projected screen coordinates.
	private Vec3	mTouchDownCoord	= new Vec3();
	private Vec3	mTouchUpCoord	= new Vec3();

	// Motion event stuff
	private boolean mMotionEventDown;
	private boolean mMotionEventMove;
	private float	mDeltaMoveX;
	private float	mDeltaMoveY;

	// Game in progress best score
	private int mBestTime;
	private int mBestMoves;

	private LinkedList<Object> mDisposables = new LinkedList<Object>();

	private Renderer renderer;

	private LinkedList<RendererObserver> mRendererObserverList = new LinkedList<RendererObserver>();

	private FrameBuffer	mAuxFrameBuffer;

	//GuiScreen guiTile;
	// TODO
	private LinkedList<GuiElement> mGuiElements			= new LinkedList<GuiElement>();
	private LinkedList<GuiElement> mDialogBoxElements	= new LinkedList<GuiElement>();

	private GuiTile bg;

	private SensorManager sensorManager;
	private Sensor gyroSensor;

	@SuppressLint("ShowToast")
	public FifteenPuzzleScene(Renderer renderer) {
		this.renderer = renderer;
		mActivity			= renderer.getActivity();
		mNbrAllowedMoves	= -1;

		mTimer			= new Timer();
		mLightPos		= new Vec3(0f, 20.0f, 0f);

		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glEnable(GLES20.GL_CULL_FACE);

		lightingShader = new TangentSpaceShaderImpl(this.renderer, R.raw.lighting_vert, R.raw.lighting_frag);
		depth2ColorShader = new SimpleShaderProg(this.renderer, R.raw.depth2color_vert, R.raw.depth2color_frag);
		simpleColorShader = new ColorShaderImpl(this.renderer, R.raw.simple_color_vert, R.raw.simple_color_frag);

		final MeshLoader ml = new MeshLoader();
		ml.loadFromObj(mActivity, "tile.obj");
		mTileVertexData		= ml.getVertexData();
		tileBBVertexData = ml.getBoundingBoxMesh();

		TileFactory.setRenderer(this.renderer);

		TileFactory.setMesh(mTileVertexData);
		TileFactory.setBBMesh(tileBBVertexData);

		TileFactory.setMeshShader(lightingShader);
		TileFactory.setBBMeshShader(simpleColorShader);

		for (int i = 1; i < 16; i++) {
			TileFactory.setBaseMapTexture(i, new Texture(this.renderer, "tile" + i + "_d.png"));
			TileFactory.setNormalMapTexture(i, new Texture(this.renderer, "tile" + i + "_n.png"));
		}

		//restartCurrentGame();

		// TODO
		//float boardWidth = mTileVertexData.getBoundaries().getWidth() * mBoard.getWidth();
		float boardWidth = 10.0f;

		// Initialize the buffers.
		float[] planeVertexData = {  boardWidth, -1.0f, -boardWidth,
									-boardWidth, -1.0f, -boardWidth,
									 boardWidth, -1.0f,  boardWidth,
									-boardWidth, -1.0f,  boardWidth };

		VertexData vd = MeshLoader.getVertexDataFromArray(planeVertexData);
		plane = new SimpleMesh(this.renderer, vd, depth2ColorShader);
		plane.setRenderMode(GLES20.GL_TRIANGLE_STRIP);

		mFont = new Font(this.renderer, "arial.png", 0.9f * this.renderer.getDisplayDensity());

		// Framebuffer will be recreated.
		if (mAuxFrameBuffer != null) {
			mAuxFrameBuffer.destroy();
			mAuxFrameBuffer = null;
		}

		Vec4 bgColor = new Vec4(this.renderer.getActivity().getString(R.string.gui_bg_color));

		// Background
		GuiTile.Builder guiTileBuilder = new GuiTile.Builder();
		guiTileBuilder.setRenderer(this.renderer);
		guiTileBuilder.setOrthoSize(1.0f, 1.0f);
		guiTileBuilder.setBaseColor(bgColor);
		//guiTileBuilder.setAlphaFactor(0.5f);
		guiTileBuilder.setPos(0.0f, 0.0f);
		guiTileBuilder.setSize(1.0f, 1.0f);
		guiTileBuilder.setTexture(new Texture(this.renderer, "bg1.jpg"));
		bg = guiTileBuilder.create();


		// Restart button
		PickableGuiTile.Builder builder = new PickableGuiTile.Builder();
		builder.setRenderer(this.renderer);
		builder.setOrthoSize(1.0f, 1.0f);
		builder.setTexture(new Texture(this.renderer, "btn.png"));
		builder.setBaseColor(new Vec4("#D65000"));
		builder.setAlphaFactor(0.5f);
		builder.setPos(0.79f, 0.022f);
		builder.setSize(0.18f, 0.11f);
		builder.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(Object extraData) {

				Runnable r = new Runnable() {
					public void run() {
						if ((mGameState == GAMESTATUS_INPROGRESS || mGameState == GAMESTATUS_LIMITEDMOVES) && isEveryTileStoped()) {
							mGameState = GAMESTATUS_STOPPED;
							restartCurrentGame();
						}
					}
				};
				FifteenPuzzleScene.this.renderer.queueEvent(r);
			}
		});
		GuiTile chTile = builder.create();
		mGuiElements.add(chTile);

		builder.setTexture(new Texture(this.renderer, "restart.png"));
		builder.setPos(0.15f, 0.15f);
		builder.setSize(0.7f, 0.65f);
		builder.setBaseColor(new Vec4(-1.0f));
		builder.setAlphaFactor(1.0f);
		builder.setParent(chTile);
		mGuiElements.add(builder.create());

	    // For some reason devices using early android versions report wrong gyroscope data???
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
		    sensorManager = (SensorManager)renderer.getActivity().getSystemService(android.content.Context.SENSOR_SERVICE);
		    gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		}

		sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);


		// Obtain the shared Tracker instance.
		FifteenPuzzleApplication application = (FifteenPuzzleApplication) renderer.getActivity().getApplication();

		//Log.i(TAG, "Setting screen name: " + name);
		application.getDefaultTracker().setScreenName("Game Screen");
		application.getDefaultTracker().send(new HitBuilders.ScreenViewBuilder().build());
		//onSurfaceChanged(null, this.renderer.getViewportWidth(), this.renderer.getViewportHeight());
	}

	public void setGameId(String gameId1, String gameId2) {
		mGameId1 = gameId1;
		mGameId2 = gameId2;
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {

		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glEnable(GLES20.GL_CULL_FACE);

//		for (Tile t : mTiles) {
//			t.reload(mActivity);
//		}
		plane.reload();
		Texture.reloadAll(mActivity);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {

		GLES20.glViewport(0, 0, width, height);

		// Create the perspective projection matrix
		// Width will vary as per aspect ratio
		float screenRatio = (float) width / Math.max(renderer.getViewportHeight(), 1);

		if (screenRatio < 1.0f)
			cameraEye.y = -25.0f * screenRatio + 33.0f;

		float near		= 1.0f;
		float far		= 30.0f;
		float fov		= 45.0f;
		float top		= (float) Math.tan((float) (fov * (float) Math.PI / 360.0f)) * near;
		float bottom	= -top;
		float left		= screenRatio * bottom;
		float right		= screenRatio * top;

		Matrix.frustumM(projMatrix, 0, left, right, bottom, top, near, far);

		Matrix.setLookAtM(vMatrix, 0, cameraEye.x, cameraEye.y, cameraEye.z,
                cameraCenter.x, cameraCenter.y, cameraCenter.z,
                cameraUp.x, cameraUp.y, cameraUp.z);

		mAuxFrameBuffer = new FrameBuffer(renderer, width, height);

		// Notify the observers.
		for (RendererObserver observer : mRendererObserverList)
			observer.onSurfaceChanged(gl, width, height);
	}

	public Tile createTile(int number, int x, int y) {

		int nbrBoardSquaresX = mBoard.getWidth();
		int nbrBoardSquaresY = mBoard.getHeight();

		Tile tile = TileFactory.buildTile(number, nbrBoardSquaresX, nbrBoardSquaresY);
		tile.setPosX(x);
		tile.setPosY(y);

		mBoard.setObj(x, y, tile);
		mTiles.add(tile);

		return tile;
	}

	private boolean isSolved() {
		Tile t;
		for (int x = 0; x < mBoard.getWidth(); x++) {
			for (int y = 0; y < mBoard.getHeight(); y++) {
				t = (Tile) mBoard.getObj(x, y);
				if (t != null && !t.isInPlace()) {
					return false;
				}
			}
		}
		return true;
	}

	public void resetGameBoard() {
		// Remove every tile from the board.
		for (int x = 0; x < mBoard.getWidth(); x++) {
			for (int y = 0; y < mBoard.getHeight(); y++) {
				mBoard.setObj(x, y, null);
			}
		}
		// Place the tiles in the correct position on the board.
		for (Tile t : mTiles) {
			t.setPosX(t.getCorrectPosX());
			t.setPosY(t.getCorrectPosY());
			mBoard.setObj(t.getCorrectPosX(), t.getCorrectPosY(), t);
		}

		getMovableTiles();

		mTimer.stop();
		mNbrMoves = 0;

		flyEveryTileDown();

		mGameState = GAMESTATUS_STOPPED;
	}

	private void flyEveryTileDown() {
		for (Tile t : mTiles) {
			t.flyToPlace();
		}
	}

	private void getMovableTiles() {
		for (int x = 0; x < mBoard.getWidth(); x++) {
			for (int y = 0; y < mBoard.getHeight(); y++) {
				if (mBoard.getObj(x, y) == null) {
					mCubeDown = (Tile) mBoard.getObj(x, y - 1);
					mCubeUp = (Tile) mBoard.getObj(x, y + 1);
					mCubeLeft = (Tile) mBoard.getObj(x + 1, y);
					mCubeRight = (Tile) mBoard.getObj(x - 1, y);
				}
			}
		}
	}

	/**
	 * Musn't run in renderer thread.
	 *
	 * @param iterations
	 */
	public void shuffle(int iterations) {
		Random rand = new Random();
		int last = -1;
		int n;
		boolean moved = false;

		mNbrMoves = 0;
		mGameState = GAMESTATUS_SHUFFLING;
		Tile.setSpeedFactor(1.3f);

		for (int i = 0; i < iterations; i++) {
			n = -1;
			moved = false;

			// Loop until a tile was moved
			while (!moved) {
				n = rand.nextInt(4);

				// Guarantee that every tile is stopped.
				for (Tile t : mTiles) {
					if (!t.isStopped())
						n = -1;
				}

				// Do not move the same tile
				if (n == 0 && last != 1 || n == 1 && last != 0 || n == 2
						&& last != 3 || n == 3 && last != 2) {
					switch (n) {
					case 0:
						moved = moveTile(mCubeRight, 1, 0);
						break;
					case 1:
						moved = moveTile(mCubeLeft, -1, 0);
						break;
					case 2:
						moved = moveTile(mCubeUp, 0, -1);
						break;
					case 3:
						moved = moveTile(mCubeDown, 0, 1);
						break;
					}
					if (moved) {
						last = n;
					}

				}
			}

			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		Tile.setSpeedFactor(1.0f);
		mGameState = GAMESTATUS_INPROGRESS;
	}

	private boolean moveTile(Tile c, int xOfsset, int yOfsset) {
		if (c == null)
			return false;

		int targetX = c.getPosX() + xOfsset;
		int targetY = c.getPosY() + yOfsset;

		if (targetX < 0 || targetY < 0 || targetX >= mBoard.getWidth()
				|| targetY >= mBoard.getHeight())
			return false;

		Object targetTile = mBoard.getObj(targetX, targetY);

		if (targetTile == null) {
			mBoard.setObj(c.getPosX(), c.getPosY(), null);
			mBoard.setObj(targetX, targetY, c);

			mCubeDown	= (Tile) mBoard.getObj(c.getPosX(), c.getPosY() - 1);
			mCubeUp		= (Tile) mBoard.getObj(c.getPosX(), c.getPosY() + 1);
			mCubeLeft	= (Tile) mBoard.getObj(c.getPosX() + 1, c.getPosY());
			mCubeRight	= (Tile) mBoard.getObj(c.getPosX() - 1, c.getPosY());

			if (targetX > c.getPosX()) {
				c.changeState(Tile.ROTATING_POSX);
			}

			else if (targetX < c.getPosX()) {
				c.changeState(Tile.ROTATING_NEGX);
			}

			if (targetY > c.getPosY()) {
				c.changeState(Tile.ROTATING_POSZ);
			}

			else if (targetY < c.getPosY()) {
				c.changeState(Tile.ROTATING_NEGZ);
			}

			return true;
		}

		return false;
	}



	private void createBoard3x3() {
		// Check if there is already a thread shuffling.
		if (shufflerThread != null && shufflerThread.isAlive()) {
			return;
		}
		mGameState = GAMESTATUS_STOPPED;
		if (mCurrentSize != SIZE3X3) {
			cleanGameBoard();
			FiffteenPuzzleBoardLoader.create3x3(this);
			initGame();
		}

		shufflerThread = new Thread(new Runnable() {
			public void run() {
				shuffle(SHUFFLE_ITERATIONS);
				mGameState = GAMESTATUS_INPROGRESS;
			}
		});
		shufflerThread.start();
	}

	private void createBoard3x4() {
		// Check if there is already a thread shuffling.
		if (shufflerThread != null && shufflerThread.isAlive()) {
			return;
		}
		mGameState = GAMESTATUS_STOPPED;
		if (mCurrentSize != SIZE3X4) {
			cleanGameBoard();
			FiffteenPuzzleBoardLoader.create3x4(this);
			initGame();
		}

		shufflerThread = new Thread(new Runnable() {
			public void run() {
				shuffle(SHUFFLE_ITERATIONS);
				mGameState = GAMESTATUS_INPROGRESS;
			}
		});
		shufflerThread.start();
	}

	private void createBoard4x4() {

		// Check if there is already a thread shuffling.
		if (shufflerThread != null && shufflerThread.isAlive()) {
			return;
		}
		mGameState = GAMESTATUS_STOPPED;
		if (mCurrentSize != SIZE4X4) {
			cleanGameBoard();
			FiffteenPuzzleBoardLoader.create4x4(this);
			initGame();
		}

		shufflerThread = new Thread(new Runnable() {
			public void run() {
				shuffle(SHUFFLE_ITERATIONS);
				mGameState = GAMESTATUS_INPROGRESS;
			}
		});
		shufflerThread.start();
	}

	/**
	 * Check if every tile is in the stopped state.
	 *
	 * @return
	 */
	private boolean isEveryTileStoped() {
		for (Tile t : mTiles) {
			if (!t.isStopped())
				return false;
		}
		return true;
	}

	public void restartCurrentGame() {

		mCurrentSize = -1;
		mTiles.clear();
		mTimer.reset();

		// Get the best score.
		mBestTime	= Score.getValue(mActivity, mGameId1, mGameId2, "time");
		mBestMoves	= Score.getValue(mActivity, mGameId1, mGameId2, "moves");

		showGameStartMsg();

		if (mGameId1.compareTo("classic") == 0) {
			if (mGameId2.compareTo("3x3") == 0)
				createBoard3x3();
			else if (mGameId2.compareTo("3x4") == 0)
				createBoard3x4();
			else if (mGameId2.compareTo("4x4") == 0)
				createBoard4x4();
		} else if (mGameId1.compareTo(GAME_ID_CHALLENGE) == 0) {
			cleanGameBoard();
			FiffteenPuzzleBoardLoader.loadBoardFromAsset(mActivity, this, LEVEL_FILE, Integer.parseInt(mGameId2));
			mGameState = GAMESTATUS_LIMITEDMOVES;
			initGame();
		}

		// Create a thread to start the time when the tiles are ready.
		new Thread(new Runnable() {
			public void run() {
				for (int i = 0; !mTimer.isRunning() && i < 100; i++) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (isEveryTileStoped() && mGameState != GAMESTATUS_SHUFFLING) {
						mTimer.start();
					}
				}
			}
		}).start();
	}

	private String mGameMsg;

	private void showGameStartMsg() {

		StringBuilder desc = new StringBuilder();

		if (mGameId1.equals("classic")) {
			// Show best time if there is one.
			if (mBestTime != -1)
				desc.append("Best time: " + Timer.secToStringMMSS(mBestTime * 1000) + " in " + mBestMoves + " moves");
		} else if (mGameId1.equals(GAME_ID_CHALLENGE)) {
			desc.append(FiffteenPuzzleBoardLoader.getLevelName(mActivity, LEVEL_FILE, Integer.parseInt(mGameId2) + 1));
			desc.append("\n");
			desc.append(FiffteenPuzzleBoardLoader.getLevelDesc(mActivity, LEVEL_FILE, Integer.parseInt(mGameId2) + 1));
			//if (mBestTime != -1) {
				//desc.append("\nBest time: " + Timer.secToStringMMSS(mBestTime * 1000));
			//}
		}

		mGameMsg = desc.toString();
	}

	private void makeEveryTileJump() {
		for (Tile t : mTiles) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			t.upAndBack();
		}
	}

	private boolean slideRight(Tile t) {
		if (mSelectedTile != null && mSelectedTile.getPosY() == mCubeRight.getPosY()) {
			// Get tiles in the same row
			Tile c1 = (Tile) mBoard.getObj(mSelectedTile.getPosX() + 1, mSelectedTile.getPosY());
			Tile c2 = (Tile) mBoard.getObj(mSelectedTile.getPosX() + 2, mSelectedTile.getPosY());
			moveTile(c2, 1, 0);
			moveTile(c1, 1, 0);
		}
		return moveTile(t, 1, 0);
	}

	private boolean slideLeft(Tile t) {
		if (mSelectedTile != null && mSelectedTile.getPosY() == mCubeLeft.getPosY()) {
			// Get tiles in the same row
			Tile c1 = (Tile) mBoard.getObj(mSelectedTile.getPosX() - 1, mSelectedTile.getPosY());
			Tile c2 = (Tile) mBoard.getObj(mSelectedTile.getPosX() - 2, mSelectedTile.getPosY());
			moveTile(c2, -1, 0);
			moveTile(c1, -1, 0);
		}
		return moveTile(t, -1, 0);
	}

	private boolean slideDown(Tile t) {
		if (mSelectedTile != null && mSelectedTile.getPosX() == mCubeDown.getPosX()) {
			// Get tiles in the same row
			Tile c1 = (Tile) mBoard.getObj(mSelectedTile.getPosX(), mSelectedTile.getPosY() + 1);
			Tile c2 = (Tile) mBoard.getObj(mSelectedTile.getPosX(), mSelectedTile.getPosY() + 2);
			moveTile(c2, 0, 1);
			moveTile(c1, 0, 1);
		}
		return moveTile(t, 0, 1);
	}

	private boolean slideUp(Tile t) {
		if (mSelectedTile != null && mSelectedTile.getPosX() == mCubeUp.getPosX()) {
			// Get tiles in the same row
			Tile c1 = (Tile) mBoard.getObj(mSelectedTile.getPosX(), mSelectedTile.getPosY() - 1);
			Tile c2 = (Tile) mBoard.getObj(mSelectedTile.getPosX(), mSelectedTile.getPosY() - 2);
			moveTile(c2, 0, -1);
			moveTile(c1, 0, -1);
		}
		return moveTile(t, 0, -1);
	}

	private void checkForMovement() {
		boolean moved = false;

		if (mMotionEventDown) {
			mMotionEventDown = false;
			mTouchDownCoord = Utils.getProjectCoords((int) mTouchPrevX, renderer.getViewportHeight() - (int) mTouchPrevY, vMatrix, projMatrix, renderer.getViewport());

			GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

			for (Tile t : mTiles) {

				// Build Model View and Model View Projection Matrices.
				Matrix.multiplyMM(mvMatrix, 0, vMatrix, 0, t.getModelMatrix(), 0);
				Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, mvMatrix, 0);
				simpleColorShader.enable();
				simpleColorShader.uniformMatrix4fv("uMVPMatrix", 1, false, mvpMatrix, 0);
				t.renderBoundingBox();
				simpleColorShader.disable();
			}

			pickedColor = Utils.getPixelColor((int) mTouchPrevX, renderer.getViewportHeight() - (int) mTouchPrevY);
			mSelectedTile = null;
			for (Tile t : mTiles) {
				if (t.isPicked(pickedColor)) {
					mSelectedTile = t;
				}
			}
			if (mGameState == GAMESTATUS_SOLVED && mSelectedTile != null && mSelectedTile.isStopped())
				mSelectedTile.upAndBack();
		} else if (mMotionEventMove) {
			mMotionEventMove = false;

			if (mSelectedTile != null) {
				// Get 3D coordinate.
				mTouchUpCoord = Utils.getProjectCoords((int) mTouchPrevX, renderer.getViewportHeight() - (int) mTouchPrevY, vMatrix, projMatrix, renderer.getViewport());

				mTouchUpCoord.sub(mTouchDownCoord);

				if (mGameState == GAMESTATUS_INPROGRESS || mGameState == GAMESTATUS_LIMITEDMOVES || mGameState == GAMESTATUS_STOPPED) {
					if (Math.abs(mTouchUpCoord.x) > Math.abs(mTouchUpCoord.z)) {
						if (mTouchUpCoord.x > 0.0f && mCubeRight != null && mSelectedTile.getPosY() == mCubeRight.getPosY()) {
							moved = slideRight(mSelectedTile);
						} else if (mTouchUpCoord.x < 0.0f && mCubeLeft != null && mSelectedTile.getPosY() == mCubeLeft.getPosY()) {
							moved = slideLeft(mSelectedTile);
						}
					} else {
						if (mTouchUpCoord.z < 0.0f && mCubeUp != null && mSelectedTile.getPosX() == mCubeUp.getPosX()) {
							moved = slideUp(mSelectedTile);
						} else if (mTouchUpCoord.z > 0.0f && mCubeDown != null && mSelectedTile.getPosX() == mCubeDown.getPosX()) {
							moved = slideDown(mSelectedTile);
						}
					}
					if (moved)
						mSelectedTile = null;
				}

				mCameraTargetRot.y -= mDeltaMoveX * renderer.getDisplayDensity() * renderer.getFrameFactor() * 0.1f;
				mCameraTargetRot.x -= mDeltaMoveY * renderer.getDisplayDensity() * renderer.getFrameFactor() * 0.1f;
			}
		}

		if (moved && (mGameState == GAMESTATUS_INPROGRESS || mGameState == GAMESTATUS_LIMITEDMOVES))
			mNbrMoves++;
	}

	@Override
	public void render() {

		if (mGameState == GAMESTATUS_WINANIMATION && animationThread != null && !animationThread.isAlive()) {
			if (isEveryTileStoped()) {
				mGameState = GAMESTATUS_SOLVED;
				updateScore();
				if (mGameId1.equals(GAME_ID_CHALLENGE)) {
					if (mGameId2.equals("47")) {
						// It was the last level!
						showChallengeFinishedDialog();
					} else {
						showChalengeWonDialog();
					}
				} else if (mGameId1.equals("classic")) {
					showClassicWonDialog();
				}
			}
		}

		if ((mGameState == GAMESTATUS_INPROGRESS || mGameState == GAMESTATUS_LIMITEDMOVES) && isSolved()) {
			animationThread = new Thread(new Runnable() {
				public void run() {
					mGameState = GAMESTATUS_WINANIMATION;
					mTimer.stop();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					makeEveryTileJump();
				}
			});
			animationThread.start();
		} else if (mGameState == GAMESTATUS_LIMITEDMOVES && mNbrAllowedMoves != -1 && mNbrMoves >= mNbrAllowedMoves && isEveryTileStoped()) { // Loosing conditions.
			mGameState = GAMESTATUS_MAX_MOVES_REACHED;


			// Thread to wait 1 sec before showing dialog bos
			new Thread(new Runnable() {
				public void run() {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					// Post show dialog action to renderer thread
					Runnable r = new Runnable() {
						public void run() {
							showMovesExaustedDialog();
						}
					};
					renderer.queueEvent(r);
				}
			}).start();
		}

		for (Tile t : mTiles) {
			t.update(renderer.getFrameFactor());
		}

		mCameraTargetRot.add(mGyroscopeValues);
		mCameraTargetRot.lerp(Vec3.ZERO, 0.1f);

		mCameraRot.lerp(mCameraTargetRot, 0.05f);

		Matrix.setLookAtM(vMatrix, 0,	cameraEye.x,		cameraEye.y,		cameraEye.z,
										cameraCenter.x,	cameraCenter.y,	cameraCenter.z,
										cameraUp.x,		cameraUp.y,		cameraUp.z);

		Matrix.rotateM(vMatrix, 0, -mCameraRot.x * 2.0f * renderer.getFrameFactor(), 1.0f, 0.0f, 0.0f);
		Matrix.rotateM(vMatrix, 0, mCameraRot.y * 2.0f * renderer.getFrameFactor(), 0.0f, 0.0f, 1.0f);


		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

		Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, vMatrix, 0);

		depth2ColorShader.enable();
		depth2ColorShader.uniformMatrix4fv("uMVPMatrix", 1, false, mvpMatrix, 0);
		plane.render();
		depth2ColorShader.disable();

		checkForMovement();

		// Clear the color picking stuff and render the main scene
		// Render to main buffer

		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

		bg.render();

		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);

		lightingShader.enable();
		lightingShader.uniformMatrix4fv("uVMatrix", 1, false, vMatrix, 0);
		lightingShader.uniform3f("aLightPos", mLightPos.x, mLightPos.y, mLightPos.z);
		lightingShader.uniform1i("uBaseMap", 0);
		lightingShader.uniform1i("uNormalMap", 1);

		for (Tile t : mTiles) {

			// Build Model View and Model View Projection Matrices
			Matrix.multiplyMM(mvMatrix, 0, vMatrix, 0, t.getModelMatrix(), 0);
			Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, mvMatrix, 0);

			// Specify matrix information
			lightingShader.uniformMatrix4fv("uMVMatrix", 1, false, mvMatrix, 0);
			lightingShader.uniformMatrix4fv("uMVPMatrix", 1, false, mvpMatrix, 0);

			// Render the model using previously specified data
			t.render();
		}

		lightingShader.disable();

		mFont.enable(1.0f, 1.0f);
		mFont.setScaleFactor(0.1f);
		mFont.setPos(0.015f, 0.005f);

		if (mGameState == GAMESTATUS_SHUFFLING) {
			mFont.print("Shuffling...");
		} else  {
			/*
			if (mGameState == GAMESTATUS_SOLVED) {
				mFont.print(0.01f, 0.01f + (int)(mFont.getVInterval() * 2), "- SOLVED -");
			} else if (mGameState == GAMESTATUS_MAX_MOVES_REACHED) {
				mFont.print(0.01f, 0.01f + (int)(mFont.getVInterval() * 2), "- MOVES EXAUSTED -");
			}*/

			mFont.print("Time: " + mTimer.getElapsedTimeString());
			mFont.print("\n");
			mFont.print("Moves: ");

			if (mNbrAllowedMoves != -1) {
				switch (mNbrAllowedMoves - mNbrMoves) {
				case 3:
					mFont.setColor(1.0f, 1.0f, 0.0f, 1.0f);
					break;
				case 2:
					mFont.setColor(1.0f, 0.5f, 0.0f, 1.0f);
					break;
				case 1:
					mFont.setColor(1.0f, 0.0f, 0.0f, 1.0f);
					break;
				case 0:
					mFont.setColor(1.0f, 0.0f, 0.0f, 1.0f);
					break;

				default:
					break;
				}

				mFont.print("" + mNbrMoves);
				if (mNbrAllowedMoves != -1) {
					mFont.print("/");
					mFont.print("" + mNbrAllowedMoves);
				}
				mFont.setColor(1.0f, 1.0f, 1.0f, 1.0f);
			} else {
				mFont.print("" + mNbrMoves);
			}

		}

		mFont.setScaleFactor(0.06f);
		mFont.setPos(0.01f, 0.907f);

		if (mGameId1.compareTo(GAME_ID_CHALLENGE) == 0)
			mFont.print(mGameMsg);
		else
			mFont.print("\n");

		if (mBestTime != -1) {
			mFont.print("\nRecord: " + Timer.secToStringMMSS(mBestTime) + " in " + mBestMoves + " moves");
		}

		mFont.disable();

		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		for (GuiElement e: mGuiElements) {
			e.render();
		}

		for (GuiElement e: mDialogBoxElements) {
			e.render();
		}
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		for (Object d : mDisposables) {
			mGuiElements.remove(d);
		}

		for (Object d : mDisposables) {
			mDialogBoxElements.remove(d);
		}
	}



	@Override
	public boolean onTouch(View view, MotionEvent event) {

		float x = event.getX();
		float y = event.getY();

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			mMotionEventDown = true;
		}

		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			mDeltaMoveX = (x - mTouchPrevX) / renderer.getDisplayDensity();
			mDeltaMoveY = (y - mTouchPrevY) / renderer.getDisplayDensity();
			mMotionEventMove = true;
		} else {
			mDeltaMoveX = 0.0f;
			mDeltaMoveY = 0.0f;
		}

		mTouchPrevX = x;
		mTouchPrevY = y;

		if (mDialogBoxElements.size() > 0) {
			for (GuiElement e : mDialogBoxElements) {
				e.onTouch(view, event);
			}
		} else {
			// GUI elements will not respond to touch if there is a dialog box.
			for (GuiElement e : mGuiElements) {
				e.onTouch(view, event);
			}
		}

		return true;
	}


	public void showMovesExaustedDialog() {

		Vec4 btnColor 			= new Vec4(renderer.getActivity().getString(R.string.gui_btn_color));
		Vec2 btnSize 			= new Vec2(renderer.getActivity().getString(R.string.gui_btn_size));

		Vec4 boxColor 			= new Vec4(renderer.getActivity().getString(R.string.gui_box_color));
		Vec2 btnPosLeft			= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_btn_pos_left));
		Vec2 btnPosRight		= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_btn_pos_right));
		Vec2 dialogBoxPos		= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_box_pos));
		Vec2 dialogBoxSize		= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_box_size));
		Vec2 dialogTextPos		= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_text_pos));

		float dialogAlpha		= Float.parseFloat (renderer.getActivity().getString (R.string.dialog_box_alpha));
		Vec2 dialogTextSize		= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_text_size_large));


		String msg			= renderer.getActivity().getString(R.string.msg_out_of_moves);

		final GuiTile dialog;
		final GuiTile bg;


		GuiTile.Builder builder = new GuiTile.Builder();
		builder.setRenderer(renderer);
		builder.setOrthoSize(1.0f, 1.0f);

		builder.setRenderer(renderer);
		builder.setTexture(new Texture(renderer, "black.png"));
		builder.setPos(0.0f, 0.0f);
		builder.setSize(1.0f, 1.0f);
		builder.setBaseColor(new Vec4(0.0f, 0.0f, 0.0f, 1.0f));
		builder.setAlphaFactor(0.5f);
		bg = builder.create();

		builder.setAlphaFactor(1.0f);
		builder.setTexture(new Texture(renderer, "box1.png"));
		builder.setPos(dialogBoxPos);
		builder.setSize(dialogBoxSize);
		builder.setBaseColor(boxColor);
		builder.setParent(bg);
		dialog = builder.create();

		GuiLabel.Builder guiLabelBuilder = new GuiLabel.Builder();
		guiLabelBuilder.setRenderer(renderer);
		guiLabelBuilder.setOrthoSize(1.0f, 1.0f);
		guiLabelBuilder.setFont(mFont);
		guiLabelBuilder.setParent(dialog);
		guiLabelBuilder.setScaleFactor(dialogTextSize);
		guiLabelBuilder.setPos(dialogTextPos);
		guiLabelBuilder.setText(msg);
		guiLabelBuilder.setParent(dialog);
		guiLabelBuilder.create();

		PickableGuiTile.Builder pickableGuiTile = new PickableGuiTile.Builder();
		pickableGuiTile.setRenderer(renderer);
		pickableGuiTile.setOrthoSize(1.0f, 1.0f);

		pickableGuiTile.setParent(dialog);
		pickableGuiTile.setPos(btnPosLeft);
		pickableGuiTile.setSize(btnSize);
		pickableGuiTile.setIsSizeParentRelative(false);
		pickableGuiTile.setTexture(new Texture(renderer, "btn_main_menu.png"));
		pickableGuiTile.setBaseColor(btnColor);
		pickableGuiTile.setRenderer(renderer);
		pickableGuiTile.setOnClickListener(new GuiTile.OnClickListener() {
			@Override
			public void onClick(Object extraData) {
				renderer.setScene(new MainMenu(mActivity, (FifteenPuzzle) renderer, mFont));
			}
		});
		pickableGuiTile.create();

		pickableGuiTile.setParent(dialog);
		pickableGuiTile.setPos(btnPosRight);
		pickableGuiTile.setSize(btnSize);
		pickableGuiTile.setIsSizeParentRelative(false);
		pickableGuiTile.setTexture(new Texture(renderer, "btn_restart.png"));
		pickableGuiTile.setBaseColor(btnColor);
		pickableGuiTile.setRenderer(renderer);
		pickableGuiTile.setOnClickListener(new GuiTile.OnClickListener() {
			@Override
			public void onClick(Object extraData) {
				Runnable r = new Runnable() {
					public void run() {
						mDisposables.add(bg);
						restartCurrentGame();
					}
				};
				renderer.queueEvent(r);
			}
		});
		pickableGuiTile.create();

		mDialogBoxElements.add(bg);
	}

	private void updateScore() {
		int time = mTimer.getElapsedSeconds();

		if (mBestTime == -1 || time < mBestTime) {
			Score.updateValue(mActivity, mGameId1, mGameId2, "time", mTimer.getElapsedSeconds());
			Score.updateValue(mActivity, mGameId1, mGameId2, "moves", mNbrMoves);
		}
	}

	public void showClassicWonDialog() {

		Vec4 btnColor 			= new Vec4(renderer.getActivity().getString(R.string.gui_btn_color));
		Vec2 btnSize 			= new Vec2(renderer.getActivity().getString(R.string.gui_btn_size));

		Vec4 boxColor 			= new Vec4(renderer.getActivity().getString(R.string.gui_box_color));
		Vec2 btnPosLeft			= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_btn_pos_left));
		Vec2 btnPosRight		= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_btn_pos_right));
		Vec2 dialogBoxPos		= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_box_pos));
		Vec2 dialogBoxSize		= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_box_size));
		Vec2 dialogTextPos		= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_text_pos));

		float dialogAlpha		= Float.parseFloat (renderer.getActivity().getString (R.string.dialog_box_alpha));
		Vec2 dialogTextSize			= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_text_size_large));
		Vec2 dialogTextSizeSmall	= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_text_size_small));
		String textHeaderColor		= renderer.getActivity().getString(R.string.gui_text_header_color);

		final GuiTile dialog;
		final GuiTile bg;


		GuiTile.Builder builder = new GuiTile.Builder();
		builder.setRenderer(renderer);
		builder.setOrthoSize(1.0f, 1.0f);

		builder.setRenderer(renderer);
		builder.setTexture(new Texture(renderer, "black.png"));
		builder.setPos(0.0f, 0.0f);
		builder.setSize(1.0f, 1.0f);
		builder.setBaseColor(new Vec4(0.0f, 0.0f, 0.0f, 1.0f));
		builder.setAlphaFactor(0.5f);
		bg = builder.create();

		builder.setAlphaFactor(1.0f);
		builder.setTexture(new Texture(renderer, "box1.png"));
		builder.setPos(dialogBoxPos);
		builder.setSize(dialogBoxSize);
		builder.setAlphaFactor(dialogAlpha);
		builder.setBaseColor(boxColor);
		builder.setParent(bg);
		dialog = builder.create();

		GuiLabel.Builder guiLabelBuilder = new GuiLabel.Builder();
		guiLabelBuilder.setRenderer(renderer);
		guiLabelBuilder.setOrthoSize(1.0f, 1.0f);
		guiLabelBuilder.setFont(mFont);
		guiLabelBuilder.setParent(dialog);
		guiLabelBuilder.setScaleFactor(dialogTextSize);
		guiLabelBuilder.setPos(dialogTextPos);
		guiLabelBuilder.setNewLineSpaceFactor(1.0f);
		guiLabelBuilder.setText("\\" + textHeaderColor + "Puzzle solved!\n" + "\\#FFFFFF\\s[" + dialogTextSizeSmall + "]" +
                "Your time: " + mTimer.getElapsedTimeString() + " in " + mNbrMoves + " moves");

		if (mBestTime != -1 && mTimer.getElapsedSeconds() >= mBestTime)
			guiLabelBuilder.appendText("\nRecord: " + Timer.secToStringMMSS(mBestTime) + " in " + mBestMoves + " moves");
		else
			guiLabelBuilder.appendText("\nNew record!");

		guiLabelBuilder.create();


		PickableGuiTile.Builder pickableGuiTile = new PickableGuiTile.Builder();
		pickableGuiTile.setRenderer(renderer);
		pickableGuiTile.setOrthoSize(1.0f, 1.0f);

		pickableGuiTile.setParent(dialog);
		pickableGuiTile.setPos(btnPosLeft);
		pickableGuiTile.setSize(btnSize);
		pickableGuiTile.setIsSizeParentRelative(false);
		pickableGuiTile.setTexture(new Texture(renderer, "btn_restart.png"));
		pickableGuiTile.setBaseColor(btnColor);
		pickableGuiTile.setRenderer(renderer);
		pickableGuiTile.setOnClickListener(new GuiTile.OnClickListener() {
			@Override
			public void onClick(Object extraData) {
				Runnable r = new Runnable() {
					public void run() {
			    		restartCurrentGame();
			    		mDisposables.add(bg);
					}
				};
				renderer.queueEvent(r);
			}
		});
		pickableGuiTile.create().zoomInAnimation();

		pickableGuiTile.setParent(dialog);
		pickableGuiTile.setPos(btnPosRight);
		pickableGuiTile.setSize(btnSize);
		pickableGuiTile.setIsSizeParentRelative(false);
		pickableGuiTile.setTexture(new Texture(renderer, "btn_main_menu.png"));
		pickableGuiTile.setBaseColor(btnColor);
		pickableGuiTile.setRenderer(renderer);
		pickableGuiTile.setOnClickListener(new GuiTile.OnClickListener() {
			@Override
			public void onClick(Object extraData) {
				renderer.setScene(new MainMenu(mActivity, (FifteenPuzzle) renderer, mFont));
			}
		});
		pickableGuiTile.create().zoomInAnimation();

		mDialogBoxElements.add(bg);
	}

	public void showChalengeWonDialog() {

		Vec4 btnColor 				= new Vec4(renderer.getActivity().getString(R.string.gui_btn_color));
		Vec2 btnSize 				= new Vec2(renderer.getActivity().getString(R.string.gui_btn_size));

		Vec4 boxColor 				= new Vec4(renderer.getActivity().getString(R.string.gui_box_color));
		Vec2 btnPosLeft				= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_btn_pos_left));
		Vec2 btnPosRight			= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_btn_pos_right));
		Vec2 dialogBoxPos			= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_box_pos));
		Vec2 dialogBoxSize			= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_box_size));
		Vec2 dialogTextPos			= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_text_pos));

		float dialogAlpha		= Float.parseFloat (renderer.getActivity().getString (R.string.dialog_box_alpha));
		Vec2 dialogTextSize			= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_text_size_large));
		Vec2 dialogTextSizeSmall	= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_text_size_small));
		String textHeaderColor		= renderer.getActivity().getString(R.string.gui_text_header_color);

		final GuiTile dialog;
		final GuiTile bg;

		GuiTile.Builder builder = new GuiTile.Builder();
		builder.setRenderer(renderer);
		builder.setOrthoSize(1.0f, 1.0f);

		builder.setRenderer(renderer);
		builder.setTexture(new Texture(renderer, "black.png"));
		builder.setPos(0.0f, 0.0f);
		builder.setSize(1.0f, 1.0f);
		builder.setBaseColor(new Vec4(0.0f, 0.0f, 0.0f, 1.0f));
		builder.setAlphaFactor(0.5f);
		bg = builder.create();

		builder.setAlphaFactor(1.0f);
		builder.setTexture(new Texture(renderer, "box1.png"));
		builder.setPos(dialogBoxPos);
		builder.setSize(dialogBoxSize);
		builder.setAlphaFactor(dialogAlpha);
		builder.setBaseColor(boxColor);
		builder.setParent(bg);
		dialog = builder.create();

		GuiLabel.Builder guiLabelBuilder = new GuiLabel.Builder();
		guiLabelBuilder.setRenderer(renderer);
		guiLabelBuilder.setOrthoSize(1.0f, 1.0f);
		guiLabelBuilder.setFont(mFont);
		guiLabelBuilder.setParent(dialog);
		guiLabelBuilder.setScaleFactor(dialogTextSize);
		guiLabelBuilder.setPos(dialogTextPos);
		guiLabelBuilder.setNewLineSpaceFactor(1.0f);
		guiLabelBuilder.setText("\\" + textHeaderColor + "Puzzle solved!\n" + "\\#FFFFFF\\s[" + dialogTextSizeSmall + "]" +
                "Your time: " + mTimer.getElapsedTimeString() + " in " + mNbrMoves + " moves");

		if (mBestTime != -1 && mTimer.getElapsedSeconds() >= mBestTime)
			guiLabelBuilder.appendText("\nRecord: " + Timer.secToStringMMSS(mBestTime) + " in " + mBestMoves + " moves");
		else
			guiLabelBuilder.appendText("\nNew record!");

		guiLabelBuilder.setParent(dialog);
		guiLabelBuilder.create();

		PickableGuiTile.Builder pickableGuiTile = new PickableGuiTile.Builder();
		pickableGuiTile.setRenderer(renderer);
		pickableGuiTile.setOrthoSize(1.0f, 1.0f);

		pickableGuiTile.setParent(dialog);
		pickableGuiTile.setPos(btnPosLeft);
		pickableGuiTile.setSize(btnSize);
		pickableGuiTile.setIsSizeParentRelative(false);
		pickableGuiTile.setTexture(new Texture(renderer, "btn_main_menu.png"));
		pickableGuiTile.setBaseColor(btnColor);
		pickableGuiTile.setRenderer(renderer);
		pickableGuiTile.setOnClickListener(new GuiTile.OnClickListener() {
            @Override
            public void onClick(Object extraData) {
                renderer.setScene(new MainMenu(mActivity, (FifteenPuzzle) renderer, mFont));
            }
        });
		pickableGuiTile.create();

		pickableGuiTile.setParent(dialog);
		pickableGuiTile.setPos(btnPosRight);
		pickableGuiTile.setSize(btnSize);
		pickableGuiTile.setIsSizeParentRelative(false);
		pickableGuiTile.setTexture(new Texture(renderer, "btn_next.png"));
		pickableGuiTile.setBaseColor(btnColor);
		pickableGuiTile.setRenderer(renderer);
		pickableGuiTile.setOnClickListener(new GuiTile.OnClickListener() {
			@Override
			public void onClick(Object extraData) {
				Runnable r = new Runnable() {
					public void run() {
						mGameId2 = String.valueOf((Integer.parseInt(mGameId2) + 1));
						showGameStartMsg();
						restartCurrentGame();
						mDisposables.add(bg);
					}
				};
				renderer.queueEvent(r);
			}
		});
		pickableGuiTile.create();

		mDialogBoxElements.add(bg);
	}

	// TODO
	public void showChallengeFinishedDialog() {

		Vec4 btnColor 				= new Vec4(renderer.getActivity().getString(R.string.gui_btn_color));
		Vec2 btnSize 				= new Vec2(renderer.getActivity().getString(R.string.gui_btn_size));

		Vec4 boxColor 				= new Vec4(renderer.getActivity().getString(R.string.gui_box_color));
		Vec2 btnPosLeft				= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_btn_pos_left));
		Vec2 btnPosRight			= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_btn_pos_right));
		Vec2 dialogBoxPos			= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_box_pos));
		Vec2 dialogBoxSize			= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_box_size));
		Vec2 dialogTextPos			= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_text_pos));

		float dialogAlpha		= Float.parseFloat (renderer.getActivity().getString (R.string.dialog_box_alpha));
		Vec2 dialogTextSize			= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_text_size_large));
		Vec2 dialogTextSizeSmall	= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_text_size_small));
		String textHeaderColor		= renderer.getActivity().getString(R.string.gui_text_header_color);

		final GuiTile dialog;
		final GuiTile bg;

		GuiTile.Builder builder = new GuiTile.Builder();
		builder.setRenderer(renderer);
		builder.setOrthoSize(1.0f, 1.0f);

		builder.setRenderer(renderer);
		builder.setTexture(new Texture(renderer, "black.png"));
		builder.setPos(0.0f, 0.0f);
		builder.setSize(1.0f, 1.0f);
		builder.setBaseColor(new Vec4(0.0f, 0.0f, 0.0f, 1.0f));
		builder.setAlphaFactor(0.5f);
		bg = builder.create();

		builder.setAlphaFactor(1.0f);
		builder.setTexture(new Texture(renderer, "box1.png"));
		builder.setPos(dialogBoxPos.x - 0.05f, dialogBoxPos.y);
		builder.setSize(dialogBoxSize.x + 0.1f, dialogBoxSize.y + 0.05f);
		builder.setAlphaFactor(dialogAlpha);
		builder.setBaseColor(boxColor);
		builder.setParent(bg);
		dialog = builder.create();

		GuiLabel.Builder guiLabelBuilder = new GuiLabel.Builder();
		guiLabelBuilder.setRenderer(renderer);
		guiLabelBuilder.setOrthoSize(1.0f, 1.0f);
		guiLabelBuilder.setFont(mFont);
		guiLabelBuilder.setParent(dialog);
		guiLabelBuilder.setScaleFactor(dialogTextSize);
		guiLabelBuilder.setPos(dialogTextPos);
		guiLabelBuilder.setNewLineSpaceFactor(1.0f);
		guiLabelBuilder.setText("\\" + textHeaderColor + "Congratulations!\n" + "\\#FFFFFF\\s[" + dialogTextSizeSmall + "]" +
                "You've solved the last challenge!\nYour time: " + mTimer.getElapsedTimeString() + " in " + mNbrMoves + " moves");

		if (mBestTime != -1 || (mBestTime != -1 && mTimer.getElapsedSeconds() >= mBestTime))
			guiLabelBuilder.appendText("\nRecord: " + Timer.secToStringMMSS(mBestTime) + " in " + mBestMoves + " moves");
		else
			guiLabelBuilder.appendText("\nNew record!");

		guiLabelBuilder.setParent(dialog);
		guiLabelBuilder.create();

		PickableGuiTile.Builder pickableGuiTile = new PickableGuiTile.Builder();
		pickableGuiTile.setRenderer(renderer);
		pickableGuiTile.setOrthoSize(1.0f, 1.0f);

		pickableGuiTile.setParent(dialog);
		pickableGuiTile.setPos(0.5f - btnSize.x * 0.5f, btnPosLeft.y);
		pickableGuiTile.setSize(btnSize);
		pickableGuiTile.setIsSizeParentRelative(false);
		pickableGuiTile.setTexture(new Texture(renderer, "btn_main_menu.png"));
		pickableGuiTile.setBaseColor(btnColor);
		pickableGuiTile.setRenderer(renderer);
		pickableGuiTile.setOnClickListener(new GuiTile.OnClickListener() {
			@Override
			public void onClick(Object extraData) {
				renderer.setScene(new MainMenu(mActivity, (FifteenPuzzle) renderer, mFont));
			}
		});
		pickableGuiTile.create();

		mDialogBoxElements.add(bg);
	}

	public void showPausedDialog() {

		mTimer.pause();

		Vec4 btnColor 			= new Vec4(renderer.getActivity().getString(R.string.gui_btn_color));
		Vec2 btnSize 			= new Vec2(renderer.getActivity().getString(R.string.gui_btn_size));

		Vec4 boxColor 			= new Vec4(renderer.getActivity().getString(R.string.gui_box_color));
		Vec2 btnPosLeft			= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_btn_pos_left));
		Vec2 btnPosRight		= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_btn_pos_right));
		Vec2 dialogBoxPos		= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_box_pos));
		Vec2 dialogBoxSize		= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_box_size));
		Vec2 dialogTextPos		= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_text_pos));

		float dialogAlpha		= Float.parseFloat (renderer.getActivity().getString (R.string.dialog_box_alpha));
		Vec2 dialogTextSize		= new Vec2(renderer.getActivity().getString(R.string.gui_dialog_text_size_large));
		String msg				= renderer.getActivity().getString(R.string.game_paused_msg);

		final GuiTile dialog;
		final GuiTile bg;

		GuiTile.Builder builder = new GuiTile.Builder();
		builder.setRenderer(renderer);
		builder.setOrthoSize(1.0f, 1.0f);

		builder.setRenderer(renderer);
		builder.setTexture(new Texture(renderer, "black.png"));
		builder.setPos(0.0f, 0.0f);
		builder.setSize(1.0f, 1.0f);
		builder.setBaseColor(new Vec4(0.0f, 0.0f, 0.0f, 1.0f));
		builder.setAlphaFactor(0.5f);
		bg = builder.create();

		builder.setAlphaFactor(1.0f);
		builder.setTexture(new Texture(renderer, "box1.png"));
		builder.setPos(dialogBoxPos);
		builder.setSize(dialogBoxSize);
		builder.setAlphaFactor(dialogAlpha);
		builder.setBaseColor(boxColor);
		builder.setParent(bg);
		dialog = builder.create();

		GuiLabel.Builder guiLabelBuilder = new GuiLabel.Builder();
		guiLabelBuilder.setRenderer(renderer);
		guiLabelBuilder.setOrthoSize(1.0f, 1.0f);
		guiLabelBuilder.setFont(mFont);
		guiLabelBuilder.setParent(dialog);
		guiLabelBuilder.setScaleFactor(dialogTextSize);
		guiLabelBuilder.setPos(dialogTextPos);
		guiLabelBuilder.setText(msg);
		guiLabelBuilder.setParent(dialog);
		guiLabelBuilder.create();

		PickableGuiTile.Builder pickableGuiTile = new PickableGuiTile.Builder();
		pickableGuiTile.setRenderer(renderer);
		pickableGuiTile.setOrthoSize(1.0f, 1.0f);

		pickableGuiTile.setParent(dialog);
		pickableGuiTile.setPos(btnPosLeft);
		pickableGuiTile.setSize(btnSize);
		pickableGuiTile.setIsSizeParentRelative(false);
		pickableGuiTile.setTexture(new Texture(renderer, "btn_continue.png"));
		pickableGuiTile.setBaseColor(btnColor);
		pickableGuiTile.setRenderer(renderer);
		pickableGuiTile.setOnClickListener(new GuiTile.OnClickListener() {
			@Override
			public void onClick(Object extraData) {
				mDisposables.add(bg);
				mTimer.resume();
			}
		});
		pickableGuiTile.create();

		pickableGuiTile.setParent(dialog);
		pickableGuiTile.setPos(btnPosRight);
		pickableGuiTile.setSize(btnSize);
		pickableGuiTile.setIsSizeParentRelative(false);
		pickableGuiTile.setTexture(new Texture(renderer, "btn_quit.png"));
		pickableGuiTile.setBaseColor(btnColor);
		pickableGuiTile.setRenderer(renderer);
		pickableGuiTile.setOnClickListener(new GuiTile.OnClickListener() {
			@Override
			public void onClick(Object extraData) {
				renderer.setScene(new MainMenu(mActivity, (FifteenPuzzle) renderer, mFont));
			}
		});
		pickableGuiTile.create();

		mDialogBoxElements.add(bg);
	}

	@Override
	public void createBoard(int size) {
		switch (size) {
		case FifteenPuzzleBoard.SIZE3X3:
			mCurrentSize = FifteenPuzzleBoard.SIZE3X3;
			mBoard = new GameBoard(3, 3);
			break;
		case FifteenPuzzleBoard.SIZE3X4:
			mCurrentSize = FifteenPuzzleBoard.SIZE3X4;
			mBoard = new GameBoard(3, 4);
			break;
		case FifteenPuzzleBoard.SIZE4X4:
			mCurrentSize = FifteenPuzzleBoard.SIZE4X4;
			mBoard = new GameBoard(4, 4);
			break;
		default:
			break;
		}
	}

	public void cleanGameBoard() {
		mTiles.clear();
		UniqueColorFactory.resetColor();
	}

	@Override
	public void setNbrAllowedMoves(int nbrAllowedMoves) {
		mNbrAllowedMoves = nbrAllowedMoves;
	}

	public void initGame() {
		getMovableTiles();
		flyEveryTileDown();
		mNbrMoves = 0;
	}

	@Override
	public void onResume() {
		if (gyroSensor != null) {
			sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
		}
		mTimer.resume();
	}

	@Override
	public void onPause() {
		if (gyroSensor != null) {
			sensorManager.unregisterListener(this);
		}
		mTimer.pause();
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {
		case Sensor.TYPE_GYROSCOPE:
			mGyroscopeValues.assign(event.values);
			break;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		;
	}

    @Override
    public void onBackPressed() {
        // If there is already a dialog box opened, do nothing.
        if (mDialogBoxElements.size() != 0) {
            return;
        }

        renderer.queueEvent(new Runnable() {
            public void run() {
                showPausedDialog();
            }
        });
        mActivity.runOnUiThread(new Runnable() {
            public void run() {

                mActivity.tryShowInterstitial();
            }
        });
    }

	public void terminate() {
		mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mActivity.terminateWithInterstitial();
            }
        });
	}
}
