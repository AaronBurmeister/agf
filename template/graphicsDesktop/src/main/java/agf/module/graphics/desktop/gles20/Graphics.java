package agf.module.graphics.desktop.gles20;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import agf.module.graphics.desktop.G;
import agf.module.graphics.desktop.util.Matrix;
import de.ab.agf.lib.AGScene;
import de.ab.agf.lib.backend.AGModel;
import de.ab.agf.lib.math.AGTransform;
import de.ab.agf.lib.util.AGAttr;

public class Graphics extends BaseGraphics {

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

	//uniforms
	private static final String iuMMatrix = "u_MMatrix", iuMVPMatrix = "u_MVPMatrix", iuTex = "u_Tex", iuTexEnabled = "u_TexEnabled", iuLLCount = "u_LLCount", iuLLType = "u_LLType", iuLLMatrix = "u_LLMatrix", iuLLColor = "u_LLColor", iuLLDirection = "u_LLDirection", iuLLAttributes = "u_LLAttributes", iuLOShadeless = "u_LOShadeless", iuLOAmbient = "u_LOAmbient", iuLODiffuse = "u_LODiffuse", iuLOSpecular = "u_LOSpecular";
	private int uMMatrix, uMVPMatrix, uTex, uTexEnabled, uLLCount, uLLType, uLLMatrix, uLLColor, uLLDirection, uLLAttributes, uLOShadeless, uLOAmbient, uLODiffuse, uLOSpecular;
	//attrs
	private static final String iaPosition = "a_Position", iaColor = "a_Color", iaTexPosition = "a_TexPosition", iaNormal = "a_Normal";
	private int aPosition, aColor, aTexPosition, aNormal;
	//indices
	private int indices;

	private float[] viewMatrix = new float[16], projectionMatrix = new float[16], modelMatrix = new float[16], MVPMatrix = new float[16];

	private ArrayList<Model> modelStack = new ArrayList<>(), currentStack = new ArrayList<>();

	private String readRawFile(String name) {
		InputStream inputStream = G.Assets.open("raw" + File.separator + name);
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

	private int loadShader(GL gl, int type, String shaderName) {
		String shaderCode = readRawFile(shaderName + ".shader");
		String log = "no information";

		int handle = gl._gl.glCreateShader(type);
		if (handle != 0) {
			gl._gl.glShaderSource(handle, 1, new String[]{shaderCode}, new int[]{shaderCode.length()}, 0);
			gl._gl.glCompileShader(handle);
			final int[] compileStatus = new int[1];
			gl._gl.glGetShaderiv(handle, GL._GL.GL_COMPILE_STATUS, compileStatus, 0);
			if (compileStatus[0] == 0) {
				byte[] l = new byte[1000];
				gl._gl.glGetShaderInfoLog(handle, 1000, new int[]{1000}, 0, l, 0);
				log = new String(l);
				gl._gl.glDeleteShader(handle);
				handle = 0;
			}
		}
		if (handle == 0)
			throw new RuntimeException("Error creating shader with type " + (type == GL._GL.GL_VERTEX_SHADER ? "vertex" : type == GL._GL.GL_FRAGMENT_SHADER ? "fragment" : type) + ":\n" + log);
		return handle;
	}

	public void onSurfaceCreated(GL gl) {
		gl._gl.glClearColor(agf.module.graphics.desktop.G.BACKGROUND[0], agf.module.graphics.desktop.G.BACKGROUND[1], agf.module.graphics.desktop.G.BACKGROUND[2], 1f);

		int vertexShader = loadShader(gl, GL._GL.GL_VERTEX_SHADER, "vertex");
		int fragmentShader = loadShader(gl, GL._GL.GL_FRAGMENT_SHADER, "fragment");
		int program = gl._gl.glCreateProgram();

		if (program != 0) {
			gl._gl.glAttachShader(program, vertexShader);
			gl._gl.glAttachShader(program, fragmentShader);
			//bind attribute pointer
			gl._gl.glBindAttribLocation(program, 0, iaPosition);
			gl._gl.glBindAttribLocation(program, 1, iaColor);
			gl._gl.glBindAttribLocation(program, 2, iaTexPosition);
			gl._gl.glBindAttribLocation(program, 3, iaNormal);

			gl._gl.glLinkProgram(program);
			final int[] status = new int[1];
			gl._gl.glGetProgramiv(program, GL._GL.GL_LINK_STATUS, status, 0);
			if (status[0] == 0) {
				gl._gl.glDeleteProgram(program);
				program = 0;
			}

			int[] ind = new int[1];
			gl._gl.glGenBuffers(1, ind, 0);
			indices = ind[0];
		}
		if (program == 0)
			throw new RuntimeException("Error creating program!");

		//get locations
		//uniforms
		uMMatrix = gl._gl.glGetUniformLocation(program, iuMMatrix);
		uMVPMatrix = gl._gl.glGetUniformLocation(program, iuMVPMatrix);
		uTex = gl._gl.glGetUniformLocation(program, iuTex);
		uTexEnabled = gl._gl.glGetUniformLocation(program, iuTexEnabled);
		uLLCount = gl._gl.glGetUniformLocation(program, iuLLCount);
		uLLType = gl._gl.glGetUniformLocation(program, iuLLType);
		uLLMatrix = gl._gl.glGetUniformLocation(program, iuLLMatrix);
		uLLColor = gl._gl.glGetUniformLocation(program, iuLLColor);
		uLLDirection = gl._gl.glGetUniformLocation(program, iuLLDirection);
		uLLAttributes = gl._gl.glGetUniformLocation(program, iuLLAttributes);
		uLOShadeless = gl._gl.glGetUniformLocation(program, iuLOShadeless);
		uLOAmbient = gl._gl.glGetUniformLocation(program, iuLOAmbient);
		uLODiffuse = gl._gl.glGetUniformLocation(program, iuLODiffuse);
		uLOSpecular = gl._gl.glGetUniformLocation(program, iuLOSpecular);
		//attrs
		aPosition = gl._gl.glGetAttribLocation(program, iaPosition);
		aColor = gl._gl.glGetAttribLocation(program, iaColor);
		aTexPosition = gl._gl.glGetAttribLocation(program, iaTexPosition);
		aNormal = gl._gl.glGetAttribLocation(program, iaNormal);

		gl._gl.glUseProgram(program);

		gl._gl.glEnable(GL._GL.GL_DEPTH_TEST);
		gl._gl.glEnable(GL._GL.GL_CULL_FACE);
		gl._gl.glCullFace(GL._GL.GL_BACK);
		gl._gl.glFrontFace(GL._GL.GL_CCW);
	}

	public void setProjection(float[] projectionMatrix) {
		this.projectionMatrix = projectionMatrix;
	}

	private GL gl;

	@Override
	public void preRenderFrame(AGScene scene, AGAttr attr) {
		gl = attr.getFor(GL.class);

		gl._gl.glClear(GL._GL.GL_COLOR_BUFFER_BIT | GL._GL.GL_DEPTH_BUFFER_BIT);

		currentStack.clear();

		if (scene == null)
			return;

		((TextureStore) scene.getTextureStore()).handleQueues(gl);

		transformCameraMatrix(scene.getCameraTransform(), viewMatrix);

		LampStore s = (LampStore) scene.getLampStore();
		int count = s.getCount();
		if (count > 0) {
			gl._gl.glUniform1i(uLLCount, count);
			gl._gl.glUniform1fv(uLLType, count, s.getTypes().floats);
			gl._gl.glUniformMatrix4fv(uLLMatrix, count, false, s.getMatrices(), 0);
			gl._gl.glUniform3fv(uLLColor, count, s.getColors().floats);
			gl._gl.glUniform3fv(uLLDirection, count, s.getDirections().floats);
			gl._gl.glUniform3fv(uLLAttributes, count, s.getAttributes().floats);
		}
	}

	@Override
	public void renderModel(AGScene scene, Object modelTransform, AGModel model) {
		Model m = (Model) model;

		boolean forced = false;

		if (!modelStack.contains(m)) {
			modelStack.add(m);
			m.createBuffers(gl);
			forced = true;
		}
		currentStack.add(m);
		m.updateBuffers(gl, forced);

		//transformMatrix((float[]) modelTransform, modelMatrix);
		de.ab.agf.lib.math.AGArrays.copy((float[]) modelTransform, 0, modelMatrix, 0, 16);
		Matrix.setIdentityM(MVPMatrix, 0);
		Matrix.multiplyMM(MVPMatrix, 0, viewMatrix, 0, modelMatrix, 0);
		Matrix.multiplyMM(MVPMatrix, 0, projectionMatrix, 0, MVPMatrix, 0);

		//pass data
		//uniforms
		gl._gl.glUniformMatrix4fv(uMMatrix, 1, false, modelMatrix, 0);
		gl._gl.glUniformMatrix4fv(uMVPMatrix, 1, false, MVPMatrix, 0);
		gl._gl.glUniform1i(uTexEnabled, m.getTexEnabled());
		gl._gl.glUniform1i(uLOShadeless, m.getLShadeless());
		gl._gl.glUniform3fv(uLOAmbient, 1, m.getLAmbient(), 0);
		gl._gl.glUniform3fv(uLODiffuse, 1, m.getLDiffuse(), 0);
		gl._gl.glUniform3fv(uLOSpecular, 1, m.getLSpecular(), 0);

		//attrs
		addAttrFArray(gl, aPosition, 3, Model.COORDINATES, m);
		addAttrFArray(gl, aColor, 4, Model.COLORS, m);
		addAttrFArray(gl, aTexPosition, 2, Model.TEX_COORDINATES, m);
		addAttrFArray(gl, aNormal, 3, Model.NORMALS, m);

		gl._gl.glBindBuffer(GL._GL.GL_ARRAY_BUFFER, 0);

		if (m.getTexEnabled() == 1) {
			gl._gl.glActiveTexture(GL._GL.GL_TEXTURE0);
			gl._gl.glBindTexture(GL._GL.GL_TEXTURE_2D, ((TextureStore) scene.getTextureStore()).getDataForKey(m.getTexture()));
		}

		if (m.getAlphaEnabled() == 1) {
			gl._gl.glEnable(GL._GL.GL_BLEND);
			gl._gl.glBlendFunc(GL._GL.GL_SRC_ALPHA, GL._GL.GL_ONE_MINUS_SRC_ALPHA);
		}

		gl._gl.glBindBuffer(GL._GL.GL_ELEMENT_ARRAY_BUFFER, indices);
		gl._gl.glBufferData(GL._GL.GL_ELEMENT_ARRAY_BUFFER, 2 * m.getCount(), m.getShorts(), GL._GL.GL_STATIC_DRAW);
		gl._gl.glDrawElements(GL._GL.GL_TRIANGLES, m.getCount(), GL._GL.GL_UNSIGNED_SHORT, 0);
		gl._gl.glBindBuffer(GL._GL.GL_ELEMENT_ARRAY_BUFFER, 0);

		//disable client states
		gl._gl.glDisable(GL._GL.GL_BLEND);
		//disable passed attr arrays
		gl._gl.glDisableVertexAttribArray(aPosition);
		gl._gl.glDisableVertexAttribArray(aColor);
		gl._gl.glDisableVertexAttribArray(aTexPosition);
		gl._gl.glDisableVertexAttribArray(aNormal);
	}

	@Override
	public void afterRenderFrame(AGScene scene, AGAttr attr) {
		for (Model m : modelStack) {
			if (!currentStack.contains(m)) {
				modelStack.remove(m);
				m.destroyBuffers(gl);
			}
		}
	}

	private void addAttrFArray(GL gl, int handle, int partCount, int which, Model m) {
		int index;
		if (m != null && (index = m.getIndex(which)) != -1) {
			int b = m.getFloats();
			gl._gl.glBindBuffer(GL._GL.GL_ARRAY_BUFFER, b);
			gl._gl.glEnableVertexAttribArray(handle);
			gl._gl.glVertexAttribPointer(handle, partCount, GL._GL.GL_FLOAT, false, m.getFloatStride() * 4, index * 4);
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
