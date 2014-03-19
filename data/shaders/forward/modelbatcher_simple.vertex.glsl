#version 330

attribute vec3 a_position;

uniform InstanceBlock {
	mat4 u_mm[MAX_INSTANCES];
};

uniform mat4 u_pv;

void main()
{	
	vec4 worldPos = u_mm[gl_InstanceID] * vec4(a_position, 1.0);
	gl_Position = u_pv * worldPos;
}