package agf.module.graphics.android.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.SurfaceView;

import agf.module.graphics.android.R;
import de.ab.agf.lib.AGAdapter;
import de.ab.agf.lib.util.AGAttr;

public class BaseControl extends SurfaceView {

	private String controlKey;
	private AGAdapter adapter;

	public BaseControl(Context context) {
		super(context);
	}

	public BaseControl(Context context, String controlKey) {
		super(context);
		this.controlKey = controlKey;
	}

	public BaseControl(Context context, AttributeSet attrs) {
		super(context, attrs);
		loadAttrs(attrs);
	}

	public BaseControl(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		loadAttrs(attrs);
	}

	private void loadAttrs(AttributeSet attrs) {
		TypedArray attr = getContext().getResources().obtainAttributes(attrs, R.styleable.BaseControl);
		controlKey = attr.getString(R.styleable.BaseControl_controlKey);
		attr.recycle();
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
			throw new RuntimeException("Use setControlKey or xml element localNameSpace:controlKey first!");
		adapter.transferControlState(controlKey, value, attr);
	}
}
