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
    coords = positionMC;
    normal = normalMC;
    positionFS = positionMC.xyz;
/*
    gl_Position = vec4(positionMC, 0.0, 1.0);

    texCoord = vec2(0.5, 0.5) + 0.5 * positionMC;
    texCoord.y = 1.0 - texCoord.y;
*/
}