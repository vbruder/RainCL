/**
 * 
 */
package util;

import static opengl.GL.GL_ARRAY_BUFFER;
import static opengl.GL.GL_FLOAT;
import static opengl.GL.GL_POINTS;
import static opengl.GL.GL_STATIC_DRAW;
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

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import opengl.GL;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

/**
 * @author Valentin Bruder
 *
 */
public class Rainstreaks {
	
    //shader
    private ShaderProgram rainstreakSP;
    
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
    
    private int maxParticles;
    
    private Geometry pointSet;

	private int vaid;
	private int vbid;

	private FloatBuffer posBuffer;

	public Rainstreaks(int maxParticles) {
		
		this.maxParticles = maxParticles;
		this.createData();
		this.createShaderProgram();
		
	}

	private void createShaderProgram() {
		
        rainstreakSP = new ShaderProgram("./shader/Raindrop.vsh", "./shader/Raindrop.fsh");
        viewProjLoc = glGetUniformLocation(rainstreakSP.getID(), "viewProj");
        diffTexLoc = glGetUniformLocation(rainstreakSP.getID(), "diffuseTex");
        specTexLoc = glGetUniformLocation(rainstreakSP.getID(), "specularTex");
        eyeLoc = glGetUniformLocation(rainstreakSP.getID(), "eyePosition");
        kaLoc = glGetUniformLocation(rainstreakSP.getID(), "k_a");
        kdLoc = glGetUniformLocation(rainstreakSP.getID(), "k_dif");
        ksLoc = glGetUniformLocation(rainstreakSP.getID(), "k_spec");
        esLoc = glGetUniformLocation(rainstreakSP.getID(), "es");
        caLoc = glGetUniformLocation(rainstreakSP.getID(), "c_a");
        rainstreakSP.use();
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
            clusterScale = 1;
            //spawning position
            float x = -0.5f + clusterScale * r.nextFloat();
            float y =  0.5f + clusterScale * r.nextFloat();
            float z = -0.5f + clusterScale * r.nextFloat();
            // if(x*x + y*y + z*z < this.clusterScale*3/2f || x*x + y*y + z*z > clusterScale*3f) continue;
            float rand = r.nextFloat() * 0.15f + 0.05f;
            
            posBuffer.put(x);
            posBuffer.put(y);
            posBuffer.put(z);
            posBuffer.put(0.1f);
            
            //spawning velocity       
            veloBuffer.put(0.f);
            veloBuffer.put(0.1f);
            veloBuffer.put(0.f);
            veloBuffer.put((float) (4/3 * Util.PI * Math.pow(rand, 3)));
            i++;
        }
        
        posBuffer.position(0);
        
        this.vaid = glGenVertexArrays();
        glBindVertexArray(this.vaid);
        this.vbid = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, this.vbid);
        glBufferData(GL_ARRAY_BUFFER, this.posBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(ShaderProgram.ATTR_POS);
        glVertexAttribPointer(ShaderProgram.ATTR_POS, 4, GL_FLOAT, false, 16, 0);
        glBindVertexArray(0);
        
        veloBuffer.position(0);
        indexBuffer.position(0);      
	}
	
    /**
     * draws the particles
     * @param cam Camera
     */
    public void draw(Camera cam) {
        
        rainstreakSP.use();
        
        glUniform3f(this.eyeLoc, cam.getCamPos().x, cam.getCamPos().y, cam.getCamPos().z);       
        Matrix4f.mul(cam.getProjection(), cam.getView(), viewProj);  
        viewProj.store(Util.MAT_BUFFER);
        Util.MAT_BUFFER.position(0);
        GL.glUniformMatrix4(viewProjLoc, false, Util.MAT_BUFFER);
        
        glBindVertexArray(vaid);
        
        GL11.glPointSize(2);
        GL11.glDrawArrays(GL_POINTS, 0, maxParticles); 
    }
    
    public ShaderProgram getShaderProgram() {
        return this.rainstreakSP;
    }

}
