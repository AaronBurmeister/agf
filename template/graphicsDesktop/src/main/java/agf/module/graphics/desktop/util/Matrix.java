package agf.module.graphics.desktop.util;

import com.jogamp.opengl.math.FloatUtil;

public class Matrix {

	public static void setIdentityM(float[] m, int offset) {
		FloatUtil.makeIdentity(m, offset);
	}

	public static void frustumM(float[] m, int offset, float left, float right, float bottom, float top, float zNear, float zFar) {
		FloatUtil.makeFrustum(m, offset, true, left, right, bottom, top, zNear, zFar);
	}

	public static void translateM(float[] m, int offset, float x, float y, float z) {
		float[] t = FloatUtil.makeTranslation(new float[16], true, x, y, z);
		FloatUtil.multMatrix(m, offset, t, 0);
	}

	public static void rotateM(float[] m, int offset, float degrees, float x, float y, float z) {
		float[] t = FloatUtil.makeRotationAxis(new float[16], 0, (float) Math.toRadians(degrees), x, y, z, new float[3]);
		FloatUtil.multMatrix(m, offset, t, 0);
	}

	public static void scaleM(float[] m, int offset, float x, float y, float z) {
		float[] t = FloatUtil.makeScale(new float[16], 0, true, x, y, z);
		FloatUtil.multMatrix(m, offset, t, 0);
	}

	public static void multiplyMM(float[] dest, int destOffset, float[] lhs, int lhsOffset, float[] rhs, int rhsOffset) {
		FloatUtil.multMatrix(lhs, lhsOffset, rhs, rhsOffset, dest, destOffset);
	}

}
