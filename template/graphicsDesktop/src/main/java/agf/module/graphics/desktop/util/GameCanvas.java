package agf.module.graphics.desktop.util;

import com.jogamp.opengl.GLCapabilitiesChooser;
import com.jogamp.opengl.GLCapabilitiesImmutable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.awt.GLCanvas;

import java.awt.GraphicsDevice;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import agf.module.graphics.desktop.G;
import de.ab.agf.lib.AGAdapter;
import de.ab.agf.lib.backend.AGGraphics;
import de.ab.agf.lib.backend.AGHandler;
import de.ab.agf.lib.util.AGAttr;
import de.ab.agf.lib.util.AGLogType;
import de.ab.agf.lib.util.AGLogicTask;

public abstract class GameCanvas extends GLCanvas implements AGHandler, ComponentListener {

	private AGGraphics graphics;
	private AGAdapter adapter;
	private AGLogicTask logicTask;

	public GameCanvas() throws GLException {
		onCreate();
	}

	public GameCanvas(GLCapabilitiesImmutable capsReqUser) throws GLException {
		super(capsReqUser);
		onCreate();
	}

	public GameCanvas(GLCapabilitiesImmutable capsReqUser, GLCapabilitiesChooser chooser, GraphicsDevice device) throws GLException {
		super(capsReqUser, chooser, device);
		onCreate();
	}

	protected AGGraphics getAGGraphics() {
		return graphics;
	}

	protected AGAdapter getAGAdapter() {
		return adapter;
	}

	protected AGLogicTask getAGLogicTask() {
		return logicTask;
	}

	protected abstract AGGraphics onCreateAGGraphics();

	protected abstract GLEventListener onCreateEventListener();

	protected AGLogicTask onCreateAGLogicTask() {
		return new AGLogicTask.AGLogicTaskThread(this);
	}

	protected AGAdapter onCreateAGAdapter() {
		return new AGAdapter(getAGGraphics(), this, getAGLogicTask(), null);
	}

	protected GLCanvas onCreateCanvas() {
		return new GLCanvas();
	}

	public void onCreate() {
		logicTask = onCreateAGLogicTask();
		graphics = onCreateAGGraphics();
		adapter = onCreateAGAdapter();
		addComponentListener(this);
		addGLEventListener(onCreateEventListener());
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

	@Override
	public void componentResized(ComponentEvent e) {

	}

	@Override
	public void componentMoved(ComponentEvent e) {

	}

	@Override
	public void componentShown(ComponentEvent e) {
		if (adapter != null)
			adapter.resumeLogic();
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		if (adapter != null)
			adapter.pauseLogic(G.PAUSE_INTERVAL);
	}
}
