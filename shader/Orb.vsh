#version 400 core

uniform mat4 model;
uniform mat4 viewProj;

in vec3 positionMC;

void main(void) {
    gl_Position = viewProj * model * vec4(positionMC, 1);
}