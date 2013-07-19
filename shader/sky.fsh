#version 400 core

in vec2 fragmentTexCoords;

out vec4 fragColor;

uniform sampler2D textureImage;
uniform vec3 fogThickness;

void main(void)
{
     vec4 skyColor = texture(textureImage, fragmentTexCoords);
     fragColor = mix(skyColor, vec4(0.3), 10*fogThickness.x);
}