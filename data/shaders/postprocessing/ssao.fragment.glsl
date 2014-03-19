uniform sampler2D u_texture;
uniform sampler2D bgl_RenderedTexture;
uniform float bgl_RenderedTextureWidth;
uniform float bgl_RenderedTextureHeight;

#define PI    3.14159265

float width = bgl_RenderedTextureWidth; //texture width
float height = bgl_RenderedTextureHeight; //texture height

//--------------------------------------------------------
//a list of user parameters

float near = 0.3; //Z-near
float far = 40.0; //Z-far

int samples = 3; //samples on the first ring (3 - 5)
int rings = 3; //ring count (3 - 5)

float radius = 1.0; //ao radius

float diffarea = 0.5; //self-shadowing reduction
float gdisplace = 0.4; //gauss bell center

float lumInfluence = 0.8; //how much luminance affects occlusion

bool noise = false; //use noise instead of pattern for sample dithering?

//--------------------------------------------------------

varying vec4 v_color;
varying vec2 v_texCoords;

vec2 texCoord = v_texCoords.st;

vec2 rand(in vec2 coord) //generating noise/pattern texture for dithering
{
  float noiseX = ((fract(1.0-coord.s*(width/2.0))*0.25)+(fract(coord.t*(height/2.0))*0.75))*2.0-1.0;
  float noiseY = ((fract(1.0-coord.s*(width/2.0))*0.75)+(fract(coord.t*(height/2.0))*0.25))*2.0-1.0;
  
  if (noise)
  {
      noiseX = clamp(fract(sin(dot(coord ,vec2(12.9898,78.233))) * 43758.5453),0.0,1.0)*2.0-1.0;
      noiseY = clamp(fract(sin(dot(coord ,vec2(12.9898,78.233)*2.0)) * 43758.5453),0.0,1.0)*2.0-1.0;
  }
  return vec2(noiseX,noiseY)*0.001;
}

float readDepth(in vec2 coord) 
{
  if (v_texCoords.x<0.0||v_texCoords.y<0.0) return 1.0;
  return (2.0 * near) / (far + near - texture2D(u_texture, coord ).x * (far-near));
}

float compareDepths(in float depth1, in float depth2,inout int far)
{   
  float garea = 2.0; //gauss bell width    
  float diff = (depth1 - depth2)*100.0; //depth difference (0-100)
  //reduce left bell width to avoid self-shadowing 
  if (diff<gdisplace)
  {
  garea = diffarea;
  }else{
  far = 1;
  }
  
  float gauss = pow(2.7182,-2.0*(diff-gdisplace)*(diff-gdisplace)/(garea*garea));
  return gauss;
}  

float calAO(float depth,float dw, float dh)
{   
  float dd = (1.0-depth)*radius;

  float temp = 0.0;
  float temp2 = 0.0;
  float coordw = v_texCoords.x + dw*dd;
  float coordh = v_texCoords.y + dh*dd;
  float coordw2 = v_texCoords.x - dw*dd;
  float coordh2 = v_texCoords.y - dh*dd;
  
  vec2 coord = vec2(coordw , coordh);
  vec2 coord2 = vec2(coordw2, coordh2);
  
  int far = 0;
  temp = compareDepths(depth, readDepth(coord),far);
  //DEPTH EXTRAPOLATION:
  if (far > 0)
  {
    temp2 = compareDepths(readDepth(coord2),depth,far);
    temp += (1.0-temp)*temp2;
  }
  
  return temp;
} 

void main(void)
{
  vec2 noise = rand(texCoord); 
  float depth = readDepth(texCoord);
  float d;
  
  float w = (1.0 / width)/clamp(depth,0.25,1.0)+(noise.x*(1.0-noise.x));
  float h = (1.0 / height)/clamp(depth,0.25,1.0)+(noise.y*(1.0-noise.y));
  
  float pw;
  float ph;
  
  float ao;
  float dl = PI*(3.0-sqrt(5.0));
  float dz = 1.0/float(samples);
  float l = 0.0;
  float z = 1.0 - dz/2.0;
  
  for (int i = 0; i <= samples; i ++)
  {     
    float r = sqrt(1.0-z);
    
    pw = cos(l)*r;
    ph = sin(l)*r;
    ao += calAO(depth,pw*w,ph*h);        
    z = z - dz;
    l = l + dl;
  }
  
  ao /= float(samples);
  ao = 1.0-ao;  
  

  vec3 color = texture2D(bgl_RenderedTexture,texCoord).rgb;
  
  vec3 lumcoeff = vec3(0.299,0.587,0.114);
  float lum = dot(color.rgb, lumcoeff);
  vec3 luminance = vec3(lum, lum, lum);
  
  gl_FragColor = vec4(vec3(mix(vec3(ao),vec3(1.0),luminance*lumInfluence)),1.0); //ambient occlusion only
}