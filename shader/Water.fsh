#version 400 core

in vec2 texCoords;

uniform sampler2D colorTex;

out vec4 fragColor;

void main(void)
{
    fragColor = vec4(texture(colorTex, texCoords.st).rgb, 1.0);
    fragColor = (1.0,1.0,1.0,1.0);
}