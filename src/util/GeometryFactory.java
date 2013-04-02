package util;

import static opengl.GL.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector3f;

/**
 * Stellt Methoden zur Erzeugung von Geometrie bereit.
 * @author Sascha Kolodzey, Nico Marniok
 */
public class GeometryFactory {
    
    final static public int NORMALTEX_UNIT = 2;
    final static public int HEIGHTTEX_UNIT = 3;
    
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
     * Erzeugt eine Kugel.
     * @param r Radius der Kugel
     * @param n Anzahl der vertikalen Streifen
     * @param k Anzahl der horizontalen Streifen
     * @param dayImage Pfad zur Bilddatei bei Tag
     * @param nightImage Pfad zur Bilddatei bei Nacht
     * @return Geometrie der Kugel
     */
    public static Geometry createSphere(float r, int n, int k, String dayImage, String nightImage) {
        float[][][] day = Util.getImageContents(dayImage);
        float[][][] night = null;
        if(nightImage != null) {
            night = Util.getImageContents(nightImage);
        }
        
        FloatBuffer fb = BufferUtils.createFloatBuffer((3+3+4+4) * (n+1)*(k+1));
        
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
                
                fb.put(day[(int)((theta / Util.PI) * (float)day.length) % day.length]
                          [(int)(phi / Util.PI_MUL2 * (float)day[0].length) % day[0].length]);
                fb.put(1.0f);
                
                if(nightImage == null) {
                    fb.put(new float[] { 0.0f, 0.0f, 0.0f, 1.0f });
                } else if(dayImage.equals(nightImage)) {
                    float color[] = night[(int)((theta / Util.PI) * (float)night.length) % night.length]
                                         [(int)(phi / Util.PI_MUL2 * (float)night[0].length) % night[0].length];
                    fb.put(0.1f * color[0]);
                    fb.put(0.1f * color[1]);
                    fb.put(0.1f * color[2]);
                    fb.put(1.0f);
                } else {
                    fb.put(night[(int)((theta / Util.PI) * (float)night.length) % night.length]
                                [(int)(phi / Util.PI_MUL2 * (float)night[0].length) % night[0].length]);
                    fb.put(1.0f);
                }
                
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
        sphere.addVertexAttribute(Util.ATTR_COLOR2, 4, 40);
        return sphere;
    }
    
    /**
     * Erzeugt ein Vierexk in der xy-Ebene. (4 Indizes)
     * @return VertexArrayObject ID
     */
    public static int createQuad() {        
        int vaid = glGenVertexArrays();
        glBindVertexArray(vaid);        
        
        // vertexbuffer
        FloatBuffer vertexData = BufferUtils.createFloatBuffer((3+4)*4); // world coords, color
        vertexData.put(new float[] {
            -1.0f, -1.0f, 0.0f,  1.0f, 1.0f, 0.4f, 1.0f,
            +1.0f, -1.0f, 0.0f,  0.4f, 1.0f, 0.4f, 1.0f,
            +1.0f, +1.0f, 0.0f,  1.0f, 1.0f, 0.4f, 1.0f,
            -1.0f, +1.0f, 0.0f,  0.4f, 1.0f, 0.4f, 1.0f,
        });
        vertexData.position(0);
                
        int vertexBufferID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferID);
        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);       
        
         // vs_in_pos  
        glEnableVertexAttribArray(Util.ATTR_POS);
        glVertexAttribPointer(Util.ATTR_POS, 3, GL_FLOAT, false, (3+4)*4, 0);       
        // vs_in_color
        glEnableVertexAttribArray(Util.ATTR_COLOR);
        glVertexAttribPointer(Util.ATTR_COLOR, 4, GL_FLOAT, false, (3+4)*4, 3*4);
        
        return vaid;
    }
    
    /**
     * Erzeugt ein Dreieck in der xy-Ebene. (3 Indizes)
     * @return VertexArrayObject ID
     */
    public static int createTriangle() {
        int vaid = glGenVertexArrays();
        glBindVertexArray(vaid);        
        
        // vertexbuffer
        FloatBuffer vertexData = BufferUtils.createFloatBuffer((3+4)*3); // color, world coords
        vertexData.put(new float[] {
            0.4f, 1.0f, 1.0f, 1.0f,  -1.0f, -1.0f, 0.0f,
            0.4f, 1.0f, 1.0f, 1.0f,  +1.0f, -1.0f, 0.0f,
            0.4f, 0.4f, 1.0f, 1.0f,   0.0f, +1.0f, 0.0f,
        });
        vertexData.position(0);
                
        int vertexBufferID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferID);
        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);       
        
        // vs_in_color
        glEnableVertexAttribArray(Util.ATTR_COLOR);
        glVertexAttribPointer(Util.ATTR_COLOR, 4, GL_FLOAT, false, (3+4)*4, 0);
         // vs_in_pos  
        glEnableVertexAttribArray(Util.ATTR_POS);
        glVertexAttribPointer(Util.ATTR_POS, 3, GL_FLOAT, false, (3+4)*4, 4*4);
        
        return vaid;        
    }
    
    /**
     * Erzeugt ein gleichmaessiges 2D n-Eck in der xy-Ebene. (n Indizes, als
     * GL_LINE_LOOP)
     * @param n Anzahl der Ecken
     * @return VertexArrayObject ID
     */
    public static int createNGon(int n) {        
        int vaid = glGenVertexArrays();
        glBindVertexArray(vaid);        
        
        // indexbuffer
        IntBuffer indexData = BufferUtils.createIntBuffer(n);
        for(int i=0; i < n; ++i) {
            indexData.put(i);
        }
        indexData.flip();
        
        int indexBufferID = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexData, GL_STATIC_DRAW); 
        
        // vertexbuffer
        FloatBuffer vertexData = BufferUtils.createFloatBuffer(3*n + 3*n); // world coords + normal coords
        double phi = 0;
        double deltaPhi = 2.0*Math.PI / (double)n;
        for(int i=0; i < n; ++i) {
            vertexData.put(0.5f*(float)Math.cos(phi));   // position x
            vertexData.put(0.5f*(float)Math.sin(phi));   // position y
            vertexData.put(0.5f*0.0f);                   // position z
            vertexData.put((float)Math.cos(phi));   // normal x
            vertexData.put((float)Math.sin(phi));   // normal y
            vertexData.put(0.0f);                   // normal z
            phi += deltaPhi;
        }
        vertexData.position(0);
                
        int vertexBufferID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferID);
        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);       
        
         // vs_in_pos  
        glEnableVertexAttribArray(Util.ATTR_POS);
        glVertexAttribPointer(Util.ATTR_POS, 3, GL_FLOAT, false, 24, 0);
         // vs_in_normal
        glEnableVertexAttribArray(Util.ATTR_NORMAL);
        glVertexAttribPointer(Util.ATTR_NORMAL, 3, GL_FLOAT, false, 24, 12);        
        
        return vaid;
    }
    
    /**
     * @brief Creates a terrain out of a height map.
     * @param map path of height map
     * @param amplitude scaling factor for height
     * @return the created terrain geometry
     */
    static public Geometry createTerrainFromMap(String map, float amplitude) {
        // vertex array id
        int vaid = glGenVertexArrays();
        glBindVertexArray(vaid);

        // load height map
        float[][][] ic = Util.getImageContents(map);
        float[][] env = new float[3][3];
        FloatBuffer vertexData = BufferUtils.createFloatBuffer(ic[0].length*ic.length*3);
        FloatBuffer normalTexBuf = BufferUtils.createFloatBuffer(ic[0].length*ic.length*4);
        FloatBuffer heightTexBuf = BufferUtils.createFloatBuffer(ic[0].length*ic.length*4);
        for (int h = 0; h < ic.length; h++) {
            for (int w = 0; w < ic[0].length; w++) {
                vertexData.put(new float[]{w/(float)ic[0].length, amplitude*ic[h][w][0], h/(float)ic.length});
                heightTexBuf.put(amplitude*ic[h][w][0]);
                heightTexBuf.put(new float[]{0,0,0});
                
                // set environment
                env[0][0] = ic[h-1 >= 0 ? h-1 : h][w-1 >= 0 ? w-1 : w][0];
                env[0][1] = ic[h][w-1 >= 0 ? w-1 : w][0];
                env[0][2] = ic[h+1 < ic.length ? h+1 : h][w-1 >= 0 ? w-1 : w][0];
                env[1][0] = ic[h-1 >= 0 ? h-1 : h][w][0];
                env[1][1] = ic[h][w][0];
                env[1][2] = ic[h+1 < ic.length ? h+1 : h][w][0];
                env[2][0] = ic[h-1 >= 0 ? h-1 : h][w+1 < ic[0].length ? w+1 : w][0];
                env[2][1] = ic[h][w+1 < ic[0].length ? w+1 : w][0];
                env[2][2] = ic[h+1 < ic.length ? h+1 : h][w+1 < ic[0].length ? w+1 : w][0];

                float gx = env[0][0] + 2*env[0][1] + env[0][2] - env[2][0] - 2*env[2][1] - env[2][2];
                float gz = env[0][0] + 2*env[1][0] + env[2][0] - env[0][2] - 2*env[1][2] - env[2][2];
                
                // put normals to normalTexBuffer
                Vector3f norm = new Vector3f(2.0f * gx, 0.5f * (float)Math.sqrt(1.0f - gx*gx - gz*gz), 2.0f * gz);
                normalTexBuf.put(norm.x);
                normalTexBuf.put(norm.y);
                normalTexBuf.put(norm.z);
                normalTexBuf.put(0);
            }
        }
        vertexData.position(0);
        normalTexBuf.position(0);
        heightTexBuf.position(0);
        
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
        
        // create normal texture from normaltexturebuffer
        Texture nTex = new Texture(GL_TEXTURE_2D, NORMALTEX_UNIT);
        nTex.bind();
        glTexImage2D(GL_TEXTURE_2D,
                0,
                GL_RGBA8,
                ic[0].length,
                ic.length,
                0,
                GL_RGBA,
                GL_FLOAT,
                normalTexBuf);
        glGenerateMipmap(GL_TEXTURE_2D);        
        
        // create height texture
        Texture hTex = new Texture(GL_TEXTURE_2D, HEIGHTTEX_UNIT);
        hTex.bind();
        glTexImage2D(GL_TEXTURE_2D,
                0,
                GL_RGBA8,
                ic[0].length,
                ic.length,
                0,
                GL_RGBA,
                GL_FLOAT,
                heightTexBuf);
        glGenerateMipmap(GL_TEXTURE_2D);        
        
        // create geometry
        Geometry geo = new Geometry();
        geo.setIndices(indexData, GL_TRIANGLE_STRIP);
        geo.setVertices(vertexData);
        geo.addVertexAttribute(ShaderProgram.ATTR_POS, 3, 0);
        geo.setNormalTex(nTex);
        geo.setHeightTex(hTex);
//        geo.addVertexAttribute(ShaderProgram.ATTR_NORMAL, 3, 12);

        return geo;
    }
}