#version 330

in vec3 positionMC;
in vec2 texCoords;

out vec2 fragmentTexCoords;

uniform mat4 model;
uniform mat4 proj;
uniform mat4 view;

void main(void)
{
    vec4 positionWC   = model * vec4(positionMC, 1.0);
    fragmentTexCoords = texCoords;
    gl_Position       = proj * view * positionWC;
}