#version 400 core

precision highp float;

in vec4 positionMC;
in vec4 vertexSeed;
in vec4 vertexVelo;

out VertexData
{
    float texArrayID;
    float randEnlight;
    vec3 positionWC;
    vec3 velocity;
} vertex;

void main(void)
{
    vertex.texArrayID = vertexSeed.w;
    vertex.randEnlight = vertexVelo.w;
    vec3 positionWC = positionMC.xyz;
    vec3 velocity	= vertexVelo.xyz;
    gl_Position = positionMC;
}