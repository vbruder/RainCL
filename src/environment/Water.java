/**
 * 
 */
package environment;

import static apiWrapper.GL.GL_ARRAY_BUFFER;
import static apiWrapper.GL.GL_DYNAMIC_DRAW;
import static apiWrapper.GL.GL_STATIC_DRAW;
import static apiWrapper.GL.GL_FLOAT;
import static apiWrapper.GL.GL_POINTS;
import static apiWrapper.GL.GL_R16F;
import static apiWrapper.GL.GL_RED;
import static apiWrapper.GL.GL_RGBA;
import static apiWrapper.GL.GL_TEXTURE_2D;
import static apiWrapper.GL.glBindBuffer;
import static apiWrapper.GL.glBindVertexArray;
import static apiWrapper.GL.glBufferData;
import static apiWrapper.GL.glDrawArrays;
import static apiWrapper.GL.glEnableVertexAttribArray;
import static apiWrapper.GL.glGenBuffers;
import static apiWrapper.GL.glGenVertexArrays;
import static apiWrapper.GL.glGenerateMipmap;
import static apiWrapper.GL.glTexImage2D;
import static apiWrapper.GL.glVertexAttribPointer;
import static apiWrapper.OpenCL.clBuildProgram;
import static apiWrapper.OpenCL.clCreateCommandQueue;
import static apiWrapper.OpenCL.clCreateProgramWithSource;
import static apiWrapper.OpenCL.create;
import static apiWrapper.OpenCL.CL_MEM_COPY_HOST_PTR;
import static apiWrapper.OpenCL.CL_MEM_READ_ONLY;
import static apiWrapper.OpenCL.CL_MEM_READ_WRITE;
import static apiWrapper.OpenCL.CL_MEM_USE_HOST_PTR;
import static apiWrapper.OpenCL.clBuildProgram;
import static apiWrapper.OpenCL.clCreateBuffer;
import static apiWrapper.OpenCL.clCreateCommandQueue;
import static apiWrapper.OpenCL.clCreateFromGLBuffer;
import static apiWrapper.OpenCL.clCreateKernel;
import static apiWrapper.OpenCL.clCreateProgramWithSource;
import static apiWrapper.OpenCL.clEnqueueAcquireGLObjects;
import static apiWrapper.OpenCL.clEnqueueNDRangeKernel;
import static apiWrapper.OpenCL.clEnqueueReleaseGLObjects;
import static apiWrapper.OpenCL.clEnqueueWriteBuffer;
import static apiWrapper.OpenCL.clFinish;
import static apiWrapper.OpenCL.clReleaseCommandQueue;
import static apiWrapper.OpenCL.clReleaseContext;
import static apiWrapper.OpenCL.clReleaseKernel;
import static apiWrapper.OpenCL.clReleaseMemObject;
import static apiWrapper.OpenCL.clReleaseProgram;
import static apiWrapper.OpenCL.create;
import static apiWrapper.OpenCL.clRetainMemObject;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL10GL;
import org.lwjgl.opencl.CLCommandQueue;
import org.lwjgl.opencl.CLContext;
import org.lwjgl.opencl.CLDevice;
import org.lwjgl.opencl.CLKernel;
import org.lwjgl.opencl.CLMem;
import org.lwjgl.opencl.CLPlatform;
import org.lwjgl.opencl.CLProgram;
import org.lwjgl.opengl.Drawable;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import util.Camera;
import util.Geometry;
import util.ShaderProgram;
import util.Texture;
import util.Util;
import util.Util.ImageContents;

import apiWrapper.OpenCL;
import apiWrapper.OpenCL.Device_Type;

/**
 * Class handles water on terrain.
 * @author Valentin Bruder <vbruder@uos.de>
 *
 */
public class Water {
	
	//openCL
	private CLDevice device;

	private FloatBuffer heightDataBuffer;
	private FloatBuffer normalDataBuffer;
	private FloatBuffer attributeDataBuffer;
	
	private PointerBuffer gws = new PointerBuffer(1);
	
	private CLMem memHeight;
	private CLMem memNormal;
	private CLMem memAttribute;
	private CLMem memWater;

    //delta time for animations
	private float dt;

	//openGL IDs
	private static int vertArrayID2;
	private static int vertBufferID2;
	
	private static CLContext context;
	private static CLCommandQueue queue;
	private static CLProgram program;
	private static CLKernel kernelAccumulate;
	private static CLKernel kernelWaterSim;

	private Geometry terrain;
	
	//shader
	private ShaderProgram WaterRenderSP;
    private final Matrix4f viewProj = new Matrix4f();
	
	/**
	 * Create water object.
	 * @param device_type OpenCl device type
	 * @param drawable	OpenCl drawable
	 * @param rain	Rainstreaks object
	 * @throws LWJGLException
	 */
	public Water(Device_Type device_type, Drawable drawable, Geometry terrain) throws LWJGLException
	{		
		this.terrain = terrain;
        createCLContext(device_type, Util.getFileContents("./kernel/WaterSim.cl"), drawable);
        createWaterData();
        createKernels();
        WaterRenderSP = new ShaderProgram("./shader/Water.vsh", "./shader/Water.fsh");
	}

	/**
	 * Create data for water map and OpenCl buffers.
	 */
	private void createWaterData()
	{
        //load height map data
        ImageContents contentHeight = Util.loadImage("media/terrain/terrainHeight01.png");
        heightDataBuffer = BufferUtils.createFloatBuffer(contentHeight.height * contentHeight.width);
        for(int i = 0; i < heightDataBuffer.capacity(); ++i)
        {
            heightDataBuffer.put(contentHeight.data.get(i));
        }
        heightDataBuffer.rewind();		
        memHeight = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, heightDataBuffer);
                        
        //load normal map data
        ImageContents contentNorm = Util.loadImage("media/terrain/terrainNormal01.png");
        normalDataBuffer = BufferUtils.createFloatBuffer(contentNorm.height * contentNorm.width * 4);
        for(int i = 0; i < (normalDataBuffer.capacity()/4); ++i)
        {
            normalDataBuffer.put(contentNorm.data.get(i));
            normalDataBuffer.put(contentNorm.data.get(i + 1));
            normalDataBuffer.put(contentNorm.data.get(i + 2));
            normalDataBuffer.put(1.0f);
        }
        normalDataBuffer.rewind();
        memNormal = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, normalDataBuffer);
        
        //load attribute map data
        ImageContents contentAttrib = Util.loadImage("media/terrain/terrainAttribute01.png");
        attributeDataBuffer = BufferUtils.createFloatBuffer(contentAttrib.height * contentAttrib.width);
        for(int i = 0; i < attributeDataBuffer.capacity(); ++i)
        {
        	attributeDataBuffer.put(contentAttrib.data.get(i));
        }
        attributeDataBuffer.rewind();
        memAttribute = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, attributeDataBuffer);
        
        //generate initial water map
        //set water map initially to height data
        //TODO: accumulate data
        gws.put(0, contentHeight.height * contentHeight.width);
        
    	vertArrayID2 = glGenVertexArrays();
    	glBindVertexArray(vertArrayID2);
    	
        vertBufferID2 = glGenBuffers();
        
        glBindBuffer(GL_ARRAY_BUFFER, vertBufferID2);        
        glBufferData(GL_ARRAY_BUFFER, terrain.getVertexValueBuffer(), GL_DYNAMIC_DRAW);

        glEnableVertexAttribArray(ShaderProgram.ATTR_POS);
        glVertexAttribPointer(ShaderProgram.ATTR_POS, 4, GL_FLOAT, false, 16,  0);
        
        memWater = clCreateFromGLBuffer(context, CL_MEM_READ_WRITE, vertBufferID2);
	}

	/**
	 * Create OpenCl context with all significant parameters.
	 * @param type
	 * @param source
	 * @param drawable
	 * @throws LWJGLException
	 */
	private void createCLContext(Device_Type type, String source, Drawable drawable) throws LWJGLException
	{
        int device_type;
        
        switch(type)
        {
            case CPU: device_type = CL10.CL_DEVICE_TYPE_CPU; break;
            case GPU: device_type = CL10.CL_DEVICE_TYPE_GPU; break;
            default: throw new IllegalArgumentException("Wrong device type!");
        }
        
        CLPlatform platform = null;
        
        for(CLPlatform plf : CLPlatform.getPlatforms())
        {
            if(plf.getDevices(device_type) != null)
            {
                this.device = plf.getDevices(device_type).get(0);
                platform = plf;
                if(this.device != null)
                {
                    break;
                }
            }
        }             
        
        context = create(platform, platform.getDevices(device_type), null, drawable);      
        queue = clCreateCommandQueue(context, this.device, 0);       
        program = clCreateProgramWithSource(context, source);

        clBuildProgram(program, this.device, "", null);
	}
	
	/**
	 * Create two kernels: one for accumulating map data and one to simulate the water.
	 */
	private void createKernels()
	{
		// TODO kernel to accumulate data (a sort of grid)
		
		//kernel for water simulation
		kernelWaterSim = clCreateKernel(program, "waterSim");
		kernelWaterSim.setArg(3, memWater);
		kernelWaterSim.setArg(1, memNormal);
		kernelWaterSim.setArg(2, memHeight);
		kernelWaterSim.setArg(0, memAttribute);
	}
	
	/**
	 * Simulate the water.
	 * @param deltaTime Time past since last update.
	 */
	public void updateSimulation(long deltaTime)
	{	        	        
	    clEnqueueAcquireGLObjects(queue, memWater, null, null);
	    
	    kernelWaterSim.setArg( 4, Rainstreaks.getMaxParticles());
	    kernelWaterSim.setArg( 5, 1e-3f*deltaTime);
	    
        clEnqueueNDRangeKernel(queue, kernelWaterSim, 1, null, gws, null, null, null);            

        clEnqueueReleaseGLObjects(queue, memWater, null, null);
        
        clFinish(queue);
	}
	
	/**
	 * Draw the water on the scene.
	 * @param cam Camera object
	 */
	public void draw(Camera cam)
	{
    	WaterRenderSP.use();
        
        Matrix4f.mul(cam.getProjection(), cam.getView(), viewProj);  
        WaterRenderSP.setUniform("viewProj", viewProj);
		
		glBindVertexArray(vertArrayID2);
		glDrawArrays(GL_POINTS, 0, (int)gws.get(0));
	}
	
	/**
	 * Clean up OpenCL objects.
	 */
	public void destroy()
	{
        clReleaseMemObject(memAttribute);
        clReleaseMemObject(memHeight);
        clReleaseMemObject(memNormal);
        clReleaseMemObject(memWater);
        clReleaseKernel(kernelWaterSim);
        clReleaseCommandQueue(queue);
        clReleaseProgram(program);
        clReleaseContext(context);
	}
}
