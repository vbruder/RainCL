#version 400 core

in vec4 positionMC;

uniform mat4 viewProj;

void main(void)
{
    gl_Position = viewProj * positionMC;
}