#version 400 core

in vec4 positionMC;

uniform mat4 viewProj;
uniform float scale;
uniform sampler2D normalTex;
uniform vec3 eyePosition;

out vec4 cubeCoords;
out vec2 texCoords;
out vec4 positionWC;

void main(void)
{
	texCoords = positionMC.xz / scale;
	//normal in world space
    vec4 normal = normalize(texture(normalTex, positionMC.xz / scale).xzyw);
	//normal = normalize(-1 + 2 * normal);
	
	vec3 positionView = positionMC.xyz - eyePosition;
	
	vec3 coords = reflect(positionView, normal.xyz);
	coords.y = (coords.y + 30.0);	
	cubeCoords.stp = coords;
	
	//vec3 refracted = refract(normalize(positionView, normal.xyz, 1.33);
	
	vec3 viewPos = normalize(eyePosition - positionMC.xyz);
	
	cubeCoords.q = 1.0 - dot(viewPos, vec3(0, normal.y, 0));
	
	positionWC = positionMC;
	gl_Position = viewProj * positionMC;
}