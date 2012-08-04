package org.openpixi.pixi.distributed;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.Simulation;

/**
 * Emulates distributed environment on local host.
 * Verifies whether the result of the distributed simulation
 * is the same as the result of non-distributed simulation.
 */
public class EmulatedDistributedEnvironment {

	private Settings settings;

	public EmulatedDistributedEnvironment(Settings settings) {
		this.settings = settings;
	}


	/**
	 * Runs the distributed simulation at once and compares the results at the end.
	 */
	public void runAtOnce() {
		final Node[] nodes = new Node[settings.getNumOfNodes()];
		for (int i = 0; i < settings.getNumOfNodes(); i++) {
			nodes[i] = new Node(settings);
		}

		runRunnables(nodes);

		// Set the master
		Master master = null;
		for (Node n: nodes) {
			if (n.isMaster()) {
				master = n.getMaster();
			}
		}

		// Run non-distributed simulation
		Simulation simulation = new Simulation(settings);
		for (int i = 0; i < settings.getIterations(); ++i) {
			simulation.step();
		}

		// Compare results
		ResultsComparator comparator = new ResultsComparator();
		comparator.compare(
				simulation.particles, master.getFinalParticles(),
				simulation.grid, master.getFinalGrid());
	}


	/**
	 * Runs the distributed and non-distributed simulations in steps
	 * and compares the result after each step.
	 */
	public void runInSteps() {
		final Node[] nodes = new Node[settings.getNumOfNodes()];
		for (int i = 0; i < settings.getNumOfNodes(); i++) {
			nodes[i] = new Node(settings);
		}

		Runnable[] distributeRuns = new Runnable[nodes.length];
		for (int i = 0; i < nodes.length; ++i) {
			final int nodeID = i;
			distributeRuns[nodeID] = new Runnable() {
				public void run() {
					nodes[nodeID].distribute();
				}
			};
		}

		Runnable[] collectRuns = new Runnable[nodes.length];
		for (int i = 0; i < nodes.length; ++i) {
			final int nodeID = i;
			collectRuns[nodeID] = new Runnable() {
				public void run() {
					nodes[nodeID].collect();
				}
			};
		}

		Runnable[] stepRuns = new Runnable[nodes.length];
		for (int i = 0; i < nodes.length; ++i) {
			final int nodeID = i;
			stepRuns[nodeID] = new Runnable() {
				public void run() {
					nodes[nodeID].step();
				}
			};
		}

		runRunnables(distributeRuns);

		// Determine the master
		Master master = null;
		for (Node n: nodes) {
			if (n.isMaster()) {
				master = n.getMaster();
			}
		}

		// Create non-distributed simulation
		Simulation simulation = new Simulation(settings);

		for (int i = 0; i < settings.getIterations(); ++i) {
			simulation.step();
			runRunnables(stepRuns);
			runRunnables(collectRuns);

			ResultsComparator comparator = new ResultsComparator(i);
			comparator.compare(
					simulation.particles, master.getFinalParticles(),
					simulation.grid, master.getFinalGrid());
		}

		Runnable[] closeRuns = new Runnable[nodes.length];
		for (int i = 0; i < nodes.length; ++i) {
			final int nodeID = i;
			closeRuns[nodeID] = new Runnable() {
				public void run() {
					nodes[nodeID].close();
				}
			};
		}

		runRunnables(closeRuns);
	}


	/**
	 * Runs array of runnables simultaneously and waits for them to finish.
	 */
	private void runRunnables(Runnable[] runnables) {
		Thread[] threads = new Thread[runnables.length];
		for (int i = 0; i < runnables.length; ++i) {
			threads[i] = new Thread(runnables[i]);
			threads[i].start();
		}

		for (Thread t: threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}
}
