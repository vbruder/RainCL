#define RADIUS 0.1f
#define DAMPING 0.005f
#define SPRING 1.0f
#define SHEAR 0.12f
#define GRAVITY 0.7f
#define TIME_SCALE 1.0f
#define LOCAL_MEM_SIZE 128
#define maxRadius 5.0f

constant sampler_t sampler = CLK_NORMALIZED_COORDS_TRUE | CLK_FILTER_LINEAR| CLK_ADDRESS_REPEAT;

kernel void rain_sim(
global float4* position,
global float3* velos,
global float3* seed,
read_only image2d_t heightmap,
read_only image2d_t normalmap,
uint maxParticles,
float dt)
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
	float4 height = read_imagef(heightmap, sampler, (float2)(1-tc.x, tc.y));
	// float4 normal = read_imagef(normalmap, sampler, myPos.xz);
	
	//pseudo random int
	int rand = (myId * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
	
	//myPos.y = height.x*0.25f;
	
	//respawn particle
	if((myPos.y <= height.x*0.25f) || (myPos.x >= maxRadius) || (myPos.z >= maxRadius))
	{
		myPos.y = rand/1000000000;
		//myPos.xyz = seed[myId];
	}

    position[myId].xyz = velos[myId].xyz;//myPos.xyz - velos[myId].xyz * dt; //(float4)(0, 0.1f, 0, 0)*dt;
}