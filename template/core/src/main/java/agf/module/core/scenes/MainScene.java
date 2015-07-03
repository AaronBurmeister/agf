package agf.module.core.scenes;

import de.ab.agf.lib.AGScene;
import de.ab.agf.lib.backend.AGLampStore;
import de.ab.agf.lib.backend.AGTextureStore;
import de.ab.agf.lib.util.AGAttr;

public class MainScene extends AGScene {

	public MainScene(AGAttr attr) {
		super(attr);
	}

	@Override
	public void onTextureStoreCreated() {
		super.onTextureStoreCreated();

		AGTextureStore store = getTextureStore();
		//register the textures that you need for your scene here
		//IMPORTANT: you have to bind registered keys in graphics->Build before using
		//example:	textureKey
		//store.putData("floor");
	}

	@Override
	public void onLampStoreCreated() {
		super.onLampStoreCreated();

		AGLampStore store = getLampStore();
		//add static lamps to the store
	}

	@Override
	public void onAttached(AGAttr attr) {
		super.onAttached(attr);

		//setContent(...);
	}
}
