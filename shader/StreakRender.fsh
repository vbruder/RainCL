#version 400 core

//in vec4 positionFS;
//in vec3 normal;
in vec3 fragmentTexCoords;
in float randEnlight;

uniform sampler2DArray rainTex;

out vec4 finalColor;

void main(void)
{
//    vec3 diffColor = texture(diffuseTex, fragmentTexCoords).xyz;
//    vec3 specColor = texture(specularTex, fragmentTexCoords).xyz;
//    vec3 color = calcLighting(positionWC, normalize(normalWC), diffColor, specColor);
//    finalColor = vec4(0.5*diffColor + color, 1.0);
//    finalColor = vec4(0.8, 0.8, 0.8, 1.0);

    finalColor = texture2DArray(rainTex, fragmentTexCoords.xyz);

    //DEBUG ONLY
//    finalColor = vec4(fragmentTexCoords.z/10.0, fragmentTexCoords.z/10.0, fragmentTexCoords.z/10.0, 1);
}