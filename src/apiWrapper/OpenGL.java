package apiWrapper;

import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * Wrapper class for OpenGL API.
 * @author Valentin Bruder
 * based on code of Sascha Kolodzey and Nico Marniok (Computergrafik 2012)
 */
public class OpenGL {
    /**
     * Width of OpenGL window.
     */
    public static final int WIDTH = 800;
    
    /**
     * Height of OpenGL window.
     */
    public static final int HEIGHT = 800;
    
    /**
     * Primitive Restart Index
     */
    public static final int RESTART_INDEX = 0xffffffff;
    
    private static boolean initialized = false;
    private static boolean checkForErrors = true;

    private static String version;

    private static String renderer;

    private static String shadinglang;

    private static String driverversion;

    public static void init() throws LWJGLException {
        if(!OpenGL.initialized) {
            Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
            Display.setTitle("RainCL");
            boolean supported = true;
            try {
                Display.create(new PixelFormat(), new ContextAttribs(4, 2).withProfileCore(true));
            } catch (LWJGLException e) {
                Display.create();
                supported = false;
            }
            GL11.glViewport(0, 0, WIDTH, HEIGHT);
            Mouse.create();
            Keyboard.create();  
            
            String vendor = OpenGL.glGetString(OpenGL.GL_VENDOR);
            version = OpenGL.glGetString(OpenGL.GL_VERSION);
            renderer = OpenGL.glGetString(OpenGL.GL_RENDERER);
            shadinglang = OpenGL.glGetString(OpenGL.GL_SHADING_LANGUAGE_VERSION);
            driverversion = Display.getVersion();
            String os = System.getProperty("os.name") + " (" + System.getProperty("os.version") + "), " + System.getProperty("os.arch");
            String java = System.getProperty("java.vm.name") + ", runtime version: " + System.getProperty("java.runtime.version");

            String infoLines[] = {
                renderer + ", " + vendor + ", Driver: " + driverversion,
                "OpenGL " + version + " - Shading Language " + shadinglang,
                "Operating system: " + os,
                "Java: " + java
            };
            String info = OpenGL.pack("OpenGL info", infoLines);
//            System.out.println(info);          
            
            if(!supported) {
                throw new RuntimeException("Your hardware does not support the required OpenGL version 4.2.");
            }
            
            glPrimitiveRestartIndex(RESTART_INDEX);
            glEnable(GL_PRIMITIVE_RESTART);
            OpenGL.initialized = true;
        }
    }
    
    public static void destroy() {
        Keyboard.destroy();
        Mouse.destroy();
        Display.destroy();
    }
    
    /**
     * GL11.GL_BACK
     */
    public static final int GL_BACK = GL11.GL_BACK;
    
    /**
     * GL15.GL_ARRAY_BUFFER
     */  
    public static final int GL_ARRAY_BUFFER = GL15.GL_ARRAY_BUFFER;
    
    /**
     * GL11.GL_BLEND
     */
    public static final int GL_BLEND = GL11.GL_BLEND;
    
    /**
     * GL11.GL_COLOR_BUFFER_BIT
     */
    public static final int GL_COLOR_BUFFER_BIT = GL11.GL_COLOR_BUFFER_BIT;
    
    /**
     * GL30.GL_COLOR_ATTACHMENT0
     */
    public static final int GL_COLOR_ATTACHMENT0 = GL30.GL_COLOR_ATTACHMENT0;
    
    /**
     * GL30.GL_COLOR
     */
    public static final int GL_COLOR = GL11.GL_COLOR;
    
    /**
     * GL11.GL_CULL_FACE
     */
    public static final int GL_CULL_FACE = GL11.GL_CULL_FACE;
    
    /**
     * GL11.GL_CCW
     */
    public static final int GL_CCW = GL11.GL_CCW;
    
    /**
     * GL11.GL_CW
     */
    public static final int GL_CW = GL11.GL_CW;
    
    /**
     * GL11.GL_DEPTH_BUFFER_BIT
     */
    public static final int GL_DEPTH_BUFFER_BIT = GL11.GL_DEPTH_BUFFER_BIT;
    
    /**
     * GL11.GL_DEPTH_TEST
     */
    public static final int GL_DEPTH_TEST = GL11.GL_DEPTH_TEST;
    
    /**
     * GL15.GL_ELEMENT_ARRAY_BUFFER
     */  
    public static final int GL_ELEMENT_ARRAY_BUFFER = GL15.GL_ELEMENT_ARRAY_BUFFER; 
    
    /**
     * GL11.GL_FILL
     */
    public static final int GL_FILL = GL11.GL_FILL;
    
    /**
     * GL11.GL_FLOAT
     */      
    public static final int GL_FLOAT = GL11.GL_FLOAT;
    
    /**
     * GL11.GL_SHORT
     */      
    public static final int GL_SHORT = GL11.GL_SHORT;
    
    /**
     * GL11.GL_UNSIGNED_BYTE
     */      
    public static final int GL_UNSIGNED_BYTE = GL11.GL_UNSIGNED_BYTE;
    
    /**
     * GL11.GL_BYTE
     */      
    public static final int GL_BYTE = GL11.GL_BYTE;
    
    /**
     * GL20.GL_FRAGMENT_SHADER
     */
    public static final int GL_FRAGMENT_SHADER = GL20.GL_FRAGMENT_SHADER;
    
    /**
     * GL32.GL_GEOMETRY_SHADER
     */
    public static final int GL_GEOMETRY_SHADER = GL32.GL_GEOMETRY_SHADER;
    
    /**
     * GL11.GL_BACK
     */
    public static final int GL_FRONT = GL11.GL_FRONT;
    
    /**
     * GL11.GL_FRONT_AND_BACK
     */
    public static final int GL_FRONT_AND_BACK = GL11.GL_FRONT_AND_BACK;
    
    /**
     * GL11.GL_LINE_LOOP
     */
    public static final int GL_LINE_LOOP = GL11.GL_LINE_LOOP;  
    
    /**
     * GL11.GL_LINE
     */
    public static final int GL_LINE = GL11.GL_LINE;
    
    /**
     * GL11.GL_LINEAR
     */
    public static final int GL_LINEAR = GL11.GL_LINEAR;
    
    /**
     * GL11.GL_LINEAR_MIPMAP_LINEAR
     */
    public static final int GL_LINEAR_MIPMAP_LINEAR = GL11.GL_LINEAR_MIPMAP_LINEAR;
    
    /**
     * GL11.GL_LINEAR_MIPMAP_NEAREST
     */
    public static final int GL_LINEAR_MIPMAP_NEAREST = GL11.GL_LINEAR_MIPMAP_NEAREST;
    
    /**
     * GL11.GL_NEAREST
     */
    public static final int GL_NEAREST = GL11.GL_NEAREST;
    
    /**
     * GL11.GL_NEAREST_MIPMAP_NEAREST
     */
    public static final int GL_NEAREST_MIPMAP_NEAREST = GL11.GL_NEAREST_MIPMAP_NEAREST;
    
    /**
     * GL11.GL_NEAREST_MIPMAP_LINEAR
     */
    public static final int GL_NEAREST_MIPMAP_LINEAR = GL11.GL_NEAREST_MIPMAP_LINEAR;
    
    /**
     * GL11.GL_ONE
     */
    public static final int GL_ONE = GL11.GL_ONE;
    
    /**
     * GL11.GL_ONE_MINUS_CONSTANT_ALPHA
     */
    public static final int GL_ONE_MINUS_CONSTANT_ALPHA = GL11.GL_ONE_MINUS_CONSTANT_ALPHA;
    
    /**
     * GL11.GL_ONE_MINUS_CONSTANT_COLOR
     */
    public static final int GL_ONE_MINUS_CONSTANT_COLOR = GL11.GL_ONE_MINUS_CONSTANT_COLOR;
    
    /**
     * GL11.GL_SRC_COLOR
     */
    public static final int GL_SRC_COLOR = GL11.GL_SRC_COLOR;
    
    /**
     * GL11.GL_ONE_MINUS_DST_ALPHA
     */
    public static final int GL_ONE_MINUS_DST_ALPHA = GL11.GL_ONE_MINUS_DST_ALPHA;
    
    /**
     * GL11.GL_ONE_MINUS_DST_COLOR
     */
    public static final int GL_ONE_MINUS_DST_COLOR = GL11.GL_ONE_MINUS_DST_COLOR;
    
    /**
     * GL11.GL_ONE_MINUS_SRC_ALPHA
     */
    public static final int GL_ONE_MINUS_SRC_ALPHA = GL11.GL_ONE_MINUS_SRC_ALPHA;
    
    /**
     * GL11.GL_ONE_MINUS_SRC_COLOR
     */
    public static final int GL_ONE_MINUS_SRC_COLOR = GL11.GL_ONE_MINUS_SRC_COLOR;
    
    /**
     * GL11.GL_DST_COLOR
     */
    public static final int GL_DST_COLOR = GL11.GL_DST_COLOR;
    
    /**
     * GL11.GL_SRC_ALPHA
     */
    public static final int GL_SRC_ALPHA = GL11.GL_SRC_ALPHA;
    
    /**
     * GL11.GL_POINTS
     */
    public static final int GL_POINTS = GL11.GL_POINTS; 
    
    /**
     * GL31.GL_PRIMITIVE_RESTART
     */
    public static final int GL_PRIMITIVE_RESTART = GL31.GL_PRIMITIVE_RESTART;     
    
    /**
     * GL11.GL_RENDERER
     */
    public static final int GL_RENDERER = GL11.GL_RENDERER;    
    
    /**
     * GL11.GL_R
     */
    public static final int GL_R = GL11.GL_R;  
    
    /**
     * GL30.GL_RENDERBUFFER
     */
    public static final int GL_RENDERBUFFER = GL30.GL_RENDERBUFFER;  
    
    /**
     * GL30.GL_FRAMEBUFFER
     */
    public static final int GL_FRAMEBUFFER = GL30.GL_FRAMEBUFFER;
    
    /**
     * GL30.GL_DEPTH_ATTACHMENT
     */
    public static final int GL_DEPTH_ATTACHMENT = GL30.GL_DEPTH_ATTACHMENT;
    
    /**
     * GL30.GL_DEPTH_COMPONENT32F
     */
    public static final int GL_DEPTH_COMPONENT32F = GL30.GL_DEPTH_COMPONENT32F;
    
    /**
     * GL30.GL_R8
     */
    public static final int GL_R8 = GL30.GL_R8;    
    
    /**
     * GL11.GL_RED
     */
    public static final int GL_RED = GL11.GL_RED;  
    
    /**
     * GL30.GL_R16F
     */
    public static final int GL_R16F = GL30.GL_R16F;  
    
    /**
     * GL30.GL_R16
     */
    public static final int GL_R16 = GL30.GL_R16;  
    
    /**
     * GL30.GL_R32F
     */
    public static final int GL_R32F = GL30.GL_R32F;  
    
    /**
     * GL30.GL_RG
     */
    public static final int GL_RG = GL30.GL_RG;   
    
    /**
     * GL30.GL_RG
     */
    public static final int GL_RG8 = GL30.GL_RG8;  
    
    /**
     * GL11.GL_RGB
     */
    public static final int GL_RGB = GL11.GL_RGB;  
    
    /**
     * GL11.GL_RGB
     */
    public static final int GL_RGB8 = GL11.GL_RGB8;
    
    /**
     * GL11.GL_RGBA
     */
    public static final int GL_RGBA = GL11.GL_RGBA;
    
    /**
     * GL11.GL_RGBA8
     */
    public static final int GL_RGBA8 = GL11.GL_RGBA8;
    
    /**
     * GL30.GL_RGBA16F
     */
    public static final int GL_RGBA16F = GL30.GL_RGBA16F;
    
    /**
     * GL20.GL_SHADING_LANGUAGE_VERSION
     */
    public static final int GL_SHADING_LANGUAGE_VERSION = GL20.GL_SHADING_LANGUAGE_VERSION;    
    
    /**
     * GL15.GL_STATIC_DRAW
     */      
    public static final int GL_STATIC_DRAW = GL15.GL_STATIC_DRAW;    
    
    /**
     * GL15.GL_STATIC_DRAW
     */      
    public static final int GL_DYNAMIC_DRAW = GL15.GL_DYNAMIC_DRAW;    
    
    /**
     * GL11.GL_STENCIL_BUFFER_BIT
     */
    public static final int GL_STENCIL_BUFFER_BIT = GL11.GL_STENCIL_BUFFER_BIT;
    
    /**
     * GL11.GL_TEXTURE_1D;
     */
    public static final int GL_TEXTURE_1D = GL11.GL_TEXTURE_1D;
    
    /**
     * GL11.GL_TEXTURE_2D;
     */
    public static final int GL_TEXTURE_2D = GL11.GL_TEXTURE_2D;
    
    /**
     * GL12.GL_TEXTURE_3D;
     */
    public static final int GL_TEXTURE_3D = GL12.GL_TEXTURE_3D;
    
    /**
     * GL30.GL_TEXTURE_2D_ARRAY;
     */
    public static final int GL_TEXTURE_2D_ARRAY = GL30.GL_TEXTURE_2D_ARRAY;    
    
    /**
     * GL11.GL_TEXTURE_MIN_FILTER
     */
    public static final int GL_TEXTURE_MIN_FILTER = GL11.GL_TEXTURE_MIN_FILTER;
    
    /**
     * GL11.GL_TEXTURE_MAG_FILTER
     */
    public static final int GL_TEXTURE_MAG_FILTER = GL11.GL_TEXTURE_MAG_FILTER;
    
    /**
     * GL12.GL_TEXTURE_WRAP_R
     */
    public static final int GL_TEXTURE_WRAP_R = GL12.GL_TEXTURE_WRAP_R;
    
    /**
     * GL11.GL_TEXTURE_WRAP_S
     */
    public static final int GL_TEXTURE_WRAP_S = GL11.GL_TEXTURE_WRAP_S;
    
    /**
     * GL11.GL_TEXTURE_WRAP_T
     */
    public static final int GL_TEXTURE_WRAP_T = GL11.GL_TEXTURE_WRAP_T;
    
    /**
     * GL11.GL_CLAMP
     */
    public static final int GL_CLAMP = GL11.GL_CLAMP;
    
    /**
     * GL11.GL_CLAMP_TO_EDGE
     */
    public static final int GL_CLAMP_TO_EDGE = GL12.GL_CLAMP_TO_EDGE;
    
    /**
     * GL11.GL_REPEAT
     */
    public static final int GL_REPEAT = GL11.GL_REPEAT;
    
    /**
     * GL11.GL_CLAMP_TO_BORDER
     */
    public static final int GL_CLAMP_TO_BORDER = GL13.GL_CLAMP_TO_BORDER;
    
    /**
     * GL13.GL_TEXTURE0
     */
    public static final int GL_TEXTURE0 = GL13.GL_TEXTURE0; 
    
    /**
     * GL11.GL_TRIANGLE_STRIP
     */
    public static final int GL_TRIANGLE_STRIP = GL11.GL_TRIANGLE_STRIP;
    
    /**
     * GL11.GL_TRIANGLES
     */    
    public static final int GL_TRIANGLES = GL11.GL_TRIANGLES;
    
    /**
     * GL11.GL_UNSIGNED_INT
     */
    public static final int GL_UNSIGNED_INT = GL11.GL_UNSIGNED_INT;
    
    /**
     * GL11.GL_VENDOR
     */
    public static final int GL_VENDOR = GL11.GL_VENDOR;
    
    /**
     * GL11.GL_VERSION
     */
    public static final int GL_VERSION = GL11.GL_VERSION;    
    
    /**
     * GL20.GL_VERTEX_SHADER
     */        
    public static final int GL_VERTEX_SHADER = GL20.GL_VERTEX_SHADER;  
    
    /**
     * GL11.GL_ZERO
     */
    public static final int GL_ZERO = GL11.GL_ZERO;
    
    /**
     * GL40.GL_TRANSFORM_FEEDBACK
     */
    public static final int GL_TRANSFORM_FEEDBACK = GL40.GL_TRANSFORM_FEEDBACK;
    
    /**
     * GL30.GL_TRANSFORM_FEEDBACK_BUFFER
     */
    public static final int GL_TRANSFORM_FEEDBACK_BUFFER = GL30.GL_TRANSFORM_FEEDBACK_BUFFER;
    
    /**
     * GL30.GL_RASTERIZER_DISCARD
     */
    public static final int GL_RASTERIZER_DISCARD = GL30.GL_RASTERIZER_DISCARD;

    /**
     * GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS
     */
    public static final int GL_DEBUG_OUTPUT_SYNCHRONOUS = GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS; 
    
    /**
     * GL30.GL_INTERLEAVED_ATTRIBS
     */
    public static final int GL_INTERLEAVED_ATTRIBS = GL30.GL_INTERLEAVED_ATTRIBS;
    
    /**
     * GL30.GL_FRAMEBUFFER_COMPLETE
     */
    public static final int GL_FRAMEBUFFER_COMPLETE = GL30.GL_FRAMEBUFFER_COMPLETE;
    
    /**
     * GL30.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT
     */
    public static final int GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT = GL30.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT;
    
    /**
     * GL30.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER
     */
    public static final int GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER = GL30.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER;
    
    /**
     * GL30.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT
     */
    public static final int GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT = GL30.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT;
    
    /**
     * GL30.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE
     */
    public static final int GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE = GL30.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE;
    
    /**
     * GL30.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER
     */
    public static final int GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER = GL30.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER;
     
    /**
     * OpenGL 1.3
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glActiveTexture.xml">glActiveTexture</a>
     * @param texture 
     */
    public static void glActiveTexture(int texture) {
        GL13.glActiveTexture(texture);
        OpenGL.checkError("glActiveTexture");
    }
    
    /**
     * OpenGL 2.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glAttachShader.xml">glAttachShader</a>
     * @param program
     * @param shader 
     */
    public static void glAttachShader(int program, int shader) {
        GL20.glAttachShader(program, shader);
        OpenGL.checkError("glAttachShader");
    }
    
    /**
     * OpenGL 2.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glBindAttribLocation.xml">glBindAttribLocation</a>
     * @param program
     * @param index
     * @param name 
     */
    public static void glBindAttribLocation(int program, int index, String name) {
        GL20.glBindAttribLocation(program, index, name);
        OpenGL.checkError("glBindAttribLocation");
    }
    
    /**
     * OpenGL 1.5
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glBindBuffer.xml">glBindBuffer</a>
     * @param target
     * @param buffer 
     */
    public static void glBindBuffer(int target, int buffer) {
        GL15.glBindBuffer(target, buffer);
        OpenGL.checkError("glBindBuffer");
    }
    
    /**
     * OpenGL 1.1
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glBindTexture.xml">glBindTexture</a>
     * @param target
     * @param texture 
     */
    public static void glBindTexture(int target, int texture) {
        GL11.glBindTexture(target, texture);
        OpenGL.checkError("glBindTexture");
    }
    
    /**
     * OpenGL 3.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glBindVertexArray.xml">glBindVertexArray</a>
     * @param array 
     */
    public static void glBindVertexArray(int array) {
        GL30.glBindVertexArray(array);
        OpenGL.checkError("glBindVertexArray");
    }
    
    /**
     * OpenGL 1.1
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glBlendFunc.xml">glBlendFunc</a>
     * @param sfactor
     * @param dfactor 
     */
    public static void glBlendFunc(int sfactor, int dfactor) {
        GL11.glBlendFunc(sfactor, dfactor);
        OpenGL.checkError("glBlendFunc");
    }
    
    /**
     * OpenGL 1.5
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glBufferData.xml">glBufferData</a>
     * @param target
     * @param data
     * @param usage 
     */
    public static void glBufferData(int target, FloatBuffer data, int usage) {
        GL15.glBufferData(target, data, usage);
        OpenGL.checkError("glBufferData");
    }
    
    /**
     * OpenGL 1.5
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glBufferData.xml">glBufferData</a>
     * @param target
     * @param data
     * @param usage 
     */
    public static void glBufferData(int target, IntBuffer data, int usage) {
        GL15.glBufferData(target, data, usage);
        OpenGL.checkError("glBufferData");
    }

    /**
     * OpenGL 1.1
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glClear.xml">glClear</a>
     * @param mask 
     */
    public static void glClear(int mask) {
        GL11.glClear(mask);
        OpenGL.checkError("glClear");
    }
    
    /**
     * OpenGL 1.1
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glClear.xml">glFinish</a>
     */
    public static void glFinish() {
        GL11.glFinish();
        OpenGL.checkError("glFinish");
    }
    
    /**
     * OpenGL 1.1
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glClearColor.xml">glClearColor</a>
     * @param red
     * @param green
     * @param blue
     * @param alpha 
     */
    public static void glClearColor(float red, float green, float blue, float alpha) {
        GL11.glClearColor(red, green, blue, alpha);
        OpenGL.checkError("glClearColor");
    }
    
    /**
     * OpenGL 2.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glCompileShader.xml">glCompileShader</a>
     * @param shader 
     */
    public static void glCompileShader(int shader) {
        GL20.glCompileShader(shader);
        OpenGL.checkError("glCompileShader");        
    }
    
    /**
     * OpenGL 2.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glCreateProgram.xml">glCreateProgram</a>
     * @return 
     */
    public static int glCreateProgram() {
        int id = GL20.glCreateProgram();
        OpenGL.checkError("glCreateProgram");
        return id;
    }
    
    /**
     * OpenGL 2.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glCreateShader.xml">glCreateShader</a>
     * @param type
     * @return 
     */
    public static int glCreateShader(int type) {
        int id = GL20.glCreateShader(type);
        OpenGL.checkError("glCreateShader");
        return id;
    }
    
    /**
     * OpenGL 1.1
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glCullFace.xml">glCullFace</a>
     * @param mode 
     */
    public static void glCullFace(int mode) {
        GL11.glCullFace(mode);
        OpenGL.checkError("glCullFace");
    }    
    
    /**
     * OpenGL 1.5
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glDeleteBuffers.xml">glDeleteBuffers</a>
     * @param buffer 
     */
    public static void glDeleteBuffers(int buffer) {
        GL15.glDeleteBuffers(buffer);
        OpenGL.checkError("glDeleteBuffers");
    }  
    
    /**
     * OpenGL 3.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glDeleteVertexArrays.xml">glDeleteVertexArrays</a>
     * @param array 
     */
    public static void glDeleteVertexArrays(int array) {
        GL30.glDeleteVertexArrays(array);
        OpenGL.checkError("glDeleteVertexArrays");
    }

    /**
     * OpenGL 1.1
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glDisable.xml">glDisable</a>
     * @param cap 
     */
    public static void glDisable(int cap) {
        GL11.glDisable(cap);
        OpenGL.checkError("glDisable");
    }
    
    /**
     * OpenGL 1.1
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glDrawArrays.xml">glDrawArrays</a>
     * @param mode
     * @param first
     * @param count 
     */
    public static void glDrawArrays(int mode, int first, int count) {
        GL11.glDrawArrays(mode, first, count);
        OpenGL.checkError("glDrawArrays");
    }
    
    /**
     * OpenGL 1.1
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glDrawElements.xml">glDrawElements</a>
     * @param mode
     * @param indices_count
     * @param type
     * @param indices_buffer_offset 
     */
    public static void glDrawElements(int mode, int indices_count, int type, int indices_buffer_offset) {
        GL11.glDrawElements(mode, indices_count, type, indices_buffer_offset);
        OpenGL.checkError("glDrawElements");
    }

    /**
     * OpenGL 1.1
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glEnable.xml">glEnable</a>
     * @param cap 
     */
    public static void glEnable(int cap) {
        GL11.glEnable(cap);
        OpenGL.checkError("glEnable");
    }
    
    /**
     * OpenGL 2.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glEnableVertexAttribArray.xml">glEnableVertexAttribArray</a>
     * @param index 
     */
    public static void glEnableVertexAttribArray(int index) {
        GL20.glEnableVertexAttribArray(index);
        OpenGL.checkError("glEnableVertexAttribArray");
    }
    
    /**
     * OpenGL 1.1
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glFrontFace.xml">glFrontFace</a>
     * @param mode 
     */
    public static void glFrontFace(int mode) {
        GL11.glFrontFace(mode);
        OpenGL.checkError("glFrontFace");
    }            
    
    /**
     * OpenGL 1.5
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glGenBuffers.xml">glGenBuffers</a>
     * @return 
     */
    public static int glGenBuffers() {
        int id = GL15.glGenBuffers();
        OpenGL.checkError("glGenBuffers");
        return id;
    }
    
    /**
     * OpenGL 1.5
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glGenBuffers.xml">glGenBuffers</a>
     * @param IntBuffer 
     */
    public static void glGenBuffers(IntBuffer ib) {
        GL15.glGenBuffers(ib);
        OpenGL.checkError("glGenBuffers");
    }
    
    /**
     * OpenGL 3.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glGenerateMipmap.xml">glGenerateMipmap</a>
     * @param target 
     */
    public static void glGenerateMipmap(int target) {
        GL30.glGenerateMipmap(target);
        OpenGL.checkError("glGenerateMipmap");
    }
    
    /**
     * OpenGL 1.1
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glGenTextures.xml">glGenTextures</a>
     * @return 
     */
    public static int glGenTextures() {
        int texture = GL11.glGenTextures();
        OpenGL.checkError("glGenTextures");
        return texture;
    }
    
    /**
     * OpenGL 3.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glGenVertexArrays.xml">glGenVertexArrays</a>
     * @return 
     */
    public static int glGenVertexArrays() {
        int id = GL30.glGenVertexArrays();
        OpenGL.checkError("glGenVertexArrays");
        return id;
    }
    
    /**
     * OpenGL 1.1
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glGetError.xml">glGetError</a>
     * @return 
     */
    public static int glGetError() {
        return GL11.glGetError();
    }
    
    /**
     * OpenGL 2.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glGetProgramInfoLog.xml">glGetProgramInfoLog</a>
     * @param program
     * @param maxLength
     * @return 
     */
    public static String glGetProgramInfoLog(int program, int maxLength) {
        String log = GL20.glGetProgramInfoLog(program, maxLength);
        OpenGL.checkError("glGetProgramInfoLog");
        return log;
    }
    
    /**
     * OpenGL 2.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glGetShaderInfoLog.xml">glGetShaderInfoLog</a>
     * @param shader
     * @param maxLength
     * @return 
     */
    public static String glGetShaderInfoLog(int shader, int maxLength) {
        String log = GL20.glGetShaderInfoLog(shader, maxLength);
        OpenGL.checkError("glGetShaderInfoLog");
        return log;
    }
    
    /**
     * OpenGL 1.1
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glGetString.xml">glGetString</a>
     * @param name
     * @return 
     */
    public static String glGetString(int name) {
        String string = GL11.glGetString(name);
        OpenGL.checkError("glGetString");
        return string;
    }
    
    /**
     * OpenGL 2.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glDrawBuffers.xml">glDrawBuffers</a>
     * @param buffer
     */
    public static void glDrawBuffers(java.nio.IntBuffer buffers) {
    	GL20.glDrawBuffers(buffers);
    	checkError("glDrawBuffers");
    }
    
    /**
     * OpenGL 3.0
     * @param buffer
     * @param drawbuffer
     * @param value
     */
    public static void glClearBuffer(int buffer, int drawbuffer, FloatBuffer value) {
    	GL30.glClearBuffer(buffer, drawbuffer, value);
    	checkError("glClearBuffer");
    }
    
    /**
     * OpenGL 2.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glGetUniformLocation.xml">glGetUniformLocation</a>
     * @param program
     * @param name
     * @return 
     */
    public static int glGetUniformLocation(int program, String name) {
        int location = GL20.glGetUniformLocation(program, name);
        OpenGL.checkError("glGetUniformLocation");
        if(location == -1) {
          //  System.err.println("WARNUNG: Uniform location von " + name + " ist -1! (Diese Meldung ist ein Service Ihres CG-Teams ;)");
        }
        return location;
    }
    
    /**
     * OpenGL 1.1
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glLineWidth.xml">glLineWidth</a>
     * @param width 
     */
    public static void glLineWidth(float width) {
        GL11.glLineWidth(width);
        OpenGL.checkError("glLineWidth");
    }
    
    /**
     * OpenGL 2.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glLinkProgram.xml">glLinkProgram</a>
     * @param program 
     */
    public static void glLinkProgram(int program) {
        GL20.glLinkProgram(program);
        OpenGL.checkError("glLinkProgram");
    }
    
    /**
     * OpenGL 1.1
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glPointSize.xml">glPointSize</a>
     * @param size 
     */
    public static void glPointSize(float size) {
        GL11.glPointSize(size);
        OpenGL.checkError("glPointSize");
    }
    
    /**
     * OpenGL 1.1
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glPolygonMode.xml">glPolygonMode</a>
     * @param face
     * @param mode 
     */
    public static void glPolygonMode(int face, int mode) {
        GL11.glPolygonMode(face, mode);
        OpenGL.checkError("glPolygonMode");
    }
    
    /**
     * OpenGL 3.1
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glPrimitiveRestartIndex.xml">glPrimitiveRestartIndex</a>
     * @param index 
     */
    public static void glPrimitiveRestartIndex(int index) {
        GL31.glPrimitiveRestartIndex(index);
        OpenGL.checkError("glPrimitiveRestartIndex");
    }
    
    /**
     * OpenGL 2.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glShaderSource.xml">glShaderSource</a>
     * @param shader
     * @param string 
     */
    public static void glShaderSource(int shader, String string) {
        GL20.glShaderSource(shader, string);
        OpenGL.checkError("glShaderSource");
    }
    
    /**
     * OpenGL 1.1
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glTexImage2D.xml">glTexImage2D</a>
     * @param target
     * @param level
     * @param internalFormat
     * @param width
     * @param height
     * @param border
     * @param format
     * @param type
     * @param pixels 
     */
    public static void glTexImage2D(int target, int level, int internalFormat, int width, int height, int border, int format, int type, FloatBuffer pixels) {
        GL11.glTexImage2D(target, level, internalFormat, width, height, border, format, type, pixels);
        OpenGL.checkError("glTexImage2D");
    }
    
    /**
     * OpenGL 1.1
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glTexImage1D.xml">glTexImage1D</a>
     * @param target
     * @param level
     * @param internalFormat
     * @param width
     * @param border
     * @param format
     * @param type
     * @param pixels
     */
    public static void glTexImage1D(int target, int level, int internalFormat, int width, int border, int format, int type, FloatBuffer pixels) {
        GL11.glTexImage1D(target, level, internalFormat, width, border, format, type, pixels);
        OpenGL.checkError("glTexImage2D");
    }
    
    /**
     * OpenGL 1.2
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glTexImage2D.xml">glTexImage3D</a>
     * @param target
     * @param level
     * @param internalFormat
     * @param width
     * @param height
     * @param depth
     * @param border
     * @param format
     * @param type
     * @param pixels 
     */
    public static void glTexImage3D(int target, int level, int internalFormat, int width, int height, int depth, int border, int format, int type, FloatBuffer pixels) {
        GL12.glTexImage3D(target, level, internalFormat, width, height, depth, border, format, type, pixels);
        OpenGL.checkError("glTexImage2D");
    }
    
    /**
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glTexImage2D.xml">glTexSubImage3D</a>
     * @param target
     * @param level
     * @param xoffset
     * @param yoffset
     * @param zoffset
     * @param width
     * @param height
     * @param depth
     * @param format
     * @param type
     * @param data
     */
    public static void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format,int type, FloatBuffer data) {
        GL12.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, data);
        OpenGL.checkError("glTexSubImage3D");
    }
    
    /**
     * OpenGL 1.1
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glTexParameter.xml">glTexParameter</a>
     * @param target
     * @param pname
     * @param param 
     */
    public static void glTexParameteri(int target, int pname, int param) {
        GL11.glTexParameteri(target, pname, param);
        OpenGL.checkError("glTexParameteri");
    }
    
    /**
     * OpenGL 1.1
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glTexParameterf.xml">glTexParameterf</a>
     * @param target
     * @param pname
     * @param param 
     */
    public static void glTexParameterf(int target, int pname, float param) {
    	GL11.glTexParameterf(target, pname, param);
    	checkError("glTexParameterf");
    }
    
    /**
     * GL 1.1
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glViewport.xml">glViewport</a>
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public static void glViewport(int x, int y, int width, int height) {
    	GL11.glViewport(x, y, width, height);
    	checkError("glViewport");
    }
    
    /**
     * OpenGL 2.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glUniform.xml">glUniform</a>
     * @param location
     * @param v0
     */
    public static void glUniform1f(int location, float v0) {
        GL20.glUniform1f(location, v0);
        OpenGL.checkError("glUniform");
    }
    
    /**
     * OpenGL 2.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glUniform.xml">glUniform</a>
     * @param location
     * @param v0 
     */
    public static void glUniform1i(int location, int v0) {
        GL20.glUniform1i(location, v0);
        OpenGL.checkError("glUniform");
    }
    
    /**
     * OpenGL 2.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glUniform.xml">glUniform</a>
     * @param location
     * @param v0
     * @param v1
     * @param v2 
     */
    public static void glUniform3f(int location, float v0, float v1, float v2) {
        GL20.glUniform3f(location, v0, v1, v2);
        OpenGL.checkError("glUniform");
    }
    
    /**
     * OpenGl 2.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glUniform.xml">glUniform</a>
     * @param location
     * @param v0
     * @param v1
     * @param v2
     * @param v3
     */
    public static void glUniform4f(int location, float v0, float v1, float v2, float v3) {
        GL20.glUniform4f(location, v0, v1, v2, v3);
        OpenGL.checkError("glUniform");
    }
    
    /**
     * OpenGL 2.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glUniform.xml">glUniform</a>
     * @param location
     * @param transpose
     * @param matrices 
     */
    public static void glUniformMatrix4(int location, boolean transpose, FloatBuffer matrices) {
        GL20.glUniformMatrix4(location, transpose, matrices);
        OpenGL.checkError("glUniform");
    }
    
    /**
     * OpenGL 2.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glUseProgram.xml">glUseProgram</a>
     * @param program 
     */
    public static void glUseProgram(int program) {
        GL20.glUseProgram(program);
        OpenGL.checkError("glUseProgram");
    }
    
    /**
     * OpenGL 2.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glVertexAttribPointer.xml">glVertexAttribPointer</a>
     * @param index
     * @param size
     * @param type
     * @param normalized
     * @param stride
     * @param buffer_buffer_offset 
     */
    public static void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long buffer_buffer_offset) {
        GL20.glVertexAttribPointer(index, size, type, normalized, stride, buffer_buffer_offset);
        OpenGL.checkError("glVertexAttribPointer");
    }

    /**
     * OpenGL 3.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glBeginTransformFeedback.xml">glBeginTransformFeedback</a>
     * @param primitiveMode - must be "GL_POINTS", "GL_LINES" or "GL_TRIANGLES"
     */
    public static void glBeginTransformFeedback(int primitiveMode){
    	GL30.glBeginTransformFeedback(primitiveMode);
    	OpenGL.checkError("glBeginTransformFeedback");
    }   
    
    /**
     * OpenGL 4.0
     * @return 
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glGenTransformFeedbacks.xml">glGenTransformFeedbacks</a>
     */
    public static int glGenTransformFeedbacks(){
    	int value = GL40.glGenTransformFeedbacks();
    	OpenGL.checkError("glGenTransformFeedbacks");
    	return value;
    }
    
    /**
     * OpenGL 4.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glGenTransformFeedbacks.xml">glGenTransformFeedbacks</a>
     */
    public static void glGenTransformFeedbacks(IntBuffer ib){
        GL40.glGenTransformFeedbacks(ib);
        OpenGL.checkError("glGenTransformFeedbacks");
    }

    /**
     * OpenGL 3.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glEndTransformFeedback.xml">glEndTransformFeedback</a>
     */
    public static void glEndTransformFeedback(){
    	GL30.glEndTransformFeedback();
    	OpenGL.checkError("glEndTransformFeedback");
    }
    
    /**
     * OpenGL 4.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glDrawTransformFeedback.xml">glDrawTransformFeedback</a>
     * @param mode
     * @param id
     */
    public static void glDrawTransformFeedback(int mode, int id){
    	GL40.glDrawTransformFeedback(mode, id);
    	OpenGL.checkError("glDrawTransformFeedback");
    }   
    
    /**
     * OpenGL 4.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glBindTransformFeedback.xml">glBindTransformFeedback</a>
     * @param target
     * @param id
     */
    public static void glBindTransformFeedback(int target, int id){
    	GL40.glBindTransformFeedback(target, id);
    	OpenGL.checkError("glBindTransformFeedback");
    }
    
    /**
     * OpenGL 3.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glTransformFeedbackVaryings.xml">glTransformFeedbackVaryings</a>
     * @param program
     * @param count
     * @param varyings
     * @param bufferMode
     */
    public static void glTransformFeedbackVaryings(int program, int count, java.nio.ByteBuffer varyings, int bufferMode) {
        GL30.glTransformFeedbackVaryings(program, count, varyings, bufferMode);
        OpenGL.checkError("glTransformFeedbackVaryings");
    }
    
    /**
     * OpenGL 3.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glTransformFeedbackVaryings.xml">glTransformFeedbackVaryings</a>
     * @param program
     * @param varyings
     * @param bufferMode
     */
    public static void glTransformFeedbackVaryings(int program, java.lang.CharSequence[] varyings, int bufferMode) {
        GL30.glTransformFeedbackVaryings(program, varyings, bufferMode);
        OpenGL.checkError("glTransformFeedbackVaryings");
    }
    
    /**
     * OpenGL 3.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glBindBufferBase.xml">glBindBufferBase</a>
     * @param target
     * @param index
     * @param buffer
     */
    public static void glBindBufferBase(int target, int index, int buffer){
        GL30.glBindBufferBase(target, index, buffer);
        OpenGL.checkError("glBindBufferBase");
    }
    
    /**
     * OpenGL 3.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glDeleteRenderbuffers.xml">glDeleteRenderbuffers</a>
     * @param target
     */
    public static void glDeleteRenderbuffers(int target){
        GL30.glDeleteRenderbuffers(target);
        OpenGL.checkError("glDeleteRenderbuffers");
    }
    
    /**
     * OpenGL 3.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glGenRenderbuffers.xml">glDeleteRenderbuffers</a>
     * @return id of render buffer object
     */
    public static int glGenRenderbuffers(){
        int id = GL30.glGenRenderbuffers();
        OpenGL.checkError("glGenRenderbuffers");
        return id;
    }
    
    /**
     * OpenGL 3.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glBindRenderbuffer.xml">glDeleteRenderbuffers</a>
     * @param renderbuffer
     * @param target
     */
    public static void glBindRenderbuffer(int renderbuffer, int target){
        GL30.glBindRenderbuffer(renderbuffer, target);
        OpenGL.checkError("glBindRenderbuffer");
    }
    
    /**
     * OpenGL 3.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glRenderbufferStorage.xml">glDeleteRenderbuffers</a>
     * @param renderbuffer
     * @param depth
     * @param width
     * @param height
     */
    public static void glRenderbufferStorage(int renderbuffer, int depth, int width, int height){
        GL30.glRenderbufferStorage(renderbuffer, depth, width, height);
        OpenGL.checkError("glRenderbufferStorage");
    }
    
    
    /**
     * OpenGL 3.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glGenFramebuffers.xml">glDeleteRenderbuffers</a>
     * @return id of frame buffer object
     */
    public static int glGenFramebuffers(){
        int id = GL30.glGenFramebuffers();
        OpenGL.checkError("glDeleteRenderbuffers");
		return id;
    }
    
    /**
     * OpenGL 3.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glBindFramebuffer.xml">glDeleteRenderbuffers</a>
     * @param framebuffer
     * @param target
     */
    public static void glBindFramebuffer(int framebuffer, int target){
        GL30.glBindFramebuffer(framebuffer, target);
        OpenGL.checkError("glBindFramebuffer");
    }
    
    /**
     * OpenGL 3.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glFramebufferTexture2D.xml">glFramebufferTexture2D</a>
     * @param target
     * @param attachment
     * @param textarget
     * @param texture
     * @param level
     */
    public static void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
    	GL30.glFramebufferTexture2D(target, attachment, textarget, texture, level);
    	checkError("glFramebufferTexture2D");
    }
    
    /**
     * OpenGL 3.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glFramebufferTexture2D.xml">glCheckFramebufferStatus</a>
     * @param target
     * @return error code
     */
    public static int glCheckFramebufferStatus(int target) {
    	int error = GL30.glCheckFramebufferStatus(target);
    	checkError("glCheckFramebufferStatus");
    	return error;
    }
    
    /**
     * OpenGL 3.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glFramebufferRenderbuffer.xml">glDeleteRenderbuffers</a>
     */
    public static void glFramebufferRenderbuffer(int framebuffer, int depthAttachment, int renderbuffer, int renderBufferObjectId){
    	GL30.glFramebufferRenderbuffer(framebuffer, depthAttachment, renderbuffer, renderBufferObjectId);
    	OpenGL.checkError("glFramebufferRenderbuffer");
    }
    
    /**
     * OpenGL 3.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glDeleteFramebuffers.xml">glDeleteFramebuffers</a>
     * @param target
     */
    public static void glDeleteFramebuffers(int target){
        GL30.glDeleteFramebuffers(target);
        OpenGL.checkError("glDeleteFramebuffers");
    }
    
    /**
     * OpenGL 3.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glBindFragDataLocation.xml">glBindFragDataLocation</a>
     * @param program
     * @param colorNumber
     * @param name
     */
    public static void glBindFragDataLocation(int program, int colorNumber, CharSequence name) {
    	GL30.glBindFragDataLocation(program, colorNumber, name);
    	checkError("glBindFragDataLocation");
    }
    
    /**
     * OpenGL 2.0
     * @see <a href="http://www.opengl.org/sdk/docs/man4/xhtml/glDisableVertexAttribArray.xml">glDisableVertexAttribArray</a>
     * @param index
     */
    public static void glDisableVertexAttribArray(int index){
    	GL20.glDisableVertexAttribArray(index);
    	OpenGL.checkError("glDisableVertexAttribArray");
    }
    
    public static void checkError(String source) {
        if(checkForErrors) {
            int errorcode = GL11.glGetError();
            String errorstring = null;
            switch(errorcode) {
                case GL11.GL_NO_ERROR: return;
                case GL11.GL_INVALID_ENUM: errorstring = "GL_INVALID_ENUM"; break;
                case GL11.GL_INVALID_OPERATION: errorstring = "GL_INVALID_OPERATION"; break;
                case GL11.GL_INVALID_VALUE: errorstring = "GL_INVALID_VALUE"; break;
                case GL30.GL_INVALID_FRAMEBUFFER_OPERATION: errorstring = "GL_INVALID_FRAMEBUFFER_OPERATION"; break;
                case GL11.GL_OUT_OF_MEMORY: errorstring = "GL_OUT_OF_MEMORY"; break;
            }
            throw new RuntimeException(source + ": " + errorstring);
        }
    }
    
    /**
     * Packs some lines into a box.
     * @param head The heading of the box
     * @param lines The lines in the box
     * @return A nicely formated string ;)
     */
    private static String pack(String head, String lines[]) {
        int maxLength = head.length() + 6;
        for(String line : lines) {
            maxLength = Math.max(maxLength, line.length());
        }
        String info = "";
        String footer = "+-";
        String header = "+- " + head + ' ';
        for(int i=0; i < maxLength; i++) {
            footer += '-';
            if(i > head.length() + 1) {
                header += '-';
            }
        }
        footer += "-+";
        header += "-+\n";
        for(String line : lines) {
            info += String.format("| %-" + maxLength + "s |\n", line);
        }
        return header + info + footer;
    }

    /**
     * @return the version
     */
    public static String getVersion()
    {
        return version;
    }

    /**
     * @return the renderer
     */
    public static String getRenderer()
    {
        return renderer;
    }

    /**
     * @return the shading language
     */
    public static String getShadinglang()
    {
        return shadinglang;
    }

    /**
     * @return the driver version
     */
    public static String getDriverversion()
    {
        return driverversion;
    }
}