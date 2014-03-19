attribute vec3 a_position;
attribute vec3 a_normal; 
attribute vec2 a_texCoord0;

#ifdef boneWeight0Flag
attribute vec2 a_boneWeight0;
#endif //boneWeight0Flag

#ifdef boneWeight1Flag
attribute vec2 a_boneWeight1;
#endif //boneWeight1Flag

#ifdef boneWeight2Flag
attribute vec2 a_boneWeight2;
#endif //boneWeight2Flag

#ifdef boneWeight3Flag
attribute vec2 a_boneWeight3;
#endif //boneWeight3Flag

#ifdef boneWeight4Flag
attribute vec2 a_boneWeight4;
#endif //boneWeight4Flag

#ifdef boneWeight5Flag
attribute vec2 a_boneWeight5;
#endif //boneWeight5Flag

#ifdef boneWeight6Flag
attribute vec2 a_boneWeight6;
#endif //boneWeight6Flag

#ifdef boneWeight7Flag
attribute vec2 a_boneWeight7;
#endif //boneWeight7Flag

#ifdef skinning
uniform mat4 u_bones[numBones];
#endif

uniform mat4 u_pv;
uniform mat4 u_mm;

uniform vec3 u_viewPos;

varying vec3 v_viewDir;
varying float v_vposLen;

varying vec2 v_texCoords;
varying vec3 v_pos;
varying vec3 v_normal;

uniform mat4 u_depthBiasMVP;
varying vec4 v_shadowCoords;

void main() {

	#ifdef skinning
	mat4 skinningMat = mat4(0.0);
	#ifdef boneWeight0Flag
		skinningMat += (a_boneWeight0.y) * u_bones[int(a_boneWeight0.x)];
	#endif //boneWeight0Flag
	#ifdef boneWeight1Flag				
		skinningMat += (a_boneWeight1.y) * u_bones[int(a_boneWeight1.x)];
	#endif //boneWeight1Flag
	#ifdef boneWeight2Flag		
		skinningMat += (a_boneWeight2.y) * u_bones[int(a_boneWeight2.x)];
	#endif //boneWeight2Flag
	#ifdef boneWeight3Flag
		skinningMat += (a_boneWeight3.y) * u_bones[int(a_boneWeight3.x)];
	#endif //boneWeight3Flag
	#ifdef boneWeight4Flag
		skinningMat += (a_boneWeight4.y) * u_bones[int(a_boneWeight4.x)];
	#endif //boneWeight4Flag
	#ifdef boneWeight5Flag
		skinningMat += (a_boneWeight5.y) * u_bones[int(a_boneWeight5.x)];
	#endif //boneWeight5Flag
	#ifdef boneWeight6Flag
		skinningMat += (a_boneWeight6.y) * u_bones[int(a_boneWeight6.x)];
	#endif //boneWeight6Flag
	#ifdef boneWeight7Flag
		skinningMat += (a_boneWeight7.y) * u_bones[int(a_boneWeight7.x)];
	#endif //boneWeight7Flag

	vec4 worldPos = u_mm * skinningMat * vec4(a_position, 1.0);
	v_normal = ( u_mm * skinningMat * vec4( a_normal, 0.0 ) ).xyz;
	#else
	vec4 worldPos = u_mm * vec4(a_position, 1.0);
	v_normal = ( u_mm * vec4( a_normal, 0.0 ) ).xyz;
	#endif
	gl_Position = u_pv * worldPos;

	v_pos = worldPos.xyz;
	v_texCoords = a_texCoord0;

	vec3 viewDir = u_viewPos-worldPos.xyz;
	v_viewDir = viewDir;
	v_vposLen = length(viewDir);

	v_shadowCoords = u_depthBiasMVP * vec4(worldPos.xyz, 1.0);
}
