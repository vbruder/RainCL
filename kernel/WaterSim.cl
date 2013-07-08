
/**
 * Kernel calculates water gain from rain and loss from oozing.
 */
kernel void rainOozing(
						global float* 	water,
						global float* 	attribute,
						global float*	tmp,
						const  float	rain,
						const  float	oozing,
						const  float	dt
						)
{
	uint id = get_global_id(0);
	uint N = get_global_size(0);
	uint rowlen = sqrt((float) N);

	if (!( (id % rowlen)-1 == 0 || id % rowlen == 0 || id < rowlen || id > rowlen*(rowlen-1) ))
	{
		float gain = 0.0;
	
		//add rain to water
		gain += rain * dt;	 					// 0.1 - 1.0
		
		//remove oozing water
		//gain -= attribute[id] * oozing * dt;	

		//calculate new water value and set water map. Set limits.
		water[id] += gain;

		tmp[id] = water[id];
	}
	
	if (water[id] < -0.1)
	{
		water[id] = -0.1;
	}
	if (water[id] > +10.0)
	{
		water[id] = 10;
	}
}


/**
 * Flow water to lower level: Raise water level of lower bucket dependant on grad.
 * Uses tangents calculated with Sobel function.
 */
kernel void flowWaterTangential(
								global float* 	water,
								global float* 	height,
								global float*	tmp,
								const  float	dt)
{
	uint id = get_global_id(0);
	uint N = get_global_size(0);
	uint rowlen = sqrt((float) N);
	int border = 0;
	float waterVal = tmp[id];
	
	//check if border bucket
	if ((id % rowlen == 0) || ((id % rowlen)-1 == 0) || id < rowlen || id > rowlen*(rowlen-1))
	{
		border = 1;
	}

	//use Sobel to calculate gradient map from height data
	float top 		= height[(abs(id-rowlen)) 		% N];
	float down 		= height[(abs(id+rowlen)) 		% N];
	float right 	= height[(id+1)					% N];
	float left 		= height[(abs(id-1)) 			% N];
	float topleft 	= height[(id-rowlen - 1) 		% N];
	float topright 	= height[(abs(id - rowlen + 1)) % N];
	float downright = height[(id + rowlen + 1) 		% N];
	float downleft 	= height[(abs(id + rowlen - 1)) % N];
	
	float dx = border ? 0.0 : topleft - topright + 2*left - 2*right + downleft - downright;
	float dz = border ? 0.0 : topleft + 2*top + topright - downleft - 2*down - downright;
	
	float sharpness = -1;
	
	float dy = sharpness * sqrt(dx*dx + dz*dz);
	
	//tangent
	float3 tangent = (float3)(dx, dy, dz);
	float len = 0.01 * length(tangent);
	tangent = normalize(tangent);

	//calculate the neighbor to flow to (Moore neighborhood)
	//
	// x: current thread
	// row up:   u = x - sqrt(gws)
	// row down: d = x + sqrt(gws)
	//
	//	u-1  u  u+1				6  7  0
	//	x-1  x  x+1		=>		5  x  1
	//  d-1  d  d+1				4  3  2
	//
	// Calculate angle starting 0° at (1,0). 
	// Divide angle by 8 (amount of neighbor fields) and roud result to nearest int.
	uint dir = (int)((atan2pi(tangent.z, tangent.x) * 180.0f)/45.0f + 0.5f);

	//do not move water smaller than epsilon
	float eps = 0.2f;
	dx  = fabs(dx) < eps ? 0 : dx;
	dy  = fabs(dy) < eps ? 0 : dy;
	
	int2 dir2 = (int2)(sign(dx), sign(dy));
	
	float ddtt = 0.001f;
	
	if (!border && dot((float)dir2.x, (float)dir2.y) != 0)
	{
		//von Neumann neighborhood
		//water[abs(id + dir2.y*rowlen + dir2.x) % N] += waterVal*dt *len;
		
		//Moore neighborhood
		
			 if (dir == 0) (water[id - rowlen + 1]) += waterVal*dt*len;
		else if (dir == 1) (water[id          + 1]) += waterVal*dt*len;
		else if (dir == 2) (water[id + rowlen + 1]) += waterVal*dt*len;
		else if (dir == 3) (water[id + rowlen + 0]) += waterVal*dt*len;
		else if (dir == 4) (water[id + rowlen - 1]) += waterVal*dt*len;
		else if (dir == 5) (water[id          - 1]) += waterVal*dt*len;
		else if (dir == 6) (water[id - rowlen - 1]) += waterVal*dt*len;
		else if (dir == 7) (water[id - rowlen - 1]) += waterVal*dt*len;
		
	}
}

/**
 * Reduce water that flowed to neighbor buckets.
 */
kernel void reduceFlowedWater(
								global float* 	water,
								global float* 	height,
								global float*	tmp,
								const  float	dt)
{
	uint id = get_global_id(0);
	uint N = get_global_size(0);
	uint rowlen = sqrt((float) N);
	int border = 0;
	
	//check if border bucket
	if ((id % rowlen == 0) || ((id % rowlen)-1 == 0) || id < rowlen || id > rowlen*(rowlen-1))
	{
		border = 1;
	}

	//use Sobel to calculate gradient map from height data
	float top 		= height[(abs(id-rowlen)) 		% N];
	float down 		= height[(abs(id+rowlen)) 		% N];
	float right 	= height[(id+1)					% N];
	float left 		= height[(abs(id-1)) 			% N];
	float topleft 	= height[(id-rowlen - 1) 		% N];
	float topright 	= height[(abs(id - rowlen + 1)) % N];
	float downright = height[(id + rowlen + 1) 		% N];
	float downleft 	= height[(abs(id + rowlen - 1)) % N];
	
	float dx = border ? 0.0 : topleft - topright + 2*left - 2*right + downleft - downright;
	float dz = border ? 0.0 : topleft + 2*top + topright - downleft - 2*down - downright;
	
	float sharpness = -3.0;
	
	float dy = sharpness * sqrt(dx*dx + dz*dz);
	
	//tangent
	float3 tangent = (float3)(dx, dy, dz);
	float len = 0.01 * length(tangent);

	//do not move water smaller than epsilon
	float eps = 0.1f;
	dx  = fabs(dx) < eps ? 0 : dx;
	dy  = fabs(dy) < eps ? 0 : dy;
	
	int2 dir2 = (int2)(sign(dx), sign(dy));
	
	if (!border && dot((float)dir2.x, (float)dir2.y) != 0)
	{
		water[id] -= tmp[id]*dt * len;
	}
}

/**
 * Distribute wate with hight field fluids technique
 */
kernel void distributeWater(
							global float4* 	waterHeight,
							global float*	heightScaled,
							global float* 	water,
							global float4*  tmp,
							global float*	velos,
							const  float	damping,
							const  float	dt)
{
	uint id = get_global_id(0);
	uint gws = get_global_size(0);
	uint rowlen = sqrt((float) gws);
	int border = 0;
	
	float heightVal  = heightScaled[id];			// -1..12?
	float waterVal   = water[id];
	
	//check if border bucket
//	if ( (id % rowlen == 0) || id < rowlen || id > rowlen*(rowlen-1) || ((id % rowlen)-1 == 0) )
//		border = 1;
	
	float hff = 0.0;
	int cnt = 0;
	float eps = 0.1;
	float rightN, leftN, topN, botN;
	rightN = leftN = topN = botN = 0.0;
	
	//right neighbor
	if ( !(id == rowlen*rowlen-1) && (id % rowlen)-1 == 0 )
	{
		rightN = waterVal;
		++cnt;
	}
	else if (heightScaled[id + 1] > heightVal + eps)
	{
		rightN = water[id + 1];
		++cnt;
	}
	
	//left neighbor
	if (!(id == 0) && id % rowlen == 0)
	{
		leftN = 0;
		++cnt;
	}
	else if (heightScaled[id - 1] > heightVal + eps)
	{
		leftN = water[id - 1];
		++cnt;
	}
	
	//bottom neighbor
	if ( !(id == rowlen*(rowlen-1)+1) && id > rowlen*(rowlen-1) )
	{
		botN = 0;
		++cnt;
	}
	else if (heightScaled[id + rowlen] > heightVal + eps)
	{
		botN = water[id + rowlen];
		++cnt;
	}
	
	//top neighbor
	if (!(id == rowlen-1) && id < rowlen)
	{
		topN = 0;
		++cnt;
	}
	else if (heightScaled[id - rowlen] > heightVal + eps)
	{
		topN = water[id - rowlen];
		++cnt;
	}
	
	//calculate height-field-fluids function
	float h = 1;//2.0f / 512.0f;
	float c = 1;
	hff = c * rightN + leftN + botN + topN - cnt*(waterVal) / h;
	
	velos[id] += hff * dt;
	float currVelos = velos[id];
	
	float finalWater = waterVal + heightVal + currVelos * dt;
	//float finalWater =  waterVal + heightVal;

	//finalWater = finalWater < -10.0 ? -10 : finalWater;
	
	water[id] = waterVal + currVelos * dt;
	
	tmp[id].y = finalWater;
	
	//debug:	
	//water[id] =  waterVal;

}


kernel void blurWater(global float4* src, global float4* dst, global float* mask, const int maskSize)
{   
    int x = get_global_id(0);
    int y = get_global_id(1);
    int w = get_global_size(0);
    int h = get_global_size(1);
    
	uint N = w*h;
	
    float hh = 0;
    int halfSize = maskSize / 2;
    
    for(int i = 0; i < maskSize; ++i)
    {
        int row = i - halfSize;
        
        for(int j = 0; j < maskSize; ++j)
        {
            int col = j - halfSize;
        
            uint index = (y + row) * w + x + col;
        
            index = index < 0 ? N-index : (index >= N ? index % N : index);

            hh += src[index].y * mask[i * maskSize + j];
        }
    }
    
    dst[y * w + x].y = hh;
} 