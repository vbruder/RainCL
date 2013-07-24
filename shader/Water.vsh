#version 400 core

in vec4 positionMC;

uniform mat4 viewProj;
uniform float scale;

out vec2 texCoords;

void main(void)
{
	texCoords.st = positionMC.xz / scale;
    gl_Position = viewProj * positionMC;
}