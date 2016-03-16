/*
 * Peer-to-Peer Systems 2015/2016
 * 
 * Final project source code
 * 
 * Author: Andrea Maggiordomo - mggndr89@gmail.com
 */
package centrality;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import peersim.config.Configuration;
import peersim.config.IllegalParameterException;
import peersim.core.Control;
import peersim.core.Node;


public class MultiBFSControl implements Control {
	
	private static final String PAR_PROTOCOL = "protocol";
	
	/**
	 * The maximum number of active sources. Useful to limit the memory requirements of
	 * a simulation at the price of increasing the number of cycles needed to complete.
	 */
	private static final String PAR_DEGREE = "degree";
	
	private int protocolID;
	private int degree;
	
	private static Iterator<Node> sourcesIterator;
	private static List<Node> visiting;
	
	private static void resetStaticFields() {
		sourcesIterator = null;
		visiting = new LinkedList<Node>();
	}
	
	static { 
		resetStaticFields();
	}
	
	public MultiBFSControl(String prefix) {
		protocolID = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
		degree = Configuration.getInt(prefix + "." + PAR_DEGREE);
		if (degree <= 0)
			throw new IllegalParameterException(prefix + "." + PAR_DEGREE, "only positive values allowed");
	}

	@Override
	public boolean execute() {
		if (sourcesIterator == null) {
			sourcesIterator = MultiBFSInitializer.getSources().iterator();
		}
		Iterator<Node> it = visiting.iterator();
		while (it.hasNext()) {
			Node n = it.next();
			MultiBFS mbfs = (MultiBFS) n.getProtocol(protocolID);
			if (mbfs.isCompleted(n)) it.remove();
		}
		
		while (visiting.size() < degree && sourcesIterator.hasNext()) {
			Node s = sourcesIterator.next();
			MultiBFS mbfs = (MultiBFS) s.getProtocol(protocolID);
			mbfs.startAccumulation(s, protocolID);
			visiting.add(s);
		}
		int counter = 0;
		Set<Node> sources = MultiBFSInitializer.getSources();
		for (Node s : sources) {
			MultiBFS mbfs = (MultiBFS) s.getProtocol(protocolID);
			if (mbfs.isCompleted(s)) counter++;
		}
		if (counter == sources.size()) {
			System.err.println("All sources completed the accumulation, stopping the simulation");
			resetStaticFields();
			return true;
		} else {
			System.err.println(counter + " out of " + sources.size() + " sources completed the accumulation");
			return false;
		}
	}
	
}
