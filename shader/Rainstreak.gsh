#version 330 core

precision highp float;

// generate two triangles with 4 vertices (2 shared in triangle strip)
// out of each point
layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

// view projection matrix and eye position
uniform mat4 viewProj;
uniform vec3 eyePosition;

void main(void)
{
    vec3 worldPos = normalize(gl_in[0].gl_Position.xyz);
    vec3 velVec = vec3(1, -20.0, 1);
    
    //size of rain streaks
    float height = 1.0/20.0;
    float width = height/10.0;
    
    velVec = normalize(velVec);
    vec3 eyeVec = normalize(eyePosition - worldPos);
    vec3 eyeOnVelVecPlane = eyePosition - ((dot(eyeVec, velVec)) * velVec);
    vec3 projectedEyeVec = eyeOnVelVecPlane - worldPos;
    vec3 sideVec = normalize(cross(projectedEyeVec, velVec));

    // create two vertices (triangle strip) out of point position
    vec4 outPos[4];
    outPos[0] = vec4(worldPos - (sideVec * 0.5*width), 1.0);
    outPos[1] = outPos[0] + vec4((sideVec * width),  1.0);
    outPos[2] = outPos[0] + vec4((velVec  * height), 1.0);
    outPos[3] = outPos[2] + vec4((sideVec * width),  1.0);

    gl_Position = viewProj * outPos[0];
    EmitVertex();
    gl_Position = viewProj * outPos[1];
    EmitVertex();
    gl_Position = viewProj * outPos[2];
    EmitVertex();
    gl_Position = viewProj * outPos[3];
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
