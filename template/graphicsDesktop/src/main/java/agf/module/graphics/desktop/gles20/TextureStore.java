package agf.module.graphics.desktop.gles20;

import com.jogamp.opengl.util.texture.TextureIO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import agf.module.graphics.desktop.G;
import de.ab.agf.lib.backend.AGGraphics;
import de.ab.agf.lib.backend.AGTextureStore;

public class TextureStore extends AGTextureStore {

	public static int loadTexture(GL gl, String assetPath) throws IOException {
		try {
			return TextureIO.newTexture(TextureIO.newTextureData(gl._profile, G.Assets.open(assetPath), false, null)).getTarget();
		}catch(Throwable t) {
			throw new RuntimeException("Error loading texture.", t);
		}
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
	}

	public void handleQueues(GL gl) {
		String[] keys = addQueue.toArray(new String[addQueue.size()]);
		for (String key : keys) {
			try {
				textures.put(key, loadTexture(gl, getAssetPathForKey(key)));
				addQueue.remove(key);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		keys = removeQueue.toArray(new String[removeQueue.size()]);
		for (String key : keys) {
			gl._gl.glDeleteTextures(1, new int[]{textures.remove(key)}, 0);
			removeQueue.remove(key);
		}
	}

	public String getAssetPathForKey(String key) {
		if (G.TEXTURE_BINDINGS.containsKey(key))
			return G.TEXTURE_BINDINGS.get(key);

		throw new RuntimeException("Cannot find texture path for " + key);
	}
}
