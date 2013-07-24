package util;

import static apiWrapper.OpenGL.WIDTH;
import static apiWrapper.OpenGL.HEIGHT;
import static apiWrapper.OpenGL.GL_TEXTURE_2D;
import static apiWrapper.OpenGL.GL_RGBA;
import static apiWrapper.OpenGL.GL_RGBA16F;

/**
 * 
 * @author Valentin Bruder
 * Based on code of Nico Marniok (Computergrafik Praktikum 2012)
 */
public class DeferredShader
{
	private ShaderProgram drawTextureSP = new ShaderProgram("./shader/ScreenQuad.vsh", "./shader/CopyTexture.fsh");    
	private Geometry screenQuadGeo = GeometryFactory.createScreenQuad();        
	private FrameBuffer frameBuffer = new FrameBuffer();
	
	private Texture texPosition;	
	private Texture texNormal;
	private Texture texVertexColor;        
	
	/**
	 * Default constructor.
	 */
	public DeferredShader()
	{   
	}
	
	/**
	 * Initialize deferred shader.
	 * @param texture unit offset
	 */
	public void init(int offsetTU)
	{    	
		frameBuffer.init(true, WIDTH, HEIGHT);            	
		//generate textures    	
		texPosition 	= new Texture(GL_TEXTURE_2D, offsetTU + 0);
		texNormal 		= new Texture(GL_TEXTURE_2D, offsetTU + 1);
		texVertexColor 	= new Texture(GL_TEXTURE_2D, offsetTU + 2);
		
		frameBuffer.addTexture(texPosition, 	GL_RGBA16F, GL_RGBA);
		frameBuffer.addTexture(texNormal, 		GL_RGBA16F, GL_RGBA);
		frameBuffer.addTexture(texVertexColor, 	GL_RGBA,	GL_RGBA);
		frameBuffer.drawBuffers();
	}
	
	/**
	 * Bind the frame buffer.
	 */
	public void bind()
	{   		
		frameBuffer.bind();    
	}
	
	/**
	 * Register a shader program.
	 * @param shaderProgram the program to regster
	 */
	public void registerShaderProgram(ShaderProgram shaderProgram)
	{    	
		shaderProgram.use();        
		frameBuffer.BindFragDataLocations(shaderProgram, "position", "normal", "color");            
	}
	
	/**
	 * Clear background color.
	 */
	public void clear()
	{    	
		frameBuffer.clearColor();     
	}
	
	/**
	 * Unbind the frame buffer.
	 */
	public void finish()
	{    	
		frameBuffer.unbind();    
	}     
	
	/**
	 * @return texture containing world coordinates. Distance to camera in w-coordinate.
	 */
	public Texture getWorldTexture()
	{       
		return frameBuffer.getTexture(0);    
	}

	/**
	 * @return texture containing normal data
	 */
	public Texture getNormalTexture()
	{        
		return frameBuffer.getTexture(1);   
	}  
	
	/**
	 * @return texture containing diffuse color
	 */
	public Texture getDiffuseTexture()
	{        
		return frameBuffer.getTexture(2);    
	}
	
	/**
	 * Draw the texture on a screen quad.
	 * @param tex
	 */
	public void DrawTexture(Texture tex)
	{        
		drawTextureSP.use();        
		drawTextureSP.setUniform("image", tex);        
		screenQuadGeo.draw();    
	}
	
	/**
	 * Clean up.
	 */
	public void delete()
	{        
		drawTextureSP.delete();        
		screenQuadGeo.delete();                
		texPosition.delete();        
		texNormal.delete();        
		texVertexColor.delete();    
	}
}
