package agf.module.graphics.android.gles20;

import de.ab.agf.lib.backend.AGLamp;
import de.ab.agf.lib.math.AGArrays;
import de.ab.agf.lib.util.AGAttr;

public class Lamp extends AGLamp {

	private short type;
	private Float pointStrength, spotlightAngle, spotlightHardness;
	private float[] color, direction;

	@Override
	protected void set(int which, AGAttr data) {
		switch (which) {
			case TYPE:
				type = data.getFor(Short.class);
				break;
			case COLOR:
				color = new float[3];
				if (data != null)
					AGArrays.convert(data.getFor(Float[].class), 0, color, 0, 3);
				break;
			case DIRECTION:
				direction = new float[3];
				if (data != null)
					AGArrays.convert(data.getFor(Float[].class), 0, direction, 0, 3);
				break;
			case POINT_STRENGTH:
				pointStrength = data.getFor(Float.class);
				break;
			case SPOTLIGHT_ANGLE:
				spotlightAngle = data.getFor(Float.class);
				break;
			case SPOTLIGHT_HARDNESS:
				spotlightHardness = data.getFor(Float.class);
				break;
		}
	}

	@Override
	protected void setDefault(int which) {
		switch (which) {
			case TYPE:
				type = TYPE_SUN;
				break;
			case COLOR:
				color = new float[3];
				break;
			case DIRECTION:
				direction = new float[3];
				break;
			case POINT_STRENGTH:
				pointStrength = .75f;
				break;
			case SPOTLIGHT_ANGLE:
				spotlightAngle = 0f;
				break;
			case SPOTLIGHT_HARDNESS:
				spotlightHardness = 1f;
				break;
		}
	}

	@Override
	protected void endModification() {
	}

	public Short getType() {
		return type;
	}

	public float[] getColor() {
		return color;
	}

	public float[] getDirection() {
		return direction;
	}

	public Float getPointStrength() {
		return pointStrength;
	}

	public Float getSpotlightAngle() {
		return spotlightAngle;
	}

	public Float getSpotlightHardness() {
		return spotlightHardness;
	}
}
