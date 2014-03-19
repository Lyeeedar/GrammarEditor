#version 330

in vec2 v_texCoords;
in vec4 v_color;

uniform sampler2D u_depth;
uniform sampler2D u_normal;
uniform sampler2D u_specular;
uniform sampler2D u_texture;

uniform mat4 u_invProj;

uniform vec3 u_dir;
uniform vec3 u_viewPos;

out vec4 fragColor;

vec3 reconstructPos(vec2 texcoords)
{
    vec2 ndc = texcoords * 2.0 - 1.0;
    float depth = texture2D(u_depth, texcoords).r;
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

	vec3 normal = texture2D(u_normal, v_texCoords).xyz * 2.0 - 1.0;
	normal = normalize(normal);
	vec4 specular = texture2D(u_specular, v_texCoords);

	vec3 light = calculateLight(u_dir, normal, v_color.xyz, vDir, specular.rgb, specular.a);

	vec4 texCol = texture2D(u_texture, v_texCoords);

	fragColor.xyz = light * texCol.rgb;
	fragColor.a = 1.0;
}