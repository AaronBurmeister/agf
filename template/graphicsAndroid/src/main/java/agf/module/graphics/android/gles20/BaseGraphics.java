package agf.module.graphics.android.gles20;

import de.ab.agf.lib.backend.AGGraphics;
import de.ab.agf.lib.backend.AGLamp;
import de.ab.agf.lib.backend.AGLampGraphics;
import de.ab.agf.lib.backend.AGLampStore;
import de.ab.agf.lib.backend.AGModel;
import de.ab.agf.lib.backend.AGModelGraphics;
import de.ab.agf.lib.backend.AGTextureStore;
import de.ab.agf.lib.backend.AGTransformer;
import de.ab.agf.lib.render.AGRenderable;

public abstract class BaseGraphics extends AGGraphics {

	@Override
	protected AGModel createModel() {
		return new Model();
	}

	@Override
	public AGModelGraphics createModelGraphics() {
		return new AGModelGraphics(this);
	}

	@Override
	public AGTransformer createTransformer(AGRenderable renderable) {
		return new Transformer(renderable);
	}

	@Override
	protected AGLamp createLamp() {
		return new Lamp();
	}

	@Override
	public AGLampGraphics createLampGraphics() {
		return new AGLampGraphics(this);
	}

	@Override
	public AGLampStore createLampStore() {
		return new LampStore(this);
	}

	@Override
	public AGTextureStore createTextureStore() {
		return new TextureStore(this);
	}

}
