#version 400 core

precision highp float;

layout (points) in;
layout (triangle_strip, max_vertices = 8) out;

uniform mat4 view;
uniform mat4 proj;
uniform vec3 eyePosition;

out vec2 fragmentTexCoords;

//Billboard technique
//Based on Tristan Lorach's "Soft Particles" demo (Nvidia SDK)
void main(void)
{
	float sz = 1;
	
    vec4 pos = gl_in[0].gl_Position;
    vec4 posWC = pos;

	// first quad
    gl_Position = proj * view * posWC;
    fragmentTexCoords = vec2(1, 0);
    EmitVertex();
    
    posWC.y -= 2*sz;    
    gl_Position = proj * view * posWC;
    fragmentTexCoords = vec2(1, 1);
    EmitVertex();
        
    posWC.x += 2*sz;
    posWC.y += 2*sz;
    gl_Position = proj * view * posWC;
    fragmentTexCoords = vec2(0, 0);
    EmitVertex();
        
    posWC.y -= 2*sz;
    gl_Position = proj * view * posWC;
    fragmentTexCoords = vec2(0, 1);
    EmitVertex();
    
    EndPrimitive();
    
    // second quad
    posWC.x -= sz;
    posWC.y += 2*sz;
    posWC.z += sz;
    gl_Position = proj * view * posWC;
    fragmentTexCoords = vec2(1, 0);
    EmitVertex();
    
    posWC.y -= 2*sz;    
    gl_Position = proj * view * posWC;
    fragmentTexCoords = vec2(1, 1);
    EmitVertex();
        
    posWC.y += 2*sz;
    posWC.z -= 2*sz;
    gl_Position = proj * view * posWC;
    fragmentTexCoords = vec2(0, 0);
    EmitVertex();
        
    posWC.y -= 2*sz;
    gl_Position = proj * view * posWC;
    fragmentTexCoords = vec2(0, 1);
    EmitVertex();
    
    EndPrimitive();
}

