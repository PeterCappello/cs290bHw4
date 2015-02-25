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
package clients;

import api.ReturnValue;
import api.Space;
import api.Task;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import applications.euclideantsp.TaskEuclideanTsp;
import applications.euclideantsp.Tour;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Peter Cappello
 */
public class ClientEuclideanTsp extends Client<Tour>
{
    private static final int NUM_PIXALS = 600;
    public static final double[][] CITIES =
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
    
    public ClientEuclideanTsp() throws RemoteException
    { 
        super( "Euclidean TSP" ); 
    }
    
    public static void main( String[] args ) throws Exception
    {
        final List<Integer> unvisitedCities = new ArrayList<>();
        for ( int city = 0; city < CITIES.length; city++ )
        {
            unvisitedCities.add( city );
        }
        final Task task = new TaskEuclideanTsp( new ArrayList<>(), unvisitedCities );
        final ClientEuclideanTsp client = new ClientEuclideanTsp();
        
        System.setSecurityManager( new SecurityManager() );
        client.begin();
        ReturnValue<Tour> result = ( ReturnValue<Tour> ) client.getSpace( 4 ).compute( task );
        client.add( client.getLabel( result.value() ) );
        client.end();
    }
    
    @Override
    public JLabel getLabel( final Tour tour )
    {
        List<Integer> cityList = tour.tour();
        Logger.getLogger( ClientEuclideanTsp.class.getCanonicalName() ).log(Level.INFO, tourToString( cityList ) );

        // display the graph graphically, as it were
        // get minX, maxX, minY, maxY, assuming they 0.0 <= mins
        double minX = CITIES[0][0], maxX = CITIES[0][0];
        double minY = CITIES[0][1], maxY = CITIES[0][1];
        for ( double[] cities : CITIES ) 
        {
            if ( cities[0] < minX ) 
                minX = cities[0];
            if ( cities[0] > maxX ) 
                maxX = cities[0];
            if ( cities[1] < minY ) 
                minY = cities[1];
            if ( cities[1] > maxY ) 
                maxY = cities[1];
        }

        // scale points to fit in unit square
        final double side = Math.max( maxX - minX, maxY - minY );
        double[][] scaledCities = new double[CITIES.length][2];
        for ( int i = 0; i < CITIES.length; i++ )
        {
            scaledCities[i][0] = ( CITIES[i][0] - minX ) / side;
            scaledCities[i][1] = ( CITIES[i][1] - minY ) / side;
        }

        final Image image = new BufferedImage( NUM_PIXALS, NUM_PIXALS, BufferedImage.TYPE_INT_ARGB );
        final Graphics graphics = image.getGraphics();

        final int margin = 10;
        final int field = NUM_PIXALS - 2*margin;
        // draw edges
        graphics.setColor( Color.BLUE );
        int x1, y1, x2, y2;
        int city1 = cityList.get( 0 ), city2;
        x1 = margin + (int) ( scaledCities[city1][0]*field );
        y1 = margin + (int) ( scaledCities[city1][1]*field );
        for ( int i = 1; i < CITIES.length; i++ )
        {
            city2 = cityList.get( i );
            x2 = margin + (int) ( scaledCities[city2][0]*field );
            y2 = margin + (int) ( scaledCities[city2][1]*field );
            graphics.drawLine( x1, y1, x2, y2 );
            x1 = x2;
            y1 = y2;
        }
        city2 = cityList.get( 0 );
        x2 = margin + (int) ( scaledCities[city2][0]*field );
        y2 = margin + (int) ( scaledCities[city2][1]*field );
        graphics.drawLine( x1, y1, x2, y2 );

        // draw vertices
        final int VERTEX_DIAMETER = 6;
        graphics.setColor( Color.RED );
        for ( int i = 0; i < CITIES.length; i++ )
        {
            int x = margin + (int) ( scaledCities[i][0]*field );
            int y = margin + (int) ( scaledCities[i][1]*field );
            graphics.fillOval( x - VERTEX_DIAMETER/2,
                               y - VERTEX_DIAMETER/2,
                              VERTEX_DIAMETER, VERTEX_DIAMETER);
        }
        final ImageIcon imageIcon = new ImageIcon( image );
        return new JLabel( imageIcon );
    }
    
    private String tourToString( List<Integer> cityList )
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append( "Tour: " );
        cityList.stream().forEach((city) -> {
            stringBuilder.append( city ).append( ' ' );
        });
        return stringBuilder.toString();
    }
}
