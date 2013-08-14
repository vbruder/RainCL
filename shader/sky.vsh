#version 400 core

in vec3 positionMC;
in vec3 texCoords;

out vec3 fragmentTexCoords;

uniform mat4 model;
uniform mat4 proj;
uniform mat4 view;

void main(void)
{
    vec4 positionWC   = model * vec4(positionMC, 1.0);
    fragmentTexCoords = positionMC;
    gl_Position       = proj * view * positionWC;
}