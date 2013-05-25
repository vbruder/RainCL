#define maxRadius 5.0f
#define heightScale 0.25f

constant sampler_t sampler = CLK_NORMALIZED_COORDS_TRUE | CLK_FILTER_LINEAR | CLK_ADDRESS_REPEAT;

__kernel void rain_sim(
	__global float4* position,
	__global float4* velos,
	__global float4* seed,
	
	read_only image2d_t heightmap,
	read_only image2d_t normalmap,
	uint maxParticles,
	float dt,
    float eyePosX,
    float eyePosY,
    float eyePosZ,
    float windDirX,
    float windDirZ)
{
    //__local float4 sharedMem[LOCAL_MEM_SIZE];
    
    int myId = get_global_id(0);
    float4 myPos = position[myId];
    int localId = get_local_id(0);
    int tileSize = get_local_size(0);
    int tileCnt = get_num_groups(0);
	
	// int2 dim = get_image_dim(heightmap);
	// float2 dimf;
	// dimf.x = normalize((float)dim.x);
	// dimf.y = (float)dim.y;
	
	float2 tc = (float2)(0.5f, 0.5f) + myPos.xz;
	float4 height = heightScale * read_imagef(heightmap, sampler, (float2)(1-tc.x, tc.y));
	// float4 normal = read_imagef(normalmap, sampler, myPos.xz);
	
	//pseudo random int
	// int rand = (myId * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
    // myPos.y = rand/1000000000;	

    //myPos.y = height.x;
    //height.x = -2.0f;
	
	//respawn particle
	if (myPos.y <= (height.x - 2.0f))
	{
		myPos.xyz = seed[myId].xyz + (float3)(eyePosX, eyePosY, eyePosZ);
        myPos.y += 1.f;
	}

    myPos.x += windDirX * dt;
    myPos.y -= velos[myId].y * dt;
    myPos.z += windDirZ * dt;

    position[myId].xyz = myPos.xyz;//(float4)(0, 0.1f, 0, 0)*dt;
}