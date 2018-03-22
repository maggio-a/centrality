/*
 * Peer-to-Peer Systems 2015/2016
 * 
 * Final project source code
 * 
 * Author: Andrea Maggiordomo - mggndr89@gmail.com
 */
package centrality;

import peersim.config.Configuration;
import peersim.core.Control;


public class CentralitySimulation implements Control {
	
	/**
	 * If this parameter is present, the control will print information
	 * about the number of messages generated during the simulation.
	 */
	public static final String PAR_LOG_MESSAGES = "logMessages";
	
	private static int totm = 0;
	private static int stepm = 0;
	
	public static void newMessage() { stepm++; }
	public static void resetStaticFields() { totm = 0; stepm = 0; }
	
	private int step;
	private boolean log;
	
	
	public CentralitySimulation(String prefix) {
		resetStaticFields();
		step = 0;
		log = Configuration.contains(prefix + "." + PAR_LOG_MESSAGES);
	}

	@Override
	public boolean execute() {
		totm += stepm;
		if (log) {
			System.err.println("Simulation at step " + step);
			System.err.println("Number of messages sent at this step: " + stepm);
			System.err.println("Total number of messages sent: " + totm);
		}
		// Stop the simulation if no messages are sent during a communication window
		if (step++ > 0 && stepm == 0) // Only after the first step
			return true;
		else {
			stepm = 0;
			return false;
		}
	}

}
