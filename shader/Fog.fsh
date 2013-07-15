#version 400 core

#extension GL_EXT_gpu_shader4 : enable

in vec2 fragmentTexCoords;

uniform sampler2DArray fogTex;
uniform uint texArrayID;

out vec4 fragColor;

void main(void)
{
	fragColor = texture2DArray(fogTex, texArrayID);
}