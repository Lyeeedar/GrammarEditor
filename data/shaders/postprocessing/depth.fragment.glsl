#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_depth;
uniform sampler2D u_texture;
uniform float u_far;

void main() {
	vec4 colour = texture2D(u_texture, v_texCoords);
	float z = 4.0 / (u_far + 2.0 - texture2D(u_depth, v_texCoords).x * (u_far - 2.0));
    gl_FragColor.rgb = colour.rgb;
    gl_FragColor.a = z;
}