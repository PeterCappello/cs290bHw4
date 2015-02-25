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

import system.Return;
import java.util.logging.Level;
import java.util.logging.Logger;
import system.SpaceImpl;

/**
 *
 * @author Peter Cappello
 * @param <T>
 */
public class ReturnValue<T> extends Return
{    
    final private int composeId;
    final private int composeArgNum;
    final private T value;
    
    public ReturnValue( Task task, T value ) 
    { 
        composeId = task.composeId();
        composeArgNum = task.composeArgNum();
        this.value = value; 
//        System.out.println("ReturnValue.constructor: " 
//            + " taskId: " + task.id()
//            + " composeId: " + composeId
//            + " composeArgNum: " + composeArgNum
//            + " value: " + value
//        );
    }
    
    public T value() { return value; }
   
    /**
     *
     * @param parentTask unused - the task whose Result is to be processed.
     * @param space
     */
    @Override
    public void process( Task parentTask, SpaceImpl space )
    {
        if ( composeId == SpaceImpl.FINAL_RETURN_VALUE )
        {
            space.putResult( this );
            return;
        }
        TaskCompose compose = space.getCompose( composeId );
        if ( compose == null )
        {
            Logger.getLogger( this.getClass().getCanonicalName() ).log(Level.SEVERE, "Waiting compose task missing." );
            System.exit( 1 );
        }
        else
        {
//            System.out.println("ReturnValue.process: updating compose: "
//                    + " composeId: " + composeId
//                    + " composeArgNum: " + composeArgNum
//                    + " value: " + value
//            );
            compose.arg( composeArgNum, value );
            if ( compose.isReady() )
            {
                space.putReadyTask( compose );
            }
        }
    }
}
