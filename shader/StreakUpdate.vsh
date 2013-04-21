#version 400
in vec4 positionMC;

//simple passthrough for geometry shader
void main(void)
{
    gl_Position = positionMC;
}