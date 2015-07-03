package agf.module.graphics.android.gles20;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import agf.module.graphics.android.G;
import de.ab.agf.lib.backend.AGGraphics;
import de.ab.agf.lib.backend.AGTextureStore;

public class TextureStore extends AGTextureStore {

	public static int loadTexture(AssetManager assets, String assetPath) throws IOException {
		final int[] textureHandle = new int[1];
		GLES20.glGenTextures(1, textureHandle, 0);
		if (textureHandle[0] != 0) {
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false;
			final Bitmap bitmap = BitmapFactory.decodeStream(assets.open(assetPath), null, options);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
			GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
			bitmap.recycle();
		}
		if (textureHandle[0] == 0)
			throw new RuntimeException("Error loading texture.");
		return textureHandle[0];
	}

	private ArrayList<String> addQueue = new ArrayList<>(), removeQueue = new ArrayList<>();
	private HashMap<String, Integer> textures = new HashMap<>();

	public TextureStore(AGGraphics graphics) {
		super(graphics);
	}

	@Override
	public void putData(String key) {
		if (key == null || textures.containsKey(key) || addQueue.contains(key))
			return;
		if (removeQueue.contains(key))
			removeQueue.remove(key);
		else
			addQueue.add(key);
	}

	@Override
	public void removeData(String key) {
		if (key == null || removeQueue.contains(key))
			return;
		if (addQueue.contains(key))
			addQueue.remove(key);
		else if (textures.containsKey(key))
			removeQueue.add(key);
	}

	@Override
	public Integer getDataForKey(String key) {
		if (key == null)
			throw new NullPointerException("key mustn't be null!");
		else if (addQueue.contains(key))
			throw new RuntimeException("Texture " + key + " is still in add queue!");
		else if (removeQueue.contains(key))
			throw new RuntimeException("Texture is in remove queue!");
		else if (!textures.containsKey(key))
			throw new RuntimeException("Texture is not registered!");
		else
			return textures.get(key);
	}

	@Override
	public Iterable<String> getPassData() {
		ArrayList<String> result = new ArrayList<>();
		for (String key : addQueue)
			result.add(key);
		for (String key : textures.keySet())
			if (!removeQueue.contains(key))
				result.add(key);
		return result;
	}

	@Override
	public void destroy() {
		addQueue.clear();
		super.destroy();
		Log.i("***********************", getClass().getSimpleName() + ".destroy: TODO: Remove data directly!!! Maybe this is the last chance to do this!");
	}

	public void handleQueues(Context context) {
		String[] keys = addQueue.toArray(new String[addQueue.size()]);
		for (String key : keys) {
			try {
				textures.put(key, loadTexture(context.getAssets(), getAssetPathForKey(key)));
				addQueue.remove(key);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		keys = removeQueue.toArray(new String[removeQueue.size()]);
		for (String key : keys) {
			GLES20.glDeleteTextures(1, new int[]{textures.remove(key)}, 0);
			removeQueue.remove(key);
		}
	}

	public String getAssetPathForKey(String key) {
		if (G.TEXTURE_BINDINGS.containsKey(key))
			return G.TEXTURE_BINDINGS.get(key);

		throw new RuntimeException("Cannot find texture path for " + key);
	}
}
