#ifdef GL_ES
	precision mediump float;
#endif

uniform vec3 u_al_col;
uniform vec3 u_pl_pos[4];
uniform vec3 u_pl_col[4];
uniform float u_pl_att[4];

uniform int u_texNum;

uniform sampler2D u_texture0;
uniform sampler2D u_texture1;
uniform sampler2D u_texture2;

uniform vec3 fog_col;
uniform float fog_min;
uniform float fog_max;

varying float v_fade;

varying vec3 v_viewDir;
varying float v_vposLen;

varying vec2 v_texCoords;
varying vec3 v_pos;
varying vec3 v_normal;

vec3 calculateLight(vec3 l_vector, vec3 n_dir, float l_attenuation, vec3 l_col, float shininess, vec3 s_col, vec3 v_dir)
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
		vec3 spec_light = l_col * s_col * attenuation * pow( max( 0.0, dot( reflect( -l_dir, n_dir ), v_dir ) ), shininess);
		light += spec_light;
	}

	light = clamp(light, 0.0, 1.0);
 
	return light;
}


void main()
{	
	float shininess = 0.0;
	vec3 s_col = vec3(1.0);
	vec3 emissive = vec3(0.0);
	vec3 normal = normalize(v_normal);
	vec3 v_dir = normalize(v_viewDir);

	if (u_texNum > 1)
	{
		vec4 col = texture2D(u_texture1, v_texCoords);
		shininess = col.a;
		s_col = col.rgb;
	}
	if (u_texNum > 2)
	{
		vec4 col = texture2D(u_texture2, v_texCoords);
		emissive = col.rgb * col.a;
	}

	vec3 light = u_al_col;

	for ( int i = 0; i < 4; i++ ) 
	{
		vec3 light_model = u_pl_pos[i] - v_pos;
		light += calculateLight(light_model, normal, u_pl_att[i], u_pl_col[i], shininess, s_col, v_dir);
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

	vec4 texCol = texture2D(u_texture0, v_texCoords);

#ifdef HAS_TRANSPARENT
	texCol.a *= v_fade;
	if (texCol.a == 0.0) discard;
#else
	texCol.a = 1.0;
#endif

	vec4 final_colour = texCol * vec4(light, 1.0);

	final_colour.rgb += emissive;
	final_colour = clamp(final_colour, 0.0, 1.0);

	gl_FragColor = mix(vec4(final_colour.xyz, texCol.a), vec4(fog_col, texCol.a), fog_fac);
}