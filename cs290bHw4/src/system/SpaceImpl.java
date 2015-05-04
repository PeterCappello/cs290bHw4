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

import api.ReturnValue;
import api.Space;
import api.TaskCompose;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SpaceImpl implements the space for coordinating sending/receiving Task and Result objects.
 * @author Peter Cappello
 */
public class SpaceImpl extends UnicastRemoteObject implements Space
{
    static final public int FINAL_RETURN_VALUE = -1;
    static final private AtomicInteger computerIds = new AtomicInteger();
    
    final private AtomicInteger taskIds = new AtomicInteger();
    final private BlockingQueue<Task>   readyTaskQ = new LinkedBlockingQueue<>();
    final private BlockingQueue<ReturnValue> resultQ    = new LinkedBlockingQueue<>();
    final private Map<Computer,ComputerProxy> computerProxies = Collections.synchronizedMap( new HashMap<>() );  // !! make concurrent
    final private Map<Integer, TaskCompose>   waitingTaskMap  = Collections.synchronizedMap( new HashMap<>() );
        
    public SpaceImpl() throws RemoteException 
    {
        Logger.getLogger(getClass().getName() )
              .log( Level.INFO, "Space started." );
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
    public ReturnValue compute( Task task )
    {
        execute( task );
        return take();
    }
    /**
     * Put a task into the Task queue.
     * @param task
     */
    @Override
    public void execute( Task task ) 
    { 
        task.id( makeTaskId() );
        task.composeId( FINAL_RETURN_VALUE );
        readyTaskQ.add( task );
    }
    
    @Override
    synchronized public void putAll( final List<Task> taskList )
    {
        for ( Task task : taskList )
        {
            readyTaskQ.add( task );
        }
    }

    /**
     * Take a Return from the Return queue.
     * @return a Return object.
     */
    @Override
    public ReturnValue take() 
    {
        try { return resultQ.take(); } 
        catch ( InterruptedException exception ) 
        {
            Logger.getLogger( SpaceImpl.class.getName() )
                    .log( Level.INFO, null, exception );
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
     * @param computer
     * @param workerList
     * @throws RemoteException
     */
    @Override
    public void register( Computer computer, List<Worker> workerList ) throws RemoteException
    {
        final ComputerProxy computerproxy = new ComputerProxy( computer, workerList );
        computerProxies.put( computer, computerproxy );
        computerproxy.startWorkerProxies();
        Logger.getLogger( this.getClass().getName() )
              .log( Level.INFO, "Registered computer {0}.", computerproxy.computerId );
    }
    
    public static void main( String[] args ) throws Exception
    {
        System.setSecurityManager( new SecurityManager() );
        LocateRegistry.createRegistry( Space.PORT )
                      .rebind( Space.SERVICE_NAME, new SpaceImpl() );
    }

    public void processResult( final Task parentTask, final Return result ) { result.process( parentTask, this ); }
    
    public int makeTaskId() { return taskIds.incrementAndGet(); }
    
    public TaskCompose getCompose( final int composeId ) { return waitingTaskMap.get( composeId ); }
            
    public void putCompose( final TaskCompose compose ) { waitingTaskMap.put( compose.id(), compose ); }
    
    public void putReadyTask( final Task task ) { readyTaskQ.add( task ); }
    
    public void putResult( final ReturnValue result ) { resultQ.add( result ); }
    
    public void removeWaitingTask( int composeId ) { waitingTaskMap.remove( composeId ); }
    
    private class ComputerProxy implements Computer 
    {
        final private Computer computer;
        final private int computerId = computerIds.getAndIncrement();
        final private Map<Worker, WorkerProxy> workerMap = new HashMap<>();

        ComputerProxy( Computer computer, List<Worker> workerList )
        { 
            this.computer = computer;
            for ( Worker worker : workerList )
            {
                WorkerProxy workerProxy = new WorkerProxy( worker );
                workerMap.put( worker, workerProxy );
            }
        }
        
        private void startWorkerProxies()
        {
            for ( WorkerProxy workerProxy : workerMap.values() )
            {
                workerProxy.start();
            }
        }
 
        @Override
        public void exit() 
        { 
            try { computer.exit(); } 
            catch ( RemoteException ignore ) {} 
        }
        
        private void unregister( Task task, Computer computer, Worker worker )
        {
            readyTaskQ.add( task );
            workerMap.remove( worker );
            Logger.getLogger( this.getClass().getName() )
                  .log( Level.WARNING, "Computer {0} Worker failed.", computerId );
            if ( workerMap.isEmpty() )
            {
                computerProxies.remove( computer );
                Logger.getLogger( ComputerProxy.class.getCanonicalName() )
                      .log( Level.WARNING, "Computer {0} failed.", computerId );
            }
        }

        private class WorkerProxy extends Thread implements Worker
        {
            final private Worker worker;
            
            private WorkerProxy( Worker worker ) { this.worker = worker; }
            
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
                        unregister( task, computer, worker );
                        return;
                    } 
                    catch ( InterruptedException ex ) 
                    { 
                        Logger.getLogger( this.getClass().getName() )
                              .log( Level.INFO, null, ex ); 
                    }
                }
            }
            
            @Override
            public Return execute( Task task ) throws RemoteException 
            {
                return worker.execute( task );
            }     
        }
    }
}
