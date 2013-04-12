#version 330

uniform mat4 proj;
uniform mat4 view;

in vec3 positionMC;
in vec3 normalMC;

out vec3 coords;
out vec3 normal;
out vec3 positionFS;

void main(void)
{
    gl_Position = proj * view * vec4(positionMC, 1.0);
    coords = positionMC + vec3(0.5, 0, 0.5);
    normal = normalMC;
    positionFS = positionMC.xyz;
}