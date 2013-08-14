#version 400 core

#define PI 3.14159265358979

in vec3 texCoords;
// in vec3 positionWC;

uniform sampler2D colorTex;
uniform sampler2D normalTex;
uniform samplerCube skyTex;
uniform vec4 color;
uniform vec3 eyePosition;
uniform vec3 fogThickness;

out vec4 fragColor;

void main(void)
{
    //vec4 normal = normalize(texture(normalTex, texCoords).xzyw);
	//normal = normalize(-1 + 2 * normal);
	
	//vec3 viewvec = normalize(positionWC - eyePosition);
	//vec3 reflected = reflect(viewvec, normal.xyz);
 
	vec4 skyColor = texture(skyTex, texCoords);
	fragColor = mix(skyColor, vec4(0.7), 11*fogThickness.x);

	//fragColor = vec4(texCoords, 1.0);

    //fragColor = vec4(texture(colorTex, texCoords.st).rgb, 0.3);
    //fragColor = vec4(texture(skyTex, texCoords).xyz, 1.0);
    //fragColor = vec4(sphereCoords, 0, 1);
}