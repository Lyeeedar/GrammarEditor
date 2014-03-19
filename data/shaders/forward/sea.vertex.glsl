attribute vec3 a_position;

const float pi = 3.14159;

// Land

uniform sampler2D u_hm1;
uniform sampler2D u_hm2;
uniform sampler2D u_hm3;

uniform float u_seaFloor;

uniform vec3 u_hm_pos[3];
uniform float u_hm_height[3];
uniform float u_hm_scale[3];
uniform float u_hm_size[3];

// Sea

uniform float delta;
uniform int numWaves;
uniform float amplitude[8];
uniform float wavelength[8];
uniform float speed[8];
uniform vec2 direction[8];

uniform int u_posx;
uniform int u_posz;
uniform mat4 u_mvp;

uniform vec3 u_viewPos;
uniform float fog_max;

varying vec3 v_viewDir;
varying float v_vposLen;
varying vec3 v_pos;
varying vec3 v_normal;
varying float v_depth;

uniform mat4 u_depthBiasMVP;
varying vec4 v_shadowCoords;

float wave(int i, float x, float y) 
{
    float frequency = 2.0*pi/wavelength[i];
    float phase = speed[i] * frequency;
    float theta = dot(direction[i], vec2(x, y));
    return amplitude[i] * sin(theta * frequency + delta * phase);
}

float waveHeight(float x, float y) 
{
    float height = 0.0;
    for (int i = 0; i < numWaves; i++)
        height += wave(i, x, y);
    return height;
}

float dWavedx(int i, float x, float y) {
    float frequency = 2.0*pi/wavelength[i];
    float phase = speed[i] * frequency;
    float theta = dot(direction[i], vec2(x, y));
    float A = amplitude[i] * direction[i].x * frequency;
    return A * cos(theta * frequency + delta * phase);
}

float dWavedy(int i, float x, float y) {
    float frequency = 2.0*pi/wavelength[i];
    float phase = speed[i] * frequency;
    float theta = dot(direction[i], vec2(x, y));
    float A = amplitude[i] * direction[i].y * frequency;
    return A * cos(theta * frequency + delta * phase);
}

vec3 waveNormal(float x, float y) {
    float dx = 0.0;
    float dy = 0.0;
    for (int i = 0; i < numWaves; ++i) {
        dx += dWavedx(i, x, y);
        dy += dWavedy(i, x, y);
    }
    vec3 n = vec3(-dx, 1.0, -dy);
    return n;
}

float calculateLand(vec4 position, sampler2D u_hm, int i)
{
    vec2 movedPos = (position.xz-u_hm_pos[i].xz)/u_hm_scale[i];
    
    float height = u_seaFloor;

    if (movedPos.x > 0.0 && movedPos.y > 0.0 && movedPos.x < 1.0 && movedPos.y < 1.0) 
    {
        vec4 tmp = texture2D(u_hm, movedPos);

        height = u_seaFloor+tmp.a*u_hm_height[i];
    }
    return height;
}
            
void main() 
{
    vec4 position = vec4(a_position.x+u_posx, 0.0, a_position.z+u_posz, 1.0);

    float height = u_seaFloor;
    
    float tmp = calculateLand(position, u_hm1, 0);
    if (tmp > u_seaFloor) 
    {
        height = tmp;
    }
    tmp = calculateLand(position, u_hm2, 1);
    if (tmp > u_seaFloor) 
    {
        height = tmp;
    }
    tmp = calculateLand(position, u_hm3, 2);
    if (tmp > u_seaFloor) 
    {
        height = tmp;
    }

    vec3 viewDir = u_viewPos-position.xyz;
    v_viewDir = viewDir;
    float vposlen = length(viewDir);

    float seabed_factor = 1.0 - clamp((height-u_seaFloor)/abs(u_seaFloor), 0.0, 1.0);
    float dist_factor = 1.0 - vposlen / fog_max;

    position.y = a_position.y+waveHeight(position.x, position.z)*seabed_factor*dist_factor;
	gl_Position = u_mvp * position;

    v_pos = position.xyz;
    v_vposLen = vposlen;
    v_normal = waveNormal(position.x, position.z);
    v_depth = seabed_factor;

    v_shadowCoords = u_depthBiasMVP * position;
}