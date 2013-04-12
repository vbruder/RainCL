#version 150 core

//uniform mat4 viewProj;

in vec3 positionMC;
in vec3 normalMC;
in vec2 vertexTexCoords;
//in vec4 instancedData;

//out vec3 positionWC;
out Vertex
{
    vec3 normalWC;
    vec2 fragmentTexCoords;
} vertex;

void main(void)
{
    vertex.fragmentTexCoords = vertexTexCoords;
    vertex.normalWC = normalMC;
    gl_Position = vec4(positionMC, 1);

//    positionWC = (positionMC * instancedData.w + instancedData.xyz);
//    normalWC = normalMC; //(model * vec4(normalMC,1)).xyz;
//    fragmentTexCoords = vertexTexCoords;
//    gl_Position = viewProj * vec4(positionWC, 1);
}