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
import static opengl.GL.glBindBuffer;
import static opengl.GL.glBindVertexArray;
import static opengl.GL.glBufferData;
import static opengl.GL.glEnableVertexAttribArray;
import static opengl.GL.glGenBuffers;
import static opengl.GL.glGenVertexArrays;
import static opengl.GL.glGetUniformLocation;
import static opengl.GL.glUniform1f;
import static opengl.GL.glUniform3f;
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

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import opengl.GL;

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
    
    private int diffuseTexture;
    private int specularTexture;
    private int viewProjLocGS;
    private int eyeLoc;
    private int diffTexLoc;
    private int specTexLoc;
    private int kaLoc;
    private int kdLoc;
    private int ksLoc;
    private int esLoc;
    private int caLoc;

    private final Matrix4f viewProj = new Matrix4f();
    
    private int maxParticles;

	private FloatBuffer posBuffer;

	//transform feedback buffer
	private int[] tfbid = new int[2];
	//particle buffer
	private int[] pbid = new int[2];
	//current vertex buffer
	private int currBuf;
	//current transform feedback buffer
	private int currTFB;

	private boolean isFirst;
	private int viewProjLocVS;

	public Rainstreaks(int maxParticles) {
		
		this.maxParticles = maxParticles;
		this.currBuf = 0;
		this.currTFB = 1;
		this.isFirst = true;
		
		this.createData();
		this.createShaderProgram();
	}

	private void createShaderProgram() {
		
        this.StreakRenderSP = new ShaderProgram("./shader/StreakRender.vsh", "./shader/StreakRender.fsh", false);
        this.StreakUpdateSP = new ShaderProgram("./shader/StreakUpdate.vsh", "./shader/StreakUpdate.gsh", true);
        
        //update shader
        // --geometry
        viewProjLocGS = glGetUniformLocation(StreakUpdateSP.getID(), "viewProj");
        eyeLoc = glGetUniformLocation(StreakUpdateSP.getID(), "eyePosition");
        
        //rendering shader
        // --vertex
        viewProjLocVS = glGetUniformLocation(StreakRenderSP.getID(), "viewProj");
        
        // --fragment
        diffTexLoc = glGetUniformLocation(StreakRenderSP.getID(), "diffuseTex");
        specTexLoc = glGetUniformLocation(StreakRenderSP.getID(), "specularTex");
        kaLoc = glGetUniformLocation(StreakRenderSP.getID(), "k_a");
        kdLoc = glGetUniformLocation(StreakRenderSP.getID(), "k_dif");
        ksLoc = glGetUniformLocation(StreakRenderSP.getID(), "k_spec");
        esLoc = glGetUniformLocation(StreakRenderSP.getID(), "es");
        caLoc = glGetUniformLocation(StreakRenderSP.getID(), "c_a");
        
        StreakRenderSP.use();
        
        //set uniforms (fragment shader)
        glUniform1f(kaLoc, 0.05f);
        glUniform1f(kdLoc, 0.6f);
        glUniform1f(ksLoc, 0.3f);
        glUniform1f(esLoc, 16.0f);
        glUniform3f(caLoc, 1.0f, 1.0f, 1.0f);
        
        //textures on raindrops
        diffuseTexture = Util.generateTexture("media/raindrop.jpg");
        specularTexture = Util.generateTexture("media/raindrop_spec.jpg");
	}

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
              
        //transform feedback buffer and particle buffer
        this.tfbid[0] = glGenTransformFeedbacks();      
        this.pbid[0] = glGenBuffers();
        this.tfbid[1] = glGenTransformFeedbacks();
        this.pbid[1] = glGenBuffers();
        
        for (int j = 0; j < tfbid.length; j++) {
			glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, tfbid[j]);
			glBindBuffer(GL_ARRAY_BUFFER, pbid[j]);
			glBufferData(GL_ARRAY_BUFFER, this.posBuffer, GL_DYNAMIC_DRAW);
			glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 0, pbid[j]);
        }
		      
        veloBuffer.position(0);
        indexBuffer.position(0);      
	}
	
    /**
     * draws the particles
     * @param cam Camera
     */
    public void draw(Camera cam, long millis) {

        updateParticles(cam, millis);       
        renderParticles(cam);
               
        //swap buffers
        this.currBuf = this.currTFB;
        this.currTFB = (this.currBuf + 1) & 0x1;
    }
    
    private void updateParticles(Camera cam, long millis){

    	this.StreakUpdateSP.use();
    	
        Matrix4f.mul(cam.getProjection(), cam.getView(), viewProj);  
        viewProj.store(Util.MAT_BUFFER);
        Util.MAT_BUFFER.position(0);
        GL.glUniformMatrix4(viewProjLocGS, false, Util.MAT_BUFFER);
    	
    	//disable rest of render pipeline
    	glEnable(GL_RASTERIZER_DISCARD);
    	
    	glBindBuffer(GL_ARRAY_BUFFER, this.pbid[this.currBuf]);
    	glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, this.tfbid[this.currTFB]);
    	
        glEnableVertexAttribArray(ShaderProgram.ATTR_POS);
        glVertexAttribPointer(ShaderProgram.ATTR_POS, 4, GL_FLOAT, false, 16, 0);
        
        glBeginTransformFeedback(GL_TRIANGLES);
    
        //first draw
        if (this.isFirst) {
            glDrawArrays(GL_POINTS, 0, maxParticles);
            this.isFirst = false;
        }
        //all other draws
        else {
            glDrawTransformFeedback(GL_TRIANGLE_STRIP, this.tfbid[currBuf]);
        }            
        glEndTransformFeedback();
        glDisableVertexAttribArray(ShaderProgram.ATTR_POS);
    }
    
    private void renderParticles(Camera cam) {
    	
    	StreakRenderSP.use();
        
        GL.glUniform3f(this.eyeLoc, cam.getCamPos().x, cam.getCamPos().y, cam.getCamPos().z);       
        Matrix4f.mul(cam.getProjection(), cam.getView(), viewProj);  
        viewProj.store(Util.MAT_BUFFER);
        Util.MAT_BUFFER.position(0);
        GL.glUniformMatrix4(viewProjLocVS, false, Util.MAT_BUFFER);
    	
		glDisable(GL_RASTERIZER_DISCARD);
		glBindBuffer(GL_ARRAY_BUFFER, this.pbid[this.currTFB]);
		glEnableVertexAttribArray(ShaderProgram.ATTR_POS);
		glVertexAttribPointer(ShaderProgram.ATTR_POS, 4, GL_FLOAT, false, 16, 0);
		glDrawTransformFeedback(GL_TRIANGLE_STRIP, this.tfbid[this.currTFB]);
        glDisableVertexAttribArray(ShaderProgram.ATTR_POS);
	}
    
}
