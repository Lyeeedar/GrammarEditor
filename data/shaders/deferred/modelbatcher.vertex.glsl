#version 330

in vec3 a_position;
in vec3 a_normal;

uniform InstanceBlock {
	mat4 u_mm[MAX_INSTANCES];
};

uniform mat4 u_pv;
uniform vec3 u_viewPos;

out float v_fade;

out float v_vposLen;

#ifdef USE_TRIPLANAR_SAMPLING
#else
	in vec2 a_texCoord0;
	out vec2 v_texCoords;
#endif

out vec3 v_pos;
out vec3 v_normal;

void main()
{	
	vec4 worldPos = u_mm[gl_InstanceID] * vec4(a_position, 1.0);
	gl_Position = u_pv * worldPos;

	v_fade = 1.0;

	v_pos = worldPos.xyz;
	v_normal = (transpose(inverse(u_mm[gl_InstanceID])) * vec4(a_normal, 0.0)).xyz;

	vec3 viewDir = u_viewPos-worldPos.xyz;
	v_vposLen = length(viewDir);

#ifdef USE_TRIPLANAR_SAMPLING
#else
	v_texCoords = a_texCoord0;
#endif
}