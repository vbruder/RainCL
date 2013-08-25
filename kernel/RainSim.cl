
constant sampler_t sampler = CLK_NORMALIZED_COORDS_TRUE | CLK_FILTER_LINEAR | CLK_ADDRESS_REPEAT;

/**
 * Kernel to move rain streaks with camera and according to wind direction.
 */
kernel void rainSim	(
						global float4* position,
						global float4* velos,
						global float4* seed,
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
    float3 eyePos = (float3)(eyePosX, eyePosY, eyePosZ);
	
	float dist = distance(eyePos, myPos.xyz);
	
	//respawn particle
	if (myPos.y <= -1.0f || dist > 50.0f)
	{
		myPos.xyz = seed[myId].xyz + eyePos;
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
	myPos.x += dt;

	if ( fabs(fmod(myPos.z, 0.3f)) < 0.1 )
		myPos.w = myPos.w < 255 ? myPos.w+1.0 : 0;
	
	position[myId].xzw = myPos.z > 250 ? (float3) (10, -250, myPos.w) : myPos.xzw;
}