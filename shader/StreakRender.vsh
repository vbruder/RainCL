#version 400 core

in vec4 positionMC;
//in vec3 normalMC;

uniform mat4 viewProj;

//out vec3 normal;
//out vec4 positionFS;
//out vec3 coords;

void main(void)
{
    gl_Position = positionMC;

//    gl_Position = viewProj * positionMC;// + vec4(0, 0.1, 0,1.0);
//    normal = normalMC;
//    coords = positionMC + vec4(0.5, 0, 0.5, 0);
//    positionFS = positionMC;
}