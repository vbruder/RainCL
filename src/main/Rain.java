package main;

import static opengl.GL.*;
import static opengl.GL.GL_BACK;
import static opengl.GL.GL_CCW;
import static opengl.GL.GL_COLOR_BUFFER_BIT;
import static opengl.GL.GL_CULL_FACE;
import static opengl.GL.GL_DEPTH_BUFFER_BIT;
import static opengl.GL.GL_DEPTH_TEST;
import static opengl.GL.GL_FILL;
import static opengl.GL.GL_FRONT_AND_BACK;
import static opengl.GL.GL_LINE;
import static opengl.GL.GL_ONE;
import static opengl.GL.GL_ONE_MINUS_SRC_COLOR;
import static opengl.GL.GL_SRC_ALPHA;
import static opengl.GL.GL_ONE_MINUS_DST_ALPHA;

import java.nio.FloatBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import opengl.OpenAL;
import opengl.OpenCL;
import opengl.OpenCL.Device_Type;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import util.Camera;
import util.Geometry;
import util.GeometryFactory;
import util.PointLightOrb;
import util.Raindrops;
import util.ShaderProgram;
import util.Texture;
import util.Util;
import util.Util.ImageContents;
import window.Settings;
import window.Settings2;
import window.TimerCaller;

/**
 * main class
 * This framework uses LWJGL (www.lwjgl.org)
 * 
 * @author Valentin Bruder (vbruder@uos.de)
 */
public class Rain {

    private static Raindrops raindrops;
    private static PointLightOrb orb;
    
    // shader programs
    private static ShaderProgram terrainSP;
    private static ShaderProgram orbSP;
    
    // current configurations
    private static boolean bContinue = true;

    private static boolean culling = true;
    private static boolean wireframe = true;
    private static boolean audio = false;
    private static final String heightmapPath = "media/terrain/terrainHeight01.png";
    
    // control
    private static final Vector3f moveDir = new Vector3f(0.0f, 0.0f, 0.0f);
    private static final Camera cam = new Camera(); 
    
    // animation params
    private static float ingameTime = 0;
    private static float ingameTimePerSecond = 1.0f;
    
    // uniform data
    private static final Matrix4f viewProjMatrix = new Matrix4f();
    private static final Vector3f inverseLightDirection = new Vector3f();
    
    // terrain
    private static Geometry terrain;
    private static Texture normalTex, heightTex, colorTex;
    private static final int COLORTEX_UNIT = 8;
    
    // sound
    private static OpenAL sound;
    
    /*
     *  2^10 ~    1000
     *	2^15 ~   32000 
     *	2^17 ~  130000
     *	2^20 ~ 1000000
     */
    private static int maxParticles = 1 << 18;
    
    private static float fps;
    
    //settings gui
    private static TimerCaller tc = new TimerCaller();

    /**
     * main
     * @param argv
     */
    public static void main(String[] argv) {
        try {
            init();
            OpenCL.init();
            
            if (audio) {
                sound = new OpenAL();
                sound.init();
            }
                
            glEnable(GL_CULL_FACE);
            glFrontFace(GL_CCW);
            glCullFace(GL_BACK);
            glEnable(GL_DEPTH_TEST);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);   
            
            createTerrain();

            //create point light(s)
            orbSP = new ShaderProgram("./shader/Orb.vsh", "./shader/Orb.fsh");
            orb = new PointLightOrb();
            orb.setRadius(0.05f);
            orb.setOrbitRadius(1.25f + (float)Math.random());
            orb.setOrbitTilt(Util.PI_DIV4 - (float)Math.random() * Util.PI_DIV2);
            orb.setSpeed((float)Math.random());
            orb.setColor(new Vector3f((float)Math.random(), (float)Math.random(), (float)Math.random()));
            
            //create rain streaks
            raindrops = new Raindrops(Device_Type.GPU, Display.getDrawable(), heightTex.getId(), normalTex.getId(), maxParticles, cam, orb);
                        
            inverseLightDirection.set(1.0f, 0.2f, 0.0f);
            inverseLightDirection.normalise();
            
            // starting position
            cam.move(1.0f, 0.0f, 1.0f);
            
            render();
            
            //cleanup 
            OpenCL.destroy();
            if (audio)
                sound.destroy();
            tc.stop();
            Settings.destroyInstance();
            destroy();         
        } catch (LWJGLException ex) {
            Logger.getLogger(Rain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void createTerrain()
    {
        terrain = GeometryFactory.createTerrainFromMap(heightmapPath , 5.0f, 16);
        normalTex = terrain.getNormalTex();
        heightTex = terrain.getHeightTex();
        terrainSP = new ShaderProgram("shader/terrain.vsh", "shader/terrain.fsh");
        colorTex = Texture.generateTexture("media/terrain/terrainTex01.png", COLORTEX_UNIT);        
    }

    /**
     * Render the scene.
     * @throws LWJGLException
     */
    public static void render() throws LWJGLException {
        glClearColor(0.5f, 0.5f, 0.5f, 1.0f); // background color: grey
        
        long last = System.currentTimeMillis();
        long now, millis;
        long frameTimeDelta = 0;
        int frames = 0;
        
        while(bContinue && !Display.isCloseRequested()) {
            // time handling
            now = System.currentTimeMillis();
            millis = now - last;
            last = now;     
            frameTimeDelta += millis;
            ++frames;
            if(frameTimeDelta > 1000) {
                fps = 1e3f * (float)frames / (float)frameTimeDelta;
                frameTimeDelta -= 1000;
                frames = 0;
            }

            // input and animation
            handleInput(millis);
            animate(millis);
            
            // clear screen
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            
            orb.bindLightInformationToShader(raindrops.getShaderProgram().getID());
            
            //terrain
            terrainSP.use();
            terrainSP.setUniform("proj", cam.getProjection());
            terrainSP.setUniform("view", cam.getView());
            terrainSP.setUniform("normalTex", normalTex);
            terrainSP.setUniform("heightTex", heightTex);
            terrainSP.setUniform("colorTex", colorTex);
            terrain.draw();
            
            //rain streaks  
            glEnable(GL_BLEND);
            raindrops.draw(cam);
            glDisable(GL_BLEND);
            
            //TODO: proper integration
            glUseProgram(orbSP.getID());
            Matrix4f viewProj = new Matrix4f();
            Matrix4f.mul(cam.getProjection(), cam.getView(), viewProj);  
            orbSP.setUniform("viewProj", viewProj);
            orb.draw(orbSP.getID());
            
            // present screen
            Display.update();
            Display.sync(60);
        }
        terrainSP.delete();
        raindrops.getShaderProgram().delete();
    }
    
    /**
     * Handle input and change camera accordingly.
     * @param millis milliseconds, passed since last update
     */
    public static void handleInput(long millis) {
        float moveSpeed = 2e-3f*(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 2.0f : 1.0f)*(float)millis;
        float camSpeed = 5e-3f;
        
        while(Keyboard.next()) {
            if(Keyboard.getEventKeyState()) {
                switch(Keyboard.getEventKey()) {
                    case Keyboard.KEY_W: moveDir.z += 1.0f; break;
                    case Keyboard.KEY_S: moveDir.z -= 1.0f; break;
                    case Keyboard.KEY_A: moveDir.x += 1.0f; break;
                    case Keyboard.KEY_D: moveDir.x -= 1.0f; break;
                    case Keyboard.KEY_SPACE: moveDir.y += 1.0f; break;
                    case Keyboard.KEY_C: moveDir.y -= 1.0f; break;
                }
            } else {
                switch(Keyboard.getEventKey()) {
                    case Keyboard.KEY_W: moveDir.z -= 1.0f; break;
                    case Keyboard.KEY_S: moveDir.z += 1.0f; break;
                    case Keyboard.KEY_A: moveDir.x -= 1.0f; break;
                    case Keyboard.KEY_D: moveDir.x += 1.0f; break;
                    case Keyboard.KEY_SPACE: moveDir.y -= 1.0f; break;
                    case Keyboard.KEY_C: moveDir.y += 1.0f; break;
                    case Keyboard.KEY_F1: cam.changeProjection(); break;
                    case Keyboard.KEY_UP: break;
                    case Keyboard.KEY_DOWN: break;
                    case Keyboard.KEY_M:
                                        tc.start();
                                        tc.addTimerListener(Settings.getInstance());
                                        break;
                    case Keyboard.KEY_F2: glPolygonMode(GL_FRONT_AND_BACK, (wireframe ^= true) ? GL_FILL : GL_LINE); break;
                    case Keyboard.KEY_F3: if(culling ^= true) glEnable(GL_CULL_FACE); else glDisable(GL_CULL_FACE); break;
                }
            }
        }
        
        cam.move(moveSpeed * moveDir.z, moveSpeed * moveDir.x, moveSpeed * moveDir.y);
        
        while(Mouse.next()) {
            if(Mouse.getEventButton() == 0) {
                Mouse.setGrabbed(Mouse.getEventButtonState());
            }
            if(Mouse.isGrabbed()) {
                cam.rotate(-camSpeed*Mouse.getEventDX(), -camSpeed*Mouse.getEventDY());
            }
        }
        
        if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) bContinue = false;
        
        Matrix4f.mul(cam.getProjection(), cam.getView(), viewProjMatrix);        
    }
    
    /**
     * updates all moving particles
     * @param millis milliseconds, passed since last update
     */
    private static void animate(long millis) {
        // update time properly
        ingameTime += ingameTimePerSecond * 1e-3f * (float)millis;        
        raindrops.updateSimulation(millis);
        orb.animate(millis);
    }
    
    public static int getMaxParticles()
    {
        return maxParticles;
    }

    public static void setMaxParticles(int maxParticles)
    {
        Rain.maxParticles = maxParticles;
    }
    
    public static boolean isAudio()
    {
        return audio;
    }

    public static void setAudio(boolean audio)
    {
        Rain.audio = audio;
    }

    public static float getFPS()
    {
        return fps;
    }
}