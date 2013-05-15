package util;

import static opengl.GL.GL_FLOAT;
import static opengl.GL.GL_SHORT;
import static opengl.GL.GL_UNSIGNED_BYTE;
import static opengl.GL.GL_R8;
import static opengl.GL.GL_RED;
import static opengl.GL.GL_RG;
import static opengl.GL.GL_RG8;
import static opengl.GL.GL_RGB8;
import static opengl.GL.GL_RGBA;
import static opengl.GL.GL_RGB;
import static opengl.GL.GL_RGBA8;
import static opengl.GL.GL_STATIC_DRAW;
import static opengl.GL.GL_TEXTURE_2D;
import static opengl.GL.GL_TEXTURE_2D_ARRAY;
import static opengl.GL.GL_UNSIGNED_INT;
import static opengl.GL.glDrawElements;
import static opengl.GL.glGenerateMipmap;
import static opengl.GL.glGetUniformLocation;
import static opengl.GL.glTexImage2D;
import static opengl.GL.glUniform1f;
import static opengl.GL.glUniform3f;
import static opengl.GL.glGenVertexArrays;
import static opengl.GL.glBindVertexArray;
import static opengl.GL.glGenBuffers;
import static opengl.GL.GL_ARRAY_BUFFER;
import static opengl.GL.GL_DYNAMIC_DRAW;
import static opengl.GL.GL_POINTS;
import static opengl.GL.glVertexAttribPointer;
import static opengl.GL.glEnableVertexAttribArray;
import static opengl.GL.glBindBuffer;
import static opengl.GL.glBufferData;
import static opengl.GL.glTexImage3D;
import static opengl.GL.glTexSubImage3D;
import static opengl.GL.glDrawArrays;
import static opengl.GL.glBindTexture;
import static opengl.GL.glEnable;

import static opengl.OpenCL.CL_MEM_COPY_HOST_PTR;
import static opengl.OpenCL.CL_MEM_USE_HOST_PTR;
import static opengl.OpenCL.CL_MEM_READ_WRITE;
import static opengl.OpenCL.CL_MEM_READ_ONLY;
import static opengl.OpenCL.clBuildProgram;
import static opengl.OpenCL.clCreateBuffer;
import static opengl.OpenCL.clCreateCommandQueue;
import static opengl.OpenCL.clCreateFromGLBuffer;
import static opengl.OpenCL.clCreateKernel;
import static opengl.OpenCL.clCreateProgramWithSource;
import static opengl.OpenCL.clEnqueueAcquireGLObjects;
import static opengl.OpenCL.clEnqueueNDRangeKernel;
import static opengl.OpenCL.clEnqueueReleaseGLObjects;
import static opengl.OpenCL.clEnqueueWriteBuffer;
import static opengl.OpenCL.clFinish;
import static opengl.OpenCL.clReleaseCommandQueue;
import static opengl.OpenCL.clReleaseContext;
import static opengl.OpenCL.clReleaseKernel;
import static opengl.OpenCL.clReleaseMemObject;
import static opengl.OpenCL.clReleaseProgram;
import static opengl.OpenCL.create;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.text.DecimalFormat;
import java.util.Random;

import javax.imageio.ImageIO;

import opengl.GL;
import opengl.OpenCL;
import opengl.OpenCL.Device_Type;

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
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import util.Util.ImageContents;

/**
 * Raindrop particle system
 * @author Valentin Bruder (vbruder@uos.de)
 */
public class Raindrops {
    
    private static final int RAINTEX_UNIT = 6;
    private static final int HEIGHTTEX_UNIT = 4;
    
    private static final int numTextures = 370;
    
    //opencl pointer
    private CLContext context;
    private CLProgram program;
    private CLDevice device;
    private CLCommandQueue queue;
    private CLKernel kernelMoveStreaks;
    private CLKernel kernel1;
    
    //data
    private FloatBuffer posBuffer, seedBuffer, veloBuffer, vertexDataBuffer;
    
    //opencl buffer
    private CLMem position, velos, seed, heightmap, normalmap;
    
    //particle settings
    private int maxParticles;
    private float clusterScale;
    private float veloFactor;
    
    //kernel settings
    private int localWorkSize = 128;
    private final PointerBuffer gwz = BufferUtils.createPointerBuffer(1);
    private final PointerBuffer lwz = BufferUtils.createPointerBuffer(1);
    
    // terrain texture IDs
    private int heightTexId, normalTexId;
    private Texture hTex, rainTex;
    
    //shader
    private ShaderProgram StreakRenderSP;
    private Vector3f eyePos = new Vector3f(0.f, 0.f, 0.f);
    private final Matrix4f viewProj = new Matrix4f();
    //array IDs
	private int vertArrayID;
	//buffer IDs
    private int vertBufferID;
    
    //lighting parameters (uniforms in StreakRender FS)
    private Vector3f sunDir = new Vector3f(10.0f, 10.0f, 0.0f);
    private Vector3f sunColor = new Vector3f(1.0f, 1.0f, 1.0f);
    private float sunIntensity = 0.025f;
    private Vector3f pointLightColor = new Vector3f(1.0f, 1.0f, 1.0f);
    private Vector3f pointLightDir = new Vector3f(1.0f, 1.0f, 1.0f);
    private float pointLightIntensity = 1.0f;
    
    /**
     * particle system
     * @param device_type GPU /CPU
     * @param drawable OpenGL drawable.
     * @throws LWJGLException
     */
    public Raindrops(Device_Type device_type, Drawable drawable, int heightTexId, int normalTexId, int maxParticles, Camera cam) throws LWJGLException {
        
        this.maxParticles = maxParticles;
        this.heightTexId = heightTexId;
        this.normalTexId = normalTexId;
        this.eyePos = cam.getCamPos();
        //range of cylinder around cam
        clusterScale = 5.0f;
        //velocity factor
        veloFactor = 60.0f;
        
        this.gwz.put(0, this.maxParticles);
        this.lwz.put(0, this.localWorkSize);  
        
        //openCL context
        createCLContext(device_type, Util.getFileContents("./shader/RainSim.cl"), drawable);
        createData();
        createBuffer();
        createKernels();
        createShaderProgram();
    }
    
    /**
     * Sets the called device (GPU/CPU) (if existent else throw error)
     * Creates context, queue and program.
     * @param type
     * @param source
     * @param drawable
     * @throws LWJGLException
     */
    private void createCLContext(Device_Type type, String source, Drawable drawable) throws LWJGLException {
        
        int device_type;
        
        switch(type) {
        case CPU: device_type = CL10.CL_DEVICE_TYPE_CPU; break;
        case GPU: device_type = CL10.CL_DEVICE_TYPE_GPU; break;
        default: throw new IllegalArgumentException("Wrong device type!");
        }
        
        CLPlatform platform = null;
        
        for(CLPlatform plf : CLPlatform.getPlatforms()) {
            if(plf.getDevices(device_type) != null) {
                this.device = plf.getDevices(device_type).get(0);
                platform = plf;
                if(this.device != null) {
                    break;
                }
            }
        }             
        
        this.context = create(platform, platform.getDevices(device_type), null, drawable);      
        this.queue = clCreateCommandQueue(this.context, this.device, 0);       
        this.program = clCreateProgramWithSource(this.context, source);

        clBuildProgram(this.program, this.device, "", null);
    }
    
    public ShaderProgram getShaderProgram() {
        return this.StreakRenderSP;
    }
    
    /**
     * Creates OpenGL shader program to render particles
     */
    private void createShaderProgram() { 
        
        this.StreakRenderSP = new ShaderProgram("./shader/StreakRender.vsh", "./shader/StreakRender.gsh", "./shader/StreakRender.fsh");
        
        //load the 370 point light textures into a texture array
        ImageContents content = Util.loadImage("media/rainTex/point/cv0_vPos_000.png");       
        rainTex = new Texture(GL_TEXTURE_2D_ARRAY, RAINTEX_UNIT);
        rainTex.bind();
        glTexImage3D(   GL_TEXTURE_2D_ARRAY,
                        0,
                        GL30.GL_R16,
                        content.width,
                        content.height,
                        numTextures,
                        0,
                        GL_RED,
                        GL_FLOAT,
                        null);

        for (int i = 0; i < numTextures; i++)
        {
            DecimalFormat df =   new DecimalFormat  ( "000" );
            content = Util.loadImage("media/rainTex/point/cv0_vPos_" + df.format(i) + ".png");
            glTexSubImage3D(GL_TEXTURE_2D_ARRAY,
                            0,
                            0,
                            0,
                            i,
                            content.width,
                            content.height,
                            1,
                            GL_RED,
                            GL_FLOAT,
                            content.data);
        }
        glGenerateMipmap(GL_TEXTURE_2D_ARRAY);
    }
    

    
    /**
     * Create initial position and velocity data pseudo randomly.
     */
    private void createData() {      
        
	    //init attribute buffer: position, starting position (seed), velocity, random and texture type
		posBuffer  = BufferUtils.createFloatBuffer(4 * maxParticles);
		seedBuffer = BufferUtils.createFloatBuffer(4 * maxParticles);
		veloBuffer = BufferUtils.createFloatBuffer(4 * maxParticles);
		
		Random r = new Random(1);
		
		//fill buffers
		for (int i = 0; i < this.maxParticles; i++) {

		    //TODO: LOD particle distribution
            //spawning position
            float x = (r.nextFloat() - 0.5f) * clusterScale;
            float y;
            do
                y = (r.nextFloat()) * clusterScale;
            while (y < 0.1f);   
            float z = (r.nextFloat() - 0.5f) * clusterScale;
            
            //add to seed buffer
            seedBuffer.put(x);
            seedBuffer.put(y);
            seedBuffer.put(z);
            //add random type to w coordinate in buffer
            //type is for choosing 1 out of 10 different textures
            seedBuffer.put((float) r.nextInt(10));
            
            //add to position buffer
            posBuffer.put(x);
            posBuffer.put(y);
            posBuffer.put(z);
            posBuffer.put(1.f);
            
            //add spawning velocity (small random velocity in x- and z-direction for variety and against AA 
            veloBuffer.put(veloFactor*(r.nextFloat() / 20.f));
            veloBuffer.put(veloFactor*((r.nextFloat() + 0.2f) / 10.f));
            veloBuffer.put(veloFactor*(r.nextFloat() / 20.f));
            //add random number in w coordinate, used to light up random streaks
            float tmpR = r.nextFloat();
            if (tmpR > 0.75f) {
                veloBuffer.put(1.f + tmpR);
            }
            else {
                veloBuffer.put(1.f);
            }
        }
		//flip buffers
        posBuffer.position(0);
        seedBuffer.position(0);
        veloBuffer.position(0);
                
        //3 buffers * 4 floats * maxParticles
        vertexDataBuffer = BufferUtils.createFloatBuffer(3 * 4 * maxParticles);
        for (int i = 0; i < maxParticles; i++)
        {
            vertexDataBuffer.put(posBuffer.get(i + 0));
            vertexDataBuffer.put(posBuffer.get(i + 1));
            vertexDataBuffer.put(posBuffer.get(i + 2));
            vertexDataBuffer.put(posBuffer.get(i + 3));
            
            vertexDataBuffer.put(seedBuffer.get(i + 0));
            vertexDataBuffer.put(seedBuffer.get(i + 1));
            vertexDataBuffer.put(seedBuffer.get(i + 2));
            vertexDataBuffer.put(seedBuffer.get(i + 3));
            
            vertexDataBuffer.put(veloBuffer.get(i + 0));
            vertexDataBuffer.put(veloBuffer.get(i + 1));
            vertexDataBuffer.put(veloBuffer.get(i + 2));
            vertexDataBuffer.put(veloBuffer.get(i + 3));
        }
        
        posBuffer.position(0);
        seedBuffer.position(0);
        veloBuffer.position(0);
        vertexDataBuffer.position(0);
    }
    
    /**
     * Creates all significant OpenCL buffers
     */
    private void createBuffer() {
   	
    	this.vertexDataBuffer.position(0);
    	
    	this.vertArrayID = glGenVertexArrays();
    	glBindVertexArray(this.vertArrayID);
    	
        this.vertBufferID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, this.vertBufferID);
        glBufferData(GL_ARRAY_BUFFER, this.vertexDataBuffer, GL_DYNAMIC_DRAW);
        
        glEnableVertexAttribArray(ShaderProgram.ATTR_POS);
        glEnableVertexAttribArray(ShaderProgram.ATTR_SEED);
        glEnableVertexAttribArray(ShaderProgram.ATTR_VELO);
        
        glVertexAttribPointer(ShaderProgram.ATTR_POS, 4, GL_FLOAT, false, 16, 0);
        glVertexAttribPointer(ShaderProgram.ATTR_SEED, 4, GL_FLOAT, false, 16, 16);      
        glVertexAttribPointer(ShaderProgram.ATTR_VELO, 4, GL_FLOAT, false, 16, 32);
        
        glBindVertexArray(0);
        
        this.position = clCreateFromGLBuffer(this.context, CL_MEM_READ_WRITE, vertBufferID);
        this.velos = clCreateBuffer(this.context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, this.veloBuffer);
        this.seed = clCreateBuffer(this.context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, this.seedBuffer);
        
        //load hight map
        IntBuffer errorCheck = BufferUtils.createIntBuffer(1);
        
        ImageContents content = Util.loadImage("media/highmaps/map1.png");
        FloatBuffer data = BufferUtils.createFloatBuffer(content.height * content.width);
        for(int i = 0; i < content.height * content.width; ++i)
        {
        	data.put(content.data.get(4 * i));
        }
        data.position(0);
        
        hTex = new Texture(GL_TEXTURE_2D, HEIGHTTEX_UNIT);
        hTex.bind();
        glTexImage2D(GL_TEXTURE_2D,
                0,
                GL30.GL_R16F,
                content.width,
                content.height,
                0,
                GL11.GL_RED,
                GL_FLOAT,
                data);
        glGenerateMipmap(GL_TEXTURE_2D);

        this.heightmap = CL10GL.clCreateFromGLTexture2D(this.context, CL10.CL_MEM_READ_ONLY, GL11.GL_TEXTURE_2D, 0, hTex.getId(), errorCheck);
        this.normalmap = CL10GL.clCreateFromGLTexture2D(this.context, CL10.CL_MEM_READ_ONLY, GL11.GL_TEXTURE_2D, 0, this.normalTexId, errorCheck);
        
        OpenCL.checkError(errorCheck.get(0));
    }
    
    /**
     * Creates two OpenCL kernels.
     */
    private void createKernels() {
               
        //kernel
        this.kernelMoveStreaks = clCreateKernel(this.program, "rain_sim");
        this.kernelMoveStreaks.setArg(0, this.position);
        this.kernelMoveStreaks.setArg(1, this.velos);
        this.kernelMoveStreaks.setArg(2, this.seed);
        this.kernelMoveStreaks.setArg(3, this.heightmap);
        this.kernelMoveStreaks.setArg(4, this.normalmap);
        this.kernelMoveStreaks.setArg(5, this.maxParticles);
        this.kernelMoveStreaks.setArg(6, 0.f);
        this.kernelMoveStreaks.setArg(7, 0.f);
        this.kernelMoveStreaks.setArg(8, 0.f);
        this.kernelMoveStreaks.setArg(9, 0.f);
    }
    
    /**
     * Calls kernel to update into the next time step. Is called once each frame. 
     * @param deltaTime
     */
    public void updateSimulation(long deltaTime) {
        
        clEnqueueAcquireGLObjects(this.queue, this.position, null, null);
        clEnqueueAcquireGLObjects(this.queue, this.heightmap, null, null);
        clEnqueueAcquireGLObjects(this.queue, this.normalmap, null, null);
           
        this.kernelMoveStreaks.setArg(6, 1e-3f*deltaTime);
        this.kernelMoveStreaks.setArg(7, eyePos.x);
        this.kernelMoveStreaks.setArg(8, eyePos.y);
        this.kernelMoveStreaks.setArg(9, eyePos.z);
        clEnqueueNDRangeKernel(this.queue, kernelMoveStreaks, 1, null, gwz, lwz, null, null);            

        clEnqueueReleaseGLObjects(this.queue, this.position, null, null);
        clEnqueueReleaseGLObjects(this.queue, this.heightmap, null, null);
        clEnqueueReleaseGLObjects(this.queue, this.normalmap, null, null);
        
        clFinish(this.queue);
    }
    
    /**
     * draws the particles
     * @param cam Camera
     */
    public void draw(Camera cam) {
        
    	StreakRenderSP.use();
        
    	eyePos = new Vector3f(cam.getCamPos().x, cam.getCamPos().y, cam.getCamPos().z);
        Matrix4f.mul(cam.getProjection(), cam.getView(), viewProj);  
        StreakRenderSP.setUniform("viewProj", viewProj);
        StreakRenderSP.setUniform("rainTex", rainTex);
        StreakRenderSP.setUniform("eyePosition", eyePos);
        //set lighting uniforms
        StreakRenderSP.setUniform("sunDir", sunDir);
        StreakRenderSP.setUniform("sunColor", sunColor);
        StreakRenderSP.setUniform("sunIntensity", sunIntensity);
//        StreakRenderSP.setUniform("pointLightDir", pointLightDir);
//        StreakRenderSP.setUniform("pointLightColor", pointLightColor);
        StreakRenderSP.setUniform("pointLightIntensity", pointLightIntensity);
        
        glBindVertexArray(vertArrayID);
        glDrawArrays(GL_POINTS, 0, maxParticles); 
    }
      
    /**
     * Frees memory.
     */
    public void destroy() {
        clReleaseMemObject(this.position);
        clReleaseMemObject(this.velos);
        clReleaseMemObject(this.seed);
        clReleaseMemObject(this.heightmap);
        clReleaseMemObject(this.normalmap);
        clReleaseKernel(this.kernelMoveStreaks);
        clReleaseKernel(this.kernel1);
        clReleaseCommandQueue(this.queue);
        clReleaseProgram(this.program);
        clReleaseContext(this.context);
    }
}

