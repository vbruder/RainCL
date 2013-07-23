package util;

import static apiWrapper.OpenGL.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL33;


/**
 * Class for vertex array objects.
 * @author Sascha Kolodzey, Nico Marniok
 */
public class Geometry {
    private int vaid = -1;                  // vertex array id
	private FloatBuffer vertexValueBuffer;  // vertex buffer values
	private FloatBuffer instanceData;
    private IntBuffer indexValueBuffer;     // index buffer values
    private int topology;                   // index topology
    private int indexCount;                 // number of indices
    private int vbid;                       // geometry vertex buffer

	private int ibid;                       // geometry index buffer
    private int instancebid = -1;
    private int instanceStride;
    private int instanceCount;
    private int instanceAttributeSize;
    private final List<VertexAttribute> attributes = new LinkedList<VertexAttribute>();
    
    private Texture normalTex, lightTex, specularTex, colorTex;


    /**
     * Setzt den IntBuffer, der die Indexdaten dieser Geometrie beinhaltet und
     * die zugehoerige Topologie.
     * <b>Obacht:</b> Diese Methode erzeugt <i>keinen</i> Buffer auf der GPU.
     * @param indices Buffer, der die Indexdaten beinhaltet
     * @param topology Zugehoerige Topologie
     */
    public void setIndices(IntBuffer indices, int topology) {
        indexValueBuffer = indices;
        indexCount = indices.capacity();
        this.topology = topology;
    }

    /**
     * Setzt den FloatBuffer, der die Indexdaten dieser Geometrie beinhaltet.
     * <b>Obacht:</b> Diese Methode erzeugt <i>keinen</i> Buffer auf der GPU.
     * @param vertices 
     */
    public void setVertices(FloatBuffer vertices) {
        vertexValueBuffer = vertices;
    }
    
    /**
     * Erweitert die Geometrie um ein Vertexattribut vom Typ GL_FLOAT.
     * @param index Index / Location des Attributs
     * @param size Anzahl der Komponenten des Attributs
     * @param offset Offset im Vertex gemessen in Byte
     */
    public void addVertexAttribute(int index, int size, int offset) {
        VertexAttribute attr = new VertexAttribute();
        attr.index = index;
        attr.size = size;
        attr.offset = offset;
        attributes.add(attr);
    }
    
    /**
     * Loescht alle Attribute, die mittels <code>addVertexAttribute</code>
     * hinzugefuegt wurden.
     */
    public void clearVertexAttributes() {
        attributes.clear();
    }
    
    /**
     * Fuegt einen Instanzenbuffer hinzu
     * @param instanceData
     * @param stride in BYTE
     */
    public void setInstanceBuffer(FloatBuffer instanceData, int size) {
        this.instanceData = instanceData;
        this.instanceStride = 4 * size;
        this.instanceAttributeSize = size;
        this.instanceCount = instanceData.capacity() / size;
    }
    
    /**
     * 
     * @return instance buffer
     */
    public int getInstanceBuffer() {
        return this.instancebid;
    }
    
    /**
     * 
     * @return OpenGL vertex buffer ID
     */
    public int getVbid() {
    	return vbid;
    }
    
    /**
     * Erzeugt aus den gesetzten Vertex- und Indexdaten ein Vertexarray Object,
     * das die zugoerige Topologie beinhaltet.
     */
    public void construct() {
        if(vertexValueBuffer == null || indexValueBuffer == null) {
            throw new UnsupportedOperationException("Vertex- und Indexbuffer wurden noch nicht gesetzt!");
        }
        
        this.vaid = glGenVertexArrays();
        glBindVertexArray(this.vaid);
        
        this.vbid = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, this.vbid);
        glBufferData(GL_ARRAY_BUFFER, this.vertexValueBuffer, GL_STATIC_DRAW);
        
        this.ibid = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.ibid);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, this.indexValueBuffer, GL_STATIC_DRAW);
        
        int stride = 0;
        for(VertexAttribute attr : attributes) {
            stride += 4 * attr.size;
            glEnableVertexAttribArray(attr.index);
        }
        for(VertexAttribute attr : attributes) {
            glVertexAttribPointer(attr.index, attr.size, GL_FLOAT, false, stride, attr.offset);
        }
        if(this.instanceData != null) {
            this.instancebid = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, this.instancebid);
            glBufferData(GL_ARRAY_BUFFER, this.instanceData, GL_STATIC_DRAW);
            glEnableVertexAttribArray(Util.ATTR_INSTANCE);
            glVertexAttribPointer(Util.ATTR_INSTANCE, this.instanceAttributeSize, GL_FLOAT, false, this.instanceStride, 0);
            GL33.glVertexAttribDivisor(Util.ATTR_INSTANCE, 1);
            this.instanceData = null;
        }
        glBindVertexArray(0);
    }
    
    /**
     * Loescht alle GPU Resourcen dieser Geometrie, die in
     * <code>construct()</code> generiert wurden (Indexbuffer, Vertexbuffer und 
     * das Vertexarray Object).
     */
    public void delete() {
        glDeleteBuffers(vbid);
        vbid = -1;
        glDeleteBuffers(ibid);
        ibid = -1;
        if(instancebid != -1) {
            glDeleteBuffers(this.instancebid);
        }
        glDeleteVertexArrays(vaid);
        vaid = -1;
    }
    
    /**
     * Erzeugt die Geometrie, falls noch nicht geschehen und zeichnet sie
     * anschliessend.
     */
    public void draw() {
        if(vaid == -1) {
            construct();
        }
        glBindVertexArray(vaid);
        if(this.instancebid != -1) {
            GL31.glDrawElementsInstanced(this.topology, this.indexCount, GL11.GL_UNSIGNED_INT, 0, this.instanceCount);
        } else {
            glDrawElements(topology, indexCount, GL_UNSIGNED_INT, 0); 
        }
    }
    
    private class VertexAttribute {
        /**
         * Attribut index / location
         */
        private int index;
        
        /**
         * Anzahl der Elemente des Attributs
         */
        private int size;
        
        /**
         * Offset des Attributs in einem Vertex, gemessen in Byte
         */
        private int offset;
    }
    /**
     * Set normal texture.
     * @param normalTex texture containing normal information
     */
    public void setNormalTex(Texture normalTex) {
    	this.normalTex = normalTex;
    }
    
    /**
     * Set light texture.
     * @param lightTex texture containing light information
     */
    public void setLightTex(Texture lightTex) {
    	this.lightTex = lightTex;
    }
    
    /**
     * Set specular texture.
     * @param specularTex texture containing specular lighting information
     */
    public void setSpecularTex(Texture specularTex) {
    	this.specularTex = specularTex;
    }
    
    /**
     * Set color texture.
     * @param colorTex texture containing normal information
     */
    public void setColorTex(Texture colorTex) {
    	this.colorTex = colorTex;
    }
    
    /**
     * Returns the normal texture.
     * @return normal texture
     */
    public Texture getNormalTex() {
    	return normalTex;
    }
    
    /**
     * Returns the light texture.
     * @return light texture
     */
    public Texture getLightTex()
    {
    	return lightTex;
    }
    
    /**
     * Returns the specular texture.
     * @return specular texture
     */
    public Texture getSpecularTex()
    {
    	return specularTex;
    }
    
    /**
     * Returns color texture.
     * @return color texture
     */
    public Texture getColorTex()
    {
    	return colorTex;
    }
    
    /**
     * Returns vertex data.
     * @return Float buffer containing vertex data.
     */
    public FloatBuffer getVertexValueBuffer() {
    	return vertexValueBuffer;
    }
}