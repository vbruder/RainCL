

kernel void waterSim(
					global float* 	attribute,
					global float4*	normal,
					global float* 	height,
					global float4* 	water,
					const  uint		rain,
					const  float	dt)
{
	uint id = get_global_id(0);
	uint gws = get_global_size(0);
	uint rowlen = sqrt((float) gws);
	
	//TODO: rain factor size??
	float rainFactor = rain * 0.000001 * dt;
	float heightVal  = height[id] / 10 - 5.0;
	float waterVal   = water[id].y;
	
	//*********************************************************************************
	//add rain water
	waterVal += rainFactor;	
	//remove oozing water
	waterVal += (-attribute[id]) * 0.003;
	
	barrier(CLK_GLOBAL_MEM_FENCE);
	
	
	//*********************************************************************************
	//flow water to lower level
    //TODO: what to do on boarders??
	
	//calculate tangent
	float4 tangent = cross(normalize(normal[id]), (float4) (0.0, 1.0, 0.0, 1.0));
	tangent.w = 1.0;
	//make sure direction is downwards
	if (tangent.z > 0)
	{
		tangent = 0 - tangent;
		tangent.w = 1.0;
	}
	//grad of terrain as flow amount factor
	//TODO: size?? -> angle between tangent and (0,0,1)?
	float grad = dot(tangent, (float4) (1.0, 0.0, 0.0, 1.0)) * 0.0;
	normalize(tangent);
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
	uint dir = (int)((atan2pi(tangent.x, tangent.y) * 180.0f)/45.0f + 0.5f);

	//map to IDs and increase neighbor dependant on grad and time
	//TODO: improve conditional mess and do atomic int (on floats ?)
	
		 if (dir == 0) (water[id - rowlen + 1].y) += grad*waterVal*dt;
	else if (dir == 1) (water[id          + 1].y) += grad*waterVal*dt;
	else if (dir == 2) (water[id + rowlen + 1].y) += grad*waterVal*dt;
	else if (dir == 3) (water[id + rowlen + 0].y) += grad*waterVal*dt;
	else if (dir == 4) (water[id + rowlen - 1].y) += grad*waterVal*dt;
	else if (dir == 5) (water[id          - 1].y) += grad*waterVal*dt;
	else if (dir == 6) (water[id - rowlen - 1].y) += grad*waterVal*dt;
	else if (dir == 7) (water[id - rowlen - 1].y) += grad*waterVal*dt;
	
	barrier(CLK_GLOBAL_MEM_FENCE);
	
	//*********************************************************************************
	//distribute water equally to neighbors
	//TODO: add speed factor c^2 ??
	/*
	float damping = 	height[id + 1] 		+ water[id + 1].y
					  + height[id - 1] 		+ water[id - 1].y
					  + height[id + rowlen] + water[id + rowlen].y
					  + height[id - rowlen] + water[id - rowlen].y
					  - 4*(heightVal + waterVal);
	*/
	float damping = water[id + 1].y + water[id - 1].y + water[id + rowlen].y + water[id - rowlen].y - 4*(waterVal);
					  
	//calculate new water value and set water map. Value cannot be smaller than height.
	float newWaterVal = waterVal + damping * dt * 1;
	if (heightVal > newWaterVal)
	{
		water[id].y = heightVal;
	}
	else
	{
		water[id].y = newWaterVal;
	}
	//TODO: blend water ~ amount
}