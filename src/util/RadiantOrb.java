/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import static opengl.GL.*;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author NMARNIOK
 */
public class RadiantOrb {
    private static final Geometry geo = GeometryFactory.createSphere(1.0f, 16, 8);
    
    private final Matrix4f model = new Matrix4f();
    private Vector3f position = new Vector3f();
    private final Vector3f color = new Vector3f();
    private float radius = 1.0f;
    private float orbitRadius = 0.0f;
    private float orbitAngle = 0.0f;
    private float orbitTilt = 0.0f;
    private float speed = 0.0f;
    
    public void setColor(Vector3f color) {
        this.color.set(color);
    }
    
    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setOrbitRadius(float orbitRadius) {
        this.orbitRadius = orbitRadius;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setOrbitTilt(float orbitTilt) {
        this.orbitTilt = orbitTilt;
    }
    
    public void animate(long millis) {
        //this.orbitAngle += 1e-3f * (float)millis * this.speed;
        this.buildModelMatrix();
    }
    
    private void buildModelMatrix() {
        Util.mul(this.model, Util.rotationY(this.orbitAngle, null), Util.rotationX(this.orbitTilt, null), Util.translationZ(this.orbitRadius, null), Util.scale(this.radius, null));
        Util.transformCoord(this.model, new Vector3f(), this.position);
        this.position = new Vector3f(10.0f, 10.0f, 0.0f);
    }
    
    public void bindLightInformationToShader(int program) {
        glUseProgram(program);
        
        int intesityLoc = glGetUniformLocation(program, "pointLightColor");
        glUniform3f(intesityLoc, this.color.x, this.color.y, this.color.z);

        int positionLoc = glGetUniformLocation(program, "pointLightDir");
        glUniform3f(positionLoc, this.position.x, this.position.y, this.position.z);
    }
    
    public void draw(int program) {
        glUseProgram(program);
        
        int modelLoc = glGetUniformLocation(program, "model");
        Util.MAT_BUFFER.position(0);
        this.model.store(Util.MAT_BUFFER);
        Util.MAT_BUFFER.position(0);
        glUniformMatrix4(modelLoc, false, Util.MAT_BUFFER);
        Util.MAT_BUFFER.position(0);
        
        int colorLoc = glGetUniformLocation(program, "color");
        glUniform3f(colorLoc, this.color.x, this.color.y, this.color.z);
        
        RadiantOrb.geo.draw();
    }
}