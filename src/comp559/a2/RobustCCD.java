// NAME: Zhiguo(Frank) Zhang
// ID: 260550226
package comp559.a2;

import javax.vecmath.Vector2d;

/**
 * Implementation of a robust collision detection.
 * @author kry
 */
public class RobustCCD {

    double restitution;
    
    double H;
    
    int MAX_ITERATION;
    
    /**
     * Creates the new continuous collision detection and response object
     */
    public RobustCCD() {
        // do nothing
    }
    
    /**
     * Try to deal with contacts before they happen
     * @param h
     * @param system
     */
    public void applyRepulsion( double h, ParticleSystem system ) {
        
        // TODO: OBJECTIVE 4 elastic repulsion forces
        
        // check for point-edge proximity between all particle-edge pairs
        // find the normal
        // take care to deal with segment end points carefully
        // compute an appropriate  impulse if the distance is less than H
        // make sure your impulse is in the correct direction!
        // don't apply the impulse if the relative velocity is separating fast enough (see Bridson et al. 2002)
        // distribute impulse to the three particles involved in the appropriate manner
    	for (Particle p : system.particles) {
    		for (Spring s : system.springs) {
    			if (needRepulsion(p,s)) {
    				applyForce(p, s, h);
    			}
    		}
    	}
    }
    
    /**
     * Apply repulsion force for a given particle, spring and time step h
     * @param p Particle
     * @param s Spring
     * @param h Time step
     */
    public void applyForce(Particle p, Spring s, double h) {
    	// initialize necessary quantities
    	double p1InvMass = (s.p1.pinned)? 0 : 1.0/s.p1.mass;
		double p2InvMass = (s.p2.pinned)? 0 : 1.0/s.p2.mass;
		double pInvMass = (p.pinned)? 0 : 1.0/p.mass;
    	double p1x = s.p1.p.x;
		double p1y = s.p1.p.y;
		double p3x = p.p.x;
		double p3y = p.p.y;
		
		// create vectors and alpha needed in computing relative velocity in normal direction before collision
		Vector2d ac = new Vector2d(p3x - p1x, p3y - p1y);
		double alpha = computeAlpha(p, s, 0);
		Vector2d n = computeNormal(p, s, 0);
    	// make sure normal is always pointing to the particle
		if (n.dot(ac) < 0) n.negate();
		
		// vector of relative velocity before collision
		Vector2d pdot = new Vector2d(  p.v.x - ((1-alpha)*s.p1.v.x + alpha*s.p2.v.x),  p.v.y - ((1-alpha)*s.p1.v.y + alpha*s.p2.v.y));
		// relative velocity in normal direction
		double vrel_neg = n.dot(pdot);
		// compute d described in Bridson Cloth 2002
		double d = n.dot(ac) - H;
		if (vrel_neg < (0.1 * d) / h) {
			// compute repulsion impulse
			double Ir = - Math.min(h * Spring.k * d, p.mass * (0.1 * d / h - vrel_neg));
			// apply repulsion to 3 particles
			double scale = - Ir * (1 - alpha) * p1InvMass;
			s.p1.v.scaleAdd(scale, n, s.p1.v);
			scale = - Ir * alpha * p2InvMass;
			s.p2.v.scaleAdd(scale, n, s.p2.v);
			scale = Ir * pInvMass;
			p.v.scaleAdd(scale, n, p.v);
		}
    }
    /**
     * Check if repulsion is needed for a spring and particle
     * @param p Particle
     * @param s Spring
     * @return whether repulsion is needed
     */
    public boolean needRepulsion(Particle p, Spring s) {
    	// initialize necessary quantities
    	double p1x = s.p1.p.x;
		double p1y = s.p1.p.y;
		double p2x = s.p2.p.x;
		double p2y = s.p2.p.y;
		double p3x = p.p.x;
		double p3y = p.p.y;
		
		// create vectors needed
		Vector2d ab = new Vector2d(p2x - p1x, p2y - p1y);
		Vector2d ac = new Vector2d(p3x - p1x, p3y - p1y);
		Vector2d bc = new Vector2d(p3x - p2x, p3y - p2y);
		Vector2d n = computeNormal(p,s,0);
		
		// return true if the particle p is within the band of 2H along spring s.
		return (n.dot(ac) >= -H && n.dot(ac) <= H) && (ac.length() < ab.length() + H && bc.length() < ab.length() + H);
    }
    
    /**
     * Checks all collisions in interval t to t+h
     * @param h
     * @param system 
     * @return true if all collisions resolved
     */
    public boolean check( double h, ParticleSystem system ) {        

        // For each particle-edge pair, find the roots for when the three particles are
        // co-linear, and then pick the first root on (0,h] which corresponds to an 
    	// actual collision.  Compute a collision response.  That is, compute an appropriate
    	// collision normal, compute the impulse, and then apply the impulse to the associated
    	// particles.  Be sure to deal with pinning constraints!  Repeat until all collisions
    	// are resolved and it is safe to advance time
       
     	// You probably want to write other methods to help with the job, and
     	// call them here.  Or alternatively you can write one large and ugly
     	// monolithic function here.
  
        // TODO: OBJECTIVE 1 continuous collision detection    	
    	// TODO: OBJECTIVE 2 compute collision impulses
    	// TODO: OBJECTIVE 3 iterative to resolve all collisions
		return iterativeCheck(h, system, 0);
    }
    
    /**
     * Iteratively resolve all collisions, give up if MAX_ITERATION is reached
     * @param h Time step
     * @param system Particle-spring system
     * @param iter current iteration
     * @return true if all collisions are resolved, false if give up
     */
    private boolean iterativeCheck(double h, ParticleSystem system, int iter) {
		if (iter > MAX_ITERATION) return false; // give up
		
    	boolean collision = false;
    	for (Particle p : system.particles) {
    		for (Spring s : system.springs) {
    			if (p != s.p1 && p != s.p2) {
    				/*
    				 * solve quadratic equation in CCD (OBJECTIVE 1)
    				 * t[0] will be positive if there is a collision in (0, h + epsilon]
    				 * t[0] will be negative or zero otherwise
    				 */
	    			double[] t = solve(p, s, h);
	    			if (t[0] > 0 ) {
	    				// Update system with velocity after collision
	    				Vector2d n = computeNormal(p, s, t[0]);
	    				double j = computeImpulse(p, s, t);
	    				// first particle on spring
	    				double p1InvMass = (s.p1.pinned)? 0 : 1.0/s.p1.mass;
	    				double scale = j * (1 - t[1]) * p1InvMass;
	    				n.scale(scale);
	    				s.p1.v.add(n);
	    				// second particle on spring
	    				n = computeNormal(p, s, t[0]);
	    				double p2InvMass = (s.p2.pinned)? 0 : 1.0/s.p2.mass;
	    				scale = j * t[1] * p2InvMass;
	    				n.scale(scale);
	    				s.p2.v.add(n);
	    				// third particle which is independent of spring
	    				n = computeNormal(p, s, t[0]);
	    				double pInvMass = (p.pinned)? 0 : 1.0/p.mass;
	    				scale = - j * pInvMass;
	    				n.scale(scale);
	    				p.v.add(n);
	    				collision = true;
	    			}
    			}
    		}
    	}
    	if (collision) return iterativeCheck(h, system, iter+1); // Might be more collision!
    	else return true; // No more collision found!
    }
    /**
     * Solve the quadratic equation and check if the roots are in range
     * @param p Particle
     * @param s Spring
     * @param h Time step
     * @return t[] an array of {t,alpha}
     */
    private double[] solve(Particle p, Spring s, double h) {
    	double[] result = new double[2];
    	// Maple code for quadratic equations
		double p1x = s.p1.p.x;
		double p1y = s.p1.p.y;
		double p2x = s.p2.p.x;
		double p2y = s.p2.p.y;
		double p3x = p.p.x;
		double p3y = p.p.y;
		double v1x = s.p1.v.x;
		double v1y = s.p1.v.y;
		double v2x = s.p2.v.x;
		double v2y = s.p2.v.y;
		double v3x = p.v.x;
		double v3y = p.v.y;
		double cg = (v2y - v3y) * v1x + (-v2x + v3x) * v1y + v2x * v3y - v2y * v3x;
		double cg0 = (v2y - v3y) * p1x + (-v2x + v3x) * p1y + (v3y - v1y) * p2x + (-v3x + v1x) * p2y + (-v2y + v1y) * p3x + p3y * (v2x - v1x);
		double cg1 = (p2y - p3y) * p1x + (-p2x + p3x) * p1y + p2x * p3y - p2y * p3x;
		
		double t1, t2;
		// Quadratic formula
		if (cg != 0) {
			t1 = (- cg0 - Math.sqrt(cg0 * cg0 - 4 * cg * cg1)) / (2 * cg);
			t2 = (- cg0 + Math.sqrt(cg0 * cg0 - 4 * cg * cg1)) / (2 * cg);
		}
		// Linear formula
		else {
			t1 = - cg1 / cg0;
			t2 = -1.0;
		}
		/*
		 * Check t1 and t2 whether they happen in (0, h + epsilon] range
		 * if so, return the first collision
		 * else, return an array with t[0] being negative
		 */
		boolean t1ok = t1 > 0 && t1 <= h + 1e-6;
		boolean t2ok = t2 > 0 && t2 <= h + 1e-6;
		if (!t1ok && t2ok ) {
			double alpha = computeAlpha(p,s,t2);
			if (alpha > 1 + 1e-6 || alpha < 0 - 1e-6) {
				result[0] = -1.0;
				result[1] = -1.0;
				return result;
			}
			else {
				result[0] = t2;
				result[1] = alpha;
				return result;
			}
		} else if (( t1ok && !t2ok)) {
			double alpha = computeAlpha(p,s,t1);
			if (alpha > 1 + 1e-6 || alpha < 0 - 1e-6) {
				result[0] = -1.0;
				result[1] = -1.0;
				return result;
			}
			else {
				result[0] = t1;
				result[1] = alpha;
				return result;
			}
		} else if ( t1ok && t2ok ) {
			double alpha1 = computeAlpha(p,s,t1);
			double alpha2 = computeAlpha(p,s,t2);
			if (alpha1 > 1 + 1e-6 || alpha1 < 0 - 1e-6) {
				if (alpha2 > 1 + 1e-6 || alpha2 < 0 - 1e-6) {
					result[0] = -1.0;
					result[1] = -1.0;
					return result;
				}
				else {
					result[0] = t2;
					result[1] = alpha2;
					return result;
				}
			}
			else {
				result[0] = t1;
				result[1] = alpha1;
				return result;
			}
		} else {
			result[0] = -1.0;
			result[1] = -1.0;
			return result;
		}
		
    }
    /**
     * Solve C = (1 - alpha) * A + alpha * B for alpha
     * Here, we use dot product method to avoid vertical or horizontal springs!
     * This is neat and robust! It can also solve alpha even if 3 particles are not co-linear
     * @param p Particle
     * @param s Spring
     * @param t Time at collision
     * @return alpha
     */
    private double computeAlpha(Particle p, Spring s, double t) {
    	// initialize necessary quantities
    	double p1x = s.p1.p.x + s.p1.v.x * t;
		double p1y = s.p1.p.y + s.p1.v.y * t;
		double p2x = s.p2.p.x + s.p2.v.x * t;
		double p2y = s.p2.p.y + s.p2.v.y * t;
		double p3x = p.p.x + p.v.x * t;
		double p3y = p.p.y + p.v.y * t;
		
		Vector2d ab = new Vector2d(p2x - p1x, p2y - p1y);
		Vector2d ac = new Vector2d(p3x - p1x, p3y - p1y);
		// using the idea of projection to compute alpha
		return ab.dot(ac)/(ab.length() * ab.length());
    }
    
    /**
     * Compute normalized normal vector (n hat) given the spring
     * @param p Particle (not used, put it here in case we want to add extra functionalities)
     * @param s Spring
     * @param t Time when we compute the normal
     * @return normal vector
     */
    private Vector2d computeNormal(Particle p, Spring s, double t) {
    	double p1x = s.p1.p.x + s.p1.v.x * t;
		double p1y = s.p1.p.y + s.p1.v.y * t;
		double p2x = s.p2.p.x + s.p2.v.x * t;
		double p2y = s.p2.p.y + s.p2.v.y * t;
		
		Vector2d n = new Vector2d( (p2y - p1y), - (p2x - p1x));
		n.normalize();
		return n;
    }
    
    /**
     * Compute the impulse j caused by collision
     * @param p particle
     * @param s spring
     * @param t time array for collision
     * @return
     */
    private double computeImpulse(Particle p, Spring s, double[] t) {
    	// compute relative velocity vector and normal vector at collision
    	Vector2d pdot = new Vector2d((1-t[1])*s.p1.v.x + t[1]*s.p2.v.x - p.v.x, (1-t[1])*s.p1.v.y + t[1]*s.p2.v.y - p.v.y);
		Vector2d n = computeNormal(p,s,t[0]);
		// compute relative velocity in normal direction
		double vrel_neg = n.dot(pdot);
		// handle pinned particles
		double p1InvMass = (s.p1.pinned)? 0 : 1.0/s.p1.mass;
		double p2InvMass = (s.p2.pinned)? 0 : 1.0/s.p2.mass;
		double pInvMass = (p.pinned)? 0 : 1.0/p.mass;
		// compute impulse
		double j = - ( 1 + restitution ) * vrel_neg / ((1 - t[1]) * (1 - t[1]) * p1InvMass + t[1] * t[1] * p2InvMass + pInvMass);
    	return j;
    }
}
