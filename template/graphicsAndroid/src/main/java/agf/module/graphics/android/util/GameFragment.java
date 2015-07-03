package agf.module.graphics.android.util;

import android.app.Fragment;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import agf.module.graphics.android.G;
import de.ab.agf.lib.AGAdapter;
import de.ab.agf.lib.backend.AGGraphics;
import de.ab.agf.lib.backend.AGHandler;
import de.ab.agf.lib.util.AGAttr;
import de.ab.agf.lib.util.AGLogType;
import de.ab.agf.lib.util.AGLogicTask;

public abstract class GameFragment extends Fragment implements AGHandler {

	private AGGraphics graphics;
	private AGAdapter adapter;
	private AGLogicTask logicTask;
	private GLSurfaceView surface;

	protected AGGraphics getAGGraphics() {
		return graphics;
	}

	protected AGAdapter getAGAdapter() {
		return adapter;
	}

	protected AGLogicTask getAGLogicTask() {
		return logicTask;
	}

	protected GLSurfaceView getSurface() {
		return surface;
	}

	protected abstract AGGraphics onCreateAGGraphics();

	protected abstract GLSurfaceView.Renderer onCreateRenderer();

	protected AGLogicTask onCreateAGLogicTask() {
		return new AGLogicTask.AGLogicTaskThread(this);
	}

	protected AGAdapter onCreateAGAdapter() {
		return new AGAdapter(getAGGraphics(), this, getAGLogicTask(), null);
	}

	protected GLSurfaceView onCreateSurfaceView() {
		return new GLSurfaceView(getActivity());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		logicTask = onCreateAGLogicTask();
		graphics = onCreateAGGraphics();
		adapter = onCreateAGAdapter();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		surface = onCreateSurfaceView();
		surface.setRenderer(onCreateRenderer());
		return surface;
	}

	@Override
	public void onPause() {
		super.onPause();
		adapter.pauseLogic(G.PAUSE_INTERVAL);
		surface.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		adapter.resumeLogic();
		surface.onResume();
	}

	@Override
	public void log(AGLogType log, String key, String message) {
		HandlerUtils.log(log, key, message);
	}

	@Override
	public void onInternalException(String at, String message, Throwable cause) {
		HandlerUtils.onInternalException(at, message, cause);
	}

	@Override
	public void onReceiveMessage(String key, Object value, AGAttr attr) {
	}

	@Override
	public void onRequestControlState(String requestedKey, AGAttr agAttr) {
	}
}
