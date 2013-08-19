/**
 * 
 */
package environment;

import static apiWrapper.OpenGL.*;
import static apiWrapper.OpenGL.GL_DYNAMIC_DRAW;
import static apiWrapper.OpenGL.GL_STATIC_DRAW;
import static apiWrapper.OpenGL.GL_FLOAT;
import static apiWrapper.OpenGL.GL_POINTS;
import static apiWrapper.OpenGL.GL_R16F;
import static apiWrapper.OpenGL.GL_RED;
import static apiWrapper.OpenGL.GL_RGBA;
import static apiWrapper.OpenGL.GL_BLEND;
import static apiWrapper.OpenGL.GL_TEXTURE_2D;
import static apiWrapper.OpenGL.GL_TRIANGLE_STRIP;
import static apiWrapper.OpenGL.glBindBuffer;
import static apiWrapper.OpenGL.glBindVertexArray;
import static apiWrapper.OpenGL.glBufferData;
import static apiWrapper.OpenGL.glDrawArrays;
import static apiWrapper.OpenGL.glEnableVertexAttribArray;
import static apiWrapper.OpenGL.glGenBuffers;
import static apiWrapper.OpenGL.glGenVertexArrays;
import static apiWrapper.OpenGL.glGenerateMipmap;
import static apiWrapper.OpenGL.glTexImage2D;
import static apiWrapper.OpenGL.glVertexAttribPointer;
import static apiWrapper.OpenGL.glFinish;
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
import org.lwjgl.opencl.CLCommandQueue;
import org.lwjgl.opencl.CLContext;
import org.lwjgl.opencl.CLDevice;
import org.lwjgl.opencl.CLKernel;
import org.lwjgl.opencl.CLMem;
import org.lwjgl.opencl.CLPlatform;
import org.lwjgl.opencl.CLProgram;
import org.lwjgl.opengl.Drawable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
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

	//float buffer for generating OpenCL memory objects
	private FloatBuffer heightScaleDataBuffer;
	private FloatBuffer heightDataBuffer;
	private FloatBuffer normalDataBuffer;
	private FloatBuffer attributeDataBuffer;
	private FloatBuffer gradientDataBuffer;
	private FloatBuffer tmpWHDataBuffer;
	private FloatBuffer waterDataBuffer;
	private FloatBuffer tmpWaterDataBuffer;
	private FloatBuffer velosDataBuffer;
	
	//global work size for OpenCL kernels
	private PointerBuffer gws = new PointerBuffer(1);
	
	//OpenCL memory objects
	private CLMem memHeight;
	private CLMem memNormal;
	private CLMem memAttribute;
	private CLMem memWaterHeight;
	private CLMem memHeightScale;
	private CLMem memGradient;
	private CLMem memTmpWaterHeight;
	private CLMem memWater;
	private CLMem memTmpWater;
	private CLMem memVelos;
	private CLMem memGauss;
	private CLMem memBlur;

	//szene manipulation factors
	private float rainfactor;
	private float rainstrength;
	private float oozingfactor;
	private float dampingfactor;

	//openGL IDs
	private int vertexArray = 0;
	
	//OpenCL Context and kernels
	private static CLContext context;
	private static CLCommandQueue queue;
	private static CLProgram program;
	private static CLKernel kernelRainOozing;
	private static CLKernel kernelFlow;
	private static CLKernel kernelReduce;
	private static CLKernel kernelDistribute;
	private CLKernel kernelBlur;

	//OpenGL Geometries
	private Geometry terrain;
	private Geometry sky;
	private Geometry waterMap;
	private Geometry waterBlured;
	
	//shader
	private ShaderProgram WaterRenderSP;
    private final Matrix4f viewProj = new Matrix4f();

    boolean swap = false;

    //gauss data
	private FloatBuffer gaussDataBuffer;
	private int maskSize = 11;
	private float sigma = 10;

	//ripples
	private float circle = 0.f;
	
    private static final int NORMALTEX_UNIT		 = 15;
    private static final int BUMPTEX_UNIT		 = 16;
    
    private static final int NUM_NORMAL_TEXTURES = 16;
    
    private Texture normalTex;
    private Texture bumpTex;

	/**
	 * Create water object.
	 * @param device_type OpenCl device type
	 * @param drawable	OpenCl drawable
	 * @param rain	Rainstreaks object
	 * @throws LWJGLException
	 */
	public Water(Device_Type device_type, Drawable drawable, Geometry terrain, Geometry sky) throws LWJGLException
	{		
		this.terrain = terrain;
		this.sky = sky;
		rainfactor = 0.075f;
		oozingfactor = 0.095f;
		dampingfactor = 0.005f;
		
        createCLContext(device_type, Util.getFileContents("./kernel/WaterSim.cl"), drawable);
        createWaterData();
        createKernels();
        WaterRenderSP = new ShaderProgram("./shader/Water.vsh", "./shader/Water.fsh");
        
        //create texture arrays for normal and bump maps (ripples)
        normalTex = Util.createTextureArray("media/rainTex/normal/normal", ".png", NORMALTEX_UNIT, GL_RGB, GL_RGB, NUM_NORMAL_TEXTURES);
        bumpTex = Util.createTextureArray("media/rainTex/bump/bump", ".png", BUMPTEX_UNIT, GL_RGB, GL_RGB, NUM_NORMAL_TEXTURES);
	}
	
	/**
	 * Change sharpness parameter of Gaussian blur function.
	 * @param delta added to the sharpness factor.
	 */
	public void sigma(float delta)
	{
		sigma += delta;
		createGauss();
	}
	
	/**
	 * Change mask size of Gaussian blur function.
	 * @param delta added to the mask size.
	 */
	public void size(int delta)
	{
		maskSize += delta;
		createGauss();
	}

	/**
	 * Create data for water map and OpenCl buffers.
	 */
	private void createWaterData()
	{
        //load height map data
        ImageContents contentHeight = Util.loadImage("media/terrain/terrainHeight02.png");
        int terrainDim = contentHeight.height * contentHeight.width;
        heightDataBuffer = BufferUtils.createFloatBuffer(terrainDim);
        for(int i = 0; i < heightDataBuffer.capacity(); ++i)
        {
            heightDataBuffer.put(contentHeight.data.get(i));
        }
        heightDataBuffer.rewind();		
        memHeight = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, heightDataBuffer);
                        
        //load normal map data
        ImageContents contentNorm = Util.loadImage("media/terrain/terrainNormal02.png");
        normalDataBuffer = BufferUtils.createFloatBuffer(terrainDim * 4);
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
        ImageContents contentAttrib = Util.loadImage("media/terrain/terrainAttribute02.png");
        attributeDataBuffer = BufferUtils.createFloatBuffer(terrainDim);
        for(int i = 0; i < attributeDataBuffer.capacity(); ++i)
        {
        	attributeDataBuffer.put(contentAttrib.data.get(i));
        }
        attributeDataBuffer.rewind();
        memAttribute = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, attributeDataBuffer);
        
        //load gradient map data
        ImageContents contentGradient = Util.loadImage("media/terrain/terrainGradient01.png");
        gradientDataBuffer = BufferUtils.createFloatBuffer(terrainDim);
        for(int i = 0; i < gradientDataBuffer.capacity(); ++i)
        {
        	gradientDataBuffer.put(contentGradient.data.get(i));
        }
        gradientDataBuffer.rewind();
        memGradient = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, gradientDataBuffer);

        //velocity buffer for height field fluid calculation
        velosDataBuffer = BufferUtils.createFloatBuffer(terrainDim);
        BufferUtils.zeroBuffer(velosDataBuffer);
        memVelos = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, velosDataBuffer);

        //buffer for water data
        waterDataBuffer = BufferUtils.createFloatBuffer(terrainDim);
        BufferUtils.zeroBuffer(waterDataBuffer);
        memWater = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, waterDataBuffer);
        
        //temporary buffer for water data (toggle buffer)
        tmpWaterDataBuffer = BufferUtils.createFloatBuffer(terrainDim);
        BufferUtils.zeroBuffer(tmpWaterDataBuffer);
        memTmpWater = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, tmpWaterDataBuffer);
        
        //height scaled
        heightScaleDataBuffer = BufferUtils.createFloatBuffer(terrainDim);        
        for (int i = 1; i < terrainDim*4; i = i+4) {
        	heightScaleDataBuffer.put(terrain.getVertexValueBuffer().get(i));
		}
        heightScaleDataBuffer.rewind();
        memHeightScale = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, heightScaleDataBuffer);
        
        //generate initial water map
        //set water map initially to height data
        gws.put(0, terrainDim);
        
        tmpWHDataBuffer = BufferUtils.createFloatBuffer(terrainDim);
        tmpWHDataBuffer = terrain.getVertexValueBuffer();
        memTmpWaterHeight = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, tmpWHDataBuffer);
        
        GL11.glPointSize(2f);
               
        //create water surface mesh
        //index buffer
        int size = (int) Math.sqrt(terrainDim);
        IntBuffer indexData = BufferUtils.createIntBuffer((size-1)*2*size+(size-2));
        for (int y = 0; y < size-1; y++)
        {
            for (int x = 0; x < size; x++)
            {
                indexData.put(y*size + x);
                indexData.put((y+1)*size + x);
            }
            if (y < size-2)
                indexData.put(-1);
        }
        indexData.position(0); 
        
        //vertex buffer (deep copy)
        FloatBuffer waterBuffer = BufferUtils.createFloatBuffer(terrainDim*4);
        waterBuffer.put(terrain.getVertexValueBuffer());
        terrain.getVertexValueBuffer().rewind();
        waterBuffer.rewind();
        
        waterMap = new Geometry();
        waterMap.setIndices(indexData, GL_TRIANGLE_STRIP);
        waterMap.setVertices(waterBuffer);
        waterMap.addVertexAttribute(ShaderProgram.ATTR_POS, 4, 0);
        waterMap.construct();
        waterBuffer.rewind();

        //water as points for visualization purposes
        memWaterHeight = clCreateFromGLBuffer(context, CL_MEM_READ_WRITE, waterMap.getVbid());
        vertexArray = glGenVertexArrays();
    	glBindVertexArray(vertexArray);
    	glBindBuffer(GL_ARRAY_BUFFER, waterMap.getVbid());        

    	glEnableVertexAttribArray(ShaderProgram.ATTR_POS);
    	glVertexAttribPointer(ShaderProgram.ATTR_POS, 4, GL_FLOAT, false, 16,  0);
    	
    	glBindVertexArray(0);
        
    	//water blurred with Gauss
        waterBlured = new Geometry();
        waterBlured.setIndices(indexData, GL_TRIANGLE_STRIP);
        waterBlured.setVertices(waterBuffer);
        waterBlured.addVertexAttribute(ShaderProgram.ATTR_POS, 4, 0);
        waterBlured.construct();
        
        memBlur = clCreateFromGLBuffer(context, CL_MEM_READ_WRITE, waterBlured.getVbid());
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
		//kernel for rain and oozing
		kernelRainOozing = clCreateKernel(program, "rainOozing");
		kernelRainOozing.setArg(0, memWater);
		kernelRainOozing.setArg(1, memAttribute);
		kernelRainOozing.setArg(2, memTmpWater);
		
		//kernel for water flow
		kernelFlow = clCreateKernel(program, "flowWaterTangential");
		kernelFlow.setArg(0, memWater);
		kernelFlow.setArg(1, memHeight);
		kernelFlow.setArg(2, memTmpWater);
		
		//kernel to reduce flowed water 
		kernelReduce = clCreateKernel(program, "reduceFlowedWater");
		kernelReduce.setArg(0, memWater);
		kernelReduce.setArg(1, memHeight);
		kernelReduce.setArg(2, memTmpWater);
		
		//kernel to distribute water
		kernelDistribute = clCreateKernel(program, "distributeWater");
		kernelDistribute.setArg(1, memHeightScale);
		kernelDistribute.setArg(2, memWater);
		kernelDistribute.setArg(4, memVelos);
		
		//kernel gauss
		kernelBlur = clCreateKernel(program, "blurWater");
		kernelBlur.setArg(2, memGauss);
		kernelBlur.setArg(3, maskSize);
		
		createGauss();
	}
	
	/**
	 * Simulate the water.
	 * @param deltaTime Time past since last update.
	 */
	public void updateSimulation(long deltaTime)
	{	        	  
		glFinish();
	    clEnqueueAcquireGLObjects(queue, memWaterHeight, null, null);
	    clEnqueueAcquireGLObjects(queue, memBlur, null, null);
	    
	    rainstrength = (float) ((double) Math.log(Rainstreaks.getMaxParticles()) / Math.log(2))/10.0f - 1.0f; // 0..1
	    kernelRainOozing.setArg( 3, rainstrength*rainfactor);
	    kernelRainOozing.setArg( 4, oozingfactor);
	    kernelRainOozing.setArg( 5, 1e-3f*deltaTime);	    
        clEnqueueNDRangeKernel(queue, kernelRainOozing, 1, null, gws, null, null, null); 
        
        //global sync
	    kernelFlow.setArg( 3, 1e-3f*deltaTime);	    
        clEnqueueNDRangeKernel(queue, kernelFlow, 1, null, gws, null, null, null);            
        
        //global sync
	    kernelReduce.setArg( 3, 1e-3f*deltaTime);	    
        clEnqueueNDRangeKernel(queue, kernelReduce, 1, null, gws, null, null, null);            
        
        //global sync
        if (swap)
        {
        	kernelDistribute.setArg( 0, memTmpWaterHeight);
        	kernelDistribute.setArg( 3, memWaterHeight);
        	swap = !swap;
        }
        else
        {
        	kernelDistribute.setArg( 0, memWaterHeight);
        	kernelDistribute.setArg( 3, memTmpWaterHeight);
        	swap = !swap;
        }
	    kernelDistribute.setArg( 5, dampingfactor);
	    kernelDistribute.setArg( 6, 1e-3f*deltaTime);	    
        clEnqueueNDRangeKernel(queue, kernelDistribute, 1, null, gws, null, null, null); 

        kernelBlur.setArg( 0, memWaterHeight);
        kernelBlur.setArg( 1, memBlur);

        clEnqueueNDRangeKernel(queue, kernelBlur, 1, null, gws, null, null, null);
        
        clEnqueueReleaseGLObjects(queue, memWaterHeight, null, null);
        clEnqueueReleaseGLObjects(queue, memBlur, null, null);
        
        clFinish(queue);
	}
	
	/**
	 * Compile OpenCL source code.
	 */
	public void compile()
	{
        CLProgram p = clCreateProgramWithSource(context, Util.getFileContents("./kernel/WaterSim.cl"));

        if(clBuildProgram(p, this.device, "", null))
        {
        	program = p;
        	createKernels();
        }
	}
	
	/**
	 * Create Gauss blur kernel and all significant objects.
	 */
	public void createGauss()
	{ 
		if(memGauss != null)
		{
			OpenCL.clReleaseMemObject(memGauss);
		}
		gaussDataBuffer = BufferUtils.createFloatBuffer(maskSize*maskSize);
    	gaussDataBuffer.put(getGaussianBlur(maskSize, sigma));
    	gaussDataBuffer.rewind();
        memGauss = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, gaussDataBuffer);
		
		kernelBlur.setArg(2, memGauss);
		kernelBlur.setArg(3, maskSize);
	}
	
	/**
	 * Create mask for Gaussian blur function.
	 * @param size of the mask
	 * @param sigma factor of sharpness
	 * @return float array containing mask
	 */
    public float[] getGaussianBlur(int size, double sigma)
    {
        float data[] = new float[size * size];
        int halfSize = size / 2;
        float sum = 0.0f;
        for(int i = 0; i < size; ++i)
        {
            for(int j = 0; j < size; ++j)
            {
                double x = i - halfSize;
                double y = j - halfSize;
                data[i * size + j] = (float)(1.0 / (Math.PI * 2 * sigma * sigma) * Math.exp(-(x * x + y * y) / (2 * sigma * sigma)));
                sum += data[i * size +j];
            }
        }
        
        for(int i = 0; i < size; ++i)
        {
            for(int j = 0; j < size; ++j)
            {
                data[i * size + j] /= sum;
            }
        }
        
        return data;
    }
	
	/**
	 * Draw the water surfaces on the scene.
	 * @param cam Camera object
	 */
	public void draw(Camera cam, boolean points, int scaleTerrain, Vector3f fogThickness, Sun sun)
	{
    	WaterRenderSP.use();
        
        Matrix4f.mul(cam.getProjection(), cam.getView(), viewProj);  
        WaterRenderSP.setUniform("viewProj", viewProj);
        WaterRenderSP.setUniform("scale", scaleTerrain);
        WaterRenderSP.setUniform("normalTex", terrain.getNormalTex());
        WaterRenderSP.setUniform("rainNormalTex", normalTex);
        WaterRenderSP.setUniform("rainBumpTex", bumpTex);
        WaterRenderSP.setUniform("skyTex", sky.getColorTex());
        WaterRenderSP.setUniform("eyePosition", cam.getCamPos());
        WaterRenderSP.setUniform("lightPos", sun.getDirection());
        WaterRenderSP.setUniform("fogThickness", fogThickness);
        WaterRenderSP.setUniform("circle", circle);
        //next step in circle for ripple animation, depends on rain strength
        circle = (circle >= 15.f) ? 0.f : circle + rainstrength;
        
        //draw point visualization of water if enabled
        if (points)
        {
        	glDisable(GL_BLEND);
        	glBindVertexArray(vertexArray);
        	glDrawArrays(GL_POINTS, 0, (int)gws.get(0));
        }
        //else draw triangle mesh
        else
        {
        	waterBlured.draw();
        }
	}
	
	/**
	 * Clean up OpenCL objects.
	 */
	public void destroy()
	{
		GL30.glDeleteVertexArrays(vertexArray);
		waterMap.delete();
		clReleaseMemObject(memGradient);
		clReleaseMemObject(memAttribute);
        clReleaseMemObject(memHeight);
        clReleaseMemObject(memNormal);
        clReleaseMemObject(memHeightScale);
        clReleaseMemObject(memTmpWater);
        clReleaseMemObject(memTmpWaterHeight);
        clReleaseMemObject(memVelos);
        clReleaseMemObject(memWater);
        clReleaseMemObject(memWaterHeight);
        
        clReleaseKernel(kernelReduce);
        clReleaseKernel(kernelFlow);
        clReleaseProgram(program);
        clReleaseCommandQueue(queue);
        //clReleaseContext(context);
	}
}
