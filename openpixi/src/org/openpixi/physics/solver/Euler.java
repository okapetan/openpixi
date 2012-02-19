/*this is a method, that  is based on the simple Euler-Richardson algorithm (it represents a neat way of finding the
* numerical solutions of a differential equation, based on the Euler algorithm),
*  where I have used the following literature:
* http://www.physics.udel.edu/~bnikolic/teaching/phys660/numerical_ode/node4.html
* I shall use this method together with the class Force.java to get some graphical results*/

package org.openpixi.physics.solver;

import org.openpixi.physics.*;

public class Euler {
	public static void algorithm(Particle2D particle, Force f, double step)
	{
		particle.setAccelerationX(f.getForceX(particle.vx, particle.vy, particle) / particle.getMass());
		particle.setAccelerationY(f.getForceX(particle.vx, particle.vy, particle) / particle.getMass());
		
		//if the particle hits the walls
		if(particle.x < 0)
		{
			particle.x = 0;
			particle.vx = - particle.vx;
		}
		if(particle.x > particle.rightBoundary)
		{
			particle.x = particle.rightBoundary;
			particle.vx = - particle.vx;
		}
		if(particle.y < 0)
		{
			particle.y = 0;
			particle.vy = - particle.vy;
		}
		if(particle.y > particle.bottomBoundary)
		{
			particle.y = particle.bottomBoundary;
			particle.vy = - particle.vy;
		}
		//starting the Euler-Richardson algorithm (the equations correspond with the ones on the above mentioned website)
		double vxmiddle = particle.vx + particle.getAccelerationX() * step / 2;
		double vymiddle = particle.vy + particle.getAccelerationY() * step / 2;
		
		//double xmiddle = x + vx * step / 2;    actually, this two equations are not needed, but I've written them
		//double ymiddle = y + vy * step / 2;    so the algorithm is complete
		
		double axmiddle = f.getForceX(vxmiddle, vymiddle, particle) / particle.getMass();
		double aymiddle = f.getForceY(vxmiddle, vymiddle, particle) / particle.getMass();
		
		particle.vx += axmiddle * step;
		particle.vy += aymiddle * step;
		
		particle.x += vxmiddle * step;
		particle.y += vymiddle * step;
		
		particle.setAccelerationX(f.getForceX(particle.vx, particle.vy, particle) / particle.getMass());
		particle.setAccelerationY(f.getForceY(particle.vx, particle.vy, particle) / particle.getMass());
	}

}