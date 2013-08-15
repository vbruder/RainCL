#version 400 core

#define PI 3.14159265358979

in vec4 texCoords;

uniform sampler2D colorTex;
uniform samplerCube skyTex;
uniform vec3 fogThickness;

out vec4 fragColor;

void main(void)
{
	vec4 skyColor = texture(skyTex, texCoords.xyz);
	fragColor = mix(skyColor, vec4(0.7), 11*fogThickness.x);
	fragColor.w = texCoords.w;
}