package main;

import static apiWrapper.OpenGL.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import apiWrapper.OpenAL;
import apiWrapper.OpenCL;
import apiWrapper.OpenCL.Device_Type;

import environment.Rainstreaks;
import environment.Sun;
import environment.Water;

import util.Camera;
import util.DeferredShader;
import util.FrameBuffer;
import util.Geometry;
import util.GeometryFactory;
import util.ShaderProgram;
import util.Texture;
import util.Util;
import window.Settings;
import window.TimerCaller;

/******************************   RainCL   **********************************
 * This framework simulates and renders a rain system.						*
 * LWJGL (Light Weight Java Game Library) is used for OpenGL, OpenCL and 	*
 * OpenAL access. For more information see <http://www.lwjgl.org/>.			*
 * 																			*
 * Copyright (C) 2013  Valentin Bruder <vbruder@gmail.com>					*
 *																			*
 * This program is free software: you can redistribute it and/or modify		*
 * it under the terms of the GNU General Public License as published by		*
 * the Free Software Foundation, either version 3 of the License, or		*
 * (at your option) any later version.										*
 *																			*
 * This program is distributed in the hope that it will be useful,			*
 * but WITHOUT ANY WARRANTY; without even the implied warranty of			*
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the			*
 * GNU General Public License for more details.								*
 *																			*
 * You should have received a copy of the GNU General Public License		*
 * along with this program. If not, see <http://www.gnu.org/licenses/>. 	*
 ****************************************************************************/
public class Main {
	
	// shader programs
    private static ShaderProgram terrainSP;
    private static ShaderProgram skySP;
    
    // current configurations
    private static boolean bContinue = true;

    private static boolean culling = true;
    private static boolean wireframe = false;
    private static boolean audio = false;
    private static boolean points = false;

	// control
    private static final Vector3f moveDir = new Vector3f(0.0f, 0.0f, 0.0f);
    private static final Camera cam = new Camera(); 
    
    // animation parameters
    private static float ingameTime = 0;
    private static float ingameTimePerSecond = 1.0f;
    
    // uniform data
    private static final Matrix4f viewProjMatrix = new Matrix4f();
        
    //environment
    private static boolean drawTerrain 	= true;
    private static boolean drawRain 	= false;
    private static boolean drawWater 	= true;
    private static boolean drawSky 		= true;
    private static boolean drawFog		= false;
    
    private static Rainstreaks raindrops = null;

    private static Sun sun;
    private static Water watermap = null;
    //sky
    private static Geometry skyBox;
    private static Texture cubeMap;
    private static Matrix4f skyMoveMatrix = new Matrix4f();

    //terrain
    private static Geometry terrain;
    private static String terrainDataPath = "media/terrain/";
    private static int scaleTerrain = 128;

	//lighting
    private static float k_diff =  15.0f;
    private static float k_spec =  20.0f;
    private static float k_ambi =  0.1f;
    
    //sound
    private static OpenAL sound;
          
    //GUI
    //settings
    private static TimerCaller tc = new TimerCaller();
    //view
    private static float fps;
    private static Vector3f fogThickness;

    /**
     * Main method as entry point into the framework.
     * @param argv
     */
    public static void main(String[] argv)
    {
        try
        {
            init();
            OpenCL.init();
            sound = new OpenAL();
            
            if (audio)
            {
                sound.init();
            }
            
            // OpenGL scene parameters
            glEnable(GL_CULL_FACE);
            glFrontFace(GL_CCW);
            glCullFace(GL_BACK);
            glEnable(GL_DEPTH_TEST);   
            
            // create environment
            createTerrain();            
            createSky();
            
            // create light sources
            sun = new Sun(new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(-200.0f, -80.0f, -100.0f), 0.1f);
            
            // create rain system
            createRainsys();

            // camera starting position
            cam.move(50.0f, 50.0f, 20.0f);
            
            // start render loop
            render();

            // cleanup
            raindrops.destroy();
            watermap.destroy();
            OpenCL.destroy();
            sound.destroy();
            tc.stop();
            Settings.destroyInstance();
            destroy();         
        }
        catch (LWJGLException ex)
        {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Create the rain-water-system.
     * @throws LWJGLException
     */
    private static void createRainsys() throws LWJGLException
    {
    	if (raindrops != null)
    	{
    		raindrops.destroy();
    	}
    	if (watermap != null)
    	{
    		watermap.destroy();
    	}
        //create rain streaks
        raindrops = new Rainstreaks(Device_Type.GPU, Display.getDrawable(), cam, sun);
        Rainstreaks.setMaxParticles(1 << 17);
        //create water map
        watermap = new Water(Device_Type.GPU, Display.getDrawable(), terrain, skyBox);
	}

	/**
     * Create data for sky box.
     */
    private static void createSky()
    {
    	skyBox = GeometryFactory.createCube(75.f);
    	
    	//create cube map data
    	cubeMap = new Texture(GL_TEXTURE_CUBE_MAP, 5);
		String[] cubeMapFileName = {"left", "right", "up", "down", "front", "back"};
		int[] cubeMapTargets = 	{	
									GL_TEXTURE_CUBE_MAP_POSITIVE_X,
									GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
									GL_TEXTURE_CUBE_MAP_POSITIVE_Y,
									GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,
									GL_TEXTURE_CUBE_MAP_POSITIVE_Z,
									GL_TEXTURE_CUBE_MAP_NEGATIVE_Z
								};
        cubeMap.bind();
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		
		//textures on cube map
        for(int i = 0; i < 6; i++)
        {
        	Util.ImageContents contents = Util.loadImage("media/skyTex/" + cubeMapFileName[i] + ".png");
        	int format = 0;
        	int internalFormat = 0;
        	switch(contents.colorComponents)
        	{
            	case 1: internalFormat = GL_R8;    format = GL_RED;  break;
            	case 2: internalFormat = GL_RG8;   format = GL_RG;   break;
            	case 3: internalFormat = GL_RGB8;  format = GL_RGB;  break;
            	case 4: internalFormat = GL_RGBA8; format = GL_RGBA; break;
        	}
        	glTexImage2D(cubeMapTargets[i], 0, internalFormat, contents.width, contents.height, 0, format, GL_FLOAT, contents.data);
		}
		glGenerateMipmap(GL_TEXTURE_CUBE_MAP);

		skyBox.setColorTex(cubeMap);
		
		skySP = new ShaderProgram("shader/Sky.vsh", "shader/Sky.fsh");
	}
    
    /**
     * Create terrain data.
     */
    private static void createTerrain()
    {
        terrain = GeometryFactory.createTerrainFromMap(terrainDataPath, 32.0f, scaleTerrain);
        terrainSP = new ShaderProgram("shader/Terrain.vsh", "shader/Terrain.fsh");      
    }

    /**
     * Render the scene.
     * @throws LWJGLException
     */
    public static void render() throws LWJGLException
    {
    	//background color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        
//        DeferredShader reflectShader = new DeferredShader();
//        reflectShader.init(0);
//        reflectShader.registerShaderProgram(defShadingSP);
        
//        DeferredShader sceneShader = new DeferredShader();
//        sceneShader.init(0);
//        sceneShader.registerShaderProgram(defShadingSP);
//        
//        FrameBuffer fbo = new FrameBuffer();
//        fbo.init(true, WIDTH, HEIGHT);
        
        long last = System.currentTimeMillis();
        long now, millis;
        long frameTimeDelta = 0;
        int frames = 0;
       
        // render loop
        while(bContinue && !Display.isCloseRequested())
        {
            // time handling
            now = System.currentTimeMillis();
            millis = now - last;
            last = now;     
            frameTimeDelta += millis;
            ++frames;
            if(frameTimeDelta > 1000)
            {
                fps = 1e3f * (float)frames / (float)frameTimeDelta;
                frameTimeDelta -= 1000;
                frames = 0;
            }

            // input and animation
            handleInput(millis);
            animate(millis);
            // clear screen
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            
//            // enable deferred shading
//            reflectShader.bind();
//            reflectShader.clear();
//
//            reflectScene();
//            Texture reflected = reflectShader.getDiffuseTexture();
//            reflectShader.finish();
//            reflectShader.DrawTexture(reflectShader.getDiffuseTexture());
//            
//            sceneShader.bind();
//            sceneShader.clear();
            
            //sky dome
            if (drawSky)
            {
	            skySP.use();
	            skySP.setUniform("proj", cam.getProjection());
	            skySP.setUniform("view", cam.getView());
	            skySP.setUniform("model", skyMoveMatrix);
	            skySP.setUniform("cubeMap", cubeMap);
	            skySP.setUniform("fogThickness", fogThickness);
	            glFrontFace(GL_CW);
	            skyBox.draw();
	            glFrontFace(GL_CCW);
            }
            
            //volumetric fog
            if (drawFog)
            {
            	glBlendFunc(GL_ONE_MINUS_SRC_ALPHA, GL_ONE);
            	glEnable(GL_BLEND);
            	glDisable(GL_DEPTH_TEST);
            	glDisable(GL_CULL_FACE);
            	raindrops.drawFog(cam);
            	glEnable(GL_CULL_FACE);
            	glDisable(GL_BLEND);
            	glEnable(GL_DEPTH_TEST);
            }
            
            //terrain
            if (drawTerrain)
            {
            	terrainSP.use();
	            //VS
	            terrainSP.setUniform("proj", cam.getProjection());
	            terrainSP.setUniform("view", cam.getView());
	            terrainSP.setUniform("scale", scaleTerrain);
	            //FS
	            terrainSP.setUniform("normalTex", terrain.getNormalTex());
	            terrainSP.setUniform("lightTex", terrain.getLightTex());
	            terrainSP.setUniform("specularTex", terrain.getSpecularTex());
	            terrainSP.setUniform("colorTex", terrain.getColorTex());
	            terrainSP.setUniform("sunIntensity", sun.getIntensity());
	            terrainSP.setUniform("sunDir", sun.getDirection());
	            terrainSP.setUniform("k_diff", k_diff);
	            terrainSP.setUniform("k_spec", k_spec);
	            terrainSP.setUniform("k_ambi", k_ambi);
	            terrainSP.setUniform("eyePosition", cam.getCamPos());
	            terrainSP.setUniform("fogThickness", fogThickness);
	            
	            terrain.draw();
            }
	            
            //water map
            if (drawWater)
            {
            	glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            	glEnable(GL_BLEND);
            	watermap.draw(cam, points, scaleTerrain, fogThickness, sun);
            	glDisable(GL_BLEND);
            }
            
            //rain streaks
            if (drawRain)
            {
            	glEnable(GL_BLEND);
            	glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
//            	glDisable(GL_BLEND);
            	raindrops.draw(cam);
            	glDisable(GL_BLEND);
            }
            
//            sceneShader.finish();        
//            sceneShader.DrawTexture(sceneShader.getDiffuseTexture());
            
            // present screen
            Display.update();
            Display.sync(60);
        }
        terrainSP.delete();
        raindrops.getShaderProgram().delete();
//        sceneShader.delete();
    }
    
    private static void reflectScene()
	{
    	//reflection matrix
        Matrix4f reflMat = new Matrix4f();
        reflMat.m00 = 1.f; reflMat.m10 =  0.f; reflMat.m20 =  0.f; reflMat.m30 = 0.f;
        reflMat.m01 = 0.f; reflMat.m11 =  1.f; reflMat.m21 =  0.f; reflMat.m31 = 0.f;
        reflMat.m02 = 0.f; reflMat.m12 =  0.f; reflMat.m22 = -1.f; reflMat.m32 = 0.f;
        reflMat.m03 = 0.f; reflMat.m13 =  0.f; reflMat.m23 =  0.f; reflMat.m33 = 1.f;
        
        Matrix4f view = new Matrix4f();
        Matrix4f.mul(reflMat, cam.getView(), view);
		
        //sky box
        skySP.use();
        skySP.setUniform("proj", cam.getProjection());
        skySP.setUniform("view", view);
        skySP.setUniform("model", skyMoveMatrix);
        skySP.setUniform("cubeMap", cubeMap);
        skySP.setUniform("fogThickness", fogThickness);
        //glFrontFace(GL_CW);
        skyBox.draw();
        //glFrontFace(GL_CCW);
        
        //terrain
        if (drawTerrain)
        {
        	terrainSP.use();
            //VS
            terrainSP.setUniform("proj", cam.getProjection());
            terrainSP.setUniform("view", view);
            terrainSP.setUniform("scale", scaleTerrain);
            //FS
            terrainSP.setUniform("normalTex", terrain.getNormalTex());
            terrainSP.setUniform("lightTex", terrain.getLightTex());
            terrainSP.setUniform("specularTex", terrain.getSpecularTex());
            terrainSP.setUniform("colorTex", terrain.getColorTex());
            terrainSP.setUniform("sunIntensity", sun.getIntensity());
            terrainSP.setUniform("sunDir", sun.getDirection());
            terrainSP.setUniform("k_diff", k_diff);
            terrainSP.setUniform("k_spec", k_spec);
            terrainSP.setUniform("k_ambi", k_ambi);
            terrainSP.setUniform("eyePosition", cam.getCamPos());
            terrainSP.setUniform("fogThickness", fogThickness);
            
            glFrontFace(GL_CW);
            //terrain.draw();
            glFrontFace(GL_CCW);
        }
	}

	/**
     * Handle input and change camera accordingly.
     * @param millis milliseconds, passed since last update
     * @throws LWJGLException 
     */
    public static void handleInput(long millis) throws LWJGLException
    {
        float moveSpeed = 2e-3f*(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 5.0f : 1.0f)*(float)millis;
        float camSpeed = 5e-3f;
        
        while(Keyboard.next())
        {
            if(Keyboard.getEventKeyState())
            {
                switch(Keyboard.getEventKey())
                {
                    case Keyboard.KEY_W: moveDir.z += 1.0f; break;
                    case Keyboard.KEY_S: moveDir.z -= 1.0f; break;
                    case Keyboard.KEY_A: moveDir.x += 1.0f; break;
                    case Keyboard.KEY_D: moveDir.x -= 1.0f; break;
                    case Keyboard.KEY_SPACE: moveDir.y += 1.0f; break;
                    case Keyboard.KEY_C: moveDir.y -= 1.0f; break;
                    case Keyboard.KEY_ESCAPE :  bContinue = false; break;
                    case Keyboard.KEY_R : watermap.compile(); break;
                    case Keyboard.KEY_T : createRainsys(); break;
                    case Keyboard.KEY_UP : watermap.sigma(0.5f); break;
                    case Keyboard.KEY_DOWN : watermap.sigma(-0.5f); break;
                    case Keyboard.KEY_LEFT : watermap.size(2); break;
                    case Keyboard.KEY_RIGHT : watermap.size(-2); break;
                    case Keyboard.KEY_NUMPAD8 : Rainstreaks.setMaxParticles(Rainstreaks.getMaxParticles()*2); break;
                    case Keyboard.KEY_NUMPAD2 : Rainstreaks.setMaxParticles(Rainstreaks.getMaxParticles()/2); break;
                    case Keyboard.KEY_NUMPAD6 : Rainstreaks.setWindForce(Rainstreaks.getWindForce() + 1.0f); break;
                    case Keyboard.KEY_NUMPAD4 : Rainstreaks.setWindForce(Rainstreaks.getWindForce() - 1.0f); break;
                }
            }
            else
            {
                switch(Keyboard.getEventKey())
                {
                    case Keyboard.KEY_W: moveDir.z -= 1.0f; break;
                    case Keyboard.KEY_S: moveDir.z += 1.0f; break;
                    case Keyboard.KEY_A: moveDir.x -= 1.0f; break;
                    case Keyboard.KEY_D: moveDir.x += 1.0f; break;
                    case Keyboard.KEY_SPACE: moveDir.y -= 1.0f; break;
                    case Keyboard.KEY_C: moveDir.y += 1.0f; break;
                    case Keyboard.KEY_F1: setDrawRain(!isDrawRain()); break;
                    case Keyboard.KEY_F2: setDrawTerrain(!isDrawTerrain()); break;
                    case Keyboard.KEY_F3: setDrawSky(!isDrawSky()); break;
                    case Keyboard.KEY_F4: setDrawFog(!isDrawFog()); break;
                    case Keyboard.KEY_F5: setDrawWater(!isDrawWater()); break;
                    case Keyboard.KEY_F6: setPoints(!isPoints()); break;
                    case Keyboard.KEY_UP: break;
                    case Keyboard.KEY_DOWN: break;
                    case Keyboard.KEY_M:
                                        Settings.getInstance();
                                        tc.start();
                                        tc.addTimerListener(Settings.getInstance());
                                        break;
                    case Keyboard.KEY_F9: glPolygonMode(GL_FRONT_AND_BACK, (wireframe ^= true) ? GL_FILL : GL_LINE); break;
                    case Keyboard.KEY_F10: if(culling ^= true) glEnable(GL_CULL_FACE); else glDisable(GL_CULL_FACE); break;
                }
            }
        }
        
        cam.move(moveSpeed * moveDir.z, moveSpeed * moveDir.x, moveSpeed * moveDir.y);
        
        while(Mouse.next())
        {
            if(Mouse.getEventButton() == 0)
            {
                Mouse.setGrabbed(Mouse.getEventButtonState());
            }
            if(Mouse.isGrabbed())
            {
                cam.rotate(-camSpeed*Mouse.getEventDX(), -camSpeed*Mouse.getEventDY());
            }
        }
        Matrix4f.mul(cam.getProjection(), cam.getView(), viewProjMatrix);        
    }
    
    /**
     * Update all moving particles.
     * @param millis milliseconds, passed since last update
     */
    private static void animate(long millis)
    {
        Util.translationX(cam.getCamPos().x, skyMoveMatrix);
        Util.mul(skyMoveMatrix, skyMoveMatrix, Util.translationY(20.f, null));
        Util.mul(skyMoveMatrix, skyMoveMatrix, Util.translationZ(cam.getCamPos().z, null));
        
        // update time properly
        ingameTime += ingameTimePerSecond * 1e-3f * (float)millis;        
        raindrops.updateSimulation(millis);
        watermap.updateSimulation(millis);
    }
    
    //Getters and setters
    
    /**
     * @return true if audio
     */
    public static boolean isAudio()
    {
        return audio;
    }

    /**
     * @param audio
     */
    public static void setAudio(boolean audio)
    {
        Main.audio = audio;
        if (audio)
            sound.init();
        else
            sound.stopSound();
    }

    /**
     * @return the FPS
     */
    public static float getFPS()
    {
        return fps;
    }

    /**
     * @return the fogThickness
     */
    public static Vector3f getFogThickness()
    {
        return fogThickness;
    }

    /**
     * @param fogThickness the fogThickness to set
     */
    public static void setFogThickness(Vector3f fogThickness)
    {
        Main.fogThickness = fogThickness;
    }
    
    /**
	 * @return the drawRain
	 */
	public static boolean isDrawRain() {
		return drawRain;
	}

	/**
	 * @param drawRain the drawRain to set
	 */
	public static void setDrawRain(boolean drawRain) {
		Main.drawRain = drawRain;
	}
	
    /**
	 * @return the drawTerrain
	 */
	public static boolean isDrawTerrain() {
		return drawTerrain;
	}

	/**
	 * @param drawTerrain the drawTerrain to set
	 */
	public static void setDrawTerrain(boolean drawTerrain) {
		Main.drawTerrain = drawTerrain;
	}
	
	/**
	 * @return the drawSky
	 */
	public static boolean isDrawSky()
	{
		return drawSky;
	}

	/**
	 * @param drawSky the drawSky to set
	 */
	public static void setDrawSky(boolean drawSky)
	{
		Main.drawSky = drawSky;
	}

	/**
	 * @return the drawFog
	 */
	public static boolean isDrawFog()
	{
		return drawFog;
	}
	
	/**
	 * @return the drawWater
	 */
	public static boolean isDrawWater()
	{
		return drawWater;
	}

	/**
	 * @param drawWater the drawWater to set
	 */
	public static void setDrawWater(boolean drawWater)
	{
		Main.drawWater = drawWater;
	}

	/**
	 * @param drawFog the drawFog to set
	 */
	public static void setDrawFog(boolean drawFog)
	{
		Main.drawFog = drawFog;
	}

	/**
	 * @return points 
	 */
    public static boolean isPoints() {
		return points;
	}

    /**
     * @param points
     */
	public static void setPoints(boolean points) {
		Main.points = points;
	}
}