#version 400 core

in vec2 texCoords;
in vec3 positionFS;

out vec4 color;
out vec4 normal;
out vec4 position;

uniform sampler2D normalTex;
uniform sampler2D lightTex;
uniform sampler2D specularTex;
uniform sampler2D colorTex;

uniform float sunIntensity, k_diff, k_spec, k_ambi, es;
uniform vec3 sunDir;
uniform vec3 eyePosition;
uniform vec3 fogThickness;

vec3 calcLighting(vec3 normal, vec3 diff, vec3 spec, vec3 ambi)
{
	vec3 viewVec = positionFS - eyePosition;
	float lenView = length(viewVec);
	vec3 view = normalize(viewVec);
	vec3 expDir = exp2(-fogThickness * lenView * 3.0);
	float wetSurface = clamp(k_spec/2.0*clamp(normal.y, 0.0, 1.0), 0.0, 1.0);
	vec3 reflVec = reflect(view, normal);
	
	vec3 lightDir = sunDir - positionFS;
    vec3 lightDirNorm = normalize(lightDir);
    vec3 sDir = normalize(sunDir - eyePosition);
    float cosGammaDir = dot(sDir, view);
    float dirLighting = k_diff * sunIntensity * clamp(dot(normal, lightDirNorm), 0.0, 1.0);
    //diffuse
    vec3 diffDirLight = dirLighting*expDir;        
    //ambient
	vec3 ambiFactor = vec3(0.96/pow(1 - cosGammaDir*0.2, 2.0)) *4;
    vec3 ambiDirLight = ambiFactor * sunIntensity * vec3(1 - expDir);
    //specular
    vec3 specDirLight = clamp(pow(dot(-lightDirNorm, reflVec), 20.0), 0.0, 1.0) * sunIntensity * k_spec * expDir; 

	return vec3(ambiDirLight.xyz + diff*(diffDirLight.xyz) + spec*specDirLight);
}

void main(void)
{
    vec4 normal = normalize(texture(normalTex, texCoords.st).xzyw);
    normal = normalize(-1 + 2 * normal);
    
    vec3 diff   = texture(colorTex, texCoords.st).rgb;
    vec3 spec   = texture(specularTex, texCoords.st).rgb;
    vec3 ambi   = texture(lightTex, texCoords.st).rgb;

    color = vec4(calcLighting(normal.xyz, diff, spec, ambi), 1.0);
    //color = normal;
}