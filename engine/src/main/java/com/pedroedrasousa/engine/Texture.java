package com.pedroedrasousa.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;
import static com.pedroedrasousa.engine.EngineGlobal.DEBUG;


public class Texture implements RendererObserver {

	private static final String	TAG	= Texture.class.getSimpleName();

	private static LinkedList<Texture> textureList = new LinkedList<Texture>();

	private enum ResourceType { ASSET, PATH }

	private Renderer renderer;

	private int		width;
	private int		height;
	private int		originalWidth;
	private int		originalHeight;
	private int[]	handle = new int[1];

	// Used to store the texture resource. Will be used for reloading.
	private String			resource;
	private ResourceType	resourceType;
	private int				maxSize;

	// Cache to reuse the texture handles based on the filename.
	private static HashMap<String, Integer> cache = new HashMap<String, Integer>();

	private String validSurfaceID;	// The active EGLConfig when the data was loaded to OpenGL. Used to know when to reload data during onSurfaceCreated.


	public static void reloadAll(Context context) {
		for (Texture t : textureList) {
			t.reload();
		}
	}

	public Texture(Renderer renderer) {
		this.renderer = renderer;
		renderer.registerObserver(this);
	}

	public Texture(Renderer renderer, String assetName) {
		this.renderer = renderer;
		loadFromAsset(assetName);
		renderer.registerObserver(this);
	}

	public void reload() {
		if (resourceType == null)
			return;

		switch (resourceType) {
		case ASSET:
			loadFromAsset(renderer.getActivity(), resource, maxSize);
			break;
		case PATH:
			loadFromPath(renderer.getActivity(), resource, maxSize);
			break;
		default:
			break;
		}
	}

	private void decodeBitmapBounds(InputStream stream) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(stream, null, options);
        originalWidth  = options.outWidth;
        originalHeight = options.outHeight;
	}

	private Bitmap decodeBitmap(InputStream stream, int maxSize) {
        int scale = 1;
        for (int size = Math.min(originalHeight, originalWidth); (size >> (scale-1)) > maxSize; scale++);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = scale;
        return BitmapFactory.decodeStream(stream, null, options);
	}

	public void loadFromAsset(String assetName) {
		loadFromAsset(renderer.getActivity(), assetName, -1);
	}

	public void loadFromAsset(Context context, String assetName, int maxSize) {

	    Bitmap bitmap = null;
	    InputStream stream1 = null;
	    InputStream stream2 = null;

	    resource = new String(assetName);
	    resourceType = ResourceType.ASSET;
	    this.maxSize = maxSize;

		if (cache.get(constructID()) != null) {
			if (DEBUG) {
				Log.i(TAG, "Reusing texture handle " + constructID());
			}
			handle[0] = cache.get(constructID());
			return;
		} else {
			if (DEBUG) {
				Log.i(TAG, "Loading texture " + constructID());
			}
		}

	    try {
		    stream1 = context.getAssets().open(assetName);
		    stream2 = context.getAssets().open(assetName);

	        decodeBitmapBounds(stream1);

	        if (maxSize <= 0) {
	        	maxSize = (originalWidth > originalHeight)? originalWidth : originalHeight;
	        }

	        bitmap = decodeBitmap(stream2, maxSize);
	        bitmap = Bitmap.createScaledBitmap(bitmap, maxSize, maxSize, false);
	    } catch (IOException e) {
	    	e.printStackTrace();
	    } finally {
	        if (stream1 != null) {
	        	try {
					stream1.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
	        if (stream2 != null) {
	        	try {
					stream2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
	    }
	    
	    if (bitmap == null) {
	    	throw new RuntimeException("Unable to load bitmap from " + assetName);
	    }

        loadGLTextureFromBitmap(bitmap);

        cache.put(constructID(), handle[0]);

        // Bitmap data isn't needed anymore
        bitmap.recycle();
	}

	public void loadFromPath(final Context context, final String pathName, int maxSize) {

		if (DEBUG) {
			Log.i(TAG, "Loading texture from path " + pathName);
		}

		File file;
	    InputStream stream1 = null;
	    InputStream stream2 = null;
	    Bitmap bitmap = null;

	    resource = new String(pathName);
	    resourceType = ResourceType.PATH;
	    this.maxSize = maxSize;

	    resource = new String(pathName);

	    try {
		    file = new File(pathName);
		    stream1 = new FileInputStream(file);
		    stream2 = new FileInputStream(file);
	        decodeBitmapBounds(stream1);
	        bitmap = decodeBitmap(stream2, maxSize);
	        bitmap = Bitmap.createScaledBitmap(bitmap, maxSize, maxSize, false);
	    } catch (FileNotFoundException e) {
	    	e.printStackTrace();
	    } finally {
	        if (stream1 != null) {
	        	try {
					stream1.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
	        if (stream2 != null) {
	        	try {
					stream2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
	    }

        loadGLTextureFromBitmap(bitmap);

        // Bitmap data isn't needed anymore
        bitmap.recycle();
	}

	// Mostly like to cause OutOfMemoryError for big bitmaps
	public void loadFromResourceId(Context context, int resourceId, int width, int height) {

		if (DEBUG) {
			Log.i(TAG, "Loading texture from resource id " + resourceId);
		}

	    Bitmap bitmap = null;
	    Resources resources = context.getResources();

	    GLES20.glGenTextures(1, handle, 0);

    	Drawable image = resources.getDrawable(resourceId);
        float density = resources.getDisplayMetrics().density;

        originalWidth  = (int)(image.getIntrinsicWidth() / density);
        originalHeight = (int)(image.getIntrinsicHeight() / density);

        // Check if dimensions are valid, if not use the original ones
        if (width > 0 && height > 0) {
            this.width  = width;
            this.height = height;
        } else {
        	this.width  = originalWidth;
            this.height = originalHeight;
        }

        image.setBounds(0, 0, this.width, this.height);

        // Create an empty, mutable bitmap
        bitmap = Bitmap.createBitmap( this.width, this.height, Bitmap.Config.ARGB_4444 );

        Canvas canvas = new Canvas(bitmap);	// Get a canvas to paint over the bitmap
        bitmap.eraseColor(0);

        image.draw(canvas);					// Draw the image onto the bitmap

        loadGLTextureFromBitmap(bitmap);

        // Bitmap data isn't needed anymore
        bitmap.recycle();
	}

	public int loadGLTextureFromBitmap(Bitmap bitmap) {

	    GLES20.glGenTextures(1, handle, 0);

	    GLES20.glBindTexture( GLES20.GL_TEXTURE_2D, handle[0] );

	    GLES20.glTexParameterf( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR );
	    GLES20.glTexParameterf( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR );

	    GLES20.glTexParameterf( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT );
	    GLES20.glTexParameterf( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT );

	    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

	    return handle[0];
	}

	public int getHandle() {
		return handle[0];
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getOriginalWidth() {
		return originalWidth;
	}

	public int getOriginalHeight() {
		return originalHeight;
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		if (renderer.getSurfaceID() != validSurfaceID) {
			cache.remove(resource + "@" + validSurfaceID);
			validSurfaceID = renderer.getSurfaceID();
		}

		reload();
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		;
	}

	private String constructID() {
		return resource + "@" + renderer.getSurfaceID();
	}
}
