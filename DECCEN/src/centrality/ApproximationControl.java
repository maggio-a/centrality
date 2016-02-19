package centrality;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import peersim.config.Configuration;
import peersim.config.IllegalParameterException;
import peersim.core.Control;
import peersim.core.Node;


public class ApproximationControl implements Control {
	
	private static final String PAR_PROTOCOL = "protocol";
	private static final String PAR_DEGREE = "degree";
	
	private int protocolID;
	private int degree;
	
	private static Iterator<Node> sourcesIterator;
	private static List<Node> visiting;
	
	private static void reset() {
		sourcesIterator = null;
		visiting = new LinkedList<Node>();
	}
	
	static { 
		reset();
	}
	
	public ApproximationControl(String prefix) {
		protocolID = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
		degree = Configuration.getInt(prefix + "." + PAR_DEGREE);
		if (degree <= 0)
			throw new IllegalParameterException(prefix + "." + PAR_DEGREE, "only positive values allowed");
	}

	@Override
	public boolean execute() {
				
		if (sourcesIterator == null) {
			sourcesIterator = ApproximationInitializer.getSources().iterator();
		}
		
		Iterator<Node> it = visiting.iterator();
		while (it.hasNext()) {
			Node n = it.next();
			CentralityApproximation ca = (CentralityApproximation) n.getProtocol(protocolID);
			if (ca.isCompleted(n)) it.remove();
		}
		
		if (visiting.size() == 0) {
			while (visiting.size() < degree && sourcesIterator.hasNext()) {
				Node s = sourcesIterator.next();
				CentralityApproximation ca = (CentralityApproximation) s.getProtocol(protocolID);
				ca.initAccumulation(s, protocolID);
				visiting.add(s);
			}
		}
		int counter = 0;
		Set<Node> sources = ApproximationInitializer.getSources();
		for (Node s : sources) {
			CentralityApproximation ca = (CentralityApproximation) s.getProtocol(protocolID);
			if (ca.isCompleted(s)) counter++;
		}
		if (counter == sources.size()) {
			System.err.println("All sources completed the accumulation, stopping the simulation");
			reset();
			return true;
		} else {
			System.err.println(counter + " out of " + sources.size() + " sources completed the accumulation");
			return false;
		}
	}
	
}
