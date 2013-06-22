#define maxRadius 5.0f
#define heightScale 0.25f

constant sampler_t sampler = CLK_NORMALIZED_COORDS_TRUE | CLK_FILTER_LINEAR | CLK_ADDRESS_REPEAT;

/**
 *  Generate pseudo random float
 *  @param seed (e.g. elem ID)
 *  @return random number
 */
float getRand(int seed)
{
    float rand = (float)((seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1));
    rand /= 1000000000;
    return rand;
}

kernel void rainSim(
    //position array contains: float4 position, float4 seed, float4 velos
	global float4* position,
	global float4* velos,
	global float4* seed,
		
	read_only image2d_t heightmap,
	read_only image2d_t normalmap,
	
	const uint maxParticles,
	const float dt,
    const float eyePosX,
    const float eyePosY,
    const float eyePosZ,
    const float windDirX,
    const float windDirZ)
{
    
    uint myId = get_global_id(0);
    
    //*3, to skip seed and velos??
    float4 myPos = position[myId];
		
	float2 tc = (float2)(0.5f, 0.5f) + myPos.xz;
	float4 height = heightScale * read_imagef(heightmap, sampler, (float2)(1-tc.x, tc.y));
	// float4 normal = read_imagef(normalmap, sampler, myPos.xz);

    //myPos.y = height.x;
    height.x = 0.0f;
	
	//respawn particle
	if (myPos.y <= (height.x - 2.0f))
	{
		myPos.xyz = seed[myId].xyz + (float3)(eyePosX, eyePosY, eyePosZ);
        myPos.y += 1.f;
	}

    myPos.x += windDirX * dt;
    myPos.y -= velos[myId].y * dt;
    myPos.z += windDirZ * dt;
    
    position[myId].xyz = myPos.xyz;
}