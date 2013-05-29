/**
 * 
 */
package environment;

import org.lwjgl.util.vector.Vector3f;

/**
 * Class representing the sun as major light source (directional lighting).
 * @author Valentin Bruder
 * @date 29.05.2013
 *
 */
public class Sun
{
    private Vector3f color;
    private Vector3f direction;
    private float intensity;
    
    /**
     * Creates a sun object with a position, direction and light intensity.
     * @param color Color of the sun light.
     * @param dir   Direction of the sun.
     * @param intensity Intensity of the sun light.
     */
    public Sun(Vector3f color, Vector3f dir, float intensity)
    {
        this.color = color;
        this.direction = dir;
        this.intensity = intensity;
    }

    /**
     * @return the position
     */
    public Vector3f getColor()
    {
        return color;
    }

    /**
     * @param position the position to set
     */
    public void setColor(Vector3f position)
    {
        this.color = position;
    }

    /**
     * @return the direction
     */
    public Vector3f getDirection()
    {
        return direction;
    }

    /**
     * @param direction the direction to set
     */
    public void setDirection(Vector3f direction)
    {
        this.direction = direction;
    }

    /**
     * @return the intensity
     */
    public float getIntensity()
    {
        return intensity;
    }

    /**
     * @param intensity the intensity to set
     */
    public void setIntensity(float intensity)
    {
        this.intensity = intensity;
    }
    
    
}
