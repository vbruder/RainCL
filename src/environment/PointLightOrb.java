package environment;

import static apiWrapper.OpenGL.*;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import util.Geometry;
import util.GeometryFactory;
import util.Util;

/**
 * @author Valentin Bruder
 * Based on code of Nico Marniok (Computergrafik 2012)
 */
public class PointLightOrb {
    private static final Geometry geo = GeometryFactory.createSphere(1.0f, 16, 8);
    
    public final Matrix4f model = new Matrix4f();
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
        this.position = new Vector3f(20.0f, 20.0f, 20.0f);
        Util.transformCoord(this.model, new Vector3f(), this.position);
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
        
        PointLightOrb.geo.draw();
    }

    public Vector3f getPosition()
    {
        return position;
    }

    public Vector3f getColor()
    {
        return color;
    }
}