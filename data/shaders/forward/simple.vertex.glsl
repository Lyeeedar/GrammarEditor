attribute vec3 a_position;
attribute vec3 a_colour;

uniform mat4 u_pv;

varying vec3 u_colour;

void main()
{	
	gl_Position = u_pv * vec4(a_position, 1.0);
	u_colour = a_colour;
}