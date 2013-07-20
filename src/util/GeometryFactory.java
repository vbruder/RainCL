package util;

import static apiWrapper.OpenGL.*;
import static apiWrapper.OpenGL.GL_TRIANGLE_STRIP;
import static apiWrapper.OpenGL.RESTART_INDEX;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;


/**
 * Factory for generating geometries.
 * @author Valentin Bruder
 * Based on code of Sascha Kolodzey and Nico Marniok (Computergrafik 2012).
 */
public class GeometryFactory {
    
    // TODO: uses Texture units 10, 11, 12, 13
    final static public int TERRAIN_TEX_UNIT = 10;
    
    /**
     * Creates a quad in xy-plane. Can be used as screen quad for deferred shading.
     * @return quad geometry
     */
    public static Geometry createScreenQuad()
    {        
        int vaid = glGenVertexArrays();
        glBindVertexArray(vaid);        
        
        // vertex buffer
        FloatBuffer vertexData = BufferUtils.createFloatBuffer(8);
        vertexData.put(new float[]
        		{
		            -1.0f, -1.0f,
		            +1.0f, -1.0f,
		            -1.0f, +1.0f,
		            +1.0f, +1.0f,
		        });
        vertexData.position(0);
        
        // index buffer
        IntBuffer indexData = BufferUtils.createIntBuffer(4);
        indexData.put(new int[] { 0, 1, 2, 3 });
        indexData.position(0);
        
        Geometry geo = new Geometry();
        geo.setIndices(indexData, GL_TRIANGLE_STRIP);
        geo.setVertices(vertexData);
        geo.addVertexAttribute(ShaderProgram.ATTR_POS, 2, 0);
        return geo;
    }
    
    /**
     * Creates a quad in xz-plane with texture coordinates. Can be used as a floor quad.
     * @param scale size scaling factor
     * @return quad geometry
     */
    public static Geometry createFloorQuad(float scale)
    {        
        int vaid = glGenVertexArrays();
        glBindVertexArray(vaid);        
        
        // vertex buffer: xyz-position, st-texture coordinates
        FloatBuffer vertexData = BufferUtils.createFloatBuffer(3*4*2);
        vertexData.put(new float[]
        		{
		            scale * +1.f, 0.f, scale * -1.f, 1.f, 1.f,
		            scale * -1.f, 0.f, scale * -1.f, 0.f, 1.f,
		            scale * +1.f, 0.f, scale * +1.f, 1.f, 0.f,
		            scale * -1.f, 0.f, scale * +1.f, 0.f, 0.f
		        });
        vertexData.position(0);
        
        // index buffer
        IntBuffer indexData = BufferUtils.createIntBuffer(4);
        indexData.put(new int[] { 0, 1, 2, 3 });
        indexData.position(0);
        
        Geometry geo = new Geometry();
        geo.setIndices(indexData, GL_TRIANGLE_STRIP);
        geo.setVertices(vertexData);
        geo.addVertexAttribute(ShaderProgram.ATTR_POS, 3, 0);
        geo.addVertexAttribute(ShaderProgram.ATTR_TEX, 2, 12);
        return geo;
    }
    
    /**
     * Creates a sphere with texture on it.
     * @param r Radius of the Sphere
     * @param n Number of vertical stripes
     * @param k Number of horizontal stripes
     * @param imageFile Path to image file for texture
     * @return Sphere geometry
     */
    public static Geometry createSphere(float r, int n, int k, String imageFile) {
        float[][][] image = Util.getImageContents(imageFile);
        
        FloatBuffer fb = BufferUtils.createFloatBuffer((3+3+4) * (n+1)*(k+1));
        
        float dTheta = Util.PI / (float)k;
        float dPhi = Util.PI_MUL2 / (float)n;
        float theta = 0;
        for(int j=0; j <= k; ++j) {
            float sinTheta = (float)Math.sin(theta);
            float cosTheta = (float)Math.cos(theta);
            float phi = 0;
            for(int i=0; i <= n; ++i) {
                float sinPhi = (float)Math.sin(phi);
                float cosPhi = (float)Math.cos(phi);
                
                // position
                fb.put(r*sinTheta*cosPhi);  
                fb.put(r*cosTheta);
                fb.put(r*sinTheta*sinPhi);
                
                // normal
                fb.put(sinTheta*cosPhi);    
                fb.put(cosTheta);
                fb.put(sinTheta*sinPhi);
                
                fb.put(image[(int)((theta / Util.PI) * (float)image.length) % image.length]
                            [(int)(phi / Util.PI_MUL2 * (float)image[0].length) % image[0].length]);
                fb.put(1.0f);
                
                phi += dPhi;
            }
            theta += dTheta;
        }
        fb.position(0);
        
        IntBuffer ib = BufferUtils.createIntBuffer(k*(2*(n+1)+1));
        for(int j=0; j < k; ++j) {
            for(int i=0; i <= n; ++i) {
                ib.put((j+1)*(n+1) + i);
                ib.put(j*(n+1) + i);
            }
            ib.put(RESTART_INDEX);
        }
        ib.position(0);
        
        Geometry sphere = new Geometry();
        sphere.setIndices(ib, GL_TRIANGLE_STRIP);
        sphere.setVertices(fb);
        sphere.addVertexAttribute(Util.ATTR_POS, 3, 0);
        sphere.addVertexAttribute(Util.ATTR_NORMAL, 3, 12);
        sphere.addVertexAttribute(Util.ATTR_COLOR, 4, 24);
        return sphere;
    }
    
    /**
     * Creates a sphere with texture coordinates and normals.
     * @param r Radius of the sphere
     * @param n Number of vertical stripes
     * @param k Number of horizontal stripes
     * @return Sphere geometry
     */
    public static Geometry createSphere(float r, int n, int k) {
        
        FloatBuffer fb = BufferUtils.createFloatBuffer((3+3+2) * (n+1)*(k+1));
        
        float dTheta = Util.PI / (float)k;
        float dPhi = Util.PI_MUL2 / (float)n;
        float theta = 0;
        for(int j=0; j <= k; ++j) {
            float sinTheta = (float)Math.sin(theta);
            float cosTheta = (float)Math.cos(theta);
            float phi = 0;
            for(int i=0; i <= n; ++i) {
                float sinPhi = (float)Math.sin(phi);
                float cosPhi = (float)Math.cos(phi);
                
                // position
                fb.put(r*sinTheta*cosPhi);  
                fb.put(r*cosTheta);
                fb.put(r*sinTheta*sinPhi);
                
                // normal
                fb.put(sinTheta*cosPhi);    
                fb.put(cosTheta);
                fb.put(sinTheta*sinPhi);
                
                //tex
                fb.put(phi / Util.PI_MUL2);
                fb.put(theta / Util.PI);
                
                phi += dPhi;
            }
            theta += dTheta;
        }
        fb.position(0);
        
        IntBuffer ib = BufferUtils.createIntBuffer(k*(2*(n+1)+1));
        for(int j=0; j < k; ++j) {
            for(int i=0; i <= n; ++i) {
                ib.put((j+1)*(n+1) + i);
                ib.put(j*(n+1) + i);
            }
            ib.put(RESTART_INDEX);
        }
        ib.position(0);
        
        Geometry sphere = new Geometry();
        sphere.setIndices(ib, GL_TRIANGLE_STRIP);
        sphere.setVertices(fb);
        sphere.addVertexAttribute(Util.ATTR_POS, 3, 0);
        sphere.addVertexAttribute(Util.ATTR_NORMAL, 3, 12);
        sphere.addVertexAttribute(Util.ATTR_TEX, 2, 24);
        return sphere;
    }   

    /**
     * Creates a sky dome.
     * @param r radius of dome
     * @param n number of vertical stripes
     * @param k number of horizontal stripes
     * @return sky dome geometry
     */
    public static Geometry createSkyDome(float r, int n, int k) {
        FloatBuffer fb = BufferUtils.createFloatBuffer((3+2) * (n+1)*((k/2)+2));
        
        float dTheta = Util.PI / (float)k;
        float dPhi = Util.PI_MUL2 / (float)n;
        float theta = 0;
        for(int j=0; j <= (k/2)+1; ++j) {
            float sinTheta = (float)Math.sin(theta);
            float cosTheta = (float)Math.cos(theta);
            float phi = 0;
            for(int i=0; i <= n; ++i) {
                float sinPhi = (float)Math.sin(phi);
                float cosPhi = (float)Math.cos(phi);
                
                // position
                fb.put(r*sinTheta*cosPhi);  
                fb.put(r*cosTheta);
                fb.put(r*sinTheta*sinPhi);
                
                // texture coordinates
                fb.put(phi / Util.PI_MUL2);
                fb.put(theta / Util.PI);
                                
                phi += dPhi;
            }
            theta += dTheta;
        }
        fb.position(0);
        
        IntBuffer ib = BufferUtils.createIntBuffer(k*(2*(n+1)+1));
        for(int j=0; j < (k/2)+1; ++j)
        {
            for(int i=0; i <= n; ++i)
            {
                ib.put(j*(n+1) + i);
                ib.put((j+1)*(n+1) + i);
            }
            ib.put(RESTART_INDEX);
        }
        ib.position(0);
        
        Geometry skyDome = new Geometry();
        skyDome.setIndices(ib, GL_TRIANGLE_STRIP);
        skyDome.setVertices(fb);
        skyDome.addVertexAttribute(ShaderProgram.ATTR_POS, 3, 0);
        skyDome.addVertexAttribute(ShaderProgram.ATTR_TEX, 2, 12);
        return skyDome;
    }
    
    /**
     * Creates a terrain out of a height map.
     * @param path to terrain data maps
     * @param amplitude as scaling factor for height
     * @param scale - scaling factor for size
     * @return terrain geometry 
     */
    static public Geometry createTerrainFromMap(String path, float amplitude, int scale) {
        // vertex array id
        int vaid = glGenVertexArrays();
        glBindVertexArray(vaid);

        // load height map
        float[][][] ic = Util.getImageContents(path + "terrainHeight02.png");
        FloatBuffer vertexData = BufferUtils.createFloatBuffer(ic[0].length*ic.length*4);
        for (int h = 0; h < ic.length; h++) {
            for (int w = 0; w < ic[0].length; w++) {                
                vertexData.put(scale     *(w/(float)ic[0].length));
                vertexData.put((amplitude * ic[h][ic[0].length - w - 1][0])); // - amplitude*0.5f
                vertexData.put(scale     *(h/(float)ic.length));
                vertexData.put(1.0f);
            }
        }
        vertexData.rewind();
        
        // index buffer
        IntBuffer indexData = BufferUtils.createIntBuffer((ic.length-1)*2*ic[0].length+(ic.length-2));
        for (int y = 0; y < ic.length-1; y++)
        {
            for (int x = 0; x < ic[0].length; x++)
            {
                indexData.put(y*ic[0].length + x);
                indexData.put((y+1)*ic[0].length + x);
            }
            if (y < ic.length-2)
                indexData.put(-1);
        }
        indexData.position(0);      
        
        // create textures
        Texture normalTex = Texture.generateTexture(path   + "terrainNormal02.png",   TERRAIN_TEX_UNIT);
        Texture lightTex = Texture.generateTexture(path    + "terrainLight02.png",    TERRAIN_TEX_UNIT + 1);
        Texture specularTex = Texture.generateTexture(path + "terrainSpecular02.png", TERRAIN_TEX_UNIT + 2);
        Texture colorTex = Texture.generateTexture(path    + "terrainTex02.png",      TERRAIN_TEX_UNIT + 3);
             
        // create geometry
        Geometry geo = new Geometry();
        geo.setIndices(indexData, GL_TRIANGLE_STRIP);
        geo.setVertices(vertexData);
        geo.addVertexAttribute(ShaderProgram.ATTR_POS, 4, 0);
        
        // set textures
        geo.setNormalTex(normalTex);
        geo.setLightTex(lightTex);
        geo.setSpecularTex(specularTex);
        geo.setColorTex(colorTex);

        return geo;
    }
}