package util;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

/**
 * Class representing a camera.
 * @author Valentin Bruder
 */
public final class Camera
{
    private float phi = 0, theta = 0;
    
    private final Vector3f viewDir = new Vector3f(0,0,1);
    private final Vector3f upDir = new Vector3f(0,1,0);
    private final Vector3f sideDir = new Vector3f(1,0,0);
    private final Vector3f camPos = new Vector3f(0,0,-1);
    
    private final Matrix4f view = new Matrix4f();
    private final Matrix4f projection = new Matrix4f();
    
    // near and far plane
    private float near = 1e-2f;
    private float far = 1e3f;
    
    /**
     * Default constructor.
     */
    public Camera()
    {
        this.updateView();
        this.updateProjection();
    }
    
    /**
     * Rotates horizontal and vertical.
     * @param dPhi horizontal rotation
     * @param dTheta vertical rotation
     */
    public void rotate(float dPhi, float dTheta)
    {
        phi += dPhi;
        theta = Util.clamp(theta + dTheta, -Util.PI_DIV2, +Util.PI_DIV2);
        
        Matrix4f rotX = Util.rotationX(theta, null);
        Matrix4f rotY = Util.rotationY(phi, null);
        Matrix4f rot = Util.mul(null, rotY, rotX);
        sideDir.set(rot.m00, rot.m01, rot.m02);
        upDir.set(rot.m10, rot.m11, rot.m12);
        viewDir.set(rot.m20, rot.m21, rot.m22);
    }
    
    /**
     * Moves the camera.
     * @param fb Move in view direction.
     * @param lr Move in side direction.
     * @param ud Move up or down.
     */
    public void move(float fb, float lr, float ud)
    {
        camPos.x += fb * viewDir.x + lr * sideDir.x;
        camPos.y += fb * viewDir.y + lr * sideDir.y + ud;
        camPos.z += fb * viewDir.z + lr * sideDir.z;
    }
    
    /**
     * Update the view matrix.
     */
    public void updateView()
    {
        Vector3f lookAt = Vector3f.add(camPos, viewDir, null);
        Util.lookAtRH(camPos, lookAt, upDir, view);
    }
    
    /**
     * Update the projection matrix.
     */
    public void updateProjection()
    {
    	Util.frustum(-1e-2f, 1e-2f, -1e-2f, 1e-2f, near, far, projection);
    }

    /**
     * @return the projection matrix
     */
    public Matrix4f getProjection()
    {
        this.updateProjection();
        return projection;
    }

    /**
     * @return the view matrix
     */
    public Matrix4f getView()
    {
        this.updateView();
        return view;
    }

    /**
     * @return the camera position
     */
    public Vector3f getCamPos()
    {
        return camPos;
    }
    
}