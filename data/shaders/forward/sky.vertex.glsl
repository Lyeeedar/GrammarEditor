attribute vec3 a_position;
					
uniform mat4 u_mvp;
		
varying vec3 v_pos;
			
void main() {
	vec4 position = u_mvp * vec4(a_position, 1.0);
	gl_Position = position.xyww;
	v_pos = a_position;
}