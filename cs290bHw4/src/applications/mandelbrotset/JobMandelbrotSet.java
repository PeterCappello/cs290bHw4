/*
 * The MIT License
 *
 * Copyright 2015 petercappello.
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
package applications.mandelbrotset;
import api.Job;
import api.JobRunner;
import system.Task;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 *
 * @author Peter Cappello
 */
public class JobMandelbrotSet implements Job<ResultValueMandelbrotSet>
{
    // Configure Job 
    static public final double LOWER_LEFT_X = -0.7510975859375;
    static public final double LOWER_LEFT_Y = 0.1315680625;
    static public final double EDGE_LENGTH = 0.01611;
    static public final int N_PIXELS = 1024;
    static public final int ITERATION_LIMIT = 512;
    static public final int BLOCK_SIZE = 256;
    static final private String TITLE = "Mandelbrot Set Visualization";
    static final private String SPACE_DOMAIN_NAME = "";
    static final private Task TASK = new TaskMandelbrotSet( LOWER_LEFT_X, LOWER_LEFT_Y, EDGE_LENGTH , N_PIXELS, ITERATION_LIMIT, 0, 0 );
          
    public static void main( final String[] args ) throws Exception
    {
        final Job job = new JobMandelbrotSet();
        final JobRunner jobRunner = new JobRunner( job, TITLE, SPACE_DOMAIN_NAME );
        jobRunner.run( TASK );
    }
    
    @Override
    public JLabel view( final ResultValueMandelbrotSet returnValue ) 
    {
        final Integer[][] counts = returnValue.counts();
        final Image image = new BufferedImage( N_PIXELS, N_PIXELS, BufferedImage.TYPE_INT_ARGB );
        final Graphics graphics = image.getGraphics();
        for ( int i = 0; i < counts.length; i++ )
            for ( int j = 0; j < counts.length; j++ )
            {
                graphics.setColor( getColor( counts[i][j] ) );
                graphics.fillRect( i, N_PIXELS - j, 1, 1 );
            }
        final ImageIcon imageIcon = new ImageIcon( image );
        return new JLabel( imageIcon );
    }
    
    private Color getColor( final int iterationCount )
    {
        return iterationCount == ITERATION_LIMIT ? Color.BLACK : Color.WHITE;
    }
}
