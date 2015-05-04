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
 * FITNESS FOR ONE PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package applications.euclideantsp;

import api.JobRunner;
import api.ReturnDecomposition;
import api.ReturnValue;
import system.Task;
import api.TaskDecompose;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import util.PermutationEnumerator;

/**
 * Find a tour of minimum cost among those that start with city 0, 
 * followed by city secondCity.
 * @author Peter Cappello
 */
public class TaskEuclideanTsp extends TaskDecompose<Tour>
{ 
    static final public double[][] CITIES =
    {
	{ 1, 1 },
	{ 8, 1 },
	{ 8, 8 },
	{ 1, 8 },
	{ 2, 2 },
	{ 7, 2 },
	{ 7, 7 },
	{ 2, 7 },
	{ 3, 3 },
	{ 6, 3 },
	{ 6, 6 },
	{ 3, 6 }
    };
    static final public double[][] DISTANCES = initializeDistances();
    static final Integer ONE = 1;
    static final Integer TWO = 2;
    static final Integer MAX_UNVISITED_CITIES = 10;
    
    static private List<Integer> initialPartialTour()
    {
        List<Integer> partialTour = new ArrayList<>();
        partialTour.add( 0 );
        return partialTour;
    }
    
    static private List<Integer> initialUnvisitedCities()
    {
        final List<Integer> unvisitedCities = new ArrayList<>();
        for ( int city = 1; city < CITIES.length; city++ )
        {
            unvisitedCities.add( city );
        }
        return unvisitedCities;
    }
    
    // Configure Job
    static final private String FRAME_TITLE = "Euclidean TSP";
    static final private Task TASK = new TaskEuclideanTsp( initialPartialTour(), initialUnvisitedCities() );
    
    public static void main( final String[] args ) throws Exception
    {
        new JobRunner( FRAME_TITLE, args ).run( TASK );
    }
    
    final private List<Integer> partialTour;
    final private List<Integer> unvisitedCities;
            
    public TaskEuclideanTsp( List<Integer> partialTour, List<Integer> unvisitedCities )
    {
        this.partialTour = partialTour;
        this.unvisitedCities = unvisitedCities;
    }
    
    @Override
    public boolean isAtomic() { return unvisitedCities.size() <= MAX_UNVISITED_CITIES; }
    
    /**
     * Produce a tour of minimum cost from the set of tours, having as its
     * elements each tour consisting of the sequence of cities in partialTour 
     * followed by a permutation of the unvisitedCities.
     * @return a tour of minimum cost.
     */
     @Override
    public ReturnValue solve() 
    {
        // initial value for shortestTour and its distance.
        List<Integer> shortestTour = new ArrayList<>( partialTour );
        shortestTour.addAll( unvisitedCities );
        double shortestTourDistance = tourDistance( CITIES, shortestTour );

        // Use my permutation enumerator
        PermutationEnumerator<Integer> permutationEnumerator = new PermutationEnumerator<>( unvisitedCities );
        for ( List<Integer> subtour = permutationEnumerator.next(); subtour != null; subtour = permutationEnumerator.next() ) 
        {
            List<Integer> tour = new ArrayList<>( partialTour );
            tour.addAll( subtour );
            if ( tour.indexOf( ONE ) >  tour.indexOf( TWO ) )
            {
                continue; // skip tour; it is the reverse of another.
            }
            double tourDistance = tourDistance( CITIES, tour );
            if ( tourDistance < shortestTourDistance )
            {
                shortestTour = tour;
                shortestTourDistance = tourDistance;
            }
        }
        return new ReturnValueTour( this, new Tour( shortestTour, shortestTourDistance ) );
    }

    /**
     * 
     * @return container that has a MinTour composition task and a list
     * of TaskEuclideanTsp subtasks, each with a partial tour that has 1 fewer
     * unvisited cities than this task.
     */
    @Override
    public ReturnDecomposition divideAndConquer() 
    {
        final List<Task> subtasks = new  LinkedList<>();
        for ( Integer unvisitedCity : unvisitedCities )
        {
            List<Integer> subtaskPartialTour = new ArrayList<>( partialTour );
            List<Integer> subtaskUnvisitedCities = new ArrayList<>( unvisitedCities );
            subtaskUnvisitedCities.remove( unvisitedCity );
            subtaskPartialTour.add( unvisitedCity ); // extend tour with this city.
            subtasks.add( new TaskEuclideanTsp( subtaskPartialTour, subtaskUnvisitedCities ) );
        }
        return new ReturnDecomposition( new MinTour(), subtasks );
    }
    
    @Override
    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append( getClass() );
        stringBuilder.append( "\n\tPartial tour: " );
        partialTour.stream().forEach(( city ) -> 
        {
            stringBuilder.append( city ).append( " " );
        } );
        stringBuilder.append( "\n\tUnvisited cities: " );
        unvisitedCities.stream().forEach(( city ) -> 
        {
            stringBuilder.append( city ).append( " " );
        } );
        return stringBuilder.toString();
    }
    
    public static double tourDistance( final double[][] cities, final List<Integer> tour )
   {
       double cost = 0.0;
       for ( int city = 0; city < tour.size() - 1; city ++ )
       {
           cost += distance( cities[ tour.get( city ) ], cities[ tour.get( city + 1 ) ] );
       }
       return cost + distance( cities[ tour.get( tour.size() - 1 ) ], cities[ tour.get( 0 ) ] );
   }
   
   private static double distance( final double[] city1, final double[] city2 )
   {
       final double deltaX = city1[ 0 ] - city2[ 0 ];
       final double deltaY = city1[ 1 ] - city2[ 1 ];
       return Math.sqrt( deltaX * deltaX + deltaY * deltaY );
   }
   
   static private double[][] initializeDistances()
    {
        double[][] distances = new double[ CITIES.length][ CITIES.length];
        for ( int i = 0; i < CITIES.length; i++ )
        for ( int j = 0; j < i; j++ )
        {
            distances[ i ][ j ] = distances[ j ][ i ] = distance( CITIES[ i ], CITIES[ j ] );
        }
       return distances;
    }
}
