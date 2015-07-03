package agf.module.graphics.desktop.gles20;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import de.ab.agf.lib.backend.AGGraphics;
import de.ab.agf.lib.backend.AGLamp;
import de.ab.agf.lib.backend.AGLampStore;
import de.ab.agf.lib.math.AGArrays;

public class LampStore extends AGLampStore {

	private Lamp[] lamps = new Lamp[0];
	private float[] types = new float[0], matrices = new float[0], colors = new float[0], directions = new float[0], attributes = new float[0];
	private FastFloatBuffer typeBuffer, colorBuffer, directionBuffer, attributeBuffer;

	public LampStore(AGGraphics graphics) {
		super(graphics);
		resetBuffers();
	}

	@Override
	public void putData(AGLamp lamp) {
		if (!(lamp instanceof Lamp))
			throw new RuntimeException("You are only allowed to call " + getClass().getSimpleName() + ".putData with " + Lamp.class.getName() + ". Current lamp class: " + lamp.getClass().getName());
		Lamp l = (Lamp) lamp;

		int lp = findLamp(l);
		boolean reset = lp == -1;
		if (lp == -1)
			lp = expandArrays() - 1;
		insert(l, lp);
		if (reset)
			resetBuffers();
		fillBuffers();
	}

	@Override
	public void removeData(AGLamp lamp) {
		if (!(lamp instanceof Lamp))
			throw new RuntimeException("You are only allowed to call " + getClass().getSimpleName() + ".removeLampData with " + Lamp.class.getName() + ". Currenct lamp class: " + lamp.getClass().getName());
		Lamp l = (Lamp) lamp;

		int lp = findLamp(l);
		boolean reset = lp == -1;
		if (lp != -1)
			shortenArrays(lp);
		if (reset) {
			resetBuffers();
			fillBuffers();
		}
	}

	@Override
	public Iterable<AGLamp> getPassData() {
		ArrayList<AGLamp> data = new ArrayList<>();
		Collections.addAll(data, lamps);
		return data;
	}

	private int expandArrays() {
		lamps = Arrays.copyOf(lamps, lamps.length + 1);
		types = Arrays.copyOf(types, types.length + 1);
		matrices = Arrays.copyOf(matrices, matrices.length + 16);
		colors = Arrays.copyOf(colors, colors.length + 3);
		directions = Arrays.copyOf(directions, directions.length + 3);
		attributes = Arrays.copyOf(attributes, attributes.length + 3);

		if (lamps.length > 8)
			throw new RuntimeException("No more space for more lamps! Change max declaration in the vertex shader and in " + getClass().getSimpleName() + ".expandArrays");

		return lamps.length;
	}

	private void insert(Lamp l, int at) {
		lamps[at] = l;
		types[at] = l.getType();
		float[] matrix = new float[16];
		Graphics.transformMatrix(l.getLampTransform(), matrix, true);
		AGArrays.copy(matrix, 0, matrices, at * 16, 16);
		AGArrays.copy(l.getColor(), 0, colors, at * 3, 3);
		AGArrays.copy(l.getDirection(), 0, directions, at * 3, 3);
		AGArrays.copy(new float[]{l.getPointStrength(), l.getSpotlightAngle(), l.getSpotlightHardness()}, 0, attributes, at * 3, 3);
	}

	private void shortenArrays(int at) {
		lamps = shortenArray(lamps, new Lamp[lamps.length - 1], at, 1);
		types = shortenArray(types, new float[types.length - 1], at, 1);
		matrices = shortenArray(matrices, new float[matrices.length - 16], at * 16, 16);
		colors = shortenArray(colors, new float[colors.length - 3], at * 3, 3);
		directions = shortenArray(directions, new float[directions.length - 3], at * 3, 3);
		attributes = shortenArray(attributes, new float[attributes.length - 3], at * 3, 3);
	}

	private void resetBuffers() {
		(typeBuffer = new FastFloatBuffer(types.length)).position(0);
		(colorBuffer = new FastFloatBuffer(colors.length)).position(0);
		(directionBuffer = new FastFloatBuffer(directions.length)).position(0);
		(attributeBuffer = new FastFloatBuffer(attributes.length)).position(0);
	}

	private void fillBuffers() {
		typeBuffer.position(0);
		typeBuffer.put(types).position(0);
		colorBuffer.position(0);
		colorBuffer.put(colors).position(0);
		directionBuffer.position(0);
		directionBuffer.put(directions).position(0);
		attributeBuffer.position(0);
		attributeBuffer.put(attributes).position(0);
	}

	//get

	public int getCount() {
		return lamps.length;
	}

	public FastFloatBuffer getTypes() {
		return typeBuffer;
	}

	public float[] getMatrices() {
		return matrices;
	}

	public FastFloatBuffer getColors() {
		return colorBuffer;
	}

	public FastFloatBuffer getDirections() {
		return directionBuffer;
	}

	public FastFloatBuffer getAttributes() {
		return attributeBuffer;
	}

	//utility

	private int findLamp(Lamp l) {
		for (int i = 0; i < lamps.length; i++)
			if (lamps[i] == l)
				return i;
		return -1;
	}

	private <T> T[] shortenArray(T[] oldA, T[] newA, int skipMin, int skipCount) {
		int indexBuffer = 0;
		for (int i = 0; i < oldA.length; i++)
			if (i < skipMin || i >= skipMin + skipCount)
				newA[indexBuffer++] = oldA[i];
		return newA;
	}

	private short[] shortenArray(short[] oldA, short[] newA, int skipMin, int skipCount) {
		int indexBuffer = 0;
		for (int i = 0; i < oldA.length; i++)
			if (i < skipMin || i >= skipMin + skipCount)
				newA[indexBuffer++] = oldA[i];
		return newA;
	}

	private float[] shortenArray(float[] oldA, float[] newA, int skipMin, int skipCount) {
		int indexBuffer = 0;
		for (int i = 0; i < oldA.length; i++)
			if (i < skipMin || i >= skipMin + skipCount)
				newA[indexBuffer++] = oldA[i];
		return newA;
	}

}
