#version 400 core

in vec3 fragmentTexCoords;

out vec4 fragColor;

uniform samplerCube cubeMap;
uniform vec3 fogThickness;

void main(void)
{
     vec4 skyColor = texture( cubeMap, fragmentTexCoords);
     fragColor = mix(skyColor, vec4(0.6), 20*fogThickness.x);
}