// NAME: Zhiguo(Frank) Zhang
// ID: 260550226
package comp559.a2;

import java.util.LinkedList;
import java.util.List;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;
import javax.vecmath.Vector2d;

import mintools.parameters.BooleanParameter;
import mintools.parameters.IntParameter;
import mintools.parameters.DoubleParameter;
import mintools.swing.CollapsiblePanel;
import mintools.swing.VerticalFlowPanel;
import mintools.viewer.SceneGraphNode;

/**
 * Implementation of a simple particle system.
 * 
 * Note that the particle system implements Function, that is, it evaluates
 * its derivatives to return to the step method which is called by implementations
 * of the Integrator interface.
 * 
 * Note also that it is actually the updateParticles method in this class which 
 * should be calling the Integrator step method! 
 * 
 * @author kry
 */
public class ParticleSystem implements SceneGraphNode {
    
    /** the particle list */
    public List<Particle> particles = new LinkedList<Particle>();
    
    /** the spring list (treat this as the edge list for geometry */
    public List<Spring> springs = new LinkedList<Spring>();
    
    /** leaf springs connect 3 particles with a hinge like spring */
    public List<LeafSpring> leafSprings = new LinkedList<LeafSpring>();
    
    public String name = "";
    
    /**
     * Creates an empty particle system
     */
    public ParticleSystem() {
        // creates an empty system!
    }
    
    /**
     * Resets the positions of all particles to their initial states
     */
    public void resetParticles() {
        for ( Particle p : particles ) {
            p.reset();
        }
        time = 0;
    }
    
    /**
     * Deletes all particles, and as such removes all springs too.
     */
    public void clear() {        
        particles.clear();
        springs.clear();
        leafSprings.clear();
        name = "";
    }    
    
    public double time = 0;
    
    private RobustCCD robustCCD = new RobustCCD();
    
    /**
     * Advances time and updates the position of all particles
     * @param h 
     * @return true if update was successful
     */
    public boolean updateParticles( double h ) {
        boolean resultOK = true;
        // set the global spring properties
        Spring.k = k.getValue();
        Spring.b = b.getValue();
        LeafSpring.k = kls.getValue();
                
        // first compute all forces
        Vector2d tmp = new Vector2d();
        
        double damping  = c.getValue();
        for ( Particle p : particles ) {
            p.f.set( 0, useg.getValue() ? g.getValue() : 0 );
            tmp.scale( -damping, p.v );
            p.f.add( tmp );                        
        }
        
        for ( Spring s : springs ) {
            s.apply();
        }
        for ( LeafSpring ls : leafSprings ) {
            ls.apply();
        }
        
        // Update the velocity of the particles as per symplectic Euler
        for ( Particle p : particles ) {
            if ( p.pinned ) {            
                p.f.set(0,0); // just to make sure!
                p.v.set(0,0);
            } else {
                tmp.scale( h / p.mass, p.f );
                p.v.add( tmp );            
            }
        }
        
        // perform robust collision detection here.  
        // note the calls have been made to the robustCCD class for you
        // so you should focus your efforts there!
        
        robustCCD.restitution = restitution.getValue();
        robustCCD.MAX_ITERATION = iterations.getValue();
        robustCCD.H = H.getValue();
        if ( repulsion.getValue() ) {
            robustCCD.applyRepulsion( h, this );            
        }
        if ( collision.getValue() ) {
            if ( ! robustCCD.check( h, this ) ) {
                resultOK = false;
            }
        }
        
        // Finally, update the positions using the velocity at the next time step
        for ( Particle p : particles ) {
            if ( p.pinned ) continue;
            // symplectic Euler 
            tmp.scale( h, p.v );
            p.p.add( tmp );
            p.f.set(0,0);
        }
                        
        time = time + h;
        return resultOK;
    }

    /**
     * Creates a new particle and adds it to the system
     * @param x
     * @param y
     * @param vx
     * @param vy
     * @return the new particle
     */
    public Particle createParticle( double x, double y, double vx, double vy ) {
        Particle p = new Particle( x, y, vx, vy );
        particles.add( p );
        return p;
    }
    
    /**
     * Creates a new spring between two particles and adds it to the system.
     * @param p1
     * @param p2
     * @return the new spring
     */
    public Spring createSpring( Particle p1, Particle p2 ) {
        Spring s = new Spring( p1, p2 ); 
        springs.add( s );         
        return s;
    }
    
    @Override
    public void init(GLAutoDrawable drawable) {
        // do nothing
    }

    /**
     * Height of the canvas, useful for wall collisions
     */
    public int height;
    
    /**
     * Width of the canvas, useful for wall collisions
     */
    public int width;

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        // We'll keep track of the width and the height 
        // of the drawable as this might be useful for 
        // processing collisions with walls
        height = drawable.getSurfaceHeight();
        width = drawable.getSurfaceWidth();
        
        if ( drawParticles.getValue() ) {
            gl.glPointSize( pointSize.getFloatValue() );
            gl.glBegin( GL.GL_POINTS );
            for ( Particle p : particles ) {
                // transparency is used to get smooth edges on the particles
                double alpha = 0.5;
                if ( p.pinned ) {
                    gl.glColor4d( 1, 0, 0, alpha );
                } else {
                    gl.glColor4d( 0, 0.95,0, alpha );
                }
                gl.glVertex2d( p.p.x, p.p.y );
            }
            gl.glEnd();
        }
        
        gl.glColor4d( 0, 0.5, 0.5, 0.5 );
        gl.glLineWidth( 2 );
        gl.glBegin( GL.GL_LINES );
        for (Spring s : springs) {
            gl.glVertex2d( s.p1.p.x, s.p1.p.y );
            gl.glVertex2d( s.p2.p.x, s.p2.p.y );
        }
        gl.glEnd();
    }    
    
    private BooleanParameter drawParticles = new BooleanParameter( "draw Particles", true ) ;
    
    private DoubleParameter pointSize = new DoubleParameter("point size", 5, 1, 10);
    
    private BooleanParameter useg = new BooleanParameter( "use gravity", true );
    
    private DoubleParameter g = new DoubleParameter( "gravity", 9.8, 0.01, 1000 );
    
    private DoubleParameter k = new DoubleParameter( "spring stiffness", 100,0.01, 100000 );
    
    private DoubleParameter kls = new DoubleParameter( "leaf spring stiffness", 10000, 1000, 1e5 );
    
    private DoubleParameter b = new DoubleParameter( "spring damping", 1, 0, 10 );
    
    private DoubleParameter c = new DoubleParameter( "viscous damping", .01, 0, 10 );

    private DoubleParameter restitution = new DoubleParameter( "restitution", .0001, 0, 1 );
    
    private DoubleParameter H = new DoubleParameter( "min distance (H)", 2, 0.1, 10 );
    
    private IntParameter iterations = new IntParameter( "iterations for collision", 100, 100, 1000);

    private JTextArea comments = new JTextArea("<comments>");
        
    private BooleanParameter showCommentsAndParameters = new BooleanParameter("show comments and parameters", true );
    
    private BooleanParameter repulsion = new BooleanParameter( "apply repulsion impulses", true );
    
    private BooleanParameter collision = new BooleanParameter( "apply collision impulses", true );
        
    public JPanel getControls() {
        VerticalFlowPanel vfp = new VerticalFlowPanel();
        
        VerticalFlowPanel vfp0 = new VerticalFlowPanel();
        vfp0.setBorder( new TitledBorder("Viewing Parameters" ) );
        vfp0.add( drawParticles.getControls() );
        vfp0.add( pointSize.getSliderControls(false) );
        vfp0.add( comments );
        vfp0.add( showCommentsAndParameters.getControls() );
        CollapsiblePanel cp0 = new CollapsiblePanel( vfp0.getPanel() );
        cp0.collapse();
        vfp.add( cp0 );
        
        VerticalFlowPanel vfp1 = new VerticalFlowPanel();
        vfp1.setBorder( new TitledBorder("Simulation Parameters"));
        vfp1.add( repulsion.getControls() );
        vfp1.add( collision.getControls() );
        vfp1.add( useg.getControls() );
        vfp1.add( g.getSliderControls(true) );
        vfp1.add( kls.getSliderControls(true) );
        vfp1.add( k.getSliderControls(true) );
        vfp1.add( b.getSliderControls(false) );
        vfp1.add( c.getSliderControls(false) );        
        vfp1.add( restitution.getSliderControls(false) );
        vfp1.add( H.getSliderControls(false) );
        vfp1.add( iterations.getSliderControls());
        CollapsiblePanel cp1 = new CollapsiblePanel( vfp1.getPanel() );
        cp1.collapse();
        vfp.add( cp1 );
        
        return vfp.getPanel();        
    }
    
    @Override
    public String toString() {
        String s = "particles = " + particles.size() + " time = " + time;
        if ( showCommentsAndParameters.getValue() ) {
            s += "\n" + comments.getText() + "\n" + 
               "stiffness = " + k.getValue() + "\n" +               
               "spring damping = " + b.getValue() + "\n" +
               "viscous damping = " + c.getValue() + "\n" +
               "bending stiffness = " + kls.getValue() + "\n" +
               "restitution = " + restitution.getValue() + "\n"+ 
               "H = " + H.getValue() + "\n" +
               "iterations = " + iterations.getValue() + "\n"; 
        }
        return s;
    }
    
    public double[] pack()
    {
    	double[] state = new double[6*particles.size()];
    	int i = 0;
    	for(Particle p : particles) {
    		state[i++] = p.p.x;
    		state[i++] = p.p.y;
    		state[i++] = p.v.x;
    		state[i++] = p.v.y;
    		state[i++] = p.f.x;
    		state[i++] = p.f.y;
    	}
    	return state;
    }
 
}
