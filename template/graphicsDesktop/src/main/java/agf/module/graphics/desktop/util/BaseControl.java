package agf.module.graphics.desktop.util;

import java.awt.Canvas;
import java.awt.GraphicsConfiguration;

import de.ab.agf.lib.AGAdapter;
import de.ab.agf.lib.util.AGAttr;

public class BaseControl extends Canvas {

	private String controlKey;
	private AGAdapter adapter;

	public BaseControl(String controlKey) {
		this.controlKey = controlKey;
	}

	public BaseControl(GraphicsConfiguration config, String controlKey) {
		super(config);
		this.controlKey = controlKey;
	}

	public void setControlKey(String controlKey) {
		this.controlKey = controlKey;
	}

	public void setAdapter(AGAdapter adapter) {
		this.adapter = adapter;
	}

	public String getControlKey() {
		return controlKey;
	}

	public AGAdapter getAdapter() {
		return adapter;
	}

	protected void transferControlState(Object value, AGAttr attr) {
		if (adapter == null)
			throw new RuntimeException("Use setAdapter first!");
		if (controlKey == null)
			throw new RuntimeException("Use setControlKey first!");
		adapter.transferControlState(controlKey, value, attr);
	}
}
