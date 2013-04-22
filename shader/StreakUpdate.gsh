#version 400 core

precision highp float;

layout (points) in;
layout (points, max_vertices = 1) out;

in vec4 pos0[];
in vec3 seed0[];
in vec3 velo0[];
in float rand0[];
in float type0[];

// view projection matrix and eye position
//uniform mat4 viewProj;
//uniform vec3 eyePosition;

out vec4 position;
out vec3 seed;
out vec3 velo;
out float rand;
out float type;

void main(void)
{
    position = vec4(1.0);//normalize(gl_in[0].gl_Position) + vec4(velo0[0], 1.0);
    seed = seed0[0];
    velo = velo0[0];
    rand = rand0[0];
    type = type0[0];

    gl_Position = vec4(1.0);//position;
    EmitVertex();
    EndPrimitive();
}