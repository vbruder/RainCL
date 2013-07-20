package util;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.BufferUtils;

import static apiWrapper.OpenGL.glGenFramebuffers;
import static apiWrapper.OpenGL.glGenRenderbuffers;
import static apiWrapper.OpenGL.glBindRenderbuffer;
import static apiWrapper.OpenGL.glRenderbufferStorage;
import static apiWrapper.OpenGL.glFramebufferRenderbuffer;
import static apiWrapper.OpenGL.glEnable;
import static apiWrapper.OpenGL.glDisable;
import static apiWrapper.OpenGL.glClear;
import static apiWrapper.OpenGL.glBindFramebuffer;
import static apiWrapper.OpenGL.glViewport;
import static apiWrapper.OpenGL.glTexParameterf;
import static apiWrapper.OpenGL.glTexImage2D;
import static apiWrapper.OpenGL.glFramebufferTexture2D;
import static apiWrapper.OpenGL.glCheckFramebufferStatus;
import static apiWrapper.OpenGL.glBindFragDataLocation;
import static apiWrapper.OpenGL.glDrawBuffers;
import static apiWrapper.OpenGL.GL_RENDERBUFFER;
import static apiWrapper.OpenGL.GL_DEPTH_COMPONENT32F;
import static apiWrapper.OpenGL.GL_FLOAT;
import static apiWrapper.OpenGL.WIDTH;
import static apiWrapper.OpenGL.HEIGHT;
import static apiWrapper.OpenGL.GL_FRAMEBUFFER;
import static apiWrapper.OpenGL.GL_DEPTH_ATTACHMENT;
import static apiWrapper.OpenGL.GL_COLOR_ATTACHMENT0;
import static apiWrapper.OpenGL.GL_COLOR_BUFFER_BIT;
import static apiWrapper.OpenGL.GL_DEPTH_BUFFER_BIT;
import static apiWrapper.OpenGL.GL_DEPTH_TEST;
import static apiWrapper.OpenGL.GL_TEXTURE_2D;
import static apiWrapper.OpenGL.GL_TEXTURE_MIN_FILTER;
import static apiWrapper.OpenGL.GL_TEXTURE_MAG_FILTER;
import static apiWrapper.OpenGL.GL_LINEAR;
import static apiWrapper.OpenGL.GL_FRAMEBUFFER_COMPLETE;
import static apiWrapper.OpenGL.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT;
import static apiWrapper.OpenGL.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER;
import static apiWrapper.OpenGL.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT;
import static apiWrapper.OpenGL.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE;
import static apiWrapper.OpenGL.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER;

/**
 * Represents a frame buffer used for deferred shading.
 * @author Valentin Bruder
 * Based on code of Nico Marniok (Computergrafik Praktikum 2012)
 */
public class FrameBuffer
{
	private int frameBufferObjectId = -1;
	private List<Texture> textureList = new LinkedList<Texture>();
	private int count;
	private int renderBufferObjectId;
    private int width, height;

	public void init(boolean depthTest, int width, int height)
	{
        this.width = width;
        this.height = height;
        count = 0;
        frameBufferObjectId = glGenFramebuffers();
        
        if(depthTest)
        {
            this.bind();
            renderBufferObjectId = glGenRenderbuffers();
            glBindRenderbuffer(GL_RENDERBUFFER, renderBufferObjectId);
            glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT32F, WIDTH, HEIGHT);
            glBindRenderbuffer(GL_RENDERBUFFER, 0);
            glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, renderBufferObjectId);
            this.unbind();
            this.checkForErrors();
        }
	}
	
	public void addTexture(Texture tex, int internalFormat, int format)
	{
		this.bind();
		bindTexture(tex, GL_COLOR_ATTACHMENT0 + count, internalFormat, format);
		textureList.add(tex);
		count++;
		this.unbind();
	}
	
	public void drawBuffers() {
	     // draw buffers
	     int[] buffersArray = new int[count];
	     for(int i = 0; i < buffersArray.length; i++) {
	     buffersArray[i] = GL_COLOR_ATTACHMENT0 + i;
	     }
	     IntBuffer buffers = BufferUtils.createIntBuffer(buffersArray.length);
	     buffers.put(buffersArray);
	     buffers.position(0);
	    
	     this.bind();
	     glDrawBuffers(buffers);
	     this.unbind();
	}
	
    public Texture getTexture(int i) {
        return textureList.get(i);
    }
    
    public void clearColor() {
    	// set clear color
    	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }
    
    public void bind() {
    	glBindFramebuffer(GL_FRAMEBUFFER, frameBufferObjectId);
        glViewport(0, 0, this.width, this.height);

        if(renderBufferObjectId == -1) {
            glDisable(GL_DEPTH_TEST);
        } else {
            glEnable(GL_DEPTH_TEST);
        }
        
    }
    
    public void unbind() {
    	glBindFramebuffer(GL_FRAMEBUFFER, 0);
    	glViewport(0, 0, WIDTH, HEIGHT);
    }
    
    public void BindFragDataLocations(ShaderProgram program, String ...fsOutVarNames) {
        for(int i=0; i < fsOutVarNames.length; ++i) {
            glBindFragDataLocation(program.getID(), i, fsOutVarNames[i]);
        }
    }
    
	/**
	* Binds a texture to the current Frame Buffer Object.
	* @param Texture texture to bind
	* @param attachment Must be GL_COLOR_ATTACHMENT0-15, binding point
	* @param internalFormat GL internal Format
	* @param format GL Format
	*/
    private void bindTexture(Texture texture, int attachment, int internalFormat, int format) {
	     // bind texture
	     texture.bind();
	    
	     // add filters
	     glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	     glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	    
	     // set texture information
	     glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, this.width, this.height, 0, format, GL_FLOAT, (FloatBuffer) null);
	    
	     // attach texture to framebuffer
	     glFramebufferTexture2D(GL_FRAMEBUFFER, attachment, GL_TEXTURE_2D, texture.getId(), 0);
	    
	     this.checkForErrors();
    }
    
    /**
     * Check for errors.
     */
    public void checkForErrors() {
        int error = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        switch(error) {
            case GL_FRAMEBUFFER_COMPLETE: break;
            case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT: System.err.println("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT"); break;
            case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER: System.err.println("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER"); break;
            case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT: System.err.println("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT"); break;
            case GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE: System.err.println("GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE"); break;
            case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER: System.err.println("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER"); break;
        }
    }
    
    public void reset(){
    	count=0;
    }
}
