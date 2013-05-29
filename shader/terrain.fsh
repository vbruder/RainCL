#version 330

in vec2 texCoords;
in vec3 positionFS;

out vec4 fragColor;

uniform sampler2D normalTex;
uniform sampler2D lightTex;
uniform sampler2D specularTex;
uniform sampler2D colorTex;

uniform float sunIntensity, k_diff, k_spec, k_ambi;
uniform vec3 sunDir;
uniform vec3 eyePosition;

vec3 calcLighting(vec3 normal, vec3 diff, vec3 spec, vec3 ambi)
{
    vec3 d;
    vec3 s;
    vec3 a;
    vec3 ref;
    vec3 v;
    vec3 erg = vec3(0, 0, 0);

    //diffuse
    d = sunIntensity * k_diff * diff * max(dot(-normalize(positionFS - sunDir), normal), 0.0);
        
    //specular
    ref = reflect(normalize(positionFS - sunDir), normal);
    v = normalize(eyePosition - positionFS);
    s = k_spec * spec * max(dot(ref, v), 0.0);
        
    erg += (d + s);

    //ambient
    a = k_ambi * ambi;
    erg += a;
    
    return erg;
}

void main(void)
{
    vec4 normal = normalize(texture(normalTex, texCoords.st));
    vec3 diff   = texture(colorTex, texCoords.st).rgb;
    vec3 spec   = texture(specularTex, texCoords.st).rgb;
    vec3 ambi   = texture(lightTex, texCoords.st).rgb;

    fragColor = vec4(calcLighting(normal.xyz, diff, spec, ambi), 1.0);
    //fragColor = vec4(vec3(k_diff), 1.0);
}