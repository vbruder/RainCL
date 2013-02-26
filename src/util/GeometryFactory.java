package util;

import static opengl.GL.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;

/**
 * Stellt Methoden zur Erzeugung von Geometrie bereit.
 * @author Sascha Kolodzey, Nico Marniok
 */
public class GeometryFactory {
    
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
}