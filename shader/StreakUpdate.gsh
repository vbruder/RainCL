#version 400 core

precision highp float;

layout (points) in;
layout (points, max_vertices = 1) out;

// view projection matrix and eye position
uniform mat4 viewProj;
uniform vec3 eyePosition;

out vec4 positionFS;

void main(void)
{
    positionFS = normalize(gl_in[0].gl_Position);
    gl_Position = normalize(gl_in[0].gl_Position);
    EmitVertex();
    EndPrimitive();
}