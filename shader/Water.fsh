#version 400 core

#extension GL_EXT_gpu_shader4 : enable

in vec4 cubeCoords;
in vec2 texCoords;
in vec4 positionWC;

uniform sampler2DArray rainNormalTex;
uniform sampler2DArray rainBumpTex;
uniform samplerCube skyTex;
uniform vec3 fogThickness;
uniform vec3 lightPos;
uniform float circle;
uniform float whiteFac;

out vec4 fragColor;

void main(void)
{
	//repeat texture multiple times over terrain
	vec2 texCoordsRepeat = texCoords * 16.0;
	//calculate surface color (reflection and fresnel)
	vec4 reflectionColor = texture(skyTex, cubeCoords.stp);
	vec4 surfaceColor = mix(reflectionColor, vec4(0.6), 20*fogThickness.x);
	
	//calculate bump mapping
	vec3 normal = normalize(texture2DArray(rainNormalTex, vec3(texCoordsRepeat, circle)).xyz);
	vec3 position = positionWC.xyz + (1.5 * texture2DArray(rainBumpTex, vec3(texCoordsRepeat, circle)).r * normal);

	vec3 vPosLight = (position - lightPos);

	fragColor.rgb = surfaceColor.rgb * max(dot(normalize(vPosLight), normal), 0);
	fragColor.a = cubeCoords.q;
	
	fragColor *= whiteFac;
}