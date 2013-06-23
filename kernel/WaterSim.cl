#define WAVE_SPEED 16.0

kernel void waterSim(
					global float4* 	water,
					global float*   velos,
					global float* 	height,
					global float4*	normal,
					global float* 	attribute,
					global float* 	gradient,
					const  float	rain,
					const  float	oozing,
					const  float	damping,
					const  float	dt)
{
	uint id = get_global_id(0);
	uint gws = get_global_size(0);
	uint rowlen = sqrt((float) gws);
	
	//check if border bucket
	if (id % (rowlen-1) == 0 || id %  rowlen == 0)
		;
	
	//TODO: rain factor size??
	float rainFactor = rain * dt;
	float heightVal  = height[id] / 10 - 9.0;
	float waterVal   = water[id].y;
	
	//*********************************************************************************
	//add rain water
	waterVal += rainFactor;	
	//remove oozing water
	waterVal -= attribute[id] * oozing * dt;
	
	barrier(CLK_GLOBAL_MEM_FENCE);
	
	
	//*********************************************************************************
	//flow water to lower level
    //TODO: what to do on borders??
	
	//calculate tangent
	float3 tangent;
	float3 t1 = cross(normalize(normal[id].xzy), (float3) (0.0, 0.0, 1.0));
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
	
	//grad of terrain as flow amount factor
	//TODO: size?? 
	//angle between tangent and (1,0,0)
	normalize(tangent);
	float grad = gradient[id]*30; 	// 0-9
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
	uint dir = (int)((atan2pi(tangent.y, tangent.x) * 180.0f)/45.0f + 0.5f);

	//map to IDs and increase neighbor dependant on grad and time
	//TODO: improve conditional mess and do atomic_add (on floats ?)
	// maybe atomic_xchg (read - swap - store)
	
		 if (dir == 0) (water[id - rowlen + 1].y) += grad*dt*0.8;
	else if (dir == 1) (water[id          + 1].y) += grad*dt*0.9;
	else if (dir == 2) (water[id + rowlen + 1].y) += grad*dt*0.9;
	else if (dir == 3) (water[id + rowlen + 0].y) += grad*dt*0.9;
	else if (dir == 4) (water[id + rowlen - 1].y) += grad*dt*0.9;
	else if (dir == 5) (water[id          - 1].y) += grad*dt*0.9;
	else if (dir == 6) (water[id - rowlen - 1].y) += grad*dt*0.9;
	else if (dir == 7) (water[id - rowlen - 1].y) += grad*dt*0.9;
	
	barrier(CLK_GLOBAL_MEM_FENCE);
	//water[id - 1].y += grad*dt;
	water[id].y -= grad*dt*1.2;
	
	//*********************************************************************************
	//distribute water equally to neighbors with height field method
	//TODO: add speed factor c^2 ??
	float f = damping * (water[id + 1].y + water[id - 1].y + water[id + rowlen].y + water[id - rowlen].y - 4*(waterVal));
	//velos[id] += f*dt/10;
	 
	//calculate new water value and set water map. Value cannot be smaller than height.
	float newWaterVal = waterVal + f*dt; //velos[id]*dt;
	if (newWaterVal < -10.0)
	{
		water[id].y = -10.0;
	}
	else
	{
		water[id].y = newWaterVal;
	}
	
	//debug:
	//water[id].y = grad *10;
	//water[id].y = tangent.y * 5;
	
	//TODO: blend water ~ amount
}