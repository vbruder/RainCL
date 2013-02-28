package util;

import static opengl.GL.glGetUniformLocation;
import static opengl.GL.glUniform1f;
import static opengl.GL.glUniform3f;
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
import java.util.Random;

import opengl.GL;
import opengl.OpenCL.Device_Type;

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
import org.lwjgl.util.vector.Matrix4f;

/**
 * 
 * @author Valentin Bruder (vbruder@uos.de)
 */
public class Raindrops {
    
    //opencl pointer
    private CLContext context;
    private CLProgram program;
    private CLDevice device;
    private CLCommandQueue queue;
    private CLKernel kernel0;
    private CLKernel kernel1;
    
    //data
    private FloatBuffer posBuffer, veloBuffer;
    
    //opencl buffer
    private CLMem old_pos, new_pos, old_velos, new_velos;
    
    //particle settings
    private int count = (int)Math.pow(2.0, 14.0);    //2^n to work properly with local memory
    private float clusterScale = 0.5f;
    //private float veloScale = 1.f;
    
    //kernel settings
    private boolean swap = true;
    private int localWorkSize = 32;
    private final PointerBuffer gwz = BufferUtils.createPointerBuffer(1);
    private final PointerBuffer lwz = BufferUtils.createPointerBuffer(1);
    
    //raindrop geometry
    private Geometry raindrops_old;
    private Geometry raindrops_new;
    
    //shader
    private ShaderProgram raindropSP;
    
    private int diffuseTexture;
    private int specularTexture;
    private int viewProjLoc;
    private int eyeLoc;
    private int diffTexLoc;
    private int specTexLoc;
    private int kaLoc;
    private int kdLoc;
    private int ksLoc;
    private int esLoc;
    private int caLoc;
    
    private final Matrix4f viewProj = new Matrix4f();
    
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
    public Raindrops(Device_Type device_type, Drawable drawable) throws LWJGLException {
        
        this.createCLContext(device_type, Util.getFileContents("./shader/RainSim.cl"), drawable);
        this.createData();
        this.createBuffer();
        this.createKernels();
        this.createShaderProgram();
        this.gwz.put(0, this.count);
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
        return this.raindropSP;
    }
    
    /**
     * Creates OpenGL shader program to render particles
     */
    private void createShaderProgram() { 
        
        raindropSP = new ShaderProgram("./shader/Raindrop.vsh", "./shader/Raindrop.fsh");
        viewProjLoc = glGetUniformLocation(raindropSP.getID(), "viewProj");
        diffTexLoc = glGetUniformLocation(raindropSP.getID(), "diffuseTex");
        specTexLoc = glGetUniformLocation(raindropSP.getID(), "specularTex");
        eyeLoc = glGetUniformLocation(raindropSP.getID(), "eyePosition");
        kaLoc = glGetUniformLocation(raindropSP.getID(), "k_a");
        kdLoc = glGetUniformLocation(raindropSP.getID(), "k_dif");
        ksLoc = glGetUniformLocation(raindropSP.getID(), "k_spec");
        esLoc = glGetUniformLocation(raindropSP.getID(), "es");
        caLoc = glGetUniformLocation(raindropSP.getID(), "c_a");
        raindropSP.use();
        glUniform1f(kaLoc, 0.05f);
        glUniform1f(kdLoc, 0.6f);
        glUniform1f(ksLoc, 0.3f);
        glUniform1f(esLoc, 16.0f);
        glUniform3f(caLoc, 1.0f, 1.0f, 1.0f);
        
        //textures on raindrops
        diffuseTexture = Util.generateTexture("media/raindrop.jpg");
        specularTexture = Util.generateTexture("media/raindrop_spec.jpg");
    }
    
    /**
     * Create initial position and velocity data pseudo randomly.
     * Positions Layout[x, y, z, radius]
     * Velocity Layout[v_x, v_y, v_z, _]
     */
    private void createData() {
        
        this.raindrops_old = GeometryFactory.createSphere(0.01f, 16, 8);
        this.raindrops_new = GeometryFactory.createSphere(0.01f, 16, 8);
        
        this.posBuffer = BufferUtils.createFloatBuffer(4*count);
        this.veloBuffer = BufferUtils.createFloatBuffer(4*count);
        
        int i = 0;
        Random r = new Random(4);
        while(i < this.count) {
            
            //spawning position
            float x = this.clusterScale * r.nextInt() / (float)Integer.MAX_VALUE + 0.5f;
            float y = this.clusterScale * r.nextInt() / (float)Integer.MAX_VALUE;
            float z = this.clusterScale * r.nextInt() / (float)Integer.MAX_VALUE + 0.5f;
            // if(x*x + y*y + z*z < this.clusterScale*3/2f || x*x + y*y + z*z > clusterScale*3f) continue;
            float rand = r.nextFloat() * 0.15f + 0.05f;
            this.posBuffer.put(x);
            this.posBuffer.put(y);
            this.posBuffer.put(z);
            this.posBuffer.put(rand);
            //spawning velocity
            //TODO influence by wind
//            float vx = r.nextInt() / (float)Integer.MAX_VALUE * this.veloScale;
            float vy = 1.0f; //Math.abs(r.nextInt() / (float)Integer.MAX_VALUE * this.veloScale);
//            float vz = r.nextInt() / (float)Integer.MAX_VALUE * this.veloScale;
            this.veloBuffer.put(0.0f);
            this.veloBuffer.put(vy);
            this.veloBuffer.put(0.0f);
            this.veloBuffer.put((float) (4/3 * Util.PI * Math.pow(rand, 3)));
            i++;
        }
        
        this.posBuffer.position(0);
        this.veloBuffer.position(0);
    }

    /**
     * @brief Calls kernel to update into the next time step. Is called once each frame. 
     * @param deltaTime
     */
    public void updateSimulation(long deltaTime) {
        
        clEnqueueAcquireGLObjects(this.queue, this.new_pos, null, null);
        clEnqueueAcquireGLObjects(this.queue, this.old_pos, null, null);
        
        if(this.swap) {            
            this.kernel0.setArg(5, 1e-3f*deltaTime);
            clEnqueueNDRangeKernel(this.queue, kernel0, 1, null, gwz, lwz, null, null);            
        } else {           
            this.kernel1.setArg(5, 1e-3f*deltaTime);
            clEnqueueNDRangeKernel(this.queue, kernel1, 1, null, gwz, lwz, null, null);           
        }
        clEnqueueReleaseGLObjects(this.queue, this.new_pos, null, null);
        clEnqueueReleaseGLObjects(this.queue, this.old_pos, null, null);
        clFinish(this.queue);
        this.swap = !this.swap;
    }
    
    /**
     * @brief draws the particles
     * @param cam Camera
     */
    public void draw(Camera cam) {
        
        raindropSP.use();
        
        glUniform3f(this.eyeLoc, cam.getCamPos().x, cam.getCamPos().y, cam.getCamPos().z);       
        Matrix4f.mul(cam.getProjection(), cam.getView(), viewProj);  
        viewProj.store(Util.MAT_BUFFER);
        Util.MAT_BUFFER.position(0);
        GL.glUniformMatrix4(viewProjLoc, false, Util.MAT_BUFFER);
        
        GL.glActiveTexture(GL.GL_TEXTURE0 + 0);
        GL.glBindTexture(GL.GL_TEXTURE_2D, this.diffuseTexture);
        GL.glUniform1i(this.diffTexLoc, 0);
        
        GL.glActiveTexture(GL.GL_TEXTURE0 + 1);
        GL.glBindTexture(GL.GL_TEXTURE_2D, this.specularTexture);
        GL.glUniform1i(this.specTexLoc, 1);
        
//        glEnable(GL_BLEND);
        if(this.swap) {
            this.raindrops_old.draw();
        } else {
            this.raindrops_new.draw();
        }
//        glDisable(GL_BLEND);
    }
    
    /**
     * @brief Creates all significant OpenCL buffers
     */
    private void createBuffer() {
        this.raindrops_old.setInstanceBuffer(this.posBuffer, 4);
        this.raindrops_old.construct();
        this.raindrops_new.setInstanceBuffer(this.posBuffer, 4);
        this.raindrops_new.construct();
        
        this.old_velos = clCreateBuffer(this.context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, this.veloBuffer);
        this.new_velos = clCreateBuffer(this.context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, this.veloBuffer);
        this.new_pos = clCreateFromGLBuffer(this.context, CL_MEM_READ_WRITE, this.raindrops_new.getInstanceBuffer());
        this.old_pos = clCreateFromGLBuffer(this.context, CL_MEM_READ_WRITE, this.raindrops_old.getInstanceBuffer());
        
        this.posBuffer = null;
        this.veloBuffer = null;
    }
    
    /**
     * @brief Creates two OpenCL kernels.
     */
    private void createKernels() {
        this.kernel0 = clCreateKernel(this.program, "rain_sim");
        this.kernel0.setArg(0, this.old_pos);
        this.kernel0.setArg(1, this.new_pos);
        this.kernel0.setArg(2, this.old_velos);
        this.kernel0.setArg(3, this.new_velos);
        this.kernel0.setArg(4, this.count);
        this.kernel0.setArg(5, 0f);
        
        this.kernel1 = clCreateKernel(this.program, "rain_sim");
        this.kernel1.setArg(0, this.new_pos);
        this.kernel1.setArg(1, this.old_pos);
        this.kernel1.setArg(2, this.new_velos);
        this.kernel1.setArg(3, this.old_velos);
        this.kernel1.setArg(4, this.count);
        this.kernel1.setArg(5, 0f);
    }
    
    /**
     * @brief Frees memory.
     */
    public void destroy() {
        clReleaseMemObject(this.new_pos);
        clReleaseMemObject(this.old_pos);
        clReleaseMemObject(this.new_velos);
        clReleaseMemObject(this.old_velos);
        clReleaseKernel(this.kernel0);
        clReleaseKernel(this.kernel1);
        clReleaseCommandQueue(this.queue);
        clReleaseProgram(this.program);
        clReleaseContext(this.context);
        this.raindrops_new.delete();
        this.raindrops_old.delete();
    }
}

