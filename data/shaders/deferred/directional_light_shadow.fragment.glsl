#version 330

in vec2 v_texCoords;
in vec4 v_color;

uniform sampler2D u_depth;
uniform sampler2D u_normal;
uniform sampler2D u_specular;
uniform sampler2D u_texture;
uniform sampler2D u_shadowMap;

uniform mat4 u_invProj;

uniform vec3 u_dir;
uniform vec3 u_viewPos;

out vec4 fragColor;

uniform mat4 u_depthBiasMVP;
uniform vec2 u_poisson_scale;
const vec2 poissonDisk[16] = vec2[](
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

vec3 reconstructPos(vec2 texcoords)
{
    vec2 ndc = texcoords * 2.0 - 1.0;
    float depth = texture(u_depth, texcoords).r;
    depth = depth * 2.0 - 1.0;

    vec4 pos = vec4(ndc, depth, 1.0);

    pos = u_invProj * pos;
    pos.xyz /= pos.w;

    return pos.xyz;
}

vec3 calculateLight(vec3 l_dir, vec3 n_dir, vec3 l_col, vec3 vDir, vec3 s_col, float shininess)
{
    float NdotL = dot( n_dir, l_dir );

	vec3 light = l_col * NdotL;

	if (NdotL > 0.0 && shininess > 0.0)
	{
		vec3 spec_light = l_col * s_col * pow( max( 0.0, dot( reflect( -l_dir, n_dir ), vDir ) ), shininess);
		light += spec_light;
	}

	light = clamp(light, 0.0, 1.0);
 
	return light;
}

void main()
{
	vec3 pos = reconstructPos(v_texCoords);
	vec3 vDir = u_viewPos-pos;

	vec3 normal = texture(u_normal, v_texCoords).xyz * 2.0 - 1.0;
	normal = normalize(normal);
	vec4 specular = texture(u_specular, v_texCoords);

	vec3 light = calculateLight(u_dir, normal, v_color.rgb, vDir, specular.rgb, specular.a);

	vec4 v_shadowCoords = u_depthBiasMVP * vec4(pos, 1.0);

	float hitVisibility = 0.8 / 16.0;
	float visibility = 1.0;
	for (int i = 0; i < 16; i++)
	{
	  	if (texture(u_shadowMap, v_shadowCoords.xy + poissonDisk[i]*u_poisson_scale ).z  <  v_shadowCoords.z )
	  	{
	    	visibility -= hitVisibility;
	  	}
	}

	//visibility = 0.8 * texture(u_shadowMap, v_shadowCoords.str);

	vec4 texCol = texture(u_texture, v_texCoords);

	fragColor.rgb = light * texCol.rgb * visibility;
	fragColor.a = 1.0;
}