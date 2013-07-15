#version 400 core

precision highp float;

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

uniform mat4 viewProj;
uniform vec3 eyePosition;

out vec2 fragmentTexCoords;

void main(void)
{
    vec4 pos = gl_in[0].gl_Position;
    vec3 toCam = normalize(eyePosition - pos);

	// fade out if close to the near plane        
    float d = 1.0 - clamp(0.1 - toCam.z, 0.0, 1.0);

	float sz = 5.0;
    vec3 v1 = float3(0,sz,0);
	vec3 v2 = float3(sz,0,0);

    pos.xyz += v2 - v1;
    gl_Position = mul(pos, viewProj);
    fragmentTexCoords = vec2(1, 0);
    EmitVertex();
    
    pos.xyz += 2.0*v1;    
    gl_Position = mul(pos, viewProj);
    fragmentTexCoords = vec2(1, 1);
    EmitVertex();
        
    pos.xyz -= 2.0*(v1 + v2);
    gl_Position = mul(pos, viewProj);
    fragmentTexCoords = vec2(0, 0);
    EmitVertex();
        
    pos.xyz += 2.0*v1;
    gl_Position = mul(pos, viewProj);
    fragmentTexCoords = vec2(0, 1);
    EmitVertex();
}

