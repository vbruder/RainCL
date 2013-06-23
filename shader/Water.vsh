#version 400 core

in vec4 positionMC;

uniform mat4 viewProj;

out vec2 texCoords;

void main(void)
{
	texCoords.st = positionMC.xz;
    gl_Position = viewProj * positionMC;
}