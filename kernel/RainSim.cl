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

/**
 * Kernel to move rain streaks with camera and according to wind direction.
 */
kernel void rainSim	(
						global float4* position,
						global float4* velos,
						global float4* seed,
						//height map
						read_only image2d_t heightmap,
						//constants
						const uint maxParticles,
						const float dt,
					    const float eyePosX,
					    const float eyePosY,
					    const float eyePosZ,
					    const float windDirX,
					    const float windDirZ
					 )
{
    
    uint myId = get_global_id(0);
    
    float4 myPos = position[myId].s0123;
    float3 myVelos = velos[myId].xyz;
		
	float2 tc = (float2)(0.5f, 0.5f) + myPos.xz;
	float4 height = heightScale * read_imagef(heightmap, sampler, (float2)(1-tc.x, tc.y));

    //myPos.y = height.x;
    height.x = 0.0f;
	
	//respawn particle
	if (myPos.y <= (height.x - 2.0f))
	{
		myPos.xyz = seed[myId].xyz + (float3)(eyePosX, eyePosY, eyePosZ);
        myPos.y += 1.f;
	}

    myPos.x += (windDirX + myVelos.x) * dt;
    myPos.y -= 			   myVelos.y  * dt;
    myPos.z += (windDirZ + myVelos.z) * dt;
    
    position[myId].s0123 = myPos;
}


/**
 * move fog clouds
 */
kernel void fogSim  (
						global float4* position,
						const float dt,
						const float windDirX,
						const float windDirZ
					)
{
	uint myId = get_global_id(0);
	
	float4 myPos = position[myId].xyzw;
	myPos.z += dt * 50;

	if ( fabs(fmod(myPos.z, 0.3f)) < 0.1 )
<<<<<<< HEAD
		myPos.w = myPos.w < 255 ? ++myPos.w : 0;
=======
		myPos.w = myPos.w < 255 ? myPos.w + 1.0 : 0.0;
>>>>>>> 383a5a0c41279a8fceb17aa183f7a540671604c6
	
	//myPos.xz += 0.1 * (float2)(windDirX, windDirZ);
	
	position[myId].xzw = myPos.z > 1000 ? (float3) (10, -1000, myPos.w) : myPos.xzw;
}