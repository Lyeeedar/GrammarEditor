
attribute vec3 a_position;
attribute vec2 a_texCoord0;

uniform mat4 u_mm;
uniform mat4 u_mvp;

uniform float u_time;

uniform vec3 u_pos;

uniform float u_height;

varying vec2 v_texCoords1;
varying vec2 v_texCoords2;
varying vec2 v_texCoords3;
varying vec2 v_texCoords4;

void main()
{	
	vec3 truepos = vec3(a_position.x, 0, a_position.z);
	vec4 position = u_mm * vec4(a_position, 1.0);
	position.y = a_position.y*(u_height+u_pos.y);
	gl_Position = (u_mvp * position).xyww;

	vec3 pos = truepos+u_pos;
	v_texCoords1 = pos.xz/10000.0 + u_time/512.0;
	v_texCoords2 = pos.xz/10000.0 + u_time/256.0;
	v_texCoords3 = pos.xz/10000.0 + u_time/128.0;
	v_texCoords4 = pos.xz/10000.0 + u_time/64.0;
}