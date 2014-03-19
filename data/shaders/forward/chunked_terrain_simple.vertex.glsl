
attribute vec3 a_position;

uniform mat4 u_pv;
uniform mat4 u_mm;

void main()
{	
	vec4 worldPos = u_mm * vec4(a_position, 1.0);
	gl_Position = u_pv * worldPos;
}