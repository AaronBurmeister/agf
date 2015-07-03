#define MAX_LAMPS (8)

#define TYPE_SUN (0.0)
#define TYPE_POINT (1.0)
#define TYPE_SPOTLIGHT (2.0)

#define ATTR_POINT_STRENGTH (0)
#define ATTR_SPOTLIGHT_ANGLE (1)
#define ATTR_SPOTLIGHT_HARDNESS (2)

uniform mat4 u_MMatrix;
uniform mat4 u_MVPMatrix;
uniform int u_LLCount;
uniform float u_LLType[MAX_LAMPS];
uniform mat4 u_LLMatrix[MAX_LAMPS];
uniform vec3 u_LLColor[MAX_LAMPS];
uniform vec3 u_LLDirection[MAX_LAMPS];
uniform vec3 u_LLAttributes[MAX_LAMPS];
uniform int u_LOShadeless;
uniform vec3 u_LOAmbient;
uniform vec3 u_LODiffuse;
uniform vec3 u_LOSpecular;

attribute vec4 a_Position;
attribute vec4 a_Color;
attribute vec2 a_TexPosition;
attribute vec3 a_Normal;

varying vec4 v_Color;
varying vec2 v_TexPosition;

float getDiffuse(int lamp) {
	if (u_LLType[lamp] == TYPE_SUN) {
		vec4 vNormal = u_MMatrix * vec4(a_Normal, 0.0);
		vec4 vLamp = u_LLMatrix[lamp] * vec4(u_LLDirection[lamp], 0.0);
		float dot = dot(normalize(vNormal), normalize(vLamp));
		return max(-dot, 0.0);
	}

	if (u_LLType[lamp] == TYPE_POINT) {
		vec4 vNormal = u_MMatrix * vec4(a_Normal, 0.0);
		vec4 vLamp = (u_LLMatrix[lamp] * vec4(0.0, 0.0, 0.0, 1.0)) - (u_MMatrix * a_Position);
		float dot = dot(normalize(vNormal), normalize(vLamp));
		float cut = max(dot, 0.0);
		float distance = length(vLamp);
		float att = cut * (1.0 / (1.0 + ((1.0 - min(u_LLAttributes[lamp][ATTR_POINT_STRENGTH], 1.0)) * distance * distance)));
		return max(min(att, 1.0), 0.0);
	}

	if (u_LLType[lamp] == TYPE_SPOTLIGHT) {
		//spotlight calculations
	}

	return 0.0;
}

void main() {
	if (u_LOShadeless == 1)
		v_Color = a_Color;
	else {
		vec3 diffuse;

		for(int i = 0; i < u_LLCount && i < MAX_LAMPS; i ++)
			diffuse += u_LLColor[i] * getDiffuse(i);

		v_Color = a_Color * (vec4(u_LODiffuse * diffuse, 1.0) + vec4(u_LOAmbient, 1.0));
	}
	
	v_TexPosition = a_TexPosition;
	gl_Position = u_MVPMatrix * a_Position;
}
