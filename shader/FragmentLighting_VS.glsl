#version 150 core

uniform mat4 viewProj;
uniform mat4 model;
uniform mat4 modelIT;

in vec3 positionMC;
in vec3 normalMC;
in vec2 vertexTexCoords;

out vec3 positionWC;
out vec3 normalWC;
out vec2 fragmentTexCoords;

void main(void)
{
	vec4 world = model * vec4(positionMC, 1.0);
	positionWC = world.xyz / world.w;
	normalWC = (modelIT * vec4(normalMC, 0.0)).xyz;
	fragmentTexCoords = vertexTexCoords;
	gl_Position = viewProj * world;
}