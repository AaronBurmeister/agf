package agf.module.graphics.desktop.util;

import agf.module.graphics.desktop.G;
import de.ab.agf.lib.util.AGLogType;

public class HandlerUtils {

	public static void onInternalException(String at, String message, Throwable cause) {
		output("system", at + "::" + message + ":" + (cause == null ? "unknown" : cause.getMessage()));
		if (cause != null)
			cause.printStackTrace();
	}

	public static void log(AGLogType log, String key, String message) {
		if (G.DEBUG)
			switch (log) {
				case ERROR:
					output("e: " + key, message);
					break;
				case INFO:
					output("i: " + key, message);
					break;
				case DEBUG:
					output("d: " + key, message);
					break;
				case VERBOSE:
					output("v: " + key, message);
					break;
				case WARN:
					output("w: " + key, message);
					break;
				default:
					output("log", log + "/" + key + ": " + message);
			}
	}

	private static void output(String key, String msg) {
		System.out.print("agf: ");
		System.out.println(key);
		System.out.println(msg);
	}
}
