package agf.module.graphics.android.gles20;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import agf.module.graphics.android.G;
import agf.module.graphics.android.R;
import de.ab.agf.lib.AGScene;
import de.ab.agf.lib.backend.AGModel;
import de.ab.agf.lib.math.AGTransform;
import de.ab.agf.lib.util.AGAttr;

public class Graphics extends BaseGraphics {

	public static final int EGL_CONTEXT_CLIENT_VERSION = 2;

	public static float[] makeDefaultProjection(int width, int height, float fieldOfView) {
		final float ratio = (float) width / height,
				left = -ratio / fieldOfView,
				right = ratio / fieldOfView,
				bottom = -1f / fieldOfView,
				top = 1f / fieldOfView,
				near = 1f / fieldOfView,
				far = 10f * fieldOfView;
		float[] projectionMatrix = new float[16];
		Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far);
		return projectionMatrix;
	}

	private Context context;

	//uniforms
	private static final String iuMMatrix = "u_MMatrix", iuMVPMatrix = "u_MVPMatrix", iuTex = "u_Tex", iuTexEnabled = "u_TexEnabled", iuLLCount = "u_LLCount", iuLLType = "u_LLType", iuLLMatrix = "u_LLMatrix", iuLLColor = "u_LLColor", iuLLDirection = "u_LLDirection", iuLLAttributes = "u_LLAttributes", iuLOShadeless = "u_LOShadeless", iuLOAmbient = "u_LOAmbient", iuLODiffuse = "u_LODiffuse", iuLOSpecular = "u_LOSpecular";
	private int uMMatrix, uMVPMatrix, uTex, uTexEnabled, uLLCount, uLLType, uLLMatrix, uLLColor, uLLDirection, uLLAttributes, uLOShadeless, uLOAmbient, uLODiffuse, uLOSpecular;
	//attrs
	private static final String iaPosition = "a_Position", iaColor = "a_Color", iaTexPosition = "a_TexPosition", iaNormal = "a_Normal";
	private int aPosition, aColor, aTexPosition, aNormal;

	private float[] viewMatrix = new float[16], projectionMatrix = new float[16], modelMatrix = new float[16], MVPMatrix = new float[16];

	private ArrayList<Model> modelStack = new ArrayList<>(), currentStack = new ArrayList<>();

	public Graphics(Context context) {
		this.context = context;
	}

	private String readRawFile(int resId) {
		InputStream inputStream = context.getResources().openRawResource(resId);
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
			reader.close();
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private int loadShader(int type, int shaderCodeResId) {
		String shaderCode = readRawFile(shaderCodeResId);
		String log = "no information";

		int handle = GLES20.glCreateShader(type);
		if (handle != 0) {
			GLES20.glShaderSource(handle, shaderCode);
			GLES20.glCompileShader(handle);
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(handle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
			if (compileStatus[0] == 0) {
				log = GLES20.glGetShaderInfoLog(handle);
				GLES20.glDeleteShader(handle);
				handle = 0;
			}
		}
		if (handle == 0)
			throw new RuntimeException("Error creating shader with type " + (type == GLES20.GL_VERTEX_SHADER ? "vertex" : type == GLES20.GL_FRAGMENT_SHADER ? "fragment" : type) + ":\n" + log);
		return handle;
	}

	public void onSurfaceCreated() {
		GLES20.glClearColor(G.BACKGROUND[0], G.BACKGROUND[1], G.BACKGROUND[2], 1f);

		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, R.raw.vertex);
		int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, R.raw.fragment);
		int program = GLES20.glCreateProgram();

		if (program != 0) {
			GLES20.glAttachShader(program, vertexShader);
			GLES20.glAttachShader(program, fragmentShader);
			//bind attribute pointer
			GLES20.glBindAttribLocation(program, 0, iaPosition);
			GLES20.glBindAttribLocation(program, 1, iaColor);
			GLES20.glBindAttribLocation(program, 2, iaTexPosition);
			GLES20.glBindAttribLocation(program, 3, iaNormal);

			GLES20.glLinkProgram(program);
			final int[] status = new int[1];
			GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0);
			if (status[0] == 0) {
				GLES20.glDeleteProgram(program);
				program = 0;
			}
		}
		if (program == 0)
			throw new RuntimeException("Error creating program!");

		//get locations
		//uniforms
		uMMatrix = GLES20.glGetUniformLocation(program, iuMMatrix);
		uMVPMatrix = GLES20.glGetUniformLocation(program, iuMVPMatrix);
		uTex = GLES20.glGetUniformLocation(program, iuTex);
		uTexEnabled = GLES20.glGetUniformLocation(program, iuTexEnabled);
		uLLCount = GLES20.glGetUniformLocation(program, iuLLCount);
		uLLType = GLES20.glGetUniformLocation(program, iuLLType);
		uLLMatrix = GLES20.glGetUniformLocation(program, iuLLMatrix);
		uLLColor = GLES20.glGetUniformLocation(program, iuLLColor);
		uLLDirection = GLES20.glGetUniformLocation(program, iuLLDirection);
		uLLAttributes = GLES20.glGetUniformLocation(program, iuLLAttributes);
		uLOShadeless = GLES20.glGetUniformLocation(program, iuLOShadeless);
		uLOAmbient = GLES20.glGetUniformLocation(program, iuLOAmbient);
		uLODiffuse = GLES20.glGetUniformLocation(program, iuLODiffuse);
		uLOSpecular = GLES20.glGetUniformLocation(program, iuLOSpecular);
		//attrs
		aPosition = GLES20.glGetAttribLocation(program, iaPosition);
		aColor = GLES20.glGetAttribLocation(program, iaColor);
		aTexPosition = GLES20.glGetAttribLocation(program, iaTexPosition);
		aNormal = GLES20.glGetAttribLocation(program, iaNormal);

		GLES20.glUseProgram(program);

		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glCullFace(GLES20.GL_BACK);
		GLES20.glFrontFace(GLES20.GL_CCW);
	}

	public void setProjection(float[] projectionMatrix) {
		this.projectionMatrix = projectionMatrix;
	}

	@Override
	public void preRenderFrame(AGScene scene, AGAttr attr) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		currentStack.clear();

		if (scene == null)
			return;

		((TextureStore) scene.getTextureStore()).handleQueues(context);

		transformCameraMatrix(scene.getCameraTransform(), viewMatrix);

		LampStore s = (LampStore) scene.getLampStore();
		int count = s.getCount();
		GLES20.glUniform1i(uLLCount, count);
		GLES20.glUniform1fv(uLLType, count, s.getTypes().floats);
		GLES20.glUniformMatrix4fv(uLLMatrix, count, false, s.getMatrices(), 0);
		GLES20.glUniform3fv(uLLColor, count, s.getColors().floats);
		GLES20.glUniform3fv(uLLDirection, count, s.getDirections().floats);
		GLES20.glUniform3fv(uLLAttributes, count, s.getAttributes().floats);
	}

	@Override
	public void renderModel(AGScene scene, Object modelTransform, AGModel model) {
		Model m = (Model) model;

		boolean forced = false;

		if (!modelStack.contains(m)) {
			modelStack.add(m);
			m.createBuffers();
			forced = true;
		}
		currentStack.add(m);
		m.updateBuffers(forced);

		//transformMatrix((float[]) modelTransform, modelMatrix);
		de.ab.agf.lib.math.AGArrays.copy((float[]) modelTransform, 0, modelMatrix, 0, 16);
		Matrix.setIdentityM(MVPMatrix, 0);
		Matrix.multiplyMM(MVPMatrix, 0, viewMatrix, 0, modelMatrix, 0);
		Matrix.multiplyMM(MVPMatrix, 0, projectionMatrix, 0, MVPMatrix, 0);

		//pass data
		//uniforms
		GLES20.glUniformMatrix4fv(uMMatrix, 1, false, modelMatrix, 0);
		GLES20.glUniformMatrix4fv(uMVPMatrix, 1, false, MVPMatrix, 0);
		GLES20.glUniform1i(uTexEnabled, m.getTexEnabled());
		GLES20.glUniform1i(uLOShadeless, m.getLShadeless());
		GLES20.glUniform3fv(uLOAmbient, 1, m.getLAmbient(), 0);
		GLES20.glUniform3fv(uLODiffuse, 1, m.getLDiffuse(), 0);
		GLES20.glUniform3fv(uLOSpecular, 1, m.getLSpecular(), 0);

		//attrs
		addAttrFArray(aPosition, 3, Model.COORDINATES, m);
		addAttrFArray(aColor, 4, Model.COLORS, m);
		addAttrFArray(aTexPosition, 2, Model.TEX_COORDINATES, m);
		addAttrFArray(aNormal, 3, Model.NORMALS, m);

		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

		if (m.getTexEnabled() == 1) {
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, ((TextureStore) scene.getTextureStore()).getDataForKey(m.getTexture()));
		}

		if (m.getAlphaEnabled() == 1) {
			GLES20.glEnable(GLES20.GL_BLEND);
			GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		}

		GLES20.glDrawElements(GLES20.GL_TRIANGLES, m.getCount(), GLES20.GL_UNSIGNED_SHORT, m.getShorts());

		//disable client states
		GLES20.glDisable(GLES20.GL_BLEND);
		//disable passed attr arrays
		GLES20.glDisableVertexAttribArray(aPosition);
		GLES20.glDisableVertexAttribArray(aColor);
		GLES20.glDisableVertexAttribArray(aTexPosition);
		GLES20.glDisableVertexAttribArray(aNormal);
	}

	@Override
	public void afterRenderFrame(AGScene scene, AGAttr attr) {
		for (Model m : modelStack) {
			if (!currentStack.contains(m)) {
				modelStack.remove(m);
				m.destroyBuffers();
			}
		}
	}

	private void addAttrFArray(int handle, int partCount, int which, Model m) {
		int index;
		if (m != null && (index = m.getIndex(which)) != -1) {
			int b = m.getFloats();
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, b);
			GLES20.glEnableVertexAttribArray(handle);
			GLES20.glVertexAttribPointer(handle, partCount, GLES20.GL_FLOAT, false, m.getFloatStride() * 4, index * 4);
		}
	}

	public static void transformCameraMatrix(float[] t, float[] m) {
		//invert transform and apply transformations to matrix (global rotation)
		Matrix.setIdentityM(m, 0);
		Matrix.scaleM(m, 0, 1f / AGTransform.getScaleX(t, 0), 1f / AGTransform.getScaleY(t, 0), 1f / AGTransform.getScaleZ(t, 0));
		Matrix.rotateM(m, 0, -AGTransform.getRotationX(t, 0), 1f, 0f, 0f);
		Matrix.rotateM(m, 0, -AGTransform.getRotationY(t, 0), 0f, 1f, 0f);
		Matrix.rotateM(m, 0, -AGTransform.getRotationZ(t, 0), 0f, 0f, 1f);
		Matrix.translateM(m, 0, -AGTransform.getPositionX(t, 0), -AGTransform.getPositionY(t, 0), -AGTransform.getPositionZ(t, 0));
	}

	public static void transformMatrix(float[] t, float[] m, boolean identity) {
		//apply transformation to matrix (local rotation)
		if (identity)
			Matrix.setIdentityM(m, 0);
		Matrix.scaleM(m, 0, AGTransform.getScaleX(t, 0), AGTransform.getScaleY(t, 0), AGTransform.getScaleZ(t, 0));
		Matrix.translateM(m, 0, AGTransform.getPositionX(t, 0), AGTransform.getPositionY(t, 0), AGTransform.getPositionZ(t, 0));
		Matrix.rotateM(m, 0, AGTransform.getRotationX(t, 0), 1f, 0f, 0f);
		Matrix.rotateM(m, 0, AGTransform.getRotationY(t, 0), 0f, 1f, 0f);
		Matrix.rotateM(m, 0, AGTransform.getRotationZ(t, 0), 0f, 0f, 1f);
	}

}
