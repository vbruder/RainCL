#version 400 core

precision highp float;

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

in VertexData
{
    float texArrayID;
    float randEnlight;
} vertex[];

uniform mat4 viewProj;
uniform vec3 eyePosition;
uniform vec3 windDir;
uniform float dt;

out vec3 fragmentTexCoords;
out float randEnlight;
out float texArrayID;

// GS for billboard technique (make two triangles from point).
// based on http://ogldev.atspace.co.uk/www/tutorial27/tutorial27.html
void main()                                                                         
{
    //streak size
    float height = 0.1;
    float width = height/20.0;
                                                                                 
    vec3 pos = gl_in[0].gl_Position.xyz;                                            
    vec3 toCamera = normalize(eyePosition - pos);                                    
    vec3 up = vec3(0.0, 1.0, 0.0);                                                  
    vec3 right = cross(toCamera, up) * width * length(eyePosition - pos);                              
                                                                                    
    //bottom left
    pos -= right;
    fragmentTexCoords.xy = vec2(0, 0);
    fragmentTexCoords.z = vertex[0].texArrayID;
    randEnlight = vertex[0].randEnlight;
    texArrayID = vertex[0].texArrayID;
    gl_Position = viewProj * vec4(pos + (windDir*dt), 1.0);
    EmitVertex();

    //top left
    pos.y += height;
    fragmentTexCoords.xy = vec2(0, 1);
    fragmentTexCoords.z = vertex[0].texArrayID;
    randEnlight = vertex[0].randEnlight;
    texArrayID = vertex[0].texArrayID;
    gl_Position = viewProj * vec4(pos, 1.0);
    EmitVertex();

    //bottom right
    pos.y -= height;
    pos += right;
    fragmentTexCoords.xy = vec2(1, 0);
    fragmentTexCoords.z = vertex[0].texArrayID;
    randEnlight = vertex[0].randEnlight;
    texArrayID = vertex[0].texArrayID;
    gl_Position = viewProj * vec4(pos + (windDir*dt), 1.0);
    EmitVertex();

    //top right
    pos.y += height;
    fragmentTexCoords.xy = vec2(1, 1);
    fragmentTexCoords.z = vertex[0].texArrayID;
    randEnlight = vertex[0].randEnlight;
    texArrayID = vertex[0].texArrayID;
    gl_Position = viewProj * vec4(pos, 1.0);
    EmitVertex();
                                                                                    
    EndPrimitive();                                                                 
}