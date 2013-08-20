#version 400 core

#extension GL_EXT_gpu_shader4 : enable

#define PI 3.14159265
#define MAX_POINT_LIGHTS 2

//in vec4 positionFS;
//in vec3 normal;
in vec3 fragmentTexCoords;
in float randEnlight;
in float texArrayID;
in vec3 particlePosition;
in vec3 particleVelocity;

uniform sampler2DArray rainTex;
uniform sampler1D rainfactors;
uniform vec3 eyePosition;

//lighting parameter
uniform vec3 sunDir;
uniform vec3 sunColor;
uniform float sunIntensity;

//TODO: point lights
//uniform vec3 pointLightDir;
//uniform vec3 pointLightColor;
//uniform float pointLightIntensity;

//uniform vec3 pointLightDir[MAX_POINT_LIGHTS];
//uniform vec3 pointLightIntensity[MAX_POINT_LIGHTS];

out vec4 finalColor;

/**
 * Calculate Rain response to light source.
 * Based on Sarah Tariq's "Rain" demo
 * @see http://developer.download.nvidia.com/SDK/10/direct3d/Source/rain/doc/RainSDKWhitePaper.pdf
 */
vec4 rainResponse(vec3 lightVec, vec3 lightColor, float lightIntensity, bool fallOffFactor)
{
    float opacity = 0.0;
    float fallOff = 1.0;

    if (fallOffFactor)
    {  
        float distToLight = length(lightVec);
        fallOff = 1.0/(distToLight*distToLight);
        fallOff = clamp(fallOff, 0.0, 1.0); 
    }

    if ((fallOff > 0.01) && (lightIntensity > 0.01))
    {
        int maxVIDX = 4;
        int maxHIDX = 8;

        // Inputs: lightDir, eyePosition, dropDir
        lightVec = normalize(particlePosition - lightVec);
        vec3 eyeVec = normalize(particlePosition - eyePosition);
        // float dist = distance(eyePosition, particlePosition);

        vec3 dropDir  = normalize(particleVelocity);
        
        bool is_EpLp_angle_ccw = true;
        float hangle = 0;
        // angle between light vactor and drop direction
        float vangle = acos(dot(lightVec, dropDir)) / PI; // 0 to 1
        // light vector projection
        vec3 lightProj = normalize(lightVec - dot(lightVec, dropDir)*dropDir);
        // eye vector projection
        vec3 eyeProj = normalize(eyeVec - dot(eyeVec, dropDir)*dropDir);
        hangle = acos(dot(eyeProj, lightProj)) / PI; // 0 to 1

        //hangle = (hangle-10)/20.0;                      // -0.5 to 8.5
        //vangle = (vangle-10.0)/20.0; // -0.5 to 4.5
        is_EpLp_angle_ccw = dot(dropDir, cross(eyeProj, lightProj)) > 0;
        
        if (vangle * PI / 180.0 >= 88.0)
        {
            hangle = 0;
            is_EpLp_angle_ccw = true;
        }
        
        // Outputs:
        // verticalLightIndex[1|2] - two indices in the vertical direction
        // t - fraction at which the vangle is between these two indices (for mix)
        int verticalLightIndex1 = int(floor(vangle * 5)); // 0 to 5
        int verticalLightIndex2 = int(min(maxVIDX, (verticalLightIndex1 + 1))); // 4 to 6
        float t = fract(vangle); //0 to 0.999..

        // textureCoordsH[1|2] used in case we need to flip the texture horizontally
        float textureCoordsH1 = fragmentTexCoords.x;
        float textureCoordsH2 = fragmentTexCoords.x;
        
        // horizontalLightIndex[1|2] - two indices in the horizontal direction
        // s - fraction at which the hangle is between these two indices (for mix)
        int horizontalLightIndex1 = int(floor(hangle * 8)); // 0 to 8
        int horizontalLightIndex2 = horizontalLightIndex1 + 1; // 1 to 9
        float s = fract(hangle); //0 to 0.999..
                       
        if (is_EpLp_angle_ccw)
        {
            if (horizontalLightIndex2 > maxHIDX) 
            {
                horizontalLightIndex2 = maxHIDX;
                textureCoordsH2 = 1.0 - textureCoordsH2;
            }
        } else
        {
            textureCoordsH1 = 1.0 - textureCoordsH1;
            if (horizontalLightIndex2 > maxHIDX) 
            {
                horizontalLightIndex2 = maxHIDX;
            } else 
            {
                textureCoordsH2 = 1.0 - textureCoordsH2;
            }
        }     
        if (verticalLightIndex1 >= maxVIDX)
        {
            textureCoordsH2 = fragmentTexCoords.x;
            horizontalLightIndex1 = 0;
            horizontalLightIndex2 = 0;
            //s = 0;
        }
        
        // Generate final texture coordinates for each sample
        int type = int(texArrayID); // 0 to 7
        ivec2 texIndicesV1 = ivec2( (verticalLightIndex1*90 + horizontalLightIndex1*10 + type), 
                                    (verticalLightIndex1*90 + horizontalLightIndex2*10 + type));
        vec3 tex1 = vec3(textureCoordsH1, fragmentTexCoords.y, texIndicesV1.x);
        vec3 tex2 = vec3(textureCoordsH2, fragmentTexCoords.y, texIndicesV1.y);
        if ((verticalLightIndex1 < maxVIDX) && (verticalLightIndex2 >= maxVIDX)) 
        {
            //s = 0;
            horizontalLightIndex1 = 0;
            horizontalLightIndex2 = 0;
            textureCoordsH1 = fragmentTexCoords.x;
            textureCoordsH2 = fragmentTexCoords.x;
        }
        
        ivec2 texIndicesV2 = ivec2( (verticalLightIndex2*90 + horizontalLightIndex1*10 + type),
                                    (verticalLightIndex2*90 + horizontalLightIndex2*10 + type));
        vec3 tex3 = vec3(textureCoordsH1, fragmentTexCoords.y, texIndicesV2.x);        
        vec3 tex4 = vec3(textureCoordsH2, fragmentTexCoords.y, texIndicesV2.y);

        // Sample opacity from textures
        float col1 = texture2DArray(rainTex, tex1).r * texelFetch(rainfactors, texIndicesV1.x, 0).r;
        float col2 = texture2DArray(rainTex, tex2).r * texelFetch(rainfactors, texIndicesV1.y, 0).r;
        float col3 = texture2DArray(rainTex, tex3).r * texelFetch(rainfactors, texIndicesV2.x, 0).r;
        float col4 = texture2DArray(rainTex, tex4).r * texelFetch(rainfactors, texIndicesV2.y, 0).r;

        // Compute interpolated opacity using the s and t factors
        float hOpacity1 = mix(col1, col2, s*2);
        float hOpacity2 = mix(col3, col4, s*2);
        opacity = mix(hOpacity1, hOpacity2, t);
        // inverse gamma correction (expand dynamic range)
        //opacity = pow(opacity, 0.7);    
        opacity *= 3.0 * lightIntensity * fallOff;// * max(dist, 0.5);
    	//return vec4(t, 0, 0, opacity);

    }
	return vec4(lightColor, opacity);         
}

void main(void)
{

    //sun (directional) lighting
    vec4 sunLight = rainResponse(sunDir, sunColor, 1.0, false);//sunIntensity * 10 * randEnlight, false);

    //TODO: point lighting
    vec4 pointLight = vec4(0,0,0,0); 
/*
    vec3 lightDir = normalize(pointLightDir);
    float angleToSpotLight = dot(-lightDir, vec3(0.0, -1.0, 0.0));
    float cosSpotlightAngle = 0.8;

    if(angleToSpotLight > cosSpotlightAngle)
        pointLight = rainResponse(pointLightDir, pointLightColor, 2*pointLightIntensity*randEnlight, true);
*/      
    float totalOpacity = pointLight.a + sunLight.a;

    finalColor = vec4(vec3(pointLight.rgb*pointLight.a/totalOpacity + sunLight.rgb*sunLight.a/totalOpacity), totalOpacity);

    //DEBUG ONLY
//    finalColor = vec4(fragmentTexCoords.z/10.0, fragmentTexCoords.z/10.0, fragmentTexCoords.z/10.0, 1);
//    finalColor = vec4(texture2DArray(rainTex, fragmentTexCoords.xyz).r, texture2DArray(rainTex, fragmentTexCoords.xyz).r, texture2DArray(rainTex, fragmentTexCoords.xyz).r, 0.0 );
//    finalColor = pointLight;
}