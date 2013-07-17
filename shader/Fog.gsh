#version 400 core

precision highp float;

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

in float texArrayID[];

uniform mat4 viewProj;
uniform vec3 eyePosition;

out vec3 fragmentTexCoords;
out float opacity;

void main(void)
{
	float sz = 100;
    vec3 pos = gl_in[0].gl_Position.xyz;
    vec3 toCam = normalize(eyePosition - pos.xyz);
    vec3 right = cross(toCam, vec3(0.0, 1.0, 0.0)) * sz; 

	// fade out if close to the near plane        
    float d = 1.0 - clamp(0.02 - toCam.z, 0.0, 1.0);

    pos -= right;
    gl_Position = viewProj * vec4(pos, 1.0);
    fragmentTexCoords = vec3(0, 0, texArrayID[0]);
    opacity = d;
    EmitVertex();
    
    pos.y += sz*0.2;    
    gl_Position = viewProj * vec4(pos, 1.0);
    fragmentTexCoords = vec3(0, 1, texArrayID[0]);
    opacity = d;
    EmitVertex();
        
    pos.y -= sz*0.2;
    pos += right;
    gl_Position = viewProj * vec4(pos, 1.0);
    fragmentTexCoords = vec3(1, 0, texArrayID[0]);
    opacity = d;
    EmitVertex();
        
    pos.y += sz*0.2;
    gl_Position = viewProj * vec4(pos, 1.0);
    fragmentTexCoords = vec3(1, 1, texArrayID[0]);
    opacity = d;
    EmitVertex();
    
    EndPrimitive();
}

