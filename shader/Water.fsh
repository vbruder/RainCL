#version 400 core

in vec2 texCoords;

uniform sampler2D colorTex;
uniform vec4 color;

out vec4 fragColor;

void main(void)
{
    fragColor = vec4(texture(colorTex, texCoords.st).rgb, 0.3);
    //fragColor = color;
}