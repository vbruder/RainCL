#version 400 core

#extension GL_EXT_gpu_shader4 : enable

in vec3 fragmentTexCoords;
in float opacity;

uniform sampler2DArray fogTex;

out vec4 fragColor;

void main(void)
{
	vec4 fogColor = vec4( vec3(texture2DArray(fogTex, fragmentTexCoords).r), opacity);
	fragColor = mix(fogColor, vec4(0.0), 0.9);
}