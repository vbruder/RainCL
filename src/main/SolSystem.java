/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import static opengl.GL.*;
import static opengl.GL.GL_BACK;
import static opengl.GL.GL_BLEND;
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
import static opengl.GL.GL_TEXTURE_2D;

import java.util.logging.Level;
import java.util.logging.Logger;

import opengl.OpenCL;
import opengl.OpenCL.Device_Type;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import util.Camera;
import util.Geometry;
import util.GeometryFactory;
import util.RadiantOrb;
import util.Raindrops;
import util.Util;

/**
 *
 * @author Sascha Kolodzey, Nico Marniok
 */
public class SolSystem {
    private static final int MAX_ORBS = 8;

    private static Raindrops raindrops;
    // shader programs
    private static int fragmentLightingSP;
    private static int flDiffuseTexLoc;
    private static int flSpecularTexLoc;
    private static int flEyePositionLoc;
    private static int flkaLoc;
    private static int flkdLoc;
    private static int flksLoc;
    private static int flesLoc;
    private static int flcaLoc;
    
    private static int orbSP;
    
    // common uniform locations
    private static int modelLoc;
    private static int flModelITLoc;
    private static int viewProjLoc;
    
    // scene data
    private static Geometry earth = null;
    private static Geometry moon = null;
    private static Geometry clouds = null;

    private static int earthFineness = 0;
    private static RadiantOrb orbs[] = new RadiantOrb[MAX_ORBS];

    // current configurations
    private static boolean bContinue = true;
    private static boolean culling = true;
    private static boolean wireframe = true;
    
    // control
    private static final Vector3f moveDir = new Vector3f(0.0f, 0.0f, 0.0f);
    private static final Camera cam = new Camera(); 
    
    // animation params
    private static float ingameTime = 0;
    private static float ingameTimePerSecond = 1.0f;
    
    // uniform data
    private static final Matrix4f earthModelMatrix = new Matrix4f();
    private static final Matrix4f moonModelMatrix = new Matrix4f();
    private static final Matrix4f cloudsModelMatrix = new Matrix4f();
    private static final Matrix4f viewProjMatrix = new Matrix4f();
    private static final Vector3f inverseLightDirection = new Vector3f();
    private static int earthTexture;
    private static int earthSpecularTexture;
    private static int moonTexture;
    private static int cloudsTexture;
    
    // temp data
    private static final Matrix4f moonRotation = new Matrix4f();
    private static final Matrix4f moonTilt = new Matrix4f();
    private static final Matrix4f moonTranslation = new Matrix4f();
    
    public static void main(String[] argv) {
        try {
            init();
            OpenCL.init();
            glEnable(GL_CULL_FACE);
            glFrontFace(GL_CCW);
            glCullFace(GL_BACK);
            glEnable(GL_DEPTH_TEST);
            glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_COLOR);
            
            raindrops = new Raindrops(Device_Type.GPU, Display.getDrawable());
            
            fragmentLightingSP = Util.createShaderProgram("./shader/FragmentLighting_VS.glsl", "./shader/FragmentLighting_FS.glsl");
            flModelITLoc = glGetUniformLocation(fragmentLightingSP, "modelIT");
            flDiffuseTexLoc = glGetUniformLocation(fragmentLightingSP, "diffuseTex");
            flSpecularTexLoc = glGetUniformLocation(fragmentLightingSP, "specularTex");
            flEyePositionLoc = glGetUniformLocation(fragmentLightingSP, "eyePosition");
            flkaLoc = glGetUniformLocation(fragmentLightingSP, "k_a");
            flkdLoc = glGetUniformLocation(fragmentLightingSP, "k_dif");
            flksLoc = glGetUniformLocation(fragmentLightingSP, "k_spec");
            flesLoc = glGetUniformLocation(fragmentLightingSP, "es");
            flcaLoc = glGetUniformLocation(fragmentLightingSP, "c_a");
            
            orbSP = Util.createShaderProgram("./shader/Orb_VS.glsl", "./shader/Orb_FS.glsl");

            inverseLightDirection.set(1.0f, 0.2f, 0.0f);
            inverseLightDirection.normalise();           

            earthTexture = Util.generateTexture("earth.jpg");
            earthSpecularTexture = Util.generateTexture("earth_spec.jpg");
            moonTexture = Util.generateTexture("moon.jpg");
            cloudsTexture = Util.generateTexture("clouds.jpg");
            
            for(int i=0; i < MAX_ORBS; ++i) {
                orbs[i] = new RadiantOrb();
                orbs[i].setRadius(0.2f);
                orbs[i].setOrbitRadius(2f + (float)Math.random());
                orbs[i].setOrbitTilt(Util.PI_DIV4 - (float)Math.random() * Util.PI_DIV2);
                orbs[i].setSpeed((float)Math.random());
                orbs[i].setColor(new Vector3f((float)Math.random(), (float)Math.random(), (float)Math.random()));
            }
            
            cam.move(-10.0f, 0.0f, 0.0f);
            changeFineness(32);
           
            Util.translationX(3.0f, moonTranslation);
            Util.rotationX((float)Math.toRadians(15.0), moonTilt);
                        
            render();
            raindrops.destroy();
            OpenCL.destroy();
            destroy();
        } catch (LWJGLException ex) {
            Logger.getLogger(SolSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void render() throws LWJGLException {
        glClearColor(0.2f, 0.2f, 0.2f, 1.0f); // background color: dark grey
        
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
                System.out.println(1e3f * (float)frames / (float)frameTimeDelta + " FPS");
                frameTimeDelta -= 1000;
                frames = 0;
            }
            
            // input and animation
            handleInput(millis);
            animate(millis);
            
            // clear screen
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            
            glUseProgram(raindrops.getShaderProgram());
            for(int i=0; i < MAX_ORBS; ++i) {
                orbs[i].bindLightInformationToShader(raindrops.getShaderProgram(), i);
            }
            //raindrops
            raindrops.draw(cam);

            setActiveProgram(fragmentLightingSP);
            glUniform3f(flEyePositionLoc, cam.getCamPos().x, cam.getCamPos().y, cam.getCamPos().z);           
            glUniform1f(flkaLoc, 0.05f);
            glUniform1f(flkdLoc, 0.6f);
            glUniform1f(flksLoc, 0.3f);
            glUniform1f(flesLoc, 16.0f);
            glUniform3f(flcaLoc, 1.0f, 1.0f, 1.0f);
                     
            for(int i=0; i < MAX_ORBS; ++i) {
                orbs[i].bindLightInformationToShader(fragmentLightingSP, i);
            }

            // earth
            texture2uniform(earthTexture, GL_TEXTURE_2D, 0, flDiffuseTexLoc);
            texture2uniform(earthSpecularTexture, GL_TEXTURE_2D, 1, flSpecularTexLoc);
            matrix2uniform(earthModelMatrix, modelLoc);
            matrix2uniform(earthModelMatrix, flModelITLoc); // just rotation
            //earth.draw();

            // moon
            matrix2uniform(moonModelMatrix, modelLoc);
            matrix2uniform(moonModelMatrix, flModelITLoc); // just rotation and translation
            texture2uniform(moonTexture, GL_TEXTURE_2D, 2, flDiffuseTexLoc);
            texture2uniform(0, GL_TEXTURE_2D, 1, flSpecularTexLoc);
            //moon.draw();
            
            // clouds
            matrix2uniform(cloudsModelMatrix, modelLoc);
            matrix2uniform(cloudsModelMatrix, flModelITLoc); // just rotation
            texture2uniform(cloudsTexture, GL_TEXTURE_2D, 3, flDiffuseTexLoc);
            glEnable(GL_BLEND);
            //clouds.draw();
            glDisable(GL_BLEND);

            // orbs
            setActiveProgram(orbSP);
            for(int i=0; i < MAX_ORBS; ++i) {
                orbs[i].draw(orbSP);
            } 
            
            // present screen
            Display.update();
            Display.sync(60);
        }
    }
    
    /**
     * Behandelt Input und setzt die Kamera entsprechend.
     * @param millis Millisekunden seit dem letzten Aufruf
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
                    case Keyboard.KEY_UP: changeFineness(2 * earthFineness); break;
                    case Keyboard.KEY_DOWN: changeFineness(earthFineness / 2); break;
                    case Keyboard.KEY_LEFT:
                        if(Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                            ingameTimePerSecond = 0.0f;
                        } else {
                            ingameTimePerSecond = Math.max(1.0f / 64.0f, 0.5f * ingameTimePerSecond);
                        }
                        break;
                    case Keyboard.KEY_RIGHT:
                        if(Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                            ingameTimePerSecond = 1.0f;
                        } else {
                            ingameTimePerSecond = Math.min(64.0f, 2.0f * ingameTimePerSecond);
                        }
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
     * Hilfsmethode, um eine Matrix in eine Uniform zu schreiben. Das
     * zugehoerige Programmobjekt muss aktiv sein.
     * @param matrix Quellmatrix
     * @param uniform Ziellocation
     */
    private static void matrix2uniform(Matrix4f matrix, int uniform) {
        matrix.store(Util.MAT_BUFFER);
        Util.MAT_BUFFER.position(0);
        glUniformMatrix4(uniform, false, Util.MAT_BUFFER);
    }
    
    /**
     * Hilfsmethode, um eine Textur in eine Uniform zu schreiben. Das
     * zugehoerige Programmobjekt muss aktiv sein.
     * @param texture ID der Textur
     * @param target Bindtarget der Textur (z.B. GL_TEXTURE_2D)
     * @param slot Slot, der die Textur zur Vefuegung stellen soll
     * @param location Location der Uniform
     */
    private static void texture2uniform(int texture, int target, int slot, int location) {
        glActiveTexture(GL_TEXTURE0 + slot);
        glBindTexture(target, texture);
        glUniform1i(location, slot);
    }
    
    /**
     * Aktualisiert Model Matrizen der Erde und des Mondes.
     * @param millis Millisekunden, die seit dem letzten Aufruf vergangen sind.
     */
    private static void animate(long millis) {
        // update ingame time properly
        ingameTime += ingameTimePerSecond * 1e-3f * (float)millis;
        
        // earth
        float earthRotationAngle = Util.PI_MUL2 * ingameTime;
        Util.rotationY(earthRotationAngle, earthModelMatrix);
        
        // clouds
        float cloudsRotationAngle = earthRotationAngle * 0.7f;
        Util.rotationY(cloudsRotationAngle, cloudsModelMatrix);
        
        // moon
        float moonRotationAngle = earthRotationAngle / 27.0f;
        Util.rotationY(moonRotationAngle, moonRotation);
        Util.mul(moonModelMatrix, moonTilt, moonRotation, moonTranslation);
        
        // orbs
        for(int i=0; i < MAX_ORBS; ++i) {
            orbs[i].animate(millis);
        }
        
        raindrops.updateSimulation(millis);
    }
    
    /**
     * Aendert die Feinheit der Kugelannaeherung der Erde und des Mondes.
     * @param newFineness die neue Feinheit
     */
    private static void changeFineness(int newFineness) {
        if(newFineness >= 4 && newFineness <= 8192) {
            if(earth != null) {
                earth.delete();
            }
            if(moon != null) {
                moon.delete();
            }
            if(clouds != null) {
                clouds.delete();
            }
            earth = GeometryFactory.createSphere(1.0f, newFineness, newFineness/2);
            clouds = GeometryFactory.createSphere(1.05f, newFineness/2, newFineness/4);
            moon = GeometryFactory.createSphere(0.5f, newFineness/2, newFineness/4);
            earthFineness = newFineness;
        }
    }
    
    /**
     * Aendert das aktuelle Programm und alle zugehoerigen Uniform locations.
     * @param program das neue aktuelle Programm
     */
    private static void setActiveProgram(int program) {
        glUseProgram(program);        
        modelLoc = glGetUniformLocation(program, "model");
        viewProjLoc = glGetUniformLocation(program, "viewProj");
        matrix2uniform(viewProjMatrix, viewProjLoc);
    }
}