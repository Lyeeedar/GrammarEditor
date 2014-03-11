#ifdef GL_ES
	precision mediump float;
#endif

uniform vec4 u_colour;

varying vec3 v_viewDir;
varying float v_vposLen;

varying vec2 v_texCoords;
varying vec3 v_pos;
varying vec3 v_normal;

void main()
{	
	gl_FragColor = u_colour;
}