attribute vec3 a_position;

uniform sampler2D u_hm1;
uniform sampler2D u_hm2;
uniform sampler2D u_hm3;

uniform float u_seaFloor;

uniform vec3 u_hm_pos[3];
uniform float u_hm_height[3];
uniform float u_hm_scale[3];
uniform float u_hm_size[3];

uniform int u_step;

uniform int u_posx;
uniform int u_posz;
uniform mat4 u_mvp;

uniform vec3 u_viewPos;

varying vec3 v_viewDir;
varying float v_vposLen;
varying vec3 v_pos;
varying vec3 v_normal;
varying vec3 v_splat_opacities;

uniform mat4 u_depthBiasMVP;
varying vec4 v_shadowCoords;

vec3 calculateNormal(vec3 v1, vec3 v2, vec3 v3)
{
    vec3 U = v2-v1;
    vec3 V = v3-v1;

    return cross(U, V);
}

float calculateLand(vec4 position, sampler2D u_hm, int i)
{
    vec2 movedPos = (position.xz-u_hm_pos[i].xz)/u_hm_scale[i];
    
    float height = u_seaFloor;
    float offset = u_step / u_hm_scale[i];

    if (movedPos.x > 0.0 && movedPos.y > 0.0 && movedPos.x < 1.0 && movedPos.y < 1.0) 
    {
        vec4 tmp = texture2D(u_hm, movedPos);

        height = u_seaFloor+tmp.a*u_hm_height[i];

        vec2 pos;
        pos = movedPos+vec2(0.0, offset);
        vec3 up = vec3(pos.x*u_hm_scale[i], texture2D(u_hm, pos).a*u_hm_height[i], pos.y*u_hm_scale[i]);

        pos = movedPos+vec2(0.0, -offset);
        vec3 down = vec3(pos.x*u_hm_scale[i], texture2D(u_hm, pos).a*u_hm_height[i], pos.y*u_hm_scale[i]);

        pos = movedPos+vec2(-offset, 0.0);
        vec3 left = vec3(pos.x*u_hm_scale[i], texture2D(u_hm, pos).a*u_hm_height[i], pos.y*u_hm_scale[i]);

        pos = movedPos+vec2(offset, 0.0);
        vec3 right = vec3(pos.x*u_hm_scale[i], texture2D(u_hm, pos).a*u_hm_height[i], pos.y*u_hm_scale[i]);

        vec3 opos = vec3(movedPos.x*u_hm_scale[i], tmp.a*u_hm_height[i], movedPos.y*u_hm_scale[i]);

        vec3 normal = calculateNormal(opos, up, right);
        normal += calculateNormal(opos, right, down);
        normal += calculateNormal(opos, left, up);
        normal += calculateNormal(opos, down, left);

        normal /= 4.0;

        v_normal = normal;
        v_splat_opacities = tmp.rgb;
    }
    return height;
}
            
void main() 
{
    vec4 position = vec4(a_position.x+u_posx, 0.0, a_position.z+u_posz, 1.0);

    float height = u_seaFloor;
    v_splat_opacities = vec3(0.0, 0.0, 0.0);
    v_normal = vec3(0.0, 1.0, 0.0);
    
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

    position.y = a_position.y+(height);
    gl_Position = u_mvp * position;

    v_pos = position.xyz;
    vec3 viewDir = u_viewPos-position.xyz;
    v_viewDir = viewDir;
    v_vposLen = length(viewDir);

    v_shadowCoords = u_depthBiasMVP * position;
}