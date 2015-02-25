/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Peter Cappello
 * @param <I> input type.
 */
public abstract class TaskCompose<I> extends Task
{
    private int numUnsetArgs;
    private List<I> args;
    
    @Override
    abstract public ReturnValue call();
    
    public List<I> args() { return args; }
    
    public void arg( int argNum, I argValue ) 
    { 
        args.set( argNum, argValue );
        numUnsetArgs--;
    }
    
    public void numArgs( int numArgs )
    {
        numUnsetArgs = numArgs;
        args = new ArrayList<>( numArgs );
        for ( int i = 0; i < numArgs; i++ )
        {
            args.add( null );
        }
    }
    
    public boolean isReady() { return numUnsetArgs == 0; }
}
