#version 300

in vec2 fragmentTexCoords;

out vec4 fragColor;

uniform sampler2D textureImage;

void main(void)
{
    fragColor = texture(textureImage, fragmentTexCoords);
}