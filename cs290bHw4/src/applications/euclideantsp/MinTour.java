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
package applications.euclideantsp;

import api.ReturnValue;
import api.TaskCompose;
import java.util.Comparator;

/**
 *
 * @author Peter Cappello
 */
public class MinTour extends TaskCompose<Tour> //implements Comparator<Tour>
{
    /**
     * Find the minimum distance tour of its input tours.
     * @return the minimum distance tour of its input tours.
     */
    @Override
    public ReturnValue call() 
    {
//        return new ReturnValueTour( this, args().stream().min( this ).get() );
        return new ReturnValueTour( this, args().stream().min( Comparator.comparingDouble( Tour::cost ) ).get() );
    }
    /**
     * Compare the cost of thisTour to thatTour.
     * @param thisTour
     * @param thatTour
     * @return -1: thisTour is shorter than thatTour
     *          1: thatTour is shorter than thisTour
     *          0: otherwise
     */
//    @Override
//    public int compare( Tour thisTour, Tour thatTour ) 
//    {
//        return thisTour.cost() < thatTour.cost() ? -1 : thisTour.cost() > thatTour.cost() ? 1 : 0;
//    }
}
