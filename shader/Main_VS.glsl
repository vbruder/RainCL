#version 150

uniform mat4 viewProj;
uniform mat4 model;
uniform vec3 camPos;
uniform mat4 view;
uniform float camFar;
uniform mat4 shadowMatrix;

in vec3 positionMC;
in vec3 normalMC;
in vec4 vertexColor;
in vec3 tangentMC;
in vec2 texCoords;

out vec4 positionWC;
out vec4 normalWC;
out vec4 tangentWC;
out vec2 fragmentTexCoords;
out float depth;
out vec4 shadowCoordWC;

void main(void)
{ 
   positionWC		 = model * vec4(positionMC, 1.0);
   shadowCoordWC	 = shadowMatrix * positionWC;
   depth			 = ((view*positionWC).z/15) * (-1); 
   gl_Position 		 = viewProj * positionWC;
   normalWC			 = model * vec4(normalMC, 0.0);
   fragmentTexCoords = texCoords;
   positionWC.w		 = distance(positionWC.xyz, camPos);
   tangentWC		 = model * vec4(tangentMC, 0);
}