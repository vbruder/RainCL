
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

	if (!( ((id % rowlen)-1 == 0) || id % rowlen == 0 || id < 512 || id > 512*511))
	{
		float gain = 0.0;
	
		//add rain to water
		gain += rain * dt;	 					// 0.1 - 1.0
		
		//remove oozing water
		gain -= attribute[id] * oozing * dt;	
		//gain += 0.0001;
		//calculate new water value and set water map. Set minimal limit.
		water[id] += gain;
		if (water[id] < -0.1)
		{
			water[id] = -0.1;
		}
		if (water[id] > +0.2)
		{
			water[id] = 0.2;
		}
		
		tmp[id] = water[id];
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
	if ((id % rowlen == 0) || ((id % rowlen)-1 == 0) || id < 512 || id > 512*511)
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
	float eps = 0.1f;
	dx  = fabs(dx) < eps ? 0 : dx;
	dy  = fabs(dy) < eps ? 0 : dy;
	
	int2 dir2 = (int2)(sign(dx), sign(dy));
	
	float ddtt = 0.001f;
	
	if (!border && dot((float)dir2.x, (float)dir2.y) != 0)
	{
		//von Neumann neighborhood
		water[abs(id + dir2.y*rowlen + dir2.x) % N] += waterVal*dt*len;
		
		//Moore neighborhood
		/*
			 if (dir == 0) (water[id - rowlen + 1]) += waterVal;
		else if (dir == 1) (water[id          + 1]) += waterVal;
		else if (dir == 2) (water[id + rowlen + 1]) += waterVal;
		else if (dir == 3) (water[id + rowlen + 0]) += waterVal;
		else if (dir == 4) (water[id + rowlen - 1]) += waterVal;
		else if (dir == 5) (water[id          - 1]) += waterVal;
		else if (dir == 6) (water[id - rowlen - 1]) += waterVal;
		else if (dir == 7) (water[id - rowlen - 1]) += waterVal;
		*/
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
	if ((id % rowlen == 0) || ((id % rowlen)-1 == 0) || id < 512 || id > 512*511)
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
		water[id] -= tmp[id]*dt*len;
	}
}

kernel void distributeWater(
							global float4* 	waterHeight,
							global float*	heightScaled,
							global float* 	water,
							global float4*  tmp,
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
	//TODO one edge...
	if ( (id % rowlen == 0) || id < 512 || id > 512*511 || ((id % rowlen)-1 == 0) )
		border = 1;
	
	float hff = 0.0;
	int cnt = 0;
	float eps = 0.1;
	float rightN, leftN, topN, botN;
	rightN = leftN = topN = botN = 0.0;
	

//	if (!border)
//	{
//		hff = damping * (water[id + 1] + water[id - 1] + water[id + rowlen] + water[id - rowlen] - 4*(waterVal));
//	}

	
	//right neighbor
	
	if (!border && heightScaled[id + 1] <= heightVal + eps)
	{
		rightN = water[id + 1];
		++cnt;
	}
	//left neighbor
	if (!border && heightScaled[id - 1] <= heightVal + eps)
	{
		leftN = water[id - 1];
		++cnt;
	}
	//bottom neighbor
	if (!border && heightScaled[id + rowlen] <= heightVal + eps)
	{
		botN = water[id + rowlen];
		++cnt;
	}
	//top neighbor
	if (!border && heightScaled[id - rowlen] <= heightVal + eps)
	{
		topN = water[id - rowlen];
		++cnt;
	}
	
	//calculate height-field-fluids function
	hff = damping * (rightN + leftN + botN + topN - 4*(waterVal));
	
	tmp[id].y = hff + waterVal + heightVal;
	water[id] = hff + waterVal;
}
















kernel void waterSim(
					global float4* 	water,
					global float*   velos,
					global float* 	height,
					global float4*	normal,
					global float* 	attribute,
					global float* 	gradMap,
					const  float	rain,
					const  float	oozing,
					const  float	damping,
					const  float	dt)
{
	uint id = get_global_id(0);
	uint gws = get_global_size(0);
	uint rowlen = sqrt((float) gws);
	int border = 0;
	
	//check if border bucket
	if ((id % rowlen)-1 == 0 || id %  rowlen == 0 || id < 512 || id > 512*511)
		border = 1;
	
	//water.y initital -1..12 something??
	
	//TODO: rain factor size??
	float rainFactor = rain * dt;			// 0.1 .. 1.0 *dt
	float heightVal  = height[id]/32.0;			// 0..255 -> 0..1
	float waterVal   = water[id].y - heightVal;
	//grad of terrain as flow amount factor
	float grad 		 = gradMap[id]*10.0; 	// grad: 0.0 .. 1.0 -> 0.0 .. 20.0
	
	//*********************************************************************************
	//add rain water
	waterVal += rainFactor;	
	//remove oozing water
	waterVal -= attribute[id] * oozing * dt;
		
	//*********************************************************************************
	//flow water to lower level

	uint N = gws;
	float top = height[(abs(id-rowlen)) % N];
	float down = height[(abs(id+rowlen)) % N];
	float right = height[(id+1) % N];
	float left = height[(abs(id-1)) % N];
	float topleft = height[(id-rowlen - 1) % N];
	float topright = height[(abs(id - rowlen + 1)) % N];
	float downright = height[(id + rowlen + 1) % N];
	float downleft = height[(abs(id + rowlen - 1)) % N];
	
	float dx = topleft - topright + 2 * left - 2 * right + downleft - downright;
	float dy = topleft + 2 * top + topright - downleft - 2 * down - downright;
	
	//calculate tangent
	float3 tangent;
	/*float3 t1 = cross(normalize(normal[id].xzy), (float3) (0.0, 0.0, 1.0));
	float3 t2 = cross(normalize(normal[id].xzy), (float3) (0.0, 1.0, 0.0));
	if (length(t1) > length(t2))
	{
	    tangent = t1;
	}
	else
	{
	    tangent = t2;
	}
	//make sure direction is downwards
	
	if (tangent.z > 0)
	{
		tangent = (float3) (0.0) - tangent;
	}
	*/
	tangent = (float3)(dx, sqrt(dot(dx,dy) + dot(dy,dy)), dy);
	
	tangent = normalize(tangent);
	tangent.y *= -1;

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
	// Calculate angle starting 0Â° at (1,0). 
	// Divide angle by 8 (amount of neighbor fields) and roud result to nearest int.
	uint dir = (int)((atan2pi(tangent.y, tangent.x) * 180.0f)/45.0f + 0.5f);

	//map to IDs and increase neighbor dependant on grad and time
	//TODO: improve conditional mess and do atomic_add (on floats ?)
	// maybe atomic_xchg (read - swap - store) or integer
	float eps = 1e-1f;
	dx  = fabs(dx) < eps ? 0 : dx;
	dy  = fabs(dy) < eps ? 0 : dy;
	
	int2 dir2 = (int2)(sign(dx), sign(dy));

	/*
		 if (dir == 0) (water[id - rowlen + 1].y) += grad*dt;
	else if (dir == 1) (water[id          + 1].y) += grad*dt;
	else if (dir == 2) (water[id + rowlen + 1].y) += grad*dt;
	else if (dir == 3) (water[id + rowlen + 0].y) += grad*dt;
	else if (dir == 4) (water[id + rowlen - 1].y) += grad*dt;
	else if (dir == 5) (water[id          - 1].y) += grad*dt;
	else if (dir == 6) (water[id - rowlen - 1].y) += grad*dt;
	else if (dir == 7) (water[id - rowlen - 1].y) += grad*dt;*/
	
	float ddtt = 0.001f;
	
	if(dot((float)dir2.x, (float)dir2.y) > 0) // && water[id].y > heightVal)
	{
		water[abs(id + dir2.y * rowlen + dir2.x) % N].y += ddtt;
	}

	
	//water[id].y = water[id].y < heightVal ? heightVal : water[id].y-ddtt;

	
	/*
	// *********************************************************************************
	//distribute water equally to neighbors with height field method
	//pick data from von Neumann neighborhood 
	//but only if grad of neighbors <= grad of current item
	int cnt = 0;
	float rightN, leftN, topN, botN;
	rightN = leftN = topN = botN = 0.0;
	
	//right neighbor
	if (!border && gradMap[id + 1] <= grad)
	{
		rightN = water[id + 1].y;
		++cnt;
	}
	//left neighbor
	if (!border && gradMap[id - 1] <= grad)
	{
		leftN = water[id - 1].y;
		++cnt;
	}
	//bottom neighbor
	if (!border && gradMap[id + rowlen] <= grad)
	{
		botN = water[id + rowlen].y;
		++cnt;
	}
	//top neighbor
	if (!border && gradMap[id - rowlen] <= grad)
	{
		topN = water[id - rowlen].y;
		++cnt;
	}
	
	//calculate height-field-fluids function
	float hff = damping * (rightN + leftN + botN + topN - cnt*(waterVal));
	
	float newWaterVal = hff*dt;
	//velos[id] += f*dt;
	newWaterVal += waterVal;
	newWaterVal += heightVal;

	//calculate new water value and set water map. Set minimal limit.
	if (newWaterVal < -10.0)
	{
		water[id].y = -10.0;
	}
	else
	{
		water[id].y = newWaterVal;
	} */
	
	//debug:
	//water[id].y = grad;

	//water[id].y = 20*tangent.x;
	
	//TODO: blend water ~ amount
	//water[id].s3 = 1.0;
}

