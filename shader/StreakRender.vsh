#version 330 core

in vec3 positionMC;
in vec3 normalMC;

uniform mat4 viewProj;

out vec3 normal;
out vec3 positionFS;
out vec3 coords;

void main(void)
{
    gl_Position = viewProj * vec4(positionMC, 1.0);
    normal = normalMC;
    coords = positionMC + vec3(0.5, 0, 0.5);
    positionFS = positionMC.xyz;
}