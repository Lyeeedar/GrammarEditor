#version 330

attribute vec3 a_position;
attribute vec3 a_normal;

uniform InstanceBlock {
	mat4 u_mm[MAX_INSTANCES];
};

uniform mat4 u_pv;
uniform vec3 u_viewPos;

varying float v_fade;

varying vec3 v_viewDir;
varying float v_vposLen;

#ifdef USE_TRIPLANAR_SAMPLING
#else
	attribute vec2 a_texCoord0;
	varying vec2 v_texCoords;
#endif

varying vec3 v_pos;
varying vec3 v_normal;

void main()
{	
	vec4 worldPos = u_mm[gl_InstanceID] * vec4(a_position, 1.0);
	gl_Position = u_pv * worldPos;

	v_fade = 1.0;//fade[gl_InstanceID];

	v_pos = worldPos.xyz;
	v_normal = (transpose(inverse(u_mm[gl_InstanceID])) * vec4(a_normal, 0.0)).xyz;

	vec3 viewDir = u_viewPos-worldPos.xyz;
	v_viewDir = viewDir;
	v_vposLen = length(viewDir);

#ifdef USE_TRIPLANAR_SAMPLING
#else
	v_texCoords = a_texCoord0;
#endif
}