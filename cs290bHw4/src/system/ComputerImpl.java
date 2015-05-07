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
import api.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static system.Configuration.MULTI_COMPUTERS;

/**
 * An implementation of the Remote Computer interface.
 * @author Peter Cappello
 */
public class ComputerImpl extends UnicastRemoteObject implements Computer
{
    static final private int FACTOR = 2;
           final private List<Worker> workerList = makeWorkerList();
           
    public ComputerImpl() throws RemoteException
    {
        Logger.getLogger( ComputerImpl.class.getName() )
              .log(Level.INFO, "Computer: started with {0} workers.", workerList.size());
    }
    
    public List<Worker> makeWorkerList()
    {
        final int numAvailableProcessors = MULTI_COMPUTERS ? FACTOR * Runtime.getRuntime().availableProcessors() : 1;
        final List<Worker> workers = new ArrayList<>( numAvailableProcessors );
        for ( int workerNum = 0; workerNum < numAvailableProcessors; workerNum++ )
        {
            workers.add( new WorkerImpl() );
        }
        return workers;
    }
    
    public List<Worker> workList() { return workerList; }
    
    public static void main( String[] args ) throws Exception
    {
        System.setSecurityManager( new SecurityManager() );
        final String domainName = args.length == 0 ? "localhost" : args[ 0 ];
        final String url = "rmi://" + domainName + ":" + Space.PORT + "/" + Space.SERVICE_NAME;
        final Space space = (Space) Naming.lookup( url );
        ComputerImpl computer = new ComputerImpl();
        space.register( computer, computer.workerList() );
    }

    /**
     * Terminate the JVM.
     * @throws RemoteException - always!
     */
    @Override
    public void exit() throws RemoteException 
    { 
        Logger.getLogger( this.getClass().getName() )
              .log( Level.INFO, "Computer: exiting." );
        /*System.exit( 0 ); */ 
    }
    
    public List<Worker> workerList() { return workerList; }
    
    private class WorkerImpl implements Worker
    {
        WorkerImpl() {}

        @Override
        public Return execute( Task task ) throws RemoteException 
        {
            final long startTime = System.nanoTime();
            final Return returnValue = task.call();
            final long runTime = ( System.nanoTime() - startTime ) / 1000000; // milliseconds
            returnValue.taskRunTime( runTime );
            return returnValue;
        }
    }
}
