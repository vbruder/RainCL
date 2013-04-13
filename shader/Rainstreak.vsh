#version 150 core

in vec3 positionMC;

//simple passthrough for geometry shader
void main(void)
{
    gl_Position = vec4(positionMC, 1);
}