/**
 * 
 */
package util;

import static opengl.GL.GL_ARRAY_BUFFER;
import static opengl.GL.GL_FLOAT;
import static opengl.GL.GL_POINTS;
import static opengl.GL.GL_TRIANGLES;
import static opengl.GL.GL_TRIANGLE_STRIP;
import static opengl.GL.GL_STATIC_DRAW;
import static opengl.GL.GL_DYNAMIC_DRAW;
import static opengl.GL.GL_TRANSFORM_FEEDBACK;
import static opengl.GL.GL_TRANSFORM_FEEDBACK_BUFFER;
import static opengl.GL.GL_RASTERIZER_DISCARD;
import static opengl.GL.GL_INTERLEAVED_ATTRIBS;
import static opengl.GL.glBindBuffer;
import static opengl.GL.glBindVertexArray;
import static opengl.GL.glBufferData;
import static opengl.GL.glEnableVertexAttribArray;
import static opengl.GL.glGenBuffers;
import static opengl.GL.glLinkProgram;
import static opengl.GL.glVertexAttribPointer;
import static opengl.GL.glGenTransformFeedbacks;
import static opengl.GL.glBindBufferBase;
import static opengl.GL.glEnable;
import static opengl.GL.glDisable;
import static opengl.GL.glDisableVertexAttribArray;
import static opengl.GL.glEndTransformFeedback;
import static opengl.GL.glDrawTransformFeedback;
import static opengl.GL.glBindTransformFeedback;
import static opengl.GL.glBeginTransformFeedback;
import static opengl.GL.glDrawArrays;
import static opengl.GL.glTransformFeedbackVaryings;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;

/**
 * @author Valentin Bruder
 *
 */
public class Rainstreaks {
	
    //shader programs
    private ShaderProgram StreakRenderSP;
	private ShaderProgram StreakUpdateSP;

    private final Matrix4f viewProj = new Matrix4f();
    
    private int maxParticles;
    private FloatBuffer particleBuffer;

	//transform feedback buffers
	private int[] tfbid = new int[2];
	//particle buffers
	private int[] pbid = new int[2];
	//current vertex buffer
	private int currBuf;
	//current transform feedback buffer
	private int currTFB;

	private boolean isFirstFrame;
    
    private int clusterScale;
	
	/**
	 * Constructor
	 * @param maxParticles number of particles to draw
	 */
	public Rainstreaks(int maxParticles) {
		
		this.maxParticles = maxParticles;
		this.currBuf = 0;
		this.currTFB = 1;
		this.isFirstFrame = true;
		this.clusterScale = 1;
		
	    this.createShaderProgram();
		this.createData();
	}

	/**
	 * Create shader programs for update and draw.
	 */
	private void createShaderProgram() {
		
	    this.StreakUpdateSP = new ShaderProgram("./shader/StreakUpdate.vsh", "./shader/StreakUpdate.gsh", true);
        this.StreakRenderSP = new ShaderProgram("./shader/StreakRender.vsh", "./shader/StreakRender.gsh", "./shader/StreakRender.fsh");
	}

	/**
	 * Create initial particle positions, velocities and other dates.
	 */
	private void createData() {

	    //init attribute buffer: position, starting position (seed), velocity, random and texture type
		FloatBuffer posBuffer  = BufferUtils.createFloatBuffer(4 * maxParticles);
		FloatBuffer seedBuffer = BufferUtils.createFloatBuffer(3 * maxParticles);
		FloatBuffer veloBuffer = BufferUtils.createFloatBuffer(3 * maxParticles);
		FloatBuffer randBuffer = BufferUtils.createFloatBuffer(maxParticles);
		FloatBuffer typeBuffer = BufferUtils.createFloatBuffer(maxParticles);
		
		Random r = new Random(1);
		
		//fill buffers
		for (int i = 0; i < this.maxParticles; i++) {

		    //TODO: LOD particle distribution

            //spawning position
            float x = (r.nextFloat() - 0.5f) * clusterScale;
            float y = (r.nextFloat() - 0.5f) * clusterScale;
            float z = (r.nextFloat() - 0.5f) * clusterScale;
            
            //add to seed buffer
            seedBuffer.put(x);
            seedBuffer.put(y);
            seedBuffer.put(z);
            
            //add to position buffer
            posBuffer.put(x);
            posBuffer.put(y);
            posBuffer.put(z);
            posBuffer.put(1.f);
            
            //add spawning velocity (small random velos in x- and z-dir for variety and against AA 
            veloBuffer.put(40.f*(r.nextFloat() / 20.f));
            veloBuffer.put(40.f*(r.nextFloat()));
            veloBuffer.put(40.f*(r.nextFloat() / 20.f));

            //add random number, used to light up random streaks
            float tmpR = r.nextFloat();
            if (tmpR > 0.75f) {
                randBuffer.put(1.f + tmpR);
            }
            else {
                randBuffer.put(1.f);
            }
            
            //add random type to buffer for choosing 1 out of 8 different textures 
            typeBuffer.put((float) r.nextInt(9));
        }
		//flip buffers
        posBuffer.position(0);
        seedBuffer.position(0);
        veloBuffer.position(0);
        typeBuffer.position(0);
        randBuffer.position(0);
        
        //fill particle buffer
        particleBuffer = BufferUtils.createFloatBuffer(12 * maxParticles);
        
        particleBuffer.put(posBuffer);
        particleBuffer.put(seedBuffer);
        particleBuffer.put(veloBuffer);
        particleBuffer.put(typeBuffer);
        particleBuffer.put(randBuffer);
        
        particleBuffer.position(0);       
        
        //set out variables for transform feedback
        CharSequence[] varyings = new CharSequence[5];
        varyings[0] = "position";
        varyings[1] = "seed";
        varyings[2] = "velo";
        varyings[3] = "rand";
        varyings[4] = "type";
        
        glTransformFeedbackVaryings(this.StreakUpdateSP.getID(), varyings, GL_INTERLEAVED_ATTRIBS);
        
        // link shader program to activate TF varyings
        glLinkProgram(this.StreakUpdateSP.getID());
        
        //transform feedback buffers and particle buffers
        this.tfbid[0] = glGenTransformFeedbacks();      
        this.tfbid[1] = glGenTransformFeedbacks();
        this.pbid[0] = glGenBuffers();
        this.pbid[1] = glGenBuffers();
        
        for (int j = 0; j < tfbid.length; j++) {
			glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, tfbid[j]);
			glBindBuffer(GL_ARRAY_BUFFER, pbid[j]);
			glBufferData(GL_ARRAY_BUFFER, particleBuffer, GL_DYNAMIC_DRAW);
			glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 0, pbid[j]);
        }
	}
	
    /**
     * Update and render particles. Swap buffers afterwards.
     * @param cam Camera
     */
    public void draw(Camera cam, long millis) {

        updateParticles(cam, millis);       
        renderParticles(cam);
               
        //swap buffers
        this.currBuf = this.currTFB;
        this.currTFB = (this.currTFB + 1) & 0x1;
    }
    
    /**
     * Update particles using transform feedback.
     * @param cam
     * @param millis
     */
    private void updateParticles(Camera cam, long millis){

    	//set uniforms in GS
    	this.StreakUpdateSP.use();
//    	  Matrix4f.mul(cam.getProjection(), cam.getView(), viewProj);  
//        StreakUpdateSP.setUniform("viewProj", viewProj);
//        StreakUpdateSP.setUniform("eyePosition", cam.getCamPos());
    	
    	//disable rest of render pipeline
    	glEnable(GL_RASTERIZER_DISCARD);
    	
    	glBindBuffer(GL_ARRAY_BUFFER, this.pbid[this.currBuf]);
    	glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, this.tfbid[this.currTFB]);
    	
        glEnableVertexAttribArray(ShaderProgram.ATTR_POS);
        glEnableVertexAttribArray(ShaderProgram.ATTR_SEED);
        glEnableVertexAttribArray(ShaderProgram.ATTR_VELO);
        glEnableVertexAttribArray(ShaderProgram.ATTR_RAND);
        glEnableVertexAttribArray(ShaderProgram.ATTR_TYPE);
        
        glVertexAttribPointer(ShaderProgram.ATTR_POS,  4, GL_FLOAT, false, 48, 0);
        glVertexAttribPointer(ShaderProgram.ATTR_SEED, 3, GL_FLOAT, false, 48, 16);
        glVertexAttribPointer(ShaderProgram.ATTR_VELO, 3, GL_FLOAT, false, 48, 28);
        glVertexAttribPointer(ShaderProgram.ATTR_RAND, 1, GL_FLOAT, false, 48, 40);
        glVertexAttribPointer(ShaderProgram.ATTR_TYPE, 1, GL_FLOAT, false, 48, 44);
        
        glBeginTransformFeedback(GL_POINTS); 
            //initial draw
            if (this.isFirstFrame) {
                glDrawArrays(GL_POINTS, 0, maxParticles);
                this.isFirstFrame = false;
            }
            //other draws
            else {
                glDrawTransformFeedback(GL_POINTS, this.tfbid[currBuf]);
            }            
        glEndTransformFeedback();
        
        glDisableVertexAttribArray(ShaderProgram.ATTR_POS);
        glDisableVertexAttribArray(ShaderProgram.ATTR_SEED);
        glDisableVertexAttribArray(ShaderProgram.ATTR_VELO);
        glDisableVertexAttribArray(ShaderProgram.ATTR_RAND);
        glDisableVertexAttribArray(ShaderProgram.ATTR_TYPE);
    }
    
    /**
     * Render particles
     * @param cam
     */
    private void renderParticles(Camera cam) {

        glLinkProgram(this.StreakRenderSP.getID());
    	//set uniforms in VS, GS and FS
        StreakRenderSP.use();
        Matrix4f.mul(cam.getProjection(), cam.getView(), viewProj);
        StreakRenderSP.setUniform("viewProj", viewProj);
        StreakRenderSP.setUniform("eyePosition", cam.getCamPos());
    	
		glDisable(GL_RASTERIZER_DISCARD);
		glBindBuffer(GL_ARRAY_BUFFER, this.pbid[this.currTFB]);
		glEnableVertexAttribArray(ShaderProgram.ATTR_POS);
		glVertexAttribPointer(ShaderProgram.ATTR_POS, 4, GL_FLOAT, false, 16, 0);
		glDrawTransformFeedback(GL_POINTS, this.tfbid[this.currTFB]);
        glDisableVertexAttribArray(ShaderProgram.ATTR_POS);
	}
    
    /**
     * Returns shader program object for rendering streaks.
     * @return ShaderProgram
     */
	public ShaderProgram getStreakRenderSP() {
		return StreakRenderSP;
	}

	/**
	 * Returns shader program object for updating streaks.
	 * @return ShaderProgram
	 */
	public ShaderProgram getStreakUpdateSP() {
		return StreakUpdateSP;
	}
    
}
