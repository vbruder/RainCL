#version 400 core

#define PI 3.14159265358979

in vec2 texCoords;
in vec3 positionFS;

uniform sampler2D colorTex;
uniform sampler2D normalTex;
uniform sampler2D skyTex;
uniform vec4 color;
uniform vec3 eyePosition;

out vec4 fragColor;

void main(void)
{
    vec4 normal = normalize(texture(normalTex, texCoords).xzyw);
	normal = normalize(-1 + 2 * normal);
	
	vec3 viewvec = normalize(positionFS - eyePosition);
	vec3 reflected = reflect(viewvec, normal.xyz);
 
	vec2 sphereCoords;
	
	float theta = atan(reflected.x, reflected.z);
	float phi = acos(reflected.y / length(reflected));
	sphereCoords.s = theta / (PI);
	sphereCoords.t = phi / (PI) /4;
 
	fragColor = vec4(texture(skyTex, sphereCoords).xyz, 1.0);


    //fragColor = vec4(texture(colorTex, texCoords.st).rgb, 0.3);
    //fragColor = vec4(texture(skyTex, texCoords).xyz, 1.0);
    //fragColor = vec4(sphereCoords, 0, 1);
}