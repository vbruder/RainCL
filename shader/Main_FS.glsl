#version 150

uniform sampler2D normalTexture;
uniform sampler2D specularTexture;
uniform sampler2D textureImage;
uniform sampler2D bumpTexture;

in vec4 positionWC;
in vec4 normalWC;
in vec4 tangentWC;
in vec2 fragmentTexCoords;
in float depth;
in vec4 shadowCoordWC;

out vec4 position;
out vec4 normal;
out vec4 color;
out vec3 spec;
out vec3 skyColor;
out vec4 shadowCoord;
out vec3 bumpColor;

void main(void)
{
    vec3 normalAbs = vec3(0);
    if (length(normalWC.xyz) != 0)
    {
    	normalAbs = normalize(vec3(normalWC));
    }
   
    float depth2 = depth;
    if (depth2 > 1.0)
    {
		depth2 = 1.0;
	}
   
    vec3 tangent   = normalize(vec3(tangentWC));
    vec3 binormal  = cross(tangent,normalAbs);
    vec3 mapNormal = 2*texture(normalTexture, fragmentTexCoords).rgb-vec3(1);
   
    //depth information in w component of normal
    normal = vec4(  mapNormal.z * normalAbs 
   				  + mapNormal.y * binormal 
				  + mapNormal.x * tangent , (depth2));
		 
    color = texture(textureImage, fragmentTexCoords);
    position = positionWC;
    spec = texture(specularTexture, fragmentTexCoords).rgb ;
    skyColor = length(normalAbs) * vec3(1);
    shadowCoord = shadowCoordWC;
    bumpColor = texture( bumpTexture, fragmentTexCoords).rgb;
}