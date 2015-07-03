precision mediump float;

uniform sampler2D u_Tex;
uniform int u_TexEnabled;

varying vec4 v_Color;
varying vec2 v_TexPosition;

void main() {
	if (u_TexEnabled == 1)
		gl_FragColor = v_Color * texture2D(u_Tex, v_TexPosition);
	else
		gl_FragColor = v_Color;
}