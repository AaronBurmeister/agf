package agf.module.graphics.desktop.util;

import de.ab.agf.lib.backend.AGGraphics;
import de.ab.agf.lib.util.AGAttr;

public abstract class BaseGraphics extends AGGraphics {

	public abstract void onSurfaceCreated(AGAttr attr);

	public abstract void onSurfaceChanged(int width, int height, AGAttr attr);

}
