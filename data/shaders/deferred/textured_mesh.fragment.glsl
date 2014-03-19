#version 330

uniform int u_texNum;

uniform sampler2D u_texture0;
uniform sampler2D u_texture1;
uniform sampler2D u_texture2;
uniform vec3 u_colour;

uniform vec3 fog_col;
uniform float fog_min;
uniform float fog_max;

in float v_vposLen;

in vec2 v_texCoords;
in vec3 v_pos;
in vec3 v_normal;

layout(location = 0) out vec4 albedoOut;
layout(location = 1) out vec4 normalOut;
layout(location = 2) out vec4 specularOut;
layout(location = 3) out vec4 emissiveOut;

void main()
{	
	vec4 specular = vec4(0.0);
	vec4 emissive = vec4(0.0);

	if (u_texNum > 1)
	{
		specular = texture2D(u_texture1, v_texCoords);
	}
	if (u_texNum > 2)
	{
		emissive = texture2D(u_texture2, v_texCoords);
	}

	float fogmin = fog_min;
	float fogmax = fog_max;

	float ypos = max(-v_pos.y, 0.0);
	float fogscl = ypos / 500.0;
	fogscl *= fogscl;
	fogscl *= 5.0;
	fogscl += 1.0;

	fogmin /= fogscl;
	fogmax /= fogscl;

	float fog_fac = (v_vposLen - fogmin) / (fogmax - fogmin);
	fog_fac = clamp (fog_fac, 0.0, 1.0);

	vec4 texCol = texture2D(u_texture0, v_texCoords);

	vec4 final_colour = vec4(u_colour, 1.0) * texCol;
	final_colour.a = 1.0;

	final_colour = clamp (final_colour, 0.0, 1.0);

	final_colour = mix(final_colour, vec4(fog_col, final_colour.a), fog_fac);

	vec3 normal = normalize(v_normal);

	albedoOut = final_colour;
	normalOut = vec4((normal+1.0) * 0.5, 1.0);
	specularOut = specular;
	emissiveOut = emissive;
}