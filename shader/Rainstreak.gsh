#version 330 core

precision highp float;

// generate two triangles with 4 vertices (2 shared in triangle strip)
// out of each point
layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

// model view projection matrix
uniform mat4 viewProj;

void main(void)
{
    // transform to world coords
    vec4 pos = gl_in[0].gl_Position * viewProj;
    vec4 trans1 = vec4( 0.00, -0.03, 0, 1) * viewProj;
    vec4 trans2 = vec4( 0.01,  0.00, 0, 1) * viewProj;
    vec4 trans3 = vec4( 0.01, -0.03, 0, 1) * viewProj;
    
    // create two vertices (triangle strip) out of point position
    gl_Position = pos;
    EmitVertex();
    gl_Position = pos + trans1;
    EmitVertex();
    gl_Position = pos + trans2;
    EmitVertex();
    gl_Position = pos + trans3;
    EmitVertex();
    EndPrimitive();
}


// void GenRainSpriteVertices(float3 worldPos, float3 velVec, float3 eyePos, out float3 outPos[4])
//{
//    float height = g_SpriteSize/2.0;
//    float width = height/10.0;

//    velVec = normalize(velVec);
//    float3 eyeVec = eyePos - worldPos;
//    float3 eyeOnVelVecPlane = eyePos - ((dot(eyeVec, velVec)) * velVec);
//    float3 projectedEyeVec = eyeOnVelVecPlane - worldPos;
//    float3 sideVec = normalize(cross(projectedEyeVec, velVec));
    
//    outPos[0] = worldPos - (sideVec * 0.5*width);
//    outPos[1] = outPos[0] + (sideVec * width);
//    outPos[2] = outPos[0] + (velVec * height);
//    outPos[3] = outPos[2] + (sideVec * width );
//}
