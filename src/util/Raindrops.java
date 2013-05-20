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
import static opengl.GL.GL_TEXTURE_1D;
import static opengl.GL.GL_TEXTURE_2D;
import static opengl.GL.GL_TEXTURE_2D_ARRAY;
import static opengl.GL.GL_UNSIGNED_INT;
import static opengl.GL.glDrawElements;
import static opengl.GL.glGenerateMipmap;
import static opengl.GL.glGetUniformLocation;
import static opengl.GL.glTexImage1D;
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
    
    //TODO: implement proper unit count
    private static final int HEIGHTTEX_UNIT = 4;
    private static final int RAINTEX_UNIT = 6;
    private static final int RAINFACTORS_UNIT = 7;
    
    private static final int numTextures = 370;
    
    //opencl pointer
    private CLContext context;
    private CLProgram program;
    private CLDevice device;
    private CLCommandQueue queue;
    private CLKernel kernelMoveStreaks;
    
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
    
    private Vector3f windDir[] = new Vector3f[20];
    private int windPtr = 0;
    private float windForce = 0.1f;
    
    // terrain texture IDs
    private int heightTexId, normalTexId;
    private Texture hTex, rainTex, rainfactTex;
    
    //shader
    private ShaderProgram StreakRenderSP;
    private Vector3f eyePos = new Vector3f(0.f, 0.f, 0.f);
    private final Matrix4f viewProj = new Matrix4f();
    //array IDs
	private int vertArrayID;
	//buffer IDs
    private int vertBufferID;
    
    //lighting parameters (uniforms in StreakRender FS)
    private Vector3f sunDir = new Vector3f(10.0f, 10.0f, 10.0f);
    private Vector3f sunColor = new Vector3f(1.0f, 1.0f, 1.0f);
    private float sunIntensity = 0.05f;
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
        
        createRainfactors();
    }

    /**
     * Create initial position and velocity data pseudo randomly.
     */
    private void createData() {      
        
        Random r = new Random(1);
        
        float windRand = r.nextFloat() * windForce;
        
        for (int i = 0; i < windDir.length; i++)
        {
            windDir[i] = new Vector3f();
            windDir[i].x = (float) Math.sin((float) i / (float) windDir.length) * windRand;
            windDir[i].z = (float) Math.sin((float) i / (float) windDir.length) * windRand;
        }
        
	    //init attribute buffer: position, starting position (seed), velocity, random and texture type
		posBuffer  = BufferUtils.createFloatBuffer(4 * maxParticles);
		seedBuffer = BufferUtils.createFloatBuffer(4 * maxParticles);
		veloBuffer = BufferUtils.createFloatBuffer(4 * maxParticles);
		
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
        //Eye position
        this.kernelMoveStreaks.setArg(7, 0.f);
        this.kernelMoveStreaks.setArg(8, 0.f);
        this.kernelMoveStreaks.setArg(9, 0.f);
        //Wind direction
        this.kernelMoveStreaks.setArg(10, 0.f);
        this.kernelMoveStreaks.setArg(11, 0.f);
    }
    
    /**
     * Calls kernel to update into the next time step. Is called once each frame. 
     * @param deltaTime
     */
    public void updateSimulation(long deltaTime) {
        
        clEnqueueAcquireGLObjects(this.queue, this.position, null, null);
        clEnqueueAcquireGLObjects(this.queue, this.heightmap, null, null);
        clEnqueueAcquireGLObjects(this.queue, this.normalmap, null, null);
           
        this.kernelMoveStreaks.setArg( 6, 1e-3f*deltaTime);
        this.kernelMoveStreaks.setArg( 7, eyePos.x);
        this.kernelMoveStreaks.setArg( 8, eyePos.y);
        this.kernelMoveStreaks.setArg( 9, eyePos.z);
        this.kernelMoveStreaks.setArg(10, windDir[windPtr].x);
        this.kernelMoveStreaks.setArg(11, windDir[windPtr].z);
        clEnqueueNDRangeKernel(this.queue, kernelMoveStreaks, 1, null, gwz, lwz, null, null);            

        clEnqueueReleaseGLObjects(this.queue, this.position, null, null);
        clEnqueueReleaseGLObjects(this.queue, this.heightmap, null, null);
        clEnqueueReleaseGLObjects(this.queue, this.normalmap, null, null);
        
        clFinish(this.queue);
        
        if (windPtr < windDir.length - 1)
        {
            windPtr++;
        }
        else
            windPtr = 0;
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
        StreakRenderSP.setUniform("rainfactors", rainfactTex);
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
        clReleaseCommandQueue(this.queue);
        clReleaseProgram(this.program);
        clReleaseContext(this.context);
    }
    
    /**
     * Generate a 1D texture of normalization factors for the rain textures, one per texture.
     */
    private void createRainfactors()
    {
        float rainfactors[] =
        {
            0.004535f, 0.014777f, 0.012512f, 0.130630f, 0.013893f, 0.125165f, 0.011809f, 0.244907f, 0.010722f, 0.218252f,
            0.011450f, 0.016406f, 0.015855f, 0.055476f, 0.015024f, 0.067772f, 0.021120f, 0.118653f, 0.018705f, 0.142495f, 
            0.004249f, 0.017267f, 0.042737f, 0.036384f, 0.043433f, 0.039413f, 0.058746f, 0.038396f, 0.065664f, 0.054761f, 
            0.002484f, 0.003707f, 0.004456f, 0.006006f, 0.004805f, 0.006021f, 0.004263f, 0.007299f, 0.004665f, 0.007037f, 
            0.002403f, 0.004809f, 0.004978f, 0.005211f, 0.004855f, 0.004936f, 0.006266f, 0.007787f, 0.006973f, 0.007911f, 
            0.004843f, 0.007565f, 0.007675f, 0.011109f, 0.007726f, 0.012165f, 0.013179f, 0.021546f, 0.013247f, 0.012964f, 
            0.105644f, 0.126661f, 0.128746f, 0.101296f, 0.123779f, 0.106198f, 0.123470f, 0.129170f, 0.116610f, 0.137528f, 
            0.302834f, 0.379777f, 0.392745f, 0.339152f, 0.395508f, 0.334227f, 0.374641f, 0.503066f, 0.387906f, 0.519618f, 
            0.414521f, 0.521799f, 0.521648f, 0.498219f, 0.511921f, 0.490866f, 0.523137f, 0.713744f, 0.516829f, 0.743649f, 
            0.009892f, 0.013868f, 0.034567f, 0.025788f, 0.034729f, 0.036399f, 0.030606f, 0.017303f, 0.051809f, 0.030852f, 
            0.018874f, 0.027152f, 0.031625f, 0.023033f, 0.038150f, 0.024483f, 0.029034f, 0.021801f, 0.037730f, 0.016639f, 
            0.002868f, 0.004127f, 0.133022f, 0.013847f, 0.123368f, 0.012993f, 0.122183f, 0.015031f, 0.126043f, 0.015916f, 
            0.002030f, 0.002807f, 0.065443f, 0.002752f, 0.069440f, 0.002810f, 0.081357f, 0.002721f, 0.076409f, 0.002990f, 
            0.002425f, 0.003250f, 0.003180f, 0.011331f, 0.002957f, 0.011551f, 0.003387f, 0.006086f, 0.002928f, 0.005548f, 
            0.003664f, 0.004258f, 0.004269f, 0.009404f, 0.003925f, 0.009233f, 0.004224f, 0.009405f, 0.004014f, 0.008435f, 
            0.038058f, 0.040362f, 0.035946f, 0.072104f, 0.038315f, 0.078789f, 0.037069f, 0.077795f, 0.042554f, 0.073945f, 
            0.124160f, 0.122589f, 0.121798f, 0.201886f, 0.122283f, 0.214549f, 0.118196f, 0.192104f, 0.122268f, 0.209397f, 
            0.185212f, 0.181729f, 0.194527f, 0.420721f, 0.191558f, 0.437096f, 0.199995f, 0.373842f, 0.192217f, 0.386263f, 
            0.003520f, 0.053502f, 0.060764f, 0.035197f, 0.055078f, 0.036764f, 0.048231f, 0.052671f, 0.050826f, 0.044863f, 
            0.002254f, 0.023290f, 0.082858f, 0.043008f, 0.073780f, 0.035838f, 0.080650f, 0.071433f, 0.073493f, 0.026725f, 
            0.002181f, 0.002203f, 0.112864f, 0.060140f, 0.115635f, 0.065531f, 0.093277f, 0.094123f, 0.093125f, 0.144290f, 
            0.002397f, 0.002369f, 0.043241f, 0.002518f, 0.040455f, 0.002656f, 0.002540f, 0.090915f, 0.002443f, 0.101604f, 
            0.002598f, 0.002547f, 0.002748f, 0.002939f, 0.002599f, 0.003395f, 0.002733f, 0.003774f, 0.002659f, 0.004583f, 
            0.003277f, 0.003176f, 0.003265f, 0.004301f, 0.003160f, 0.004517f, 0.003833f, 0.008354f, 0.003140f, 0.009214f, 
            0.008558f, 0.007646f, 0.007622f, 0.026437f, 0.007633f, 0.021560f, 0.007622f, 0.017570f, 0.007632f, 0.018037f, 
            0.031062f, 0.028428f, 0.028428f, 0.108300f, 0.028751f, 0.111013f, 0.028428f, 0.048661f, 0.028699f, 0.061490f, 
            0.051063f, 0.047597f, 0.048824f, 0.129541f, 0.045247f, 0.124975f, 0.047804f, 0.128904f, 0.045053f, 0.119087f, 
            0.002197f, 0.002552f, 0.002098f, 0.200688f, 0.002073f, 0.102060f, 0.002111f, 0.163116f, 0.002125f, 0.165419f, 
            0.002060f, 0.002504f, 0.002105f, 0.166820f, 0.002117f, 0.144274f, 0.005074f, 0.143881f, 0.004875f, 0.205333f, 
            0.001852f, 0.002184f, 0.002167f, 0.163804f, 0.002132f, 0.212644f, 0.003431f, 0.244546f, 0.004205f, 0.315848f, 
            0.002450f, 0.002360f, 0.002243f, 0.154635f, 0.002246f, 0.148259f, 0.002239f, 0.348694f, 0.002265f, 0.368426f, 
            0.002321f, 0.002393f, 0.002376f, 0.074124f, 0.002439f, 0.126918f, 0.002453f, 0.439270f, 0.002416f, 0.489812f, 
            0.002484f, 0.002629f, 0.002559f, 0.150246f, 0.002579f, 0.140103f, 0.002548f, 0.493103f, 0.002637f, 0.509481f, 
            0.002960f, 0.002952f, 0.002880f, 0.294884f, 0.002758f, 0.332805f, 0.002727f, 0.455842f, 0.002816f, 0.431807f, 
            0.003099f, 0.003028f, 0.002927f, 0.387154f, 0.002899f, 0.397946f, 0.002957f, 0.261333f, 0.002909f, 0.148548f, 
            0.004887f, 0.004884f, 0.006581f, 0.414647f, 0.003735f, 0.431317f, 0.006426f, 0.148997f, 0.003736f, 0.080715f, 
            0.001969f, 0.002159f, 0.002325f, 0.200211f, 0.002288f, 0.202137f, 0.002289f, 0.595331f, 0.002311f, 0.636097f 
        };
        
        FloatBuffer rainfactBuffer = BufferUtils.createFloatBuffer(numTextures);

        rainfactBuffer.put(rainfactors, 0, numTextures);
        rainfactBuffer.position(0);
        
        rainfactTex = new Texture(GL_TEXTURE_1D, RAINFACTORS_UNIT);
        rainfactTex.bind();
        glTexImage1D(   GL_TEXTURE_1D,
                        0,
                        GL30.GL_R32F,
                        numTextures,
                        0,
                        GL_RED,
                        GL_FLOAT,
                        rainfactBuffer);
        glGenerateMipmap(GL_TEXTURE_1D);
        
    }
}

