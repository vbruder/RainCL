#define RADIUS 0.1f
#define DAMPING 0.005f
#define SPRING 1.0f
#define SHEAR 0.12f
#define GRAVITY 0.7f
#define TIME_SCALE 1.0f
#define LOCAL_MEM_SIZE 64

constant sampler_t sampler = CLK_NORMALIZED_COORDS_TRUE| CLK_FILTER_LINEAR| CLK_ADDRESS_REPEAT;

__kernel void rain_sim(
__global float4* oldPos,
__global float4* newPos, 
__global float4* oldVelo,
__global float4* newVelo,
image2d_t heightmap,
image2d_t normalmap,
uint maxParticles,
float dt)
{
    __local float4 sharedMem[LOCAL_MEM_SIZE];
    
    int myId = get_global_id(0);
    float4 myPos = oldPos[myId];
    int localId = get_local_id(0);
    int tileSize = get_local_size(0);
    int tileCnt = get_num_groups(0);
	
	newVelo[myId].xyz = oldVelo[myId].xyz;
	
	float4 height = read_imagef(heightmap, sampler, myPos.xz);
	
	//pseudo random int
	int rand = (myId * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
    
	//respawn particle
	if (myPos.y <= (height.x))// + RADIUS))
	{
		//myPos.y = height.x;
		myPos.y = 2.3f + ((float)rand / 1000000000.0f);
	}
    newPos[myId].xyz = myPos.xyz - oldVelo[myId].xyz *dt;
}