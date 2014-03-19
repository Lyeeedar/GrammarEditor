#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform float u_threshold;
uniform float width;
uniform float height;
uniform vec3 darken;
uniform float alpha_offset;

const vec3 W = vec3(0.2125, 0.7154, 0.0721);

float intensity(vec4 color)
{
    return color.xyz * W;
}
 
vec4 sobel(float offsetx, float offsety, vec2 center)
{
    // get samples around pixel
    vec3 tleft =    texture2D(u_texture,center + vec2(-offsetx,offsety)).rgb;
    vec3 left =     texture2D(u_texture,center + vec2(-offsetx,0)).rgb;
    vec3 bleft =    texture2D(u_texture,center + vec2(-offsetx,-offsety)).rgb;
    vec3 top =      texture2D(u_texture,center + vec2(0,offsety)).rgb;
    vec3 bottom =   texture2D(u_texture,center + vec2(0,-offsety)).rgb;
    vec3 tright =   texture2D(u_texture,center + vec2(offsetx,offsety)).rgb;
    vec3 right =    texture2D(u_texture,center + vec2(offsetx,0)).rgb;
    vec3 bright =   texture2D(u_texture,center + vec2(offsetx,-offsety)).rgb;
 
    // Sobel masks (to estimate gradient)
    //        1 0 -1     -1 -2 -1
    //    X = 2 0 -2  Y = 0  0  0
    //        1 0 -1      1  2  1
 
    vec3 x = tleft + 2.0*left + bleft - tright - 2.0*right - bright;
    vec3 y = -tleft - 2.0*top - tright + bleft + 2.0 * bottom + bright;
    vec3 colour = x-y;
    float color = length(colour);//sqrt((x*x) + (y*y));
    if (color > u_threshold){return vec4(0.0, 0.0, 0.0, color+alpha_offset);}
    return vec4(0.0,0.0,0.0,0.0);
 }
 
void main(void)
{
    gl_FragColor = sobel(1.0/width, 1.0/height, v_texCoords);
}