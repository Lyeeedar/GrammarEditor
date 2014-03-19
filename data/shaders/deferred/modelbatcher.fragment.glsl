#version 330

uniform int u_texNum;

uniform sampler2D u_texture0;
uniform sampler2D u_texture1;
uniform sampler2D u_texture2;

uniform vec3 fog_col;
uniform float fog_min;
uniform float fog_max;

uniform mat4 u_pv;

in float v_fade;

in float v_vposLen;

in vec3 v_pos;
in vec3 v_normal;

layout(location = 0) out vec4 albedoOut;
layout(location = 1) out vec4 normalOut;
layout(location = 2) out vec4 specularOut;
layout(location = 3) out vec4 emissiveOut;

#ifdef USE_TRIPLANAR_SAMPLING
	uniform float u_triplanarScaling;
	vec4 triplanarSample(sampler2D texture, vec3 texcoords, vec3 normal)
	{
		vec4 colour = vec4(0.0);
		colour += texture2D(texture, texcoords.zy/u_triplanarScaling) * abs(normal.x);
		colour += texture2D(texture, texcoords.xz/u_triplanarScaling) * abs(normal.y);
		colour += texture2D(texture, texcoords.xy/u_triplanarScaling) * abs(normal.z);
		return colour;
	}
#else
	in vec2 v_texCoords;
#endif

void main()
{	
	vec4 specular = vec4(0.0);
	vec4 emissive = vec4(0.0);
	vec3 normal = normalize(v_normal);

	if (u_texNum > 1)
	{
	#ifdef USE_TRIPLANAR_SAMPLING
		specular = triplanarSample(u_texture1, v_pos, normal);
	#else
		specular = texture2D(u_texture1, v_texCoords);
	#endif
	}
	if (u_texNum > 2)
	{
	#ifdef USE_TRIPLANAR_SAMPLING
		emissive = triplanarSample(u_texture2, v_pos, normal);
	#else
		emissive = texture2D(u_texture2, v_texCoords);
	#endif
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

#ifdef USE_TRIPLANAR_SAMPLING
	vec4 texCol = triplanarSample(u_texture0, v_pos, normal);
#else
	vec4 texCol = texture2D(u_texture0, v_texCoords);
#endif

#ifdef HAS_TRANSPARENT
	texCol.a *= v_fade;
	if (texCol.a == 0.0) discard;
#else
	texCol.a = 1.0;
#endif

	vec4 final_colour = texCol;

	final_colour = clamp(final_colour, 0.0, 1.0);

	final_colour = mix(vec4(final_colour.xyz, texCol.a), vec4(fog_col, texCol.a), fog_fac);

	albedoOut = final_colour;
	normalOut = vec4((normal+1.0) * 0.5, 1.0);
	specularOut = specular;
	emissiveOut = emissive;
}