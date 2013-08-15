#version 400 core

in vec4 positionMC;

uniform mat4 viewProj;
uniform float scale;
uniform sampler2D normalTex;
uniform vec3 eyePosition;

out vec4 texCoords;

void main(void)
{
	//normal in world space
    vec4 normal = normalize(texture(normalTex, positionMC.xz / scale).xzyw);
	//normal = normalize(-1 + 2 * normal);
	
	vec3 positionView = positionMC.xyz - eyePosition;
	
	vec3 coords = reflect(positionView, normal.xyz);
	coords.y = (coords.y + 30.0);	
	texCoords.xyz = coords.xyz;
	
	positionView = normalize(positionView);
	
	vec3 refracted = refract(positionView, normal.xyz, 1.33);
	
	vec3 viewPos = normalize(eyePosition - positionMC.xyz);
	texCoords.w = 1 - dot(viewPos, vec3(0,1,0));
	

	gl_Position = viewProj * positionMC;
}