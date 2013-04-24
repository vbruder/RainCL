package util;

import static opengl.GL.GL_FLOAT;
import static opengl.GL.GL_RGBA;
import static opengl.GL.GL_TEXTURE_2D;
import static opengl.GL.glGetUniformLocation;
import static opengl.GL.glTexImage2D;
import static opengl.GL.glUniform1f;
import static opengl.GL.glUniform3f;
import static opengl.GL.glGenVertexArrays;
import static opengl.GL.glBindVertexArray;
import static opengl.GL.glGenBuffers;
import static opengl.GL.GL_ARRAY_BUFFER;
import static opengl.GL.GL_STATIC_DRAW;
import static opengl.GL.GL_POINTS;
import static opengl.GL.glVertexAttribPointer;
import static opengl.GL.glEnableVertexAttribArray;
import static opengl.GL.glBindBuffer;
import static opengl.GL.glBufferData;
import static opengl.OpenCL.CL_MEM_COPY_HOST_PTR;
import static opengl.OpenCL.CL_MEM_READ_WRITE;
import static opengl.OpenCL.clBuildProgram;
import static opengl.OpenCL.clCreateBuffer;
import static opengl.OpenCL.clCreateCommandQueue;
import static opengl.OpenCL.clCreateFromGLBuffer;
import static opengl.OpenCL.clCreateKernel;
import static opengl.OpenCL.clCreateProgramWithSource;
import static opengl.OpenCL.clEnqueueAcquireGLObjects;
import static opengl.OpenCL.clEnqueueNDRangeKernel;
import static opengl.OpenCL.clEnqueueReleaseGLObjects;
import static opengl.OpenCL.clFinish;
import static opengl.OpenCL.clReleaseCommandQueue;
import static opengl.OpenCL.clReleaseContext;
import static opengl.OpenCL.clReleaseKernel;
import static opengl.OpenCL.clReleaseMemObject;
import static opengl.OpenCL.clReleaseProgram;
import static opengl.OpenCL.create;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

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
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import util.Util.ImageContents;

/**
 * Raindrop particle system
 * @author Valentin Bruder (vbruder@uos.de)
 */
public class Raindrops {
    
    //opencl pointer
    private CLContext context;
    private CLProgram program;
    private CLDevice device;
    private CLCommandQueue queue;
    private CLKernel kernelMoveStreaks;
    private CLKernel kernel1;
    
    //data
    private FloatBuffer posBuffer, veloBuffer;
    
    //opencl buffer
    private CLMem old_pos, position, velos, heightmap, normalmap;
    
    //particle settings
    private int maxParticles;
    private float clusterScale = 0.5f;
    //private float veloScale = 1.f;
    
    //kernel settings
    private boolean swap = true;
    private int localWorkSize = 256;
    private final PointerBuffer gwz = BufferUtils.createPointerBuffer(1);
    private final PointerBuffer lwz = BufferUtils.createPointerBuffer(1);
    
    //raindrop geometry
//    private Geometry raindrops_old;
//    private Geometry raindrops_new;
    
    // terrain texture IDs
    private int heightTexId, normalTexId;
    private int HEIGHTTEX_UNIT = 4;
    private Texture hTex;
    
    //shader
    private ShaderProgram StreakRenderSP;
    
    private final Matrix4f viewProj = new Matrix4f();
	private int vaid, vbid;
    
    /**
     * particle system
     * 
     * The implementation uses two buffers for each, position and velocity.
     * Buffers are swapped after each time step.
     * Swapping is realized with two kernels and two geometries.
     * In the updateSimulation method the two kernels are called in turn.
     * In draw() the last modified geometry object is drawn.
     * Thereby OpenGL instancing is used.
     * @param device_type GPU /CPU
     * @param drawable OpenGL drawable.
     * @throws LWJGLException
     */
    public Raindrops(Device_Type device_type, Drawable drawable, int heightTexId, int normalTexId, int maxParticles) throws LWJGLException {
        
        this.maxParticles = maxParticles;
        this.heightTexId = heightTexId;
        this.normalTexId = normalTexId;
        
        //openCL context
        createCLContext(device_type, Util.getFileContents("./shader/RainSim.cl"), drawable);
        createData();
        createBuffer();
        createKernels();
        createShaderProgram();
        
        this.gwz.put(0, this.maxParticles);
        this.lwz.put(0, this.localWorkSize);   
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
        
        //textures on raindrops
//        diffuseTexture = Util.generateTexture("media/raindrop.jpg");
//        specularTexture = Util.generateTexture("media/raindrop_spec.jpg");
    }
    
    /**
     * Create initial position and velocity data pseudo randomly.
     * Positions Layout[x, y, z, radius]
     * Velocity Layout[v_x, v_y, v_z, _]
     */
    private void createData() {
        
        this.posBuffer = BufferUtils.createFloatBuffer(4*maxParticles);
        this.veloBuffer = BufferUtils.createFloatBuffer(4*maxParticles);
        
        int i = 0;
        Random r = new Random(4);
        
        while(i < this.maxParticles) {
            clusterScale = 1;
            //spawning position
            float x = -0.5f + clusterScale * r.nextFloat();
            float y =  0.5f + clusterScale * r.nextFloat();
            float z = -0.5f + clusterScale * r.nextFloat();
            
            this.posBuffer.put(x);
            this.posBuffer.put(y);
            this.posBuffer.put(z);
            this.posBuffer.put(0.1f);
            
            //spawning velocity
            //TODO influence by wind          
            this.veloBuffer.put(0.f);
            this.veloBuffer.put(0.1f);
            this.veloBuffer.put(0.f);
            this.veloBuffer.put(0.1f);
            i++;
        }
        
        this.posBuffer.position(0);
        this.veloBuffer.position(0);
    }

    /**
     * Calls kernel to update into the next time step. Is called once each frame. 
     * @param deltaTime
     */
    public void updateSimulation(long deltaTime) {
        
        clEnqueueAcquireGLObjects(this.queue, this.position, null, null);
        clEnqueueAcquireGLObjects(this.queue, this.heightmap, null, null);
        clEnqueueAcquireGLObjects(this.queue, this.normalmap, null, null);
           
        this.kernelMoveStreaks.setArg(5, 1e-3f*deltaTime);
        clEnqueueNDRangeKernel(this.queue, kernelMoveStreaks, 1, null, gwz, lwz, null, null);            

        clEnqueueReleaseGLObjects(this.queue, this.position, null, null);
        clEnqueueReleaseGLObjects(this.queue, this.heightmap, null, null);
        clEnqueueReleaseGLObjects(this.queue, this.normalmap, null, null);
        
        clFinish(this.queue);
        this.swap = !this.swap;
    }
    
    /**
     * draws the particles
     * @param cam Camera
     */
    public void draw(Camera cam) {
        
    	StreakRenderSP.use();
        
    	StreakRenderSP.setUniform("eyeposition", new Vector3f(cam.getCamPos().x, cam.getCamPos().y, cam.getCamPos().z));    
        Matrix4f.mul(cam.getProjection(), cam.getView(), viewProj);  
        StreakRenderSP.setUniform("viewProj", viewProj);
        
        glBindVertexArray(vaid);
        
        GL11.glDrawArrays(GL_POINTS, 0, maxParticles); 
    }
    
    /**
     * Creates all significant OpenCL buffers
     */
    private void createBuffer() {
   	
    	this.posBuffer.position(0);
    	this.vaid = glGenVertexArrays();
    	glBindVertexArray(this.vaid);
    	
    	this.vbid = glGenBuffers();
    	glBindBuffer(GL_ARRAY_BUFFER, this.vbid);
    	glBufferData(GL_ARRAY_BUFFER, this.posBuffer, GL_STATIC_DRAW);
        
        glEnableVertexAttribArray(ShaderProgram.ATTR_POS);
        glVertexAttribPointer(ShaderProgram.ATTR_POS, 4, GL_FLOAT, false, 16, 0);
        glBindVertexArray(ShaderProgram.ATTR_POS);  	
        
        this.velos = clCreateBuffer(this.context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, this.veloBuffer);
        this.position = clCreateFromGLBuffer(this.context, CL_MEM_READ_WRITE, vbid);
        
        IntBuffer errorCheck = BufferUtils.createIntBuffer(1);
        
        ImageContents content = Util.loadImage("media/map1.png");
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
        
        this.heightmap = CL10GL.clCreateFromGLTexture2D(this.context, CL10.CL_MEM_READ_ONLY, GL11.GL_TEXTURE_2D, 0, hTex.getId(), errorCheck);
        this.normalmap = CL10GL.clCreateFromGLTexture2D(this.context, CL10.CL_MEM_READ_ONLY, GL11.GL_TEXTURE_2D, 0, this.normalTexId, errorCheck);
        
        OpenCL.checkError(errorCheck.get(0));
        
        this.posBuffer = null;
        this.veloBuffer = null;
    }
    
    /**
     * Creates two OpenCL kernels.
     */
    private void createKernels() {
               
        //kernel 0
        this.kernelMoveStreaks = clCreateKernel(this.program, "rain_sim");
        this.kernelMoveStreaks.setArg(0, this.position);
        this.kernelMoveStreaks.setArg(1, this.velos);
        this.kernelMoveStreaks.setArg(2, this.heightmap);
        this.kernelMoveStreaks.setArg(3, this.normalmap);
        this.kernelMoveStreaks.setArg(4, this.maxParticles);
        this.kernelMoveStreaks.setArg(5, 0.f);
    }
      
    /**
     * Frees memory.
     */
    public void destroy() {
        clReleaseMemObject(this.position);
        clReleaseMemObject(this.velos);
        clReleaseMemObject(this.heightmap);
        clReleaseMemObject(this.normalmap);
        clReleaseKernel(this.kernelMoveStreaks);
        clReleaseKernel(this.kernel1);
        clReleaseCommandQueue(this.queue);
        clReleaseProgram(this.program);
        clReleaseContext(this.context);
    }
}

