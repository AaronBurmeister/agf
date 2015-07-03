package agf.module.core;

import java.io.File;
import java.io.InputStream;

public final class C {

	//file resources

	public static final class Assets {

		public static InputStream open(String name) {
			String path = "/" + name.replace(File.separator, "/");
			return C.class.getResourceAsStream(path);
		}

	}
}
