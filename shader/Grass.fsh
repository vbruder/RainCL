#version 400 core

#extension GL_EXT_gpu_shader4 : enable

in vec2 fragmentTexCoords;

uniform sampler2D grassTex;
uniform sampler2D grassAlphaTex;

out vec4 fragColor;

void main(void)
{
	float opacity = texture2D(grassAlphaTex, fragmentTexCoords).r;
	vec4 grassColor = vec4( vec3(texture2D(grassTex, fragmentTexCoords)), opacity);
	fragColor = grassColor;
}