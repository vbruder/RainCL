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

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;
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
    
	private FloatBuffer posBuffer;

	//transform feedback buffers
	private int[] tfbid = new int[2];
	//particle buffers
	private int[] pbid = new int[2];
	//current vertex buffer
	private int currBuf;
	//current transform feedback buffer
	private int currTFB;

	private boolean isFirstFrame;
	
	/**
	 * Constructor
	 * @param maxParticles number of particles to draw
	 */
	public Rainstreaks(int maxParticles) {
		
		this.maxParticles = maxParticles;
		this.currBuf = 0;
		this.currTFB = 1;
		this.isFirstFrame = true;
		
	    this.createShaderProgram();
		this.createData();
	}

	/**
	 * Create shader programs for update and draw.
	 */
	private void createShaderProgram() {
		
        this.StreakRenderSP = new ShaderProgram("./shader/StreakRender.vsh", "./shader/StreakRender.fsh", false);
        this.StreakUpdateSP = new ShaderProgram("./shader/StreakUpdate.vsh", "./shader/StreakUpdate.gsh", true);
	}

	/**
	 * Create initial particle positions, velocities and other dates.
	 */
	private void createData() {

		this.posBuffer = BufferUtils.createFloatBuffer(4 * maxParticles);
		FloatBuffer veloBuffer = BufferUtils.createFloatBuffer(4 * maxParticles);
		IntBuffer indexBuffer = BufferUtils.createIntBuffer(maxParticles);
		
		Random r = new Random(4);
		int clusterScale = 1;
		int i = 0;
		
		while(i < this.maxParticles) {

            //spawning position
            float x = -0.5f + clusterScale * r.nextFloat();
            float y =  0.5f + clusterScale * r.nextFloat();
            float z = -0.5f + clusterScale * r.nextFloat();
            
            posBuffer.put(x);
            posBuffer.put(y);
            posBuffer.put(z);
            posBuffer.put(1.f);
            
            //spawning velocity       
            veloBuffer.put(0.0f);
            veloBuffer.put(0.1f);
            veloBuffer.put(0.0f);
            veloBuffer.put(1.0f);
            i++;
        }
        posBuffer.position(0);
              
        //transform feedback buffers and particle buffers
        //TODO ??
        this.tfbid[0] = glGenTransformFeedbacks();      
        this.tfbid[1] = glGenTransformFeedbacks();
        this.pbid[0] = glGenBuffers();
        this.pbid[1] = glGenBuffers();
        
        for (int j = 0; j < tfbid.length; j++) {
			glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, tfbid[j]);
			glBindBuffer(GL_ARRAY_BUFFER, pbid[j]);
			glBufferData(GL_ARRAY_BUFFER, this.posBuffer, GL_DYNAMIC_DRAW);
			glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 0, pbid[j]);
        }
        
//        ByteBuffer varyings = BufferUtils.createByteBuffer(4 * 10);
//        varyings.putChar('p');
//        varyings.putChar('o');
//        varyings.putChar('s');
//        varyings.putChar('i');
//        varyings.putChar('t');
//        varyings.putChar('i');
//        varyings.putChar('o');
//        varyings.putChar('n');
//        varyings.putChar('F');
//        varyings.putChar('S');
//        varyings.position(0);
        
        CharSequence[] varyings = new CharSequence[1];
        varyings[0] = "positionFS";
        
        glTransformFeedbackVaryings(this.StreakUpdateSP.getID(), varyings, GL_INTERLEAVED_ATTRIBS);
        glTransformFeedbackVaryings(this.StreakRenderSP.getID(), varyings, GL_INTERLEAVED_ATTRIBS);
		      
        veloBuffer.position(0);
        indexBuffer.position(0);      
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
    	Matrix4f.mul(cam.getProjection(), cam.getView(), viewProj);  
        StreakUpdateSP.setUniform("viewProj", viewProj);
        StreakUpdateSP.setUniform("eyePosition", cam.getCamPos());
    	
    	//disable rest of render pipeline
    	glEnable(GL_RASTERIZER_DISCARD);
    	
    	glBindBuffer(GL_ARRAY_BUFFER, this.pbid[this.currBuf]);
    	glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, this.tfbid[this.currTFB]);
    	
        glEnableVertexAttribArray(ShaderProgram.ATTR_POS);
        glVertexAttribPointer(ShaderProgram.ATTR_POS, 4, GL_FLOAT, false, 16, 0);
        
        //TODO: GL_INVALID_OPERATION
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
    }
    
    /**
     * Render particles
     * @param cam
     */
    private void renderParticles(Camera cam) {

    	//set uniforms in VS and FS
        StreakRenderSP.use();
        Matrix4f.mul(cam.getProjection(), cam.getView(), viewProj);
        StreakRenderSP.setUniform("viewProj", viewProj);
    	
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
