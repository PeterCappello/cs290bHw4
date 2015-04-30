/*
 * The MIT License
 *
 * Copyright 2015 Peter Cappello.
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
package api;

import system.Task;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import system.Configuration;
import system.SpaceImpl;

/**
 *
 * @author Peter Cappello
 * @param <I> input type.
 */
public abstract class TaskCompose<I> extends Task
{
    private AtomicInteger numUnsetArgs;
    private List<I> args;
    
    @Override
    abstract public ReturnValue call();
    
    synchronized public List<I> args() { return args; }
    
    synchronized public void arg( final int argNum, final I argValue, SpaceImpl space ) 
    { 
        assert numUnsetArgs.get() > 0 && numUnsetArgs.get() != 0 && argValue != null && args.get( argNum ) == null; 
        args.set( argNum, argValue );
        numUnsetArgs.getAndDecrement();
        assert args.get( argNum ) == argValue;
        if ( numUnsetArgs.get() == 0 )
        {
            if ( Configuration.SPACE_CALLABLE )
            {
                space.processResult( this, this.call() ); // assumes TaskCompose is SPACE_CALLABLE.
            }
            else
            {
                space.putReadyTask( this );
            }
            space.removeWaitingTask( id() );
        }
    }
    
    synchronized public void numArgs( int numArgs )
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
}
