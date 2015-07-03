package agf.module.graphics.android.gles20;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import de.ab.agf.lib.backend.AGModel;
import de.ab.agf.lib.math.AGArrays;
import de.ab.agf.lib.util.AGAttr;

public class Model extends AGModel {

	private int count, alphaEnabled, texEnabled, lShadeless;
	private String texture;
	private float[] lAmbient, lDiffuse, lSpecular;
	private HashMap<Integer, ArrayList> lists;
	private int floatSize, shortSize;
	private FastFloatBuffer floatBuffer;
	private ShortBuffer shortBuffer;
	private HashMap<Integer, Integer> indices;
	private int floatStride;
	private long updateKey = 0, updateState = 0;

	//region setup

	@Override
	protected void onCreate() {
		super.onCreate();
		lists = new HashMap<>();
		floatSize = 0;
		shortSize = 0;
		indices = new HashMap<>();
	}

	@Override
	protected void set(int which, AGAttr data) {
		switch (which) {
			case ALPHA_ENABLED:
				alphaEnabled = data.getFor(Boolean.class) ? 1 : 0;
				break;
			case TEX_ENABLED:
				texEnabled = data.getFor(Boolean.class) ? 1 : 0;
				break;
			case TEXTURE:
				texture = data.getFor(String.class);
				break;
			case L_SHADELESS:
				lShadeless = data.getFor(Boolean.class) ? 1 : 0;
				break;
			case L_AMBIENT:
				lAmbient = new float[3];
				AGArrays.convert(data.getFor(Float[].class), 0, lAmbient, 0, 3);
				break;
			case L_DIFFUSE:
				lDiffuse = new float[3];
				AGArrays.convert(data.getFor(Float[].class), 0, lDiffuse, 0, 3);
				break;
			case L_SPECULAR:
				lSpecular = new float[3];
				AGArrays.convert(data.getFor(Float[].class), 0, lSpecular, 0, 3);
				break;
			case COORDINATES:
			case DRAW_ORDER:
			case COLORS:
			case TEX_COORDINATES:
			case NORMALS:
				lists.put(which, data.getFor(ArrayList.class));
				break;
		}
	}

	@Override
	protected void setDefault(int which) {
		switch (which) {
			case ALPHA_ENABLED:
				alphaEnabled = 0;
				break;
			case TEX_ENABLED:
				texEnabled = 0;
				break;
			case TEXTURE:
				texture = null;
				break;
			case L_SHADELESS:
				lShadeless = 0;
				break;
			case L_AMBIENT:
				lAmbient = new float[3];
				break;
			case L_DIFFUSE:
				lDiffuse = new float[3];
				break;
			case L_SPECULAR:
				lSpecular = new float[3];
				break;
			case COORDINATES:
			case DRAW_ORDER:
			case COLORS:
			case TEX_COORDINATES:
			case NORMALS:
				lists.remove(which);
				break;
		}
	}

	@Override
	protected void endModification() {
		indices.clear();

		ArrayList<Float[]> position = null, colors = null, texPosition = null, normals = null;
		ArrayList<Short[]> drawOrder = null;

		int floatCount = 0;
		int floatStride = 0;
		int floatSize;
		int shortCount = 0;
		int shortStride = 0;
		int shortSize;

		if (lists.containsKey(COORDINATES)) {
			position = lists.get(COORDINATES);
			if (position != null) {
				indices.put(COORDINATES, floatStride);
				floatStride += 3;
				floatCount = position.size();
			}
		}
		if (lists.containsKey(COLORS)) {
			colors = lists.get(COLORS);
			if (colors != null) {
				indices.put(COLORS, floatStride);
				floatStride += 4;
				floatCount = colors.size();
			}
		}
		if (lists.containsKey(TEX_COORDINATES)) {
			texPosition = lists.get(TEX_COORDINATES);
			if (texPosition != null) {
				indices.put(TEX_COORDINATES, floatStride);
				floatStride += 2;
				floatCount = texPosition.size();
			}
		}
		if (lists.containsKey(NORMALS)) {
			normals = lists.get(NORMALS);
			if (normals != null) {
				indices.put(NORMALS, floatStride);
				floatStride += 3;
				floatCount = normals.size();
			}
		}
		if (lists.containsKey(DRAW_ORDER)) {
			drawOrder = lists.get(DRAW_ORDER);
			if (drawOrder != null) {
				indices.put(DRAW_ORDER, shortStride);
				shortStride += 3;
				shortCount = drawOrder.size();
			}
		}

		float[] f = new float[floatSize = floatCount * floatStride];
		short[] s = new short[shortSize = shortCount * shortStride];

		for (int i = 0; i < floatCount; i++) {
			int pos = i * floatStride;
			if (position != null)
				for (int i2 = 0; i2 < 3; i2++)
					f[pos++] = position.get(i)[i2];
			if (colors != null)
				for (int i2 = 0; i2 < 4; i2++)
					f[pos++] = colors.get(i)[i2];
			if (texPosition != null)
				for (int i2 = 0; i2 < 2; i2++)
					f[pos++] = texPosition.get(i)[i2];
			if (normals != null)
				for (int i2 = 0; i2 < 3; i2++)
					f[pos++] = normals.get(i)[i2];
		}
		for (int i = 0; i < shortCount; i++) {
			int pos = i * shortStride;
			if (drawOrder != null)
				for (int i2 = 0; i2 < 3; i2++)
					s[pos++] = drawOrder.get(i)[i2];
		}

		if (this.floatSize != floatSize)
			floatBuffer = new FastFloatBuffer(this.floatSize = floatSize);
		if (this.shortSize != shortSize)
			shortBuffer = ByteBuffer.allocateDirect((this.shortSize = shortSize) * 2).order(ByteOrder.nativeOrder()).asShortBuffer();

		floatBuffer.position(0);
		floatBuffer.put(f).position(0);

		shortBuffer.position(0);
		shortBuffer.put(s).position(0);

		count = shortSize;
		this.floatStride = floatStride;

		++updateState;
	}

	//endregion

	public int getCount() {
		return count;
	}

	public int getAlphaEnabled() {
		return alphaEnabled;
	}

	public int getTexEnabled() {
		return texEnabled;
	}

	public int getLShadeless() {
		return lShadeless;
	}

	public String getTexture() {
		return texture;
	}

	public float[] getLAmbient() {
		return lAmbient;
	}

	public float[] getLDiffuse() {
		return lDiffuse;
	}

	public float[] getLSpecular() {
		return lSpecular;
	}

	public int getFloats() {
		return buffers[0];
	}

	public ShortBuffer getShorts() {
		return shortBuffer;
	}

	public int getIndex(int which) {
		return indices.containsKey(which) ? indices.get(which) : -1;
	}

	public int getFloatStride() {
		return floatStride;
	}

	//region buffer system

	private int[] buffers = new int[1];

	public void createBuffers() {
		GLES20.glGenBuffers(buffers.length, buffers, 0);
	}

	public void updateBuffers(boolean forced) {
		if (forced || (updateKey != updateState)) {
			floatBuffer.position(0);
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);
			GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, floatSize * 4, floatBuffer.floats, GLES20.GL_DYNAMIC_DRAW);
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

			if (!forced)
				updateKey = updateState;
		}
	}

	public void destroyBuffers() {
		GLES20.glDeleteBuffers(buffers.length, buffers, 0);
	}

	//endregion
}
