#ifdef GL_ES
	precision mediump float;
#endif

uniform vec3 u_ambient;

uniform float CloudCover;
uniform float CloudSharpness;

uniform sampler2D u_texture1;
uniform sampler2D u_texture2;
uniform sampler2D u_texture3;
uniform sampler2D u_texture4;

varying vec2 v_texCoords1;
varying vec2 v_texCoords2;
varying vec2 v_texCoords3;
varying vec2 v_texCoords4;

float CloudExpCurve(float v)
{
    float c = v - CloudCover;
    c = max(c, 0.0);
 	
 	c *= 10.0;

    float CloudDensity = 1.0 - pow(CloudSharpness, c);

    return min(CloudDensity*2.0, 1.0);
}

void main()
{	
	vec4 col = 
		texture2D(u_texture4, v_texCoords1)*0.5 +
		texture2D(u_texture3, v_texCoords2)*0.25 +
		texture2D(u_texture2, v_texCoords3)*0.125 +
		texture2D(u_texture1, v_texCoords4)*0.0625;

		col.a = CloudExpCurve(col.a);


	gl_FragColor = col*vec4(u_ambient, 1.0f);
}