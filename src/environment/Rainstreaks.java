package environment;

import static apiWrapper.OpenGL.GL_ARRAY_BUFFER;
import static apiWrapper.OpenGL.GL_DYNAMIC_DRAW;
import static apiWrapper.OpenGL.GL_FLOAT;
import static apiWrapper.OpenGL.GL_POINTS;
import static apiWrapper.OpenGL.GL_R8;
import static apiWrapper.OpenGL.GL_RED;
import static apiWrapper.OpenGL.GL_R16;
import static apiWrapper.OpenGL.GL_R16F;
import static apiWrapper.OpenGL.GL_R32F;
import static apiWrapper.OpenGL.GL_RG;
import static apiWrapper.OpenGL.GL_RG8;
import static apiWrapper.OpenGL.GL_RGB;
import static apiWrapper.OpenGL.GL_RGB8;
import static apiWrapper.OpenGL.GL_RGBA;
import static apiWrapper.OpenGL.GL_RGBA8;
import static apiWrapper.OpenGL.GL_SHORT;
import static apiWrapper.OpenGL.GL_STATIC_DRAW;
import static apiWrapper.OpenGL.GL_TEXTURE_1D;
import static apiWrapper.OpenGL.GL_TEXTURE_2D;
import static apiWrapper.OpenGL.GL_TEXTURE_2D_ARRAY;
import static apiWrapper.OpenGL.GL_UNSIGNED_BYTE;
import static apiWrapper.OpenGL.GL_UNSIGNED_INT;
import static apiWrapper.OpenGL.glFinish;
import static apiWrapper.OpenGL.glBindBuffer;
import static apiWrapper.OpenGL.glBindTexture;
import static apiWrapper.OpenGL.glBindVertexArray;
import static apiWrapper.OpenGL.glBufferData;
import static apiWrapper.OpenGL.glDrawArrays;
import static apiWrapper.OpenGL.glDrawElements;
import static apiWrapper.OpenGL.glEnable;
import static apiWrapper.OpenGL.glEnableVertexAttribArray;
import static apiWrapper.OpenGL.glGenBuffers;
import static apiWrapper.OpenGL.glGenVertexArrays;
import static apiWrapper.OpenGL.glGenerateMipmap;
import static apiWrapper.OpenGL.glGetUniformLocation;
import static apiWrapper.OpenGL.glTexImage1D;
import static apiWrapper.OpenGL.glTexImage2D;
import static apiWrapper.OpenGL.glTexImage3D;
import static apiWrapper.OpenGL.glTexSubImage3D;
import static apiWrapper.OpenGL.glUniform1f;
import static apiWrapper.OpenGL.glUniform3f;
import static apiWrapper.OpenGL.glVertexAttribPointer;
import static apiWrapper.OpenGL.glDeleteFramebuffers;
import static apiWrapper.OpenGL.glDeleteRenderbuffers;
import static apiWrapper.OpenGL.glDeleteBuffers;
import static apiWrapper.OpenGL.glDeleteVertexArrays;
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
import java.text.DecimalFormat;
import java.util.Random;

import main.Main;

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

import apiWrapper.OpenCL;
import apiWrapper.OpenCL.Device_Type;

import util.Camera;
import util.ShaderProgram;
import util.Texture;
import util.Util;
import util.Util.ImageContents;

/**
 * Rainstreaks particle system
 * @author Valentin Bruder <vbruder@uos.de>
 */
public class Rainstreaks
{
    //TODO: implement proper texture unit count
    private static final int HEIGHTTEX_UNIT 	= 11;
    private static final int RAINTEX_UNIT 		= 12;
    private static final int RAINFACTORS_UNIT 	= 13;
    private static final int FOGTEX_UNIT 		= 14;
    
    private static final int NUM_RAIN_TEXTURES = 370;
    private static final int NUM_FOG_TEXTURES = 256;
    private static final int NUM_FOG_SPRITES = 8;
    
    //OpenCL pointer
    private static CLContext context;
    private static CLProgram program;
    private CLDevice device;
    private CLCommandQueue queue;
    private static CLKernel kernelMoveStreaks;
    private static CLKernel kernelMoveFog;
    
    //OpenCL data buffers
    private static FloatBuffer posBuffer;
    private static FloatBuffer seedBuffer;
    private static FloatBuffer veloBuffer;
    private static FloatBuffer vertexDataBuffer;
    private static FloatBuffer fogDataBuffer;
    
    //OpenCL memory objects
    private static CLMem memRainPos;
    private static CLMem memVelos;
    private static CLMem memSeed;
    private static CLMem memHeightMap;
    private static CLMem memFogPos;
    
    //particle settings
    private static int maxParticles;
    private static float clusterScale;
    private static float veloFactor;
    
    //kernel settings
    private final PointerBuffer gws = BufferUtils.createPointerBuffer(1);
    
    //wind
    private static Vector3f windDir[] = new Vector3f[500];
    private static int windPtr = 0;
    private static float windForce = 10.f;
    
    //texture IDs
    private static Texture heightTex;
    private Texture rainTex;
    private Texture rainFactTex;
    private Texture fogTex;
    
    //shader
    private ShaderProgram streakRenderSP;
    private ShaderProgram fogRenderSP;
    private static Vector3f eyePos = new Vector3f(0.f, 0.f, 0.f);
    private final Matrix4f viewProj = new Matrix4f();
    //array IDs
	private static int vertArrayID;
	private static int vertArrayFogID;
	//buffer IDs
    private static int vertBufferID;
    private static int vertBufferFogID;
    
    //delta time for animations
    private float dt;
    
    //environment
    private Sun sun;
    private float pointLightIntensity = 1.0f;
    
    private static Random r = new Random((new java.util.Date()).getTime());
    
    
    /**
     * particle system
     * @param device_type GPU / CPU
     * @param drawable OpenGL drawable.
     * @throws LWJGLException
     */
    public Rainstreaks(Device_Type device_type, Drawable drawable, Camera cam, Sun sun) throws LWJGLException
    {
        maxParticles = 1 << 17;
        eyePos = cam.getCamPos();
        this.sun = sun;
        // range of cylinder around camera
        clusterScale = 30.0f;
        // velocity factor
        veloFactor = 250.0f;
        
        this.gws.put(0, maxParticles);
        
        //openCL context
        createCLContext(device_type, Util.getFileContents("./kernel/RainSim.cl"), drawable);
        createWindData();
        createFogData();
        createRainData();
        createBuffer();
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
     * Creates OpenGL shader program and texture array to render particles.
     */
    private void createShaderProgram()
    { 
        this.streakRenderSP = new ShaderProgram("./shader/Rain.vsh", "./shader/Rain.gsh", "./shader/Rain.fsh");
        this.fogRenderSP	= new ShaderProgram("./shader/Fog.vsh", "./shader/Fog.gsh", "./shader/Fog.fsh");
        
        //load the 370 point light textures into a texture array
        rainTex = Util.createTextureArray("media/rainTex/point/cv0_vPos(", ").png", RAINTEX_UNIT, GL_R16, GL_RED, NUM_RAIN_TEXTURES);
        //create normalization 1D-texture
        createRainfactors();
    }
    
    /**
     * Create initial position and velocity data pseudo randomly.
     */
    private static void createRainData()
    { 
	    //initialize attribute buffer: position, starting position (seed), velocity, random and texture type
		posBuffer  = BufferUtils.createFloatBuffer(4 * maxParticles);
		seedBuffer = BufferUtils.createFloatBuffer(4 * maxParticles);
		veloBuffer = BufferUtils.createFloatBuffer(4 * maxParticles);
		
		//must be 2^x
		int numLodLvl = 1 << 3;
		//fill buffers
		for (int lodLvl = 0; lodLvl < numLodLvl; lodLvl++)
        {   
    		for (int i = 0; i < (maxParticles/numLodLvl); i++)
    		{
    		    //spawning position
    		    float x, y, z;
    		    do
    		    {
    		        x = (r.nextFloat() - 0.5f) * (clusterScale + lodLvl*5);
                    y = (r.nextFloat() + 0.1f) * (clusterScale);  
                    z = (r.nextFloat() - 0.5f) * (clusterScale + lodLvl*5);
    		    }
                while ((z < 0.2f && z > -0.2f) && (x < 0.2f && x > -0.2f));
    		    //respawn if particle is too close to viewer
                
                //add to seed buffer
                seedBuffer.put(x);
                seedBuffer.put(y);
                seedBuffer.put(z);
                //add random type to w coordinate in buffer
                //type is for choosing 1 out of 8 different textures
                seedBuffer.put((float) r.nextInt(8));
                
                //add to position buffer
                posBuffer.put(x);
                posBuffer.put(y);
                posBuffer.put(z);
                posBuffer.put(1.f);
                
                //add spawning velocity (small random velocity in x- and z-direction for variety and AA 
                veloBuffer.put(veloFactor*(r.nextFloat() / 100.f));
                veloBuffer.put(veloFactor*((r.nextFloat() + 0.75f) / 20.f));
                veloBuffer.put(veloFactor*(r.nextFloat() / 100.f));
                //add random number in w coordinate, used to light up random streaks
                float tmpR = r.nextFloat();
                if (tmpR > 0.9f)
                {
                    veloBuffer.put(1.f + (1.f - tmpR)*0.1f); // 1.0 to 1.01 
                }
                else
                {
                    veloBuffer.put(1.f);
                }
            }
        }
		//flip buffers
        posBuffer.rewind();
        seedBuffer.rewind();
        veloBuffer.rewind();
                
        //3 buffers * 4 floats (xyzw) * maxParticles
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
        
        posBuffer.rewind();
        seedBuffer.rewind();
        veloBuffer.rewind();
        vertexDataBuffer.rewind();
    }
    
    /**
     * Create data for wind animations.
     */
    private static void createWindData()
    {               
        float windRand = r.nextFloat() * windForce;
        //use sine to create impression of wind gust
        for (int i = 0; i < (windDir.length / 2); i++)
        {
            windDir[i] = new Vector3f();
            windDir[windDir.length - i - 1] = new Vector3f();
            
            windDir[i].x = (float) Math.sin((float) i / (float) windDir.length) * windRand;
            windDir[windDir.length - i - 1].x = (float) Math.sin((float) i / (float) windDir.length) * windRand;
            windDir[i].z = (float) Math.sin((float) i / (float) windDir.length) * windRand;
            windDir[windDir.length - i - 1].z = (float) Math.sin((float) i / (float) windDir.length) * windRand;
        }
    }
    
    /**
     * Create data and buffers for fog visualization and animation.
     */
    private void createFogData()
    {
    	fogDataBuffer = BufferUtils.createFloatBuffer(4*NUM_FOG_SPRITES);
    	for (int i = 0; i < NUM_FOG_SPRITES; i++)
    	{
    		fogDataBuffer.put(500.f);
    		fogDataBuffer.put(0.f);
    		fogDataBuffer.put(-200.f + i*50);
    		//random texture out of 8
    		fogDataBuffer.put(r.nextInt(NUM_FOG_TEXTURES));
		}
    	fogDataBuffer.rewind();
    	
        //load the 8 fog textures into a texture array
    	fogTex = Util.createTextureArray("media/fogTex/Smoke (", ").bmp", FOGTEX_UNIT, GL_RGB8, GL_RED, NUM_FOG_TEXTURES);

        //create OpenGL -CL buffer for fog
        vertArrayFogID = glGenVertexArrays();
        glBindVertexArray(vertArrayFogID);
        vertBufferFogID = glGenBuffers();
        
        glBindBuffer(GL_ARRAY_BUFFER, vertBufferFogID);
        glBufferData(GL_ARRAY_BUFFER, fogDataBuffer, GL_DYNAMIC_DRAW);

        glEnableVertexAttribArray(ShaderProgram.ATTR_POS);
        glVertexAttribPointer(ShaderProgram.ATTR_POS, 4, GL_FLOAT, false, 16,  0);
        memFogPos = clCreateFromGLBuffer(context, CL_MEM_READ_WRITE, vertBufferFogID);
    }
    
    /**
     * Creates all significant OpenCL buffers.
     */
    private static void createBuffer()
    {
    	vertArrayID = glGenVertexArrays();
    	glBindVertexArray(vertArrayID);
    	
        vertBufferID = glGenBuffers();
        
        glBindBuffer(GL_ARRAY_BUFFER, vertBufferID);        
        glBufferData(GL_ARRAY_BUFFER, vertexDataBuffer, GL_DYNAMIC_DRAW);

        //set seed and velocity attributes for vertex shader
        glEnableVertexAttribArray(ShaderProgram.ATTR_POS);
        glEnableVertexAttribArray(ShaderProgram.ATTR_SEED);
        glEnableVertexAttribArray(ShaderProgram.ATTR_VELO);
        glEnableVertexAttribArray(ShaderProgram.ATTR_COLOR);
        
        glVertexAttribPointer(ShaderProgram.ATTR_POS,   4, GL_FLOAT, false, 16,  0);
        glVertexAttribPointer(ShaderProgram.ATTR_SEED,  4, GL_FLOAT, false, 16, 16);      
        glVertexAttribPointer(ShaderProgram.ATTR_VELO,  4, GL_FLOAT, false, 16, 32);
        glVertexAttribPointer(ShaderProgram.ATTR_COLOR, 4, GL_FLOAT, false, 16, 48);

        memRainPos = clCreateFromGLBuffer(context, CL_MEM_READ_WRITE, vertBufferID);
        memSeed  = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, seedBuffer); 
        memVelos = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, veloBuffer);
        
        loadTexturesCL();
        createKernels();
    }
    
    /**
     * Load OpenGL terrain height texture into OpenCL buffer.
     */
    private static void loadTexturesCL()
    {
        IntBuffer errorCheck = BufferUtils.createIntBuffer(1);
        
        //load height map
        ImageContents contentHeight = Util.loadImage("media/terrain/terrainHeight01.png");
        FloatBuffer dataH = BufferUtils.createFloatBuffer(contentHeight.height * contentHeight.width);
        for(int i = 0; i < dataH.capacity(); ++i)
        {
            dataH.put(contentHeight.data.get(i));
        }
        dataH.rewind();
        
        heightTex = new Texture(GL_TEXTURE_2D, HEIGHTTEX_UNIT);
        heightTex.bind();
        glTexImage2D(   GL_TEXTURE_2D,
                        0,
                        GL_R16F,
                        contentHeight.width,
                        contentHeight.height,
                        0,
                        GL_RED,
                        GL_FLOAT,
                        dataH);
        glGenerateMipmap(GL_TEXTURE_2D);

        memHeightMap = CL10GL.clCreateFromGLTexture2D(context, CL10.CL_MEM_READ_ONLY, GL_TEXTURE_2D, 0, heightTex.getId(), errorCheck);

        OpenCL.checkError(errorCheck.get(0));
    }

    /**
     * Creates OpenCL kernels.
     */
    private static void createKernels() {
               
        // kernel
        kernelMoveStreaks = clCreateKernel(program, "rainSim");
        kernelMoveStreaks.setArg(0, memRainPos);
        kernelMoveStreaks.setArg(1, memVelos);
        kernelMoveStreaks.setArg(2, memSeed);
        kernelMoveStreaks.setArg(3, memHeightMap);
        kernelMoveStreaks.setArg(4, maxParticles);
        kernelMoveStreaks.setArg(5, 0.f);
        // Eye position
        kernelMoveStreaks.setArg(6, 0.f);
        kernelMoveStreaks.setArg(7, 0.f);
        kernelMoveStreaks.setArg(8, 0.f);
        // Wind direction
        kernelMoveStreaks.setArg( 9, 0.f);
        kernelMoveStreaks.setArg(10, 0.f);
        
        // Fog kernel
        kernelMoveFog = clCreateKernel(program, "fogSim");
        kernelMoveFog.setArg(0, memFogPos);
        kernelMoveFog.setArg(1, 0.f);
        kernelMoveFog.setArg(2, 0.f);
        kernelMoveFog.setArg(3, 0.f);
    }
    
    /**
     * Calls kernel to update into the next time step. Is called once each frame. 
     * @param deltaTime
     */
    public void updateSimulation(long deltaTime)
    {
        this.dt = 1e-3f*deltaTime;
        glFinish();
        
        clEnqueueAcquireGLObjects(queue, memRainPos, null, null);
        clEnqueueAcquireGLObjects(queue, memHeightMap, null, null);
        clEnqueueAcquireGLObjects(queue, memFogPos, null, null);
        
        kernelMoveStreaks.setArg( 5, dt);
        kernelMoveStreaks.setArg( 6, eyePos.x);
        kernelMoveStreaks.setArg( 7, eyePos.y);
        kernelMoveStreaks.setArg( 8, eyePos.z);
        kernelMoveStreaks.setArg( 9, windDir[windPtr].x);
        kernelMoveStreaks.setArg(10, windDir[windPtr].z);
        clEnqueueNDRangeKernel(queue, kernelMoveStreaks, 1, null, gws, null, null, null);  
        gws.put(0, NUM_FOG_SPRITES);
        kernelMoveFog.setArg(1, dt);
        kernelMoveFog.setArg(2, windDir[windPtr].x);
        kernelMoveFog.setArg(3, windDir[windPtr].z);
        clEnqueueNDRangeKernel(queue, kernelMoveFog, 1, null, gws, null, null, null);
        gws.put(0, maxParticles);        
        
        clEnqueueReleaseGLObjects(queue, memRainPos, null, null);
        clEnqueueReleaseGLObjects(queue, memFogPos, null, null);
        clEnqueueReleaseGLObjects(queue, memHeightMap, null, null);
        
        clFinish(this.queue);
        
        if (windPtr < windDir.length - 1)
        {
            windPtr++;
        }
        else
        {
            windPtr = 0;
        }
    }
    
    /**
     * Draw the rain particles.
     * @param cam Camera
     */
    public void draw(Camera cam)
    {
    	//check weather rain strength was changed
        if (maxParticles != (posBuffer.capacity()/4))
        {
        	clRetainMemObject(memRainPos);
        	clRetainMemObject(memSeed);
        	clRetainMemObject(memVelos);
        	clRetainMemObject(memHeightMap);
        	glDeleteBuffers(vertBufferID);
        	glDeleteVertexArrays(vertArrayID);
        	
            createRainData();
            createBuffer();
            kernelMoveStreaks.setArg(5, maxParticles);
            this.gws.put(0, maxParticles);            
        }
        
        //render rain streaks
    	streakRenderSP.use();
        
    	eyePos = cam.getCamPos();
        Matrix4f.mul(cam.getProjection(), cam.getView(), viewProj);  
        streakRenderSP.setUniform("viewProj", viewProj);
        streakRenderSP.setUniform("rainTex", rainTex);
        streakRenderSP.setUniform("rainfactors", rainFactTex);
        streakRenderSP.setUniform("eyePosition", eyePos);
        streakRenderSP.setUniform("windDir", windDir[windPtr]);
        streakRenderSP.setUniform("dt", dt);
        //set lighting uniforms
        streakRenderSP.setUniform("sunDir", sun.getDirection());
        streakRenderSP.setUniform("sunColor", sun.getColor());
        streakRenderSP.setUniform("sunIntensity", sun.getIntensity());
        
        glBindVertexArray(vertArrayID);
        glDrawArrays(GL_POINTS, 0, maxParticles);
    }
    
    /**
     * Draw fog sprites.
     */
    public void drawFog(Camera cam)
    {
        //render fog
        fogRenderSP.use();
        
//    	eyePos = new Vector3f(cam.getCamPos().x, cam.getCamPos().y, cam.getCamPos().z);
        
    	fogRenderSP.setUniform("view", cam.getView());
    	fogRenderSP.setUniform("proj", cam.getProjection());
    	
        fogRenderSP.setUniform("eyePosition", eyePos);
        fogRenderSP.setUniform("fogTex", fogTex);
        fogRenderSP.setUniform("texArrayID", r.nextInt(8));
        
        glBindVertexArray(vertArrayFogID);
        glDrawArrays(GL_POINTS, 0, NUM_FOG_SPRITES);
    }
    
    /**
     * Free memory.
     */
    public void destroy()
    {
        clReleaseMemObject(memRainPos);
        clReleaseMemObject(memVelos);
        clReleaseMemObject(memSeed);
        clReleaseMemObject(memHeightMap);
        clReleaseKernel(kernelMoveStreaks);
        clReleaseCommandQueue(queue);
        clReleaseProgram(program);
        clReleaseContext(context);
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
        
        FloatBuffer rainfactBuffer = BufferUtils.createFloatBuffer(NUM_RAIN_TEXTURES);

        rainfactBuffer.put(rainfactors, 0, NUM_RAIN_TEXTURES);
        rainfactBuffer.position(0);
        
        rainFactTex = new Texture(GL_TEXTURE_1D, RAINFACTORS_UNIT);
        rainFactTex.bind();
        glTexImage1D(   GL_TEXTURE_1D,
                        0,
                        GL_R32F,
                        NUM_RAIN_TEXTURES,
                        0,
                        GL_RED,
                        GL_FLOAT,
                        rainfactBuffer);
        glGenerateMipmap(GL_TEXTURE_1D);
    }

    /**
     * @return the windForce
     */
    public static float getWindForce()
    {
        return windForce;
    }

    /**
     * @param windForce the wind force to set
     */
    public static void setWindForce(float windForce)
    {
        Rainstreaks.windForce = windForce;
        createWindData();
    }

    /**
     * @return the pointLightIntensity
     */
    public float getPointLightIntensity()
    {
        return pointLightIntensity;
    }

    /**
     * @param pointLightIntensity the pointLightIntensity to set
     */
    public void setPointLightIntensity(float pointLightIntensity)
    {
        this.pointLightIntensity = pointLightIntensity;
    }
    
    /**
     * @return the maxParticles
     */
    public static int getMaxParticles()
    {
        return maxParticles;
    }

    /**
     * @param maxParticles the maxParticles to set
     */
    public static void setMaxParticles(int maxParticles)
    {
       Rainstreaks.maxParticles = maxParticles;
       Vector3f vec = new Vector3f(1.f, 1.f, 1.f);
       Main.setFogThickness( (Vector3f) vec.scale(0.03f + getLogMaxParticles()*0.05f) );
    }

    /**
     * @return the rain streaks shader program
     */
    public ShaderProgram getShaderProgram()
    {
        return this.streakRenderSP;
    }
    
    /**
     * @return the binary logarithm of the current amount of rain particles. Between 0 and 1.
     */
    public static float getLogMaxParticles()
    {
    	return ((float) Math.log( ((double) maxParticles) / Math.log(2))/ 10.f - 1.f) ;
    }
    
}

