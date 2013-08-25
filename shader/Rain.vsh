#version 400 core

precision highp float;

in vec4 positionMC;
in vec4 vertexSeed;
in vec4 vertexVelo;

out VertexData
{
    float texArrayID;
    vec4 velocity;
} vertex;

void main(void)
{
    vertex.texArrayID = vertexSeed.w;
    // random enlight value in w component of velocity
    vertex.velocity = vertexVelo;
    gl_Position = positionMC;
}