#version 330

in vec3 positionMC;

out vec2 texCoords;
out vec3 positionFS;

uniform mat4 proj;
uniform mat4 view;
uniform float scale;

void main(void)
{
    gl_Position = proj * view * vec4(positionMC, 1.0);
    texCoords.st = (vec2(1.0) - (positionMC.xz / scale));

    positionFS = positionMC.xyz;
}