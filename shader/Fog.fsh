#version 400 core

#extension GL_EXT_gpu_shader4 : enable

in vec3 fragmentTexCoords;

uniform sampler2DArray fogTex;

out vec4 fragColor;

void main(void)
{
	fragColor = vec4( vec3(texture2DArray(fogTex, fragmentTexCoords).r), 1.0);
}