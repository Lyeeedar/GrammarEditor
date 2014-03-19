#version 330

in vec2 v_texCoords;
in vec4 v_color;

uniform sampler2D u_texture;
uniform sampler2D u_normal;
uniform sampler2D u_depth;

uniform mat4 u_invProj;

uniform float distanceThreshold;
uniform vec2 filterRadius;
 
const int sample_count = 16;
const vec2 poisson16[] = vec2[](    // These are the Poisson Disk Samples
                                vec2( -0.94201624,  -0.39906216 ),
                                vec2(  0.94558609,  -0.76890725 ),
                                vec2( -0.094184101, -0.92938870 ),
                                vec2(  0.34495938,   0.29387760 ),
                                vec2( -0.91588581,   0.45771432 ),
                                vec2( -0.81544232,  -0.87912464 ),
                                vec2( -0.38277543,   0.27676845 ),
                                vec2(  0.97484398,   0.75648379 ),
                                vec2(  0.44323325,  -0.97511554 ),
                                vec2(  0.53742981,  -0.47373420 ),
                                vec2( -0.26496911,  -0.41893023 ),
                                vec2(  0.79197514,   0.19090188 ),
                                vec2( -0.24188840,   0.99706507 ),
                                vec2( -0.81409955,   0.91437590 ),
                                vec2(  0.19984126,   0.78641367 ),
                                vec2(  0.14383161,  -0.14100790 )
                               );

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
 
void main()
{
    vec3 viewPos = reconstructPos(v_texCoords);
    vec3 normal = texture(u_normal, v_texCoords).xyz * 2.0 - 1.0;
    normal = normalize(normal);
 
    float ambientOcclusion = 0;
    // perform AO
    for (int i = 0; i < sample_count; ++i)
    {
        // sample at an offset specified by the current Poisson-Disk sample and scale it by a radius (has to be in Texture-Space)
        vec2 sampleTexCoord = v_texCoords + (poisson16[i] * (filterRadius));
        vec3 samplePos = reconstructPos(sampleTexCoord);
        vec3 sampleDir = normalize(samplePos - viewPos);
 
        // angle between SURFACE-NORMAL and SAMPLE-DIRECTION (vector from SURFACE-POSITION to SAMPLE-POSITION)
        float NdotS = max(dot(normal, sampleDir), 0);
        // distance between SURFACE-POSITION and SAMPLE-POSITION
        float VPdistSP = distance(viewPos, samplePos);
 
        // a = distance function
        float a = 1.0 - smoothstep(distanceThreshold, distanceThreshold * 2, VPdistSP);
        // b = dot-Product
        float b = NdotS;
 
        ambientOcclusion += (a * b);
    }
 
    ambientOcclusion = 1.0 - (ambientOcclusion / sample_count);

    vec4 texCol = texture2D(u_texture, v_texCoords) * v_color;

    fragColor = texCol * ambientOcclusion;
}