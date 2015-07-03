package agf.module.graphics.desktop;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

public final class G {

	//pause interval (used for threaded LogicTasks) in milliseconds
	public static final int PAUSE_INTERVAL = 1000;

	//release type debug (enabled logging / internalException etc. in HandlerUtils)
	public static final boolean DEBUG = true;

	//default background color of GLSurfaces				red		green	blue
	public static final float[] BACKGROUND = new float[]{.5f, .5f, .5f};

	//texture bindings (key -> asset path)
	public static final HashMap<String, String> TEXTURE_BINDINGS;

	static {
		TEXTURE_BINDINGS = new HashMap<>();

		//Define your texture bindings here

		//example:				textureKey	assetPath to file
		//TEXTURE_BINDINGS.put(	"floor",	"dir/dir/floorTex.png");
	}

	//file resources

	public static final class Assets {

		public static InputStream open(String name) {
			String path = "/" + name.replace(File.separator, "/");
			return G.class.getResourceAsStream(path);
		}

	}
}
