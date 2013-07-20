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
<<<<<<< HEAD
	float sz = 2000;
	vec3 v1 = vec3(0, sz, 0);
	vec3 v2 = vec3(sz, 0, 0);
	
    vec4 pos = gl_in[0].gl_Position;
    vec4 posWC = view * pos;
    
=======
	float sz = 100;
    vec3 pos = gl_in[0].gl_Position.xyz;
>>>>>>> 37a0894132cf0d04fd58c3699773e444f674c8c4
    vec3 toCam = normalize(eyePosition - pos.xyz);
    vec3 right = cross(toCam, vec3(0.0, 1.0, 0.0)) * sz; 
	
	//fade out if close to the near plane        
    float d = 1.0 - clamp(0.1 + toCam.z, 0.0, 1.0);
	opacity = d;

<<<<<<< HEAD
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
=======
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
>>>>>>> 37a0894132cf0d04fd58c3699773e444f674c8c4
    EmitVertex();
    
    EndPrimitive();
}

