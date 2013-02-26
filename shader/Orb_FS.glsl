#version 150 core

uniform vec3 color;

out vec4 finalColor;

void main(void) {
    finalColor = vec4(color, 1);
}