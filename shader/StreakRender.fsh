#version 330 core

in vec3 positionFS;
in vec3 normal;
in vec2 coords;

out vec4 finalColor;

void main(void)
{
//    vec3 diffColor = texture(diffuseTex, fragmentTexCoords).xyz;
//    vec3 specColor = texture(specularTex, fragmentTexCoords).xyz;
//    vec3 color = calcLighting(positionWC, normalize(normalWC), diffColor, specColor);
//    finalColor = vec4(0.5*diffColor + color, 1.0);
	finalColor = vec4(0,1,0,1);
}