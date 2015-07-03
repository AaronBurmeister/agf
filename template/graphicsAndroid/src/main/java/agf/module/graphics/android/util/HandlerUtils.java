package agf.module.graphics.android.util;

import android.util.Log;

import agf.module.graphics.android.G;
import de.ab.agf.lib.util.AGLogType;

public class HandlerUtils {

	public static void onInternalException(String at, String message, Throwable cause) {
		Log.e("system", at + "::" + message + ":" + (cause == null ? "unknown" : cause.getMessage()));
		if (cause != null)
			cause.printStackTrace();
	}

	public static void log(AGLogType log, String key, String message) {
		if (G.DEBUG)
			switch (log) {
				case ERROR:
					Log.e(key, message);
					break;
				case INFO:
					Log.i(key, message);
					break;
				case DEBUG:
					Log.d(key, message);
					break;
				case VERBOSE:
					Log.v(key, message);
					break;
				case WARN:
					Log.w(key, message);
					break;
				default:
					Log.i("log", log + "/" + key + ": " + message);
			}
	}
}
