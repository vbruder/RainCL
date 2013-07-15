#version 400 core

precision highp float;

in vec4 positionMC;

out float texArrayID;

void main(void)
{
	
	gl_Position = positionMC;
}