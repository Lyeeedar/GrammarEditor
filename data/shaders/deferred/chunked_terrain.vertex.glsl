#version 330

in vec3 a_position;
in vec3 a_normal; 
in float a_texA0;
in float a_texA1;
in float a_texA2;
in float a_texA3;
in float a_texA4;
in float a_texA5;

uniform mat4 u_pv;
uniform mat4 u_mm;

uniform vec3 u_viewPos;

out float v_vposLen;

out vec3 v_texAlphas1;
out vec3 v_texAlphas2;

out vec3 v_texCoords;
out vec3 v_pos;
out vec3 v_normal;

void main()
{	
	vec4 worldPos = u_mm * vec4(a_position, 1.0);
	gl_Position = u_pv * worldPos;

	v_pos = worldPos.xyz;
	v_texCoords = worldPos.xyz;

	v_texAlphas1 = vec3(a_texA0, a_texA1, a_texA2);
	v_texAlphas2 = vec3(a_texA3, a_texA4, a_texA5);

	v_normal = ( u_mm * vec4( a_normal, 0.0 ) ).xyz;

	v_vposLen = length(u_viewPos-worldPos.xyz);
}