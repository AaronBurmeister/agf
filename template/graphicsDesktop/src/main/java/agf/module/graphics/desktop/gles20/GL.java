package agf.module.graphics.desktop.gles20;

import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLProfile;

public class GL {

	public static final String PROFILE = GLProfile.GL2ES2;

	public GLProfile _profile;
	public GL2ES2 _gl;

	public GL(GLProfile _profile, com.jogamp.opengl.GL _gl) {
		this._profile = _profile;
		this._gl = _gl.getGL2ES2();
	}

	public GL(GLAutoDrawable drawable) {
		this(drawable.getGLProfile(), drawable.getGL());
	}

	public interface _GL extends GL2ES2 {
	}

}
