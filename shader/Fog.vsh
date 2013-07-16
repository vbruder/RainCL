#version 400 core

precision highp float;

in vec4 positionMC;

out float texArrayID;

void main(void)
{
	texArrayID  = positionMC.w;
	gl_Position = vec4(positionMC.xyz, 1.0);
}