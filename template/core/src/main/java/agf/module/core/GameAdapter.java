package agf.module.core;

import agf.module.core.scenes.MainScene;
import de.ab.agf.lib.AGAdapter;
import de.ab.agf.lib.backend.AGGraphics;
import de.ab.agf.lib.backend.AGHandler;
import de.ab.agf.lib.util.AGAttr;
import de.ab.agf.lib.util.AGLogicTask;

public class GameAdapter extends AGAdapter {

	public GameAdapter(AGGraphics graphics, AGHandler handler, AGAttr attr) {
		super(graphics, handler, attr);
	}

	public GameAdapter(AGGraphics graphics, AGHandler handler, AGLogicTask logicThread, AGAttr attr) {
		super(graphics, handler, logicThread, attr);
	}

	@Override
	public void onCreate(AGAttr attr) {
		super.onCreate(attr);

		//setup entry point
		setScene(new MainScene(null), null, null);
	}
}
