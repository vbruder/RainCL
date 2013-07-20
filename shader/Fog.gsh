#version 400 core

precision highp float;

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

in float texArrayID[];

uniform mat4 view;
uniform mat4 proj;
uniform vec3 eyePosition;

out vec3 fragmentTexCoords;
out float opacity;

//Billboard technique
//Based on Tristan Lorach's "Soft Particles" demo (Nvidia SDK)
void main(void)
{
	float sz = 2000;
	vec3 v1 = vec3(0, sz, 0);
	vec3 v2 = vec3(sz, 0, 0);
	
    vec4 pos = gl_in[0].gl_Position;
    vec4 posWC = view * pos;
    
    vec3 toCam = normalize(eyePosition - pos.xyz);
    vec3 right = cross(toCam, vec3(0.0, 1.0, 0.0)) * sz; 
	
	//fade out if close to the near plane        
    float d = 1.0 - clamp(0.1 + toCam.z, 0.0, 1.0);
	opacity = d;

    posWC.xyz += v2 - v1;
    gl_Position = proj * posWC;
    fragmentTexCoords = vec3(1, 0, texArrayID[0]);
    EmitVertex();
    
    posWC.xyz += 2.0*v1;    
    gl_Position = proj * posWC;
    fragmentTexCoords = vec3(1, 1, texArrayID[0]);
    EmitVertex();
        
    posWC.xyz -= 2.0*(v1 + v2);
    gl_Position = proj * posWC;
    fragmentTexCoords = vec3(0, 0, texArrayID[0]);
    EmitVertex();
        
    posWC.xyz += 2.0*v1;
    gl_Position = proj * posWC;
    fragmentTexCoords = vec3(0, 1, texArrayID[0]);
    EmitVertex();
    
    EndPrimitive();
}

