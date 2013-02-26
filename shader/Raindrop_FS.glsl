#version 150 core

// point lights
#define MAX_POINT_LIGHTS 8
uniform vec3 plPosition[MAX_POINT_LIGHTS];      // positionen aller point lights
uniform vec3 plMaxIntensity[MAX_POINT_LIGHTS];  // maximale intensitaet aller point lights

// material eigenschaften
uniform float k_a, k_spec, k_dif, es;   // parameter
uniform vec3 c_a;                       // ambiente farbe
uniform sampler2D diffuseTex;           // diffuse farbe
uniform sampler2D specularTex;          // spekulare farbe

// szenenspezifische eigenschaften
uniform vec3 eyePosition;   // position der kamera

in vec3 positionWC;
in vec3 normalWC;
in vec2 fragmentTexCoords;

out vec4 finalColor;

/**
 * Berechnet die Intensitaet einer Punktlichtquelle.
 * @param p Position des beleuchteten Punktes
 * @param p_p Position der Punktlichtquelle
 * @param I_p_max Maximale Intensitaet der Lichtquelle im Punkt p_p
 * @return Intensitaet der Lichtquelle im Punkt p
 */
vec3 plIntensity(vec3 p, vec3 p_p, vec3 I_p_max)
{
    float factor = dot(p - p_p, p - p_p);
    return I_p_max / factor;
}

/**
 * Berechnet die Beleuchtungsfarbe fuer einen Punkt mit allen Lichtquellen.
 * @param pos Position des Punktes in Weltkoordinaten
 * @param normal Normel des Punktes in Weltkoordinaten
 * @param c_d Diffuse Farbe des Punktes
 * @param c_s Spekulare Farbe des Punktes
 * @return Farbe des beleuchteten Punktes
 */
vec3 calcLighting(vec3 pos, vec3 normal, vec3 c_d, vec3 c_s)
{
    vec3 color = vec3(0);
    for(int i = 0; i < MAX_POINT_LIGHTS; ++i)
    {
        vec3 lightPos = plPosition[i];
        vec3 maxI = plMaxIntensity[i];
        vec3 posToLight = normalize(lightPos - pos);
        vec3 posToEye = normalize(eyePosition - pos);
        
        vec3 diffuse = k_dif * c_d * max(dot(posToLight, normal), 0);
        vec3 reflect = normalize(reflect(-posToLight, normal));
        float spec = pow(max(dot(reflect, posToEye), 0), es);
        vec3 specular = k_spec * c_s * max(spec, 0);
        
        color += (diffuse + specular) * plIntensity(pos, lightPos, maxI);
    }
    return k_a*c_a + color;
}

void main(void)
{
    vec3 diffColor = texture(diffuseTex, fragmentTexCoords).xyz;
    vec3 specColor = texture(specularTex, fragmentTexCoords).xyz;
    vec3 color = calcLighting(positionWC, normalize(normalWC), diffColor, specColor);
    finalColor = vec4(0.5*diffColor + color,1);
}