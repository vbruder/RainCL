#version 400 core

in vec4 positionMC;

uniform mat4 viewProj;
uniform mat4 view;
uniform mat4 inverseView;
uniform float scale;
uniform sampler2D normalTex;
uniform vec3 eyePosition;

out vec3 texCoords;
//out vec3 positionWC;

void main(void)
{
    vec4 normal = normalize(texture(normalTex, positionMC.xz / scale).xzyw);
	normal = normalize(-1 + 2 * normal);
	
	vec3 positionView = normalize(positionMC.xyz - eyePosition);
	
	vec4 coords = vec4(reflect(positionView, normal.xyz), 1.0);	
	texCoords = normalize(view * -coords).xyz;
	
	gl_Position = viewProj * positionMC;

//	texCoords.st = positionMC.xz;
//	positionWC = positionMC.xyz;
//    gl_Position = viewProj * positionMC;
}