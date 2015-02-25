/*
 * The MIT License
 *
 * Copyright 2015 peter.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package system;

import api.Computer;
import api.Space;
import api.Task;
import api.TaskCompose;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Peter Cappello
 */
public class SpaceImpl extends UnicastRemoteObject implements Space, Computer2Space
{
    static final public int FINAL_RETURN_VALUE = -1;
    static final private AtomicInteger computerIds = new AtomicInteger();
           final private AtomicInteger taskIds     = new AtomicInteger();
    
    private final BlockingQueue<Task>   readyTaskQ   = new LinkedBlockingQueue<>();
    private final BlockingQueue<Return> resultQ      = new LinkedBlockingQueue<>();

    private final Map<Computer,ComputerProxy> computerProxies = Collections.synchronizedMap( new HashMap<>() );  // !! make concurrent
    private final Map<Integer, TaskCompose>   waitingTaskMap  = Collections.synchronizedMap( new HashMap<>() );
        
    public SpaceImpl() throws RemoteException 
    {
        Logger.getLogger( SpaceImpl.class.getName() ).log( Level.INFO, "Space started." );
    }
    
    /**
     * Compute a Task and return its Return.
     * To ensure that the correct Return is returned, this must be the only
 computation that the Space is serving.
     * 
     * @param task
     * @return the Task's Return object.
     */
    @Override
    public Return compute( Task task )
    {
        put( task );
        return take();
    }
    /**
     * Put a task into the Task queue.
     * @param task
     */
    @Override
    synchronized public void put(Task task) 
    { 
        task.id( taskIds.getAndIncrement() );
        task.composeId( FINAL_RETURN_VALUE );
        readyTaskQ.add( task ); 
    }

    /**
     * Take a Return from the Return queue.
     * @return a Return object.
     */
    @Override
    synchronized public Return take() 
    {
        try 
        {
            return resultQ.take();
        } 
        catch ( InterruptedException exception ) 
        {
            Logger.getLogger(SpaceImpl.class.getName()).log(Level.INFO, null, exception);
        }
        assert false; // should never reach this point
        return null;
    }

    @Override
    public void exit() throws RemoteException 
    {
        computerProxies.values().forEach( proxy -> proxy.exit() );
        System.exit( 0 );
    }

    /**
     * Register Computer with Space.  
     * Will override existing key-value pair, if any.
     * @param computer - Remote reference to computer.
     * @throws RemoteException
     */
    @Override
    synchronized public void register( Computer computer ) throws RemoteException 
    {
        final ComputerProxy computerproxy = new ComputerProxy( computer );
        computerProxies.put( computer, computerproxy );
        computerproxy.start();
        Logger.getLogger(SpaceImpl.class.getName()).log(Level.INFO, "Computer {0} started.", computerproxy.computerId);
    }
    
    public static void main( String[] args ) throws Exception
    {
        System.setSecurityManager( new SecurityManager() );
        LocateRegistry.createRegistry( Space.PORT )
                      .rebind( Space.SERVICE_NAME, new SpaceImpl() );
    }

    private void processResult( Task parentTask, Return result ) { result.process( parentTask, this ); }
    
    public int makeTaskId() { return taskIds.incrementAndGet(); }
    
    public TaskCompose getCompose( int composeId ) { return waitingTaskMap.get( composeId ); }
            
    public void putCompose( TaskCompose compose )
    {
        waitingTaskMap.put( compose.id(), compose );
        assert waitingTaskMap.get( compose.id() ) != null;
    }
    
    public void putReadyTask( Task task ) 
    { 
        try { readyTaskQ.put( task ); } catch ( InterruptedException ignore ){} 
    }
    
    public void putResult( Return result )
    {
        try { resultQ.put( result ); } catch( InterruptedException ignore ){}
    }
    
    private class ComputerProxy extends Thread implements Computer 
    {
        final private Computer computer;
        final private int computerId = computerIds.getAndIncrement();

        ComputerProxy( Computer computer ) { this.computer = computer; }

        @Override
        public Return execute( Task task ) throws RemoteException
        { 
            return computer.execute( task );
        }
        
        @Override
        public void exit() 
        { 
            try { computer.exit(); } 
            catch ( RemoteException ignore ) {} 
        }

        @Override
        public void run() 
        {
            while ( true ) 
            {
                Task task = null;
                try 
                { 
                    task = readyTaskQ.take();
                    processResult( task, execute( task ) );
                }
                catch ( RemoteException ignore )
                {
                    readyTaskQ.add( task );
                    computerProxies.remove( computer );
                    Logger.getLogger( this.getClass().getName() ).log( Level.WARNING, "Computer {0} failed.", computerId );
                } 
                catch ( InterruptedException ex ) { Logger.getLogger( this.getClass().getName()).log( Level.INFO, null, ex ); }
            }
        }
    }
}
