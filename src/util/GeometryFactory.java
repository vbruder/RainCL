package util;

import static apiWrapper.GL.*;
import static apiWrapper.GL.GL_TRIANGLE_STRIP;
import static apiWrapper.GL.RESTART_INDEX;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;


/**
 * Factory for generating geometries.
 * @author Valentin Bruder
 * Used code of Sascha Kolodzey and Nico Marniok (Computergrafik 2012).
 */
public class GeometryFactory {
    
    // uses Texture units 10, 11, 12, 13
    final static public int TERRAIN_TEX_UNIT = 10;
    
    /**
     * Erzeugt eine Kugel.
     * @param r Radius der Kugel
     * @param n Anzahl der vertikalen Streifen
     * @param k Anzahl der horizontalen Streifen
     * @param imageFile Pfad zu einer Bilddatei
     * @return Geometrie der Kugel
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
     * Erzeugt eine Kugel mit Texturekoordinaten und Normalen.
     * @param r Radius der Kugel
     * @param n Anzahl der vertikalen Streifen
     * @param k Anzahl der horizontalen Streifen
     * @return Geometrie der Kugel
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
     * Create a sky dome around view position
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
                
                // tex coords
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
     * @brief Creates a terrain out of a height map.
     * @param path path of terrain data pictures
     * @param amplitude scaling factor for height
     * @param scale scaling factor for size
     * @return the created terrain geometry
     */
    static public Geometry createTerrainFromMap(String path, float amplitude, int scale) {
        // vertex array id
        int vaid = glGenVertexArrays();
        glBindVertexArray(vaid);

        // load height map
        float[][][] ic = Util.getImageContents(path + "terrainHeight01.png");
        FloatBuffer vertexData = BufferUtils.createFloatBuffer(ic[0].length*ic.length*3);
        for (int h = 0; h < ic.length; h++) {
            for (int w = 0; w < ic[0].length; w++) {                
                vertexData.put(scale     *(w/(float)ic[0].length));
                vertexData.put((amplitude *ic[h][w][0] - amplitude*0.5f));
                vertexData.put(scale     *(h/(float)ic.length));
            }
        }
        vertexData.rewind();
        
        // indexbuffer
        IntBuffer indexData = BufferUtils.createIntBuffer((ic.length-1)*2*ic[0].length+(ic.length-2));
        for (int y = 0; y < ic.length-1; y++) {
            for (int x = 0; x < ic[0].length; x++) {
                indexData.put(y*ic[0].length + x);
                indexData.put((y+1)*ic[0].length + x);
                
            }
            if (y < ic.length-2)
                indexData.put(-1);
        }
        indexData.position(0);      
        
        // create textures
        Texture normalTex = Texture.generateTexture(path   + "terrainNormal01.png",   TERRAIN_TEX_UNIT);
        Texture lightTex = Texture.generateTexture(path    + "terrainLight01.png",    TERRAIN_TEX_UNIT + 1);
        Texture specularTex = Texture.generateTexture(path + "terrainSpecular01.png", TERRAIN_TEX_UNIT + 2);
        Texture colorTex = Texture.generateTexture(path    + "terrainTex01.png",      TERRAIN_TEX_UNIT + 3);
             
        // create geometry
        Geometry geo = new Geometry();
        geo.setIndices(indexData, GL_TRIANGLE_STRIP);
        geo.setVertices(vertexData);
        geo.addVertexAttribute(ShaderProgram.ATTR_POS, 3, 0);
        
        geo.setNormalTex(normalTex);
        geo.setLightTex(lightTex);
        geo.setSpecularTex(specularTex);
        geo.setColorTex(colorTex);

        return geo;
    }
}