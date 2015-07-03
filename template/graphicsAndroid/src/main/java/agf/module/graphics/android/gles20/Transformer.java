package agf.module.graphics.android.gles20;

import android.opengl.Matrix;

import de.ab.agf.lib.backend.AGTransformer;
import de.ab.agf.lib.math.AGBounds;
import de.ab.agf.lib.math.AGVector;
import de.ab.agf.lib.render.AGRenderable;

public class Transformer extends AGTransformer.AGSimpleTransformer<float[]> {

	private float[] transformBuffer, matrixBuffer, matrix;

	public Transformer(AGRenderable renderable) {
		super(renderable);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		matrix = new float[16];
	}

	@Override
	public float[] transform() {
		if (matrixBuffer == null || changed())
			return matrixBuffer = super.transform();
		return matrixBuffer;
	}

	@Override
	protected Transformer getParent() {
		return (Transformer) super.getParent();
	}

	private boolean changed() {
		float[] tb = transformBuffer;
		Transformer p = getParent();
		return (p != null && p.changed()) || tb == null || !de.ab.agf.lib.math.AGArrays.equals(tb, 0, transformBuffer = getRenderable().getModelTransform(), 0, de.ab.agf.lib.math.AGTransform.LENGTH);
	}

	@Override
	protected float[] transformRenderable(float[] data) {
		float[] origin = getRenderable().getOrigin();

		//move to origin space
		Matrix.translateM(data, 0, AGVector.getX(origin, 0), AGVector.getY(origin, 0), AGVector.getZ(origin, 0));

		//transform
		Graphics.transformMatrix(getRenderable().getModelTransform(), data, false);

		//move back from origin space
		Matrix.translateM(data, 0, -AGVector.getX(origin, 0), -AGVector.getY(origin, 0), -AGVector.getZ(origin, 0));

		return data;
	}

	@Override
	protected float[] transformRenderableBounds(float[] data, int offset) {
		AGBounds.transform(data, offset, getRenderable().getOrigin(), 0, getRenderable().getModelTransform(), 0);
		return data;
	}

	@Override
	protected float[] initTransformation() {
		Matrix.setIdentityM(matrix, 0);
		return matrix;
	}
}
