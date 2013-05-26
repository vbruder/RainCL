#version 330

in vec3 positionMC;
in vec3 normalMC;

out vec2 texCoords;
out vec3 normal;
out vec3 positionFS;

uniform mat4 proj;
uniform mat4 view;
//TODO: scaling factor for texture
uniform float scale;

void main(void)
{
    gl_Position = proj * view * vec4(positionMC, 1.0);
    texCoords.st = (vec2(1.0) - (positionMC.xz / 16.0));

    normal = normalMC;
    positionFS = positionMC.xyz;
}