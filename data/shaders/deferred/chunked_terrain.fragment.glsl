#version 330
#extension GL_EXT_texture_array : enable

uniform sampler2D u_noise0;
uniform sampler2D u_noise1;

uniform sampler2DArray u_texture0;
uniform sampler2DArray u_texture1;
uniform sampler2DArray u_texture2;
uniform sampler2DArray u_texture3;
uniform sampler2DArray u_texture4;
uniform sampler2DArray u_texture5;

uniform vec3 u_colour;

uniform float u_triplanarScaling;

// uniform int u_texture0Layers;
// uniform int u_texture1Layers;
// uniform int u_texture2Layers;
// uniform int u_texture3Layers;
// uniform int u_texture4Layers;
// uniform int u_texture5Layers;

uniform vec3 fog_col;
uniform float fog_min;
uniform float fog_max;

uniform mat4 u_pv;

in vec3 v_viewDir;
in float v_vposLen;

in vec3 v_texAlphas1;
in vec3 v_texAlphas2;

in vec3 v_texCoords;
in vec3 v_pos;
in vec3 v_normal;

layout(location = 0) out vec4 albedoOut;
layout(location = 1) out vec4 normalOut;
layout(location = 2) out vec4 specularOut;
layout(location = 3) out vec4 emissiveOut;

vec4 triplanarSample(sampler2D texture, vec3 texcoords, vec3 normal)
{
	vec4 colour = vec4(0.0);
	colour += texture2D(texture, texcoords.zy/u_triplanarScaling) * abs(normal.x);
	colour += texture2D(texture, texcoords.xz/u_triplanarScaling) * abs(normal.y);
	colour += texture2D(texture, texcoords.xy/u_triplanarScaling) * abs(normal.z);
	return colour;
}

vec4 triplanarSampleArray(sampler2DArray texture, vec4 texcoords, vec3 normal)
{
	vec4 colour = vec4(0.0);
	colour += texture2DArray(texture, vec3(texcoords.zy/u_triplanarScaling, texcoords.w)) * abs(normal.x);
	colour += texture2DArray(texture, vec3(texcoords.xz/u_triplanarScaling, texcoords.w)) * abs(normal.y);
	colour += texture2DArray(texture, vec3(texcoords.xy/u_triplanarScaling, texcoords.w)) * abs(normal.z);
	return colour;
}

vec4 noiseSample(sampler2DArray texture, vec3 texcoords, vec3 normal, float noiseval)
{
	vec4 val0 = triplanarSampleArray(texture, vec4(texcoords, 0.0), normal) * noiseval ;
	vec4 val1 = triplanarSampleArray(texture, vec4(texcoords, 1.0), normal) * (1.0 - noiseval) ;

	return val0 + val1;
}

vec4 alphaBlend(vec4 srcCol, vec4 dstCol, float srcAlpha)
{
	return srcCol * srcAlpha + dstCol * (1.0 - srcAlpha);
}

void main()
{	
	vec4 specular = vec4(0.0);
	vec4 emissive = vec4(0.0);

	vec3 normal = normalize(v_normal);

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

	float noiseval = triplanarSample(u_noise0, v_texCoords/50.0, normal) * 0.75 + triplanarSample(u_noise1, v_texCoords/50.0, normal) * 0.25;

	vec4 texCol = vec4(0.0);
	if (v_texAlphas1.x > 0.0) texCol = alphaBlend(noiseSample(u_texture0, v_texCoords, normal, noiseval), texCol, v_texAlphas1.x);
	if (v_texAlphas1.y > 0.0) texCol = alphaBlend(noiseSample(u_texture1, v_texCoords, normal, noiseval), texCol, v_texAlphas1.y);
	if (v_texAlphas1.z > 0.0) texCol = alphaBlend(noiseSample(u_texture2, v_texCoords, normal, noiseval), texCol, v_texAlphas1.z);
	if (v_texAlphas2.x > 0.0) texCol = alphaBlend(noiseSample(u_texture3, v_texCoords, normal, noiseval), texCol, v_texAlphas2.x);
	if (v_texAlphas2.y > 0.0) texCol = alphaBlend(noiseSample(u_texture4, v_texCoords, normal, noiseval), texCol, v_texAlphas2.y);
	if (v_texAlphas2.z > 0.0) texCol = alphaBlend(noiseSample(u_texture5, v_texCoords, normal, noiseval), texCol, v_texAlphas2.z);

	vec4 final_colour = vec4(u_colour, 1.0) * texCol;
	final_colour.a = 1.0;

	final_colour = clamp (final_colour, 0.0, 1.0);

	final_colour = mix(final_colour, vec4(fog_col, final_colour.a), fog_fac);

	albedoOut = final_colour;
	normalOut = vec4((normal+1.0) * 0.5, 1.0);
	specularOut = specular;
	emissiveOut = emissive;
}