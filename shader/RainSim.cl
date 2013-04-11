#define RADIUS 0.1f
#define DAMPING 0.005f
#define SPRING 1.0f
#define SHEAR 0.12f
#define GRAVITY 0.7f
#define TIME_SCALE 1.0f
#define LOCAL_MEM_SIZE 128

constant sampler_t sampler = CLK_NORMALIZED_COORDS_TRUE | CLK_FILTER_LINEAR| CLK_ADDRESS_REPEAT;

__kernel void rain_sim(
__global float4* oldPos,
__global float4* newPos, 
__global float4* oldVelo,
__global float4* newVelo,
__read_only image2d_t heightmap,
__read_only image2d_t normalmap,
uint maxParticles,
float dt)
{
    //__local float4 sharedMem[LOCAL_MEM_SIZE];
    
    int myId = get_global_id(0);
    float4 myPos = oldPos[myId];
    int localId = get_local_id(0);
    int tileSize = get_local_size(0);
    int tileCnt = get_num_groups(0);
	
	newVelo[myId].xyz = oldVelo[myId].xyz;
	
	// int2 dim = get_image_dim(heightmap);
	// float2 dimf;
	// dimf.x = normalize((float)dim.x);
	// dimf.y = (float)dim.y;
	
	float2 tc = (float2)(0.5f, 0.5f) + myPos.xz;
	float4 height = read_imagef(heightmap, sampler, (float2)(1-tc.x, tc.y));
	// float4 normal = read_imagef(normalmap, sampler, myPos.xz);
	
	//pseudo random int
	int rand = (myId * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
	
	myPos.y = height.x*0.25f;
	
	//respawn particle
	// if (myPos.y <= myPos.w)
	// {
		// myPos.y = 2.3f + ((float)rand / 1000000000.0f);
	// }

    newPos[myId].xyz = (float3)(myPos.x, myPos.y, myPos.z);//myPos.xyz;// - oldVelo[myId].xyz * 0.f;//dt;
}