#version 330

uniform sampler2D image;

in vec2 texCoord;

out vec4 fragColor;

void main(void)
{
    fragColor = texture(image, texCoord);
}