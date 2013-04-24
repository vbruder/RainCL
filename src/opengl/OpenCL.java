package opengl;

import static org.lwjgl.opencl.CL10.CL_DEVICE_TYPE_CPU;
import static org.lwjgl.opencl.CL10.CL_DEVICE_TYPE_GPU;
import static org.lwjgl.opencl.CL10.CL_PLATFORM_NAME;
import static org.lwjgl.opencl.CL10.CL_PLATFORM_PROFILE;
import static org.lwjgl.opencl.CL10.CL_PLATFORM_VENDOR;
import static org.lwjgl.opencl.CL10.CL_PLATFORM_VERSION;
import static org.lwjgl.opencl.CL10.CL_QUEUE_PROFILING_ENABLE;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL10GL;
import org.lwjgl.opencl.CLBuildProgramCallback;
import org.lwjgl.opencl.CLCommandQueue;
import org.lwjgl.opencl.CLContext;
import org.lwjgl.opencl.CLContextCallback;
import org.lwjgl.opencl.CLDevice;
import org.lwjgl.opencl.CLKernel;
import org.lwjgl.opencl.CLMem;
import org.lwjgl.opencl.CLPlatform;
import org.lwjgl.opencl.CLProgram;
import org.lwjgl.opengl.Drawable;

/**
 * @author Sascha Kolodzey, Nico Marniok
 */
public class OpenCL {
    
    private static boolean initialized = false;
    private static final IntBuffer lastErrorCode = BufferUtils.createIntBuffer(1);
    public static boolean checkError = true;
    
    public static final int CL_MEM_WRITE_ONLY = CL10.CL_MEM_WRITE_ONLY;
    public static final int CL_MEM_READ_ONLY = CL10.CL_MEM_READ_ONLY;
    public static final int CL_MEM_READ_WRITE = CL10.CL_MEM_READ_WRITE;
    public static final int CL_MEM_COPY_HOST_PTR = CL10.CL_MEM_COPY_HOST_PTR;
    public static final int CL_SUCCESS = CL10.CL_SUCCESS;
    
    public enum Device_Type {
        CPU, GPU
    };
    
    public static void destroy() {
        CL.destroy();
    }
    
    public static void init() throws LWJGLException {
        if(initialized) return;
        CL.create();
        initialized = true;
        lastErrorCode.put(0, CL_SUCCESS);
        
        int platformCount = CLPlatform.getPlatforms() == null ? 0 : CLPlatform.getPlatforms().size();
        
        int longestString = 0;
        String[][] lines;
        if(platformCount != 0) {
            lines = new String[platformCount][3];
            for(int i = 0; i < platformCount; ++i) {
                lines[i] = getPlatformInfo(CLPlatform.getPlatforms().get(i));
            }
        } else {
            lines = new String[1][1];
            lines[0][0] = "####No Platform####";
        }
        
        for(int i = 0; i < lines.length; ++i) {
            String[] plfs = lines[i];
            for(String line : plfs) {
                int l = line.length();
                longestString = l > longestString ? l : longestString;
            }
        }
        
        String info = "+-OpenCL Platforms";
        for(int i = 0; i < longestString-15; ++i) {
            info += "-";
        }
        info += "+\n";
        for(int i = 0; i < lines.length; ++i) {
            String[] plfs = lines[i];
            if(i > 0) {
                info += "|";
                for(int k = 0; k < longestString+2; ++k) {
                    info += "*";
                } 
                info += "|\n";
            }
            for(String line : plfs) {
                info += "| ";
                info += line;
                for(int j = 0; j < (1+longestString - line.length()); ++j) {
                    info += " ";
                }
                info += "|\n";
            }
        }
        info += "+";
        for(int i = 0; i < longestString+2; ++i) {
            info += "-";
        }
        info += "+";
        
        System.out.println(info);
    }


    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateBuffer.html">clCreateBuffer</a>
     * @param context
     * @param flags
     * @param host_ptr
     * @return
     */
    public static CLMem clCreateBuffer(CLContext context, long flags, long host_ptr) {
        CLMem mem = CL10.clCreateBuffer(context, flags, host_ptr, lastErrorCode);
        checkError();
        return mem;
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateBuffer.html">clCreateBuffer</a>
     * @param context
     * @param flags
     * @param host_ptr
     * @return CLMem
     */
    public static CLMem clCreateBuffer(CLContext context, long flags, FloatBuffer host_ptr) {
        
        CLMem mem = CL10.clCreateBuffer(context, flags, host_ptr, lastErrorCode);
        checkError();
        return mem;
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateFromGLBuffer.html">clCreateFromGLBuffer</a>
     * @param context
     * @param flags
     * @param bufobj
     * @return CLMem
     */
    public static CLMem clCreateFromGLBuffer(CLContext context, long flags, int bufobj) {
        CLMem mem = CL10GL.clCreateFromGLBuffer(context, flags, bufobj, lastErrorCode);
        checkError();
        return mem;
    }
    

    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateBuffer.html">clCreateBuffer</a>
     * @param context
     * @param flags
     * @param host_ptr
     * @return
     */
    public static CLMem clCreateBuffer(CLContext context, long flags, ByteBuffer host_ptr) {
        CLMem mem = CL10.clCreateBuffer(context, flags, host_ptr, lastErrorCode);
        checkError();
        return mem;
    }
    

    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateBuffer.html">clCreateBuffer</a>
     * @param context
     * @param flags
     * @param host_ptr
     * @return
     */
    public static CLMem clCreateBuffer(CLContext context, long flags, IntBuffer host_ptr) {
        CLMem mem = CL10.clCreateBuffer(context, flags, host_ptr, lastErrorCode);
        checkError();
        return mem;
    }
    

    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateKernel.html">clCreateKernel</a>
     * @param program
     * @param name
     * @return CLKernel
     */
    public static CLKernel clCreateKernel(CLProgram program, String name) {
        CLKernel kernel = CL10.clCreateKernel(program, name, lastErrorCode);
        checkError();
        return kernel;
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateProgramWithSource.html">clCreateProgramWithSource</a>
     * @param context
     * @param source
     * @return CLProgram
     */
    public static CLProgram clCreateProgramWithSource(CLContext context, String source) {
        CLProgram program = CL10.clCreateProgramWithSource(context, source, lastErrorCode);
        checkError();
        return program;
    }
    
    /**
     * Erstellt eine CLCommandQueue
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateCommandQueue.html">clCreateCommandQueue</a>
     * @param context CLContext Objekt
     * @param device Das CLDevice Objekt mit dem der CLContext erstellt wurde
     * @param profiling Aktiviert/Deaktiviert Profiling, Events koennen dann z.B. die Laufzeit eines Kernels messen.
     * Offensichtlich nur fuer Debugging
     * @return CLCommandQueue Objekt
     */
    public static CLCommandQueue clCreateCommandQueue(CLContext context, CLDevice device, boolean profiling) {
        CLCommandQueue queue = CL10.clCreateCommandQueue(context, device, profiling ? CL_QUEUE_PROFILING_ENABLE : 0, lastErrorCode);
        checkError();
        return queue;
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clEnqueueAcquireGLObjects.html">clEnqueueAcquireGLObjects</a>
     * @param command_queue
     * @param mem_object
     * @param event_wait_list
     * @param event
     */
    public static void clEnqueueAcquireGLObjects(CLCommandQueue command_queue, CLMem mem_object, PointerBuffer event_wait_list, PointerBuffer event) {
        lastErrorCode.put(0, CL10GL.clEnqueueAcquireGLObjects(command_queue, mem_object, event_wait_list, event));
        checkError();
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clEnqueueReleaseGLObjects.html">clEnqueueReleaseGLObjects</a>
     * @param command_queue
     * @param mem_object
     * @param event_wait_list
     * @param event
     */
    public static void clEnqueueReleaseGLObjects(CLCommandQueue command_queue, CLMem mem_object, PointerBuffer event_wait_list, PointerBuffer event) {
        lastErrorCode.put(0, CL10GL.clEnqueueReleaseGLObjects(command_queue, mem_object, event_wait_list, event));
        checkError();
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clReleaseKernel.html">clReleaseKernel</a>
     * @param kernel
     */
    public static void clReleaseKernel(CLKernel kernel) {
        lastErrorCode.put(0, CL10.clReleaseKernel(kernel));
        checkError();
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clReleaseProgram.html">clReleaseProgram</a>
     * @param program
     */
    public static void clReleaseProgram(CLProgram program) {
        lastErrorCode.put(0, CL10.clReleaseProgram(program));
        checkError();
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clReleaseCommandQueue.html">clReleaseCommandQueue</a>
     * @param queue
     */
    public static void clReleaseCommandQueue(CLCommandQueue queue) {
        lastErrorCode.put(0, CL10.clReleaseCommandQueue(queue));
        checkError();
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clReleaseContext.html">clReleaseContext</a>
     * @param context
     */
    public static void clReleaseContext(CLContext context) {
        lastErrorCode.put(0, CL10.clReleaseContext(context));
        checkError();
    }

    /**
     * Ruft einen OpenCL Kernel auf
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clEnqueueNDRangeKernel.html">clEnqueueNDRangeKernel</a>
     * @param command_queue Die CommandQueue des Contextes
     * @param kernel Das auszufuehrende Kernel Objekt
     * @param work_dim Dimension des Workgrids [1,2,3]
     * @param global_work_offset immer NULL momentan nicht gebraucht
     * @param global_work_size Groesse des gesamten Workgrids
     * @param local_work_size Grosse einer Workgroup (muss ein ganzzahliger Teiler von global_work_size sein) (wenn NULL wird diese Groesse intern bestimmt)
     * @param event_wait_list Eventliste auf die vor dem Ausfuehren gewartet werden soll (wenn nicht vorhanden = NULL)
     * @param event Event welches die Abarbeitung des Kernels signalisiert (wenn nicht vorhanden = NULL)
     */
    public static void clEnqueueNDRangeKernel(
            CLCommandQueue command_queue, 
            CLKernel kernel, 
            int work_dim, 
            PointerBuffer global_work_offset, 
            PointerBuffer global_work_size, 
            PointerBuffer local_work_size, 
            PointerBuffer event_wait_list, 
            PointerBuffer event) 
    {
        lastErrorCode.put(0, 
                CL10.clEnqueueNDRangeKernel(
                        command_queue, 
                        kernel, 
                        work_dim, 
                        global_work_offset, 
                        global_work_size, 
                        local_work_size, 
                        event_wait_list, 
                        event));
        checkError();
    }

    /**
     * OpenCL 1.0
     * @see http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clEnqueueWriteBuffer.html
     * @param command_queue
     * @param buffer
     * @param blocking_write
     * @param offset
     * @param ptr
     * @param event_wait_list
     * @param event
     */
    public static void clEnqueueWriteBuffer(
            CLCommandQueue command_queue,
            CLMem buffer,
            int blocking_write,
            long offset,
            java.nio.FloatBuffer ptr,
            PointerBuffer event_wait_list,
            PointerBuffer event
            )
    {
        lastErrorCode.put(0, 
                CL10.clEnqueueWriteBuffer(
                        command_queue,
                        buffer,
                        blocking_write,
                        offset,
                        ptr,
                        event_wait_list,
                        event));
        checkError();        
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clFinish.html">clFinish</a>
     * @param command_queue
     */
    public static void clFinish(CLCommandQueue command_queue) {
        lastErrorCode.put(0, CL10.clFinish(command_queue));
        checkError();
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clFlush.html">clFlush</a>
     * @param command_queue
     */
    public static void clFlush(CLCommandQueue command_queue) {
        lastErrorCode.put(0, CL10.clFlush(command_queue));
        checkError();
    }
    
    /**
     * Loescht OpenCL Buffer.
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clReleaseMemObject.html">clReleaseMemObject</a>
     * @param mem CLMem
     */
    public static void clReleaseMemObject(CLMem mem) {
        CL10.clReleaseMemObject(mem);
    }
    
    /**
     * Gibt auﬂerdem compiler Fehler aus.
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clBuildProgram.html">clBuildProgram</a>
     * @param program
     * @param device
     * @param options
     * @param pfn_notify
     */
    public static void clBuildProgram(CLProgram program, CLDevice device, CharSequence options, CLBuildProgramCallback pfn_notify) {
        lastErrorCode.put(0, CL10.clBuildProgram(program, device, "", null));
        checkProgram(program, device);
        checkError();
    }
    
    /**
     * 
     * @param platform
     * @param devices
     * @param pfn_notify
     * @param share_drawable
     * @param errcode_ret
     * @return CLContext
     * @throws LWJGLException 
     */
    public static CLContext create(CLPlatform platform, List<CLDevice> devices, CLContextCallback pfn_notify, Drawable share_drawable) throws LWJGLException {
        CLContext context = CLContext.create(platform, devices, null, share_drawable, lastErrorCode);
        checkError();
        return context;
    }
    
    /**
     * @see <a href="http://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clCreateCommandQueue.html">clCreateCommandQueue</a>
     * @param context
     * @param device
     * @param properties
     * @return
     */
    public static CLCommandQueue clCreateCommandQueue(CLContext context, CLDevice device, long properties) {
        CLCommandQueue queue = CL10.clCreateCommandQueue(context, device, 0, lastErrorCode);
        checkError();
        return queue;
    }
    
    //helpers
    public static String[] getPlatformInfo(CLPlatform platform) {
        String[] lines = new String[3];
        //lines.add(String.format("ID: %d", platform.getPointer()));
        lines[0] = String.format("Version: %s", platform.getInfoString(CL_PLATFORM_VERSION)) 
                + String.format(", Name: %s", platform.getInfoString(CL_PLATFORM_NAME));
        lines[1] = String.format(String.format("Vendor: %s", platform.getInfoString(CL_PLATFORM_VENDOR)) 
                + String.format(", Profile: %s", platform.getInfoString(CL_PLATFORM_PROFILE)));
        
        List<CLDevice> d = platform.getDevices(CL_DEVICE_TYPE_GPU);
        String s = String.format("GPU Devices: %s", d != null ? d.size() : 0);
        d = platform.getDevices(CL_DEVICE_TYPE_CPU);
        lines[2] = s+String.format(", CPU Devices: %s", d != null ? d.size() : 0);
        return lines;
    }
    
    /**
     * Checks a CLProgram for build errors
     * @param program CLProgram
     * @param device CLDevice
     */
    public static void checkProgram(CLProgram program, CLDevice device) {
        PointerBuffer buffer = BufferUtils.createPointerBuffer(1);
        CL10.clGetProgramBuildInfo(program, device, CL10.CL_PROGRAM_BUILD_LOG, null, buffer);
        if(buffer.get(0) > 2) {
            ByteBuffer log = BufferUtils.createByteBuffer((int)buffer.get(0));
            CL10.clGetProgramBuildInfo(program, device, CL10.CL_PROGRAM_BUILD_LOG, log, buffer);
            byte bytes[] = new byte[log.capacity()];
            log.get(bytes);
            System.out.println(String.format("CL Compiler Error/Warning:\n %s", new String(bytes)));
        }
        checkError();
    }
    
    public static void checkError() {
        checkError(lastErrorCode.get(0));
    }
    
    public static void checkError(int errorCode) {
        if(checkError && errorCode != CL_SUCCESS) {
            throw new RuntimeException(getErrorLog(errorCode));
        }
    }
    
    private static String getErrorLog(int errorCode) {
        switch(errorCode) {
        case 0x0 : return "CL_ERROR: CL_SUCCESS";
        case 0xFFFFFFFF : return "CL_ERROR: CL_DEVICE_NOT_FOUND";
        case 0xFFFFFFFE : return "CL_ERROR: CL_DEVICE_NOT_AVAILABLE";
        case 0xFFFFFFFD : return "CL_ERROR: CL_COMPILER_NOT_AVAILABLE";
        case 0xFFFFFFFC : return "CL_ERROR: CL_MEM_OBJECT_ALLOCATION_FAILURE";
        case 0xFFFFFFFB : return "CL_ERROR: CL_OUT_OF_RESOURCES";
        case 0xFFFFFFFA : return "CL_ERROR: CL_OUT_OF_HOST_MEMORY";
        case 0xFFFFFFF9 : return "CL_ERROR: CL_PROFILING_INFO_NOT_AVAILABLE";
        case 0xFFFFFFF8 : return "CL_ERROR: CL_MEM_COPY_OVERLAP";
        case 0xFFFFFFF7 : return "CL_ERROR: CL_IMAGE_FORMAT_MISMATCH";
        case 0xFFFFFFF6 : return "CL_ERROR: CL_IMAGE_FORMAT_NOT_SUPPORTED";
        case 0xFFFFFFF5 : return "CL_ERROR: CL_BUILD_PROGRAM_FAILURE";
        case 0xFFFFFFF4 : return "CL_ERROR: CL_MAP_FAILURE";
        case 0xFFFFFFE2 : return "CL_ERROR: CL_INVALID_VALUE";
        case 0xFFFFFFE1 : return "CL_ERROR: CL_INVALID_DEVICE_TYPE";
        case 0xFFFFFFE0 : return "CL_ERROR: CL_INVALID_PLATFORM";
        case 0xFFFFFFDF : return "CL_ERROR: CL_INVALID_DEVICE";
        case 0xFFFFFFDE : return "CL_ERROR: CL_INVALID_CONTEXT";
        case 0xFFFFFFDD : return "CL_ERROR: CL_INVALID_QUEUE_PROPERTIES";
        case 0xFFFFFFDC : return "CL_ERROR: CL_INVALID_COMMAND_QUEUE";
        case 0xFFFFFFDB : return "CL_ERROR: CL_INVALID_HOST_PTR";
        case 0xFFFFFFDA : return "CL_ERROR: CL_INVALID_MEM_OBJECT";
        case 0xFFFFFFD9 : return "CL_ERROR: CL_INVALID_IMAGE_FORMAT_DESCRIPTOR";
        case 0xFFFFFFD8 : return "CL_ERROR: CL_INVALID_IMAGE_SIZE";
        case 0xFFFFFFD7 : return "CL_ERROR: CL_INVALID_SAMPLER";
        case 0xFFFFFFD6 : return "CL_ERROR: CL_INVALID_BINARY";
        case 0xFFFFFFD5 : return "CL_ERROR: CL_INVALID_BUILD_OPTIONS";
        case 0xFFFFFFD4 : return "CL_ERROR: CL_INVALID_PROGRAM";
        case 0xFFFFFFD3 : return "CL_ERROR: CL_INVALID_PROGRAM_EXECUTABLE";
        case 0xFFFFFFD2 : return "CL_ERROR: CL_INVALID_KERNEL_NAME";
        case 0xFFFFFFD1 : return "CL_ERROR: CL_INVALID_KERNEL_DEFINITION";
        case 0xFFFFFFD0 : return "CL_ERROR: CL_INVALID_KERNEL";
        case 0xFFFFFFCF : return "CL_ERROR: CL_INVALID_ARG_INDEX";
        case 0xFFFFFFCE : return "CL_ERROR: CL_INVALID_ARG_VALUE";
        case 0xFFFFFFCD : return "CL_ERROR: CL_INVALID_ARG_SIZE";
        case 0xFFFFFFCC : return "CL_ERROR: CL_INVALID_KERNEL_ARGS";
        case 0xFFFFFFCB : return "CL_ERROR: CL_INVALID_WORK_DIMENSION";
        case 0xFFFFFFCA : return "CL_ERROR: CL_INVALID_WORK_GROUP_SIZE";
        case 0xFFFFFFC9 : return "CL_ERROR: CL_INVALID_WORK_ITEM_SIZE";
        case 0xFFFFFFC8 : return "CL_ERROR: CL_INVALID_GLOBAL_OFFSET";
        case 0xFFFFFFC7 : return "CL_ERROR: CL_INVALID_EVENT_WAIT_LIST";
        case 0xFFFFFFC6 : return "CL_ERROR: CL_INVALID_EVENT";
        case 0xFFFFFFC5 : return "CL_ERROR: CL_INVALID_OPERATION";
        case 0xFFFFFFC4 : return "CL_ERROR: CL_INVALID_GL_OBJECT";
        case 0xFFFFFFC3 : return "CL_ERROR: CL_INVALID_BUFFER_SIZE";
        case 0xFFFFFFC2 : return "CL_ERROR: CL_INVALID_MIP_LEVEL";
        case 0xFFFFFFC1 : return "CL_ERROR: CL_INVALID_GLOBAL_WORK_SIZE";
        default : return "CL_ERROR: CL_UNKNOWN_ERROR";
        }
    }
}