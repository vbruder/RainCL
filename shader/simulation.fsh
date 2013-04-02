#version 330

in vec3 coords;
in vec3 normal;
in vec3 positionFS;

out vec4 fragColor;

uniform sampler2D normalTex;
uniform sampler2D heightTex;

vec3 getNormal(vec2 tc)  {
    float dx = 1.0/512.0;
    float dy = 1.0/512.0;
    
    float here = texture(heightTex, tc).x;

    float v01 = texture(heightTex, tc + vec2(-dx, 0.0)).x;
    float v10 = texture(heightTex, tc + vec2(0.0, -dy)).x;
    float v12 = texture(heightTex, tc + vec2(0.0, dy)).x;
    float v21 = texture(heightTex, tc + vec2(+dx, 0.0)).x;
    
    vec3 hvec = vec3(tc.x,here,tc.y);

    vec3 v1 = vec3( -dx,    v01, 0.0)-hvec; 
    vec3 v4 = vec3( 0.0,    v10, -dy)-hvec; 
    vec3 v2 = vec3( 0.0,     v12, dy)-hvec; 
    vec3 v3 = vec3( dx,    v21,  0.0)-hvec; 

    vec3 c1 = cross(v1,v2);
    vec3 c2 = cross(v2,v3);
    vec3 c3 = cross(v3,v4);
    vec3 c4 = cross(v4,v1);

    return normalize(c1+c2+c3+c4);
}

void main(void)
{
    //fragColor = vec4(0.5 + 0.5*getNormal(vec2(coords.x,coords.z)),1.0);
    fragColor = texture(heightTex, vec2(coords.x,coords.z))*3.f;

//    vec3 color = texture(heightTex, vec2(coords.x,coords.z)).xyz + vec3(0.4);
//    fragColor = vec4(color,1.0);

//    vec3 color = texture(normalTex, vec2(coords.x,coords.z)).xyz;
//    fragColor = vec4(0.5+0.5*color,1.0);

}