// NAME: Zhiguo(Frank) Zhang
// ID: 260550226
package comp559.a2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import mintools.parameters.BooleanParameter;
import mintools.parameters.DoubleParameter;
import mintools.parameters.IntParameter;
import mintools.swing.CollapsiblePanel;
import mintools.swing.VerticalFlowPanel;

/**
 * Helper code to build a number of different test systems.  
 * Feel free to add to this if you like!
 * @author kry
 */
public class TestSystems {

    public BooleanParameter runAlphabetFactory = new BooleanParameter( "run alphabet soup factory!", false );
    
    public BooleanParameter runPastaFactory = new BooleanParameter( "run pasta factory!", false );
    
    /**
     * Anything less than 100 is likely to end up with letters being generated over top one another and killing 
     * the simulation
     */
    private IntParameter interval = new IntParameter( "new letter step interval", 130, 100, 200 );

    private IntParameter pastaParticleInterval = new IntParameter( "new pasta particle step interval", 3, 1, 40 );
        
    private DoubleParameter initialVelocity = new DoubleParameter( "initial velocity", 50, -100, 100 );
    
    private BooleanParameter clearFirst = new BooleanParameter( "clear current system before creating new systems", true );
    
    private AlphabetSoupFactory alphabetSoupFactory = new AlphabetSoupFactory();

    private ParticleSystem system;
    
    // for convenience we'll keep a copy of the particles, springs, and leaf springs inside a system,
    // though this is a bit gross    
    
    private List<Particle> particles;
    private List<Spring> springs;
    private List<LeafSpring> leafSprings;
    
    /**
     * Creates a new test system 
     * @param system
     */
    public TestSystems( ParticleSystem system ) {
        this.system = system;
        particles = system.particles;
        springs = system.springs;
        leafSprings = system.leafSprings;
    }
    
    /**
     * Quick and dirty generic test generation button
     * @author kry
     */
    private class TestButton extends JButton implements ActionListener {
        private static final long serialVersionUID = 1L;
        private int testNumber;
        public TestButton( String name, int testNumber ) {
            super( name );
            this.testNumber = testNumber;
            addActionListener( this );
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            createSystem(this.testNumber);
        }
    }
    
    /**
     * Gets the control panel for setting different systems.
     * @return the control panel
     */
    public JPanel getControls() {
        VerticalFlowPanel vfp = new VerticalFlowPanel();
        vfp.setBorder( new TitledBorder("Particle System Test Systems"));

        vfp.add( clearFirst.getControls() );
        
        for ( int i = 0; i < tests.length; i++ ) {            
            vfp.add( new TestButton(tests[i], i) );             
        }
        
        vfp.add( alphabetSoupFactory.getControls() );
                
        VerticalFlowPanel vfp1 = new VerticalFlowPanel();
        vfp1.setBorder( new TitledBorder("Endless Pasta Bowl controls"));
        vfp1.add( runPastaFactory.getControls() );                
        vfp1.add( pastaParticleInterval.getControls() );
        vfp1.add( initialVelocity.getSliderControls(false) );
        CollapsiblePanel cp1 = new CollapsiblePanel(vfp1.getPanel());
        cp1.collapse();
        vfp.add( cp1 );
        
        VerticalFlowPanel vfp0 = new VerticalFlowPanel();
        vfp0.setBorder( new TitledBorder("Alphabet Soup Factory Controls"));
        vfp0.add( runAlphabetFactory.getControls() );
        vfp0.add( interval.getControls() );
        CollapsiblePanel cp0 = new CollapsiblePanel(vfp0.getPanel());
        cp0.collapse();
        vfp.add( cp0 );
        
        CollapsiblePanel cp = new CollapsiblePanel( vfp.getPanel() );
        cp.collapse();
        return cp;   
    }
    
    private void createBox() {
        double h = system.height;
        double w = system.width;
        
        Particle p1 = system.createParticle(     5,     5, 0, 0 ); p1.pinned = true;
        Particle p2 = system.createParticle(     5, h - 5, 0, 0 ); p2.pinned = true;
        Particle p3 = system.createParticle( w - 5, h - 5, 0, 0 ); p3.pinned = true;
        Particle p4 = system.createParticle( w - 5,     5, 0, 0 ); p4.pinned = true;
        
        system.createSpring(p1, p2);
        system.createSpring(p2, p3);
        system.createSpring(p3, p4);
        
    }
    
    public String[] tests = {            
            "box",
            "letters",
            "pendulum pair",
            "zig zag",
            "interwoven hairs hard",
            "interwoven hairs easy",
            "triangle truss",
            "vertical chain",
            "pinball"
    };
    
    /**
     * Creates one of a number of simple test systems.
     *
     * Small systems are more useful for debugging!
     * 
     * @param which
     */
    public void createSystem( int which ) {
        if ( clearFirst.getValue() ) {
        	system.clear();
        }
        
        if (which == 0 ) {	// box
            createBox();
        } else  if ( which == 1) {	// letters            
            alphabetSoupFactory.createLetter(system);  
            system.name = tests[which];

        } else if ( which == 2) {	// pendulum pair
            Particle p1 = new Particle( 320, 100, 0, 0 );
            Particle p2 = new Particle( 520, 100, 0, 0 );
            particles.add( p1 );
            particles.add( p2 );
            p1.pinned = true;
            springs.add( new Spring( p1, p2 ) );
            
            Particle p3 = new Particle( 300, 150, 0, 0 );
            Particle p4 = new Particle( 260, 150, 0, 0 );
            particles.add( p3 );
            particles.add( p4 );
            p3.pinned = true;
            springs.add( new Spring( p3, p4 ) );
            system.name = tests[which];
            
        } else if ( which == 3 ) {	// zig zag
            int N = 20;
            int xpos = 100;
            Particle p0, p1, p2;
            
            p0 = null;
            p1 = null;
            p2 = null;            
            for ( int i = 0; i < N; i++ ) {               
                p2 = new Particle( xpos, 100 + 20*(i%2), 0, 0 );                
                particles.add( p2 );
                if ( i < 2 ) p2.pinned = true;
                if ( p1 != null ) springs.add( new Spring( p1, p2 ) );
                if ( p0 != null ) leafSprings.add( new LeafSpring( p0, p1, p2 ) );
                p0 = p1;
                p1 = p2;                
                xpos += 20;
            }
            system.name = tests[which];
        } else if ( which == 4 ) {	// hairs (hard)
            int N = 20;
            Particle p0, p1, p2;
            
            int M = 5;
            int offset = 10;
            
            for ( int j = 0; j < M; j++ ) {
                int xpos = 100;
                p0 = null;
                p1 = null;
                p2 = null;            
                for ( int i = 0; i < N; i++ ) {               
                    p2 = new Particle( xpos, 100 + offset*2*j, 0, 0 );                
                    particles.add( p2 );
                    if ( i < 2 ) p2.pinned = true;
                    if ( p1 != null ) springs.add( new Spring( p1, p2 ) );
                    if ( p0 != null ) leafSprings.add( new LeafSpring( p0, p1, p2 ) );
                    p0 = p1;
                    p1 = p2;                
                    xpos += 20;
                }
                xpos = 100 + (N-2) * 20 ;
                p0 = null;
                p1 = null;
                p2 = null;            
                for ( int i = 0; i < N; i++ ) {               
                    p2 = new Particle( xpos, 100 + offset*2*j + offset, 0, 0 );
                    particles.add( p2 );
                    if ( i < 2 ) p2.pinned = true;
                    if ( p1 != null ) springs.add( new Spring( p1, p2 ) );
                    if ( p0 != null ) leafSprings.add( new LeafSpring( p0, p1, p2 ) );
                    p0 = p1;
                    p1 = p2;                
                    xpos -= 20;
                }
            }
            system.name = tests[which];
        } else if ( which == 5 ) {		// hairs (easy)
            int N = 20;
            Particle p0, p1, p2;
            
            int M = 5;
            int offset = 15;
            
            for ( int j = 0; j < M; j++ ) {
                int xpos = 100;
                p0 = null;
                p1 = null;
                p2 = null;            
                for ( int i = 0; i < N; i++ ) {               
                    p2 = new Particle( xpos, 100 + offset*2*j, 0, 0 );                
                    particles.add( p2 );
                    if ( i < 2 ) p2.pinned = true;
                    if ( p1 != null ) springs.add( new Spring( p1, p2 ) );
                    if ( p0 != null ) leafSprings.add( new LeafSpring( p0, p1, p2 ) );
                    p0 = p1;
                    p1 = p2;                
                    xpos += 20;
                }
                xpos = 100 + (N-2) * 20 ;
                p0 = null;
                p1 = null;
                p2 = null;            
                for ( int i = 0; i < N; i++ ) {               
                    p2 = new Particle( xpos, 100 + offset*2*j + offset, 0, 0 );
                    particles.add( p2 );
                    if ( i < 2 ) p2.pinned = true;
                    if ( p1 != null ) springs.add( new Spring( p1, p2 ) );
                    if ( p0 != null ) leafSprings.add( new LeafSpring( p0, p1, p2 ) );
                    p0 = p1;
                    p1 = p2;                
                    xpos -= 20;
                }
            }
            system.name = tests[which];
        } else if ( which == 6 ) {	// truss
            Point2d p = new Point2d(100, 100);
            Vector2d d = new Vector2d(20, 0);
            Particle p1, p2, p3, p4;
            p1 = new Particle(p.x - d.y, p.y + d.x, 0, 0);
            particles.add(p1);
            p2 = new Particle(p.x + d.y, p.y - d.x, 0, 0);
            particles.add(p2);
            springs.add(new Spring(p1, p2));
            p1.pinned = true;
            p2.pinned = true;
            p.add(d);
            p.add(d);
            int N = 10;
            for (int i = 1; i < N; i++) {
                // d.set( 20*Math.cos(i*Math.PI/N), 20*Math.sin(i*Math.PI/N) );
                p3 = new Particle(p.x - d.y, p.y + d.x, 0, 0);
                p4 = new Particle(p.x + d.y, p.y - d.x, 0, 0);
                particles.add(p3);
                particles.add(p4);
                springs.add(new Spring(p3, p1));
                //springs.add(new Spring(p3, p2));
                springs.add(new Spring(p4, p1));
                springs.add(new Spring(p4, p2));
                springs.add(new Spring(p4, p3));
                p1 = p3;
                p2 = p4;

                p.add(d);
                p.add(d);
            }
            system.name = tests[which];
        } else if ( which == 7 ) {	// vertical chain
            int ypos = 100;
            Particle p1, p2;
            p1 = new Particle( 320, ypos, 0, 0 );
            p1.pinned = true;
            particles.add( p1 );
            int N = 10;
            for ( int i = 0; i < N; i++ ) {
                ypos += 20;
                p2 = new Particle( 320, ypos, 0, 0 );
                particles.add( p2 );
                springs.add( new Spring( p1, p2 ) );
                p1 = p2;
            }
            system.name = tests[which];
        } else if ( which == 8 ) { // pinball
        	double h = system.height;
            double w = system.width;
            
            Particle p1 = system.createParticle(     10,     10, 0, 0 ); p1.pinned = true;
            Particle p2 = system.createParticle(     10, h - 20, 0, 0 ); p2.pinned = true;
            Particle p3 = system.createParticle(     20, h - 10, 0, 0 ); p3.pinned = true;
            Particle p4 = system.createParticle( w - 20, h - 10, 0, 0 ); p4.pinned = true;
            Particle p5 = system.createParticle( w - 10, h - 20, 0, 0 ); p5.pinned = true;
            Particle p6 = system.createParticle( w - 10,     10, 0, 0 ); p6.pinned = true;
            
            system.createSpring(p1, p2);
            system.createSpring(p2, p3);
            system.createSpring(p3, p4);
            system.createSpring(p4, p5);
            system.createSpring(p5, p6);
            system.createSpring(p6, p1);
            system.createParticle(  w - 15,  h - 15, 0, -200);
            system.name = tests[which];
        }
    }
    
    /**
     * reset the factory so that it always starts with the first letter
     */
    public void resetFactory() {
        stepCount = 0;
        letterIndex = 0;
        prevParticle0 = null;
        prevParticle1 = null;
    }
    
    Particle prevParticle0 = null;
    Particle prevParticle1 = null;
    
    private int stepCount = 0;
    
    public final String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    public int letterIndex = 0;
    
    /**
     * Generates letters at regular intervals if the factory is turned on.
     */
    public void step() {
        if ( ! runAlphabetFactory.getValue() && ! runPastaFactory.getValue() ) return;
            
        stepCount++;
        
        if ( runAlphabetFactory.getValue() ) {
            if (stepCount % interval.getValue() == 1) {
                alphabetSoupFactory.createLetter(system, "" + alphabet.charAt(letterIndex++%alphabet.length()), 50, -20 );
            }
        }
        
        if ( runPastaFactory.getValue() ) {
            if ( stepCount % pastaParticleInterval.getValue() == 1 ) {
                Particle p = new Particle( 100, 30, initialVelocity.getValue(), Math.sin(stepCount/20.0)* initialVelocity.getValue() );
                particles.add( p );
                if ( prevParticle1 != null ) {
                    Spring s = new Spring( prevParticle1, p );
                    s.setRestLength();
                    springs.add( s );
                }
                if ( prevParticle0 != null && prevParticle1 != null ) {
                    LeafSpring ls = new LeafSpring(prevParticle0,prevParticle1,p);
                    ls.rest = 0;
                    leafSprings.add( ls );
                }
                prevParticle0 = prevParticle1;
                prevParticle1 = p;
            }
        }
        
    }
    
}
