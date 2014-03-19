#version 330
#extension GL_EXT_texture_array : enable

uniform vec3 u_al_col;
uniform vec3 u_pl_pos[4];
uniform vec3 u_pl_col[4];
uniform float u_pl_att[4];

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

varying vec3 v_viewDir;
varying float v_vposLen;

varying vec3 v_texAlphas1;
varying vec3 v_texAlphas2;

varying vec3 v_texCoords;
varying vec3 v_pos;
varying vec3 v_normal;

varying vec4 v_shadowCoords;
uniform sampler2D u_shadowMapTexture;
uniform float u_poisson_scale;
vec2 poissonDisk[16] = vec2[](
vec2( -0.94201624, -0.39906216 ),
vec2( 0.94558609, -0.76890725 ),
vec2( -0.094184101, -0.92938870 ),
vec2( 0.34495938, 0.29387760 ),
vec2( -0.91588581, 0.45771432 ),
vec2( -0.81544232, -0.87912464 ),
vec2( -0.38277543, 0.27676845 ),
vec2( 0.97484398, 0.75648379 ),
vec2( 0.44323325, -0.97511554 ),
vec2( 0.53742981, -0.47373420 ),
vec2( -0.26496911, -0.41893023 ),
vec2( 0.79197514, 0.19090188 ),
vec2( -0.24188840, 0.99706507 ),
vec2( -0.81409955, 0.91437590 ),
vec2( 0.19984126, 0.78641367 ),
vec2( 0.14383161, -0.14100790 )
);

vec3 calculateLight(vec3 l_vector, vec3 n_dir, float l_attenuation, vec3 l_col, float shininess, vec3 s_col)
{
    float distance = length(l_vector);
    vec3 l_dir = l_vector / distance;

    float NdotL = dot( n_dir, l_dir );

    float attenuation = 1.0;
	if (l_attenuation != 0.0)
	{
    	attenuation = 1.0 / (l_attenuation*distance);
	}

	vec3 light = l_col * NdotL * attenuation;

	if (NdotL > 0.0 && shininess > 0.0)
	{
		vec3 spec_light = l_col * s_col * attenuation * pow( max( 0.0, dot( reflect( -l_dir, n_dir ), v_viewDir ) ), shininess);
		light += spec_light;
	}

	light = clamp(light, 0.0, 1.0);
 
	return light;
}

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
	float shininess = 0.0;
	vec3 s_col = vec3(1.0);
	vec3 emissive = vec3(0.0);
	vec3 normal = normalize(v_normal);

	vec3 light = u_al_col;

	float hitVisibility = 0.8 / 16.0;
	float visibility = 1.0;
	for (int i=0;i<16;i++){
	  	if ( texture2D( u_shadowMapTexture, v_shadowCoords.xy + poissonDisk[i]/u_poisson_scale ).z  <  v_shadowCoords.z ){
	    	visibility -= hitVisibility;
	  }
	}

	for ( int i = 0; i < 4; i++ ) 
	{
		vec3 light_model = u_pl_pos[i] - v_pos;
		light += calculateLight(light_model, normal, u_pl_att[i], u_pl_col[i], shininess, s_col) * visibility;
		visibility = 1.0;
	}

	light = clamp(light, 0.0, 1.0);

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

	vec4 final_colour = vec4(u_colour, 1.0) * texCol * vec4(light, 1.0);
	final_colour.a = 1.0;

	final_colour.rgb += emissive;
	final_colour = clamp (final_colour, 0.0, 1.0);

	gl_FragColor = mix(final_colour, vec4(fog_col, final_colour.a), fog_fac);
}