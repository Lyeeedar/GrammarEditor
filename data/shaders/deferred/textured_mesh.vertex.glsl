#version 330

in vec3 a_position;
in vec3 a_normal; 
in vec2 a_texCoord0;

uniform mat4 u_pv;
uniform mat4 u_mm;

uniform vec3 u_viewPos;

out float v_vposLen;

out vec2 v_texCoords;
out vec3 v_pos;
out vec3 v_normal;

void main()
{	
	vec4 worldPos = u_mm * vec4(a_position, 1.0);
	gl_Position = u_pv * worldPos;

	v_pos = worldPos.xyz;
	v_texCoords = a_texCoord0;
	v_normal = ( u_mm * vec4( a_normal, 0.0 ) ).xyz;

	v_vposLen = length(u_viewPos-worldPos.xyz);
}