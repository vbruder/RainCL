#version 400

precision highp float;

in vec4 positionMC;
in vec3 seed;
in vec3 velo;
in float rand;
in float type;

out vec4 pos0;
out vec3 seed0;
out vec3 velo0;
out float rand0;
out float type0;

//simple pass-through for geometry shader
void main(void)
{
    pos0  = vec4(1);//positionMC;
    seed0 = seed;
    velo0 = velo;
    rand0 = rand;
    type0 = type;

    gl_Position = vec4(1);//positionMC;
}