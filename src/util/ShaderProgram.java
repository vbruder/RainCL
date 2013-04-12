package util;

import opengl.GL;
import static opengl.GL.*;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;

/**
 * 
 * @author Valentin Bruder
 * @date 28.02.2013
 *
 */
public class ShaderProgram {
    private int id, vs, fs, gs;

    private boolean isGeomShader = false;
 
    /**
     * Create a shader program <i>without</i> geometry shader.
     * @param vertexShader
     * @param fragmentShader
     */
    public ShaderProgram(String vertexShader, String fragmentShader) {
        this.createShaderProgram(vertexShader, fragmentShader);
    }
    
    /**
     * Create a shader program <i>with</i> geometry shader.
     * @param vertexShader
     * @param fragmentShader
     * @param geometryShader
     */
    public ShaderProgram(String vertexShader, String fragmentShader, String geometryShader) {
        this.isGeomShader  = true;
        this.createShaderProgram(vertexShader, fragmentShader, geometryShader);
    }
    
    public void use() {
        glUseProgram(this.id);
    }            
    
    public int getID(){
    	return this.id;
    }
    
    /**
     * @brief Helper method to write a matrix into a uniform. The related program object has to be active.
     *
     * @param matrix source matrix
     * @param varName target variable in shader
     */
    public void setUniform(String varName, Matrix4f matrix) {
        int loc = glGetUniformLocation(this.id, varName);
        if(loc != -1) {
            Util.MAT_BUFFER.position(0);
            matrix.store(Util.MAT_BUFFER);
            Util.MAT_BUFFER.position(0);
            glUniformMatrix4(loc, false, Util.MAT_BUFFER);
            Util.MAT_BUFFER.position(0);
        } else {
            System.err.println(varName);
        }
            
    }
    
    /**
     * @brief Helper method to write a texture into a uniform. The related program object has to be active.
     *
     * @param texture Texture
     * @param varName target variable in shader
     */
    public void setUniform(String varName, Texture texture) {
        int loc = glGetUniformLocation(this.id, varName);
        if(loc != -1) {
            texture.bind();
            glUniform1i(loc, texture.getUnit());
        }
    }
    
    /**
     * Attribute index of positionMC
     */
    public static final int ATTR_POS = 0;

    /**
     * Attribute index of normalMC
     */
    public static final int ATTR_NORMAL = 1;

    /**
     * Attribute index of vertexColor
     */
    public static final int ATTR_COLOR = 2;
    
    /**
     * Attribute index of vertexColor2
     */
    public static final int ATTR_COLOR2 = 3;
    
    /**
     * Attribute index of tex
     */
    public static final int ATTR_TEX = 4;
    
    /**
     * Attribute Index of instance
     */
    public static final int ATTR_INSTANCE = 5;
    
    /**
     * @brief Creates a shader program from a vertex and a fragment shader.
     * 
     * @param vs path of vertex shader
     * @param fs path of fragment shader
     * @param gs path of geometry shader
     * @return ShaderProgram ID
     */
    private void createShaderProgram(String vs, String fs, String gs) {
        this.id = glCreateProgram();
        
        this.vs = glCreateShader(GL_VERTEX_SHADER);
        this.fs = glCreateShader(GL_FRAGMENT_SHADER);
        this.gs = glCreateShader(GL_GEOMETRY_SHADER);
        
        glAttachShader(this.id, this.vs);
        glAttachShader(this.id, this.fs);
        glAttachShader(this.id, this.gs);
        
        String vertexShaderContents = Util.getFileContents(vs);
        String fragmentShaderContents = Util.getFileContents(fs);
        String geometryShaderContents = Util.getFileContents(gs);
        
        glShaderSource(this.vs, vertexShaderContents);
        glShaderSource(this.fs, fragmentShaderContents);
        glShaderSource(this.gs, geometryShaderContents);
        
        glCompileShader(this.vs);
        glCompileShader(this.fs);
        glCompileShader(this.gs);
        
        String log;
        log = glGetShaderInfoLog(this.vs, 1024);
        System.out.print(log);
        log = glGetShaderInfoLog(this.fs, 1024);
        System.out.print(log);
        log = glGetShaderInfoLog(this.gs, 1024);
        System.out.print(log);
        
        glBindAttribLocation(this.id, ATTR_POS, "positionMC");
        glBindAttribLocation(this.id, ATTR_NORMAL, "normalMC");        
        glBindAttribLocation(this.id, ATTR_COLOR, "vertexColor");
        glBindAttribLocation(this.id, ATTR_COLOR2, "vertexColor2");
        glBindAttribLocation(this.id, ATTR_TEX, "vertexTexCoords");
        glBindAttribLocation(this.id, ATTR_INSTANCE, "instancedData");
        
        glLinkProgram(this.id);        
        
        log = glGetProgramInfoLog(this.id, 1024);
        System.out.print(log);
    }
    
    /**
     * @brief Creates a shader program from a vertex and a fragment shader.
     * 
     * @param vs path of vertex shader
     * @param fs path of fragment shader
     * @param gs path of geometry shader
     * @return ShaderProgram ID
     */
    private void createShaderProgram(String vs, String fs) {
        this.id = glCreateProgram();
        
        this.vs = glCreateShader(GL_VERTEX_SHADER);
        this.fs = glCreateShader(GL_FRAGMENT_SHADER);
        
        glAttachShader(this.id, this.vs);
        glAttachShader(this.id, this.fs);
        
        String vertexShaderContents = Util.getFileContents(vs);
        String fragmentShaderContents = Util.getFileContents(fs);
        
        glShaderSource(this.vs, vertexShaderContents);
        glShaderSource(this.fs, fragmentShaderContents);
        
        glCompileShader(this.vs);
        glCompileShader(this.fs);
        
        String log;
        log = glGetShaderInfoLog(this.vs, 1024);
        System.out.print(log);
        log = glGetShaderInfoLog(this.fs, 1024);
        System.out.print(log);
        
        glBindAttribLocation(this.id, ATTR_POS, "positionMC");
        glBindAttribLocation(this.id, ATTR_NORMAL, "normalMC");        
        glBindAttribLocation(this.id, ATTR_COLOR, "vertexColor");
        glBindAttribLocation(this.id, ATTR_COLOR2, "vertexColor2");
        glBindAttribLocation(this.id, ATTR_TEX, "vertexTexCoords");
        glBindAttribLocation(this.id, ATTR_INSTANCE, "instancedData");
        
        glLinkProgram(this.id);        
        
        log = glGetProgramInfoLog(this.id, 1024);
        System.out.print(log);
    }
    
    /**
     * @brief Cleanup shader program
     */
    public void delete() {
        GL20.glDetachShader(this.id, this.fs);
        GL20.glDetachShader(this.id, this.vs);
        
        GL20.glDeleteShader(this.fs);
        GL20.glDeleteShader(this.vs);
        
        if (isGeomShader) {
            GL20.glDetachShader(this.id, this.gs);
            GL20.glDeleteShader(this.gs);
        }
        
        GL20.glDeleteProgram(this.id);
    }
}
