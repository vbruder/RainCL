package window;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
 
public class TimerCaller implements Runnable
{
    private boolean terminate;
    private List<TimerListener> listeners;
    
    public TimerCaller()
    {
        super();
        listeners = new ArrayList<TimerListener>();
    }
    public void run()
    {
        terminate = false;
        
        do
        {
            try
            {
                TimeUnit.SECONDS.sleep(1L);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            
            for(int i = 0, size = listeners.size(); i < size; i++)
                listeners.get(i).updateTex();
        }
        while(!terminate);
    }
    
    public void addTimerListener(TimerListener listener)
    {
        listeners.add(listener);
    }
    
    public void start()
    {
        Executors.newFixedThreadPool(1).submit(this);
    }
    
    public void stop()
    {
        terminate = true;
    }
}