package util;

import static apiWrapper.OpenGL.*;

import org.lwjgl.opengl.GL11;

/**
 * Class representing a OpenGL texture object.
 * @author Valentin Bruder
 * based on code by Nico Marniok (Computergrafik 2012)
 */
public class Texture {
    private int id;
    private int unit;
    private int target;

    public Texture(int target, int unit) {
        this.id = glGenTextures();
        this.unit = unit;
        this.target = target;
    }

    /**
     * @return the OpenGL texture id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the texture unit
     */
    public int getUnit() {
        return unit;
    }
    
    /**
     * Bind the texture.
     */
    public void bind() {
        glActiveTexture(GL_TEXTURE0 + this.unit);
        glBindTexture(this.target, this.id);
    }
    
    /**
     * Loads an image file and creates an OpenGL texture from it.
     * @param filename path to image file
     * @return ID of the created texture
     */
    public static Texture generateTexture(String filename, int unit) {
        Util.ImageContents contents = Util.loadImage(filename);
        int format = 0;
        int internalFormat = 0;
        switch(contents.colorComponents) {
            case 1: internalFormat = GL_R8; format = GL_RED; break;
            case 2: internalFormat = GL_RG8; format = GL_RG; break;
            case 3: internalFormat = GL_RGB8; format = GL_RGB; break;
            case 4: internalFormat = GL_RGBA8; format = GL_RGBA; break;
        }
        Texture tex = new Texture(GL_TEXTURE_2D, unit);
        tex.bind();
        
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        
        glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, contents.width, contents.height, 0, format, GL_FLOAT, contents.data);
        glGenerateMipmap(GL_TEXTURE_2D);
        return tex;
    }
    
    /**
     * Delete texture object.
     */
    public void delete() {
        GL11.glDeleteTextures(this.id);
    }
}
