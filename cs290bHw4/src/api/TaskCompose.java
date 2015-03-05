/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Peter Cappello
 * @param <I> input type.
 */
public abstract class TaskCompose<I> extends Task
{
//    static final private AtomicInteger ZERO = new AtomicInteger();
//    private int numUnsetArgs;
    private AtomicInteger numUnsetArgs;
    private List<I> args;
    
    @Override
    abstract public ReturnValue call();
    
    public List<I> args() { return args; }
    
    public void arg( final int argNum, final I argValue ) 
    { 
        assert numUnsetArgs.get() > 0 && ! isReady() && argValue != null && args.get( argNum ) == null; 
        args.set( argNum, argValue );
        numUnsetArgs.getAndDecrement();
        assert args.get( argNum ) == argValue;
    }
    
    public void numArgs( int numArgs )
    {
        assert numArgs >= 0;
        numUnsetArgs = new AtomicInteger( numArgs );
        args = Collections.synchronizedList( new ArrayList<>( numArgs ) ) ;
        for ( int i = 0; i < numArgs; i++ )
        {
            args.add( null );
            assert args.get( i ) == null;
        }
        assert args.size() == numArgs;
    }
    
    public boolean isReady() { return numUnsetArgs.get() == 0; }
}
