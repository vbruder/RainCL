#version 400 core

in vec4 positionMC;

out vec2 texCoords;
out vec3 positionFS;

uniform mat4 proj;
uniform mat4 view;
uniform float scale;

void main(void)
{
    gl_Position = proj * view * positionMC;
    texCoords.st = positionMC.xz / scale;

    positionFS = positionMC.xyz;
}