package centrality;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import peersim.config.Configuration;
import peersim.config.IllegalParameterException;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;


public class CAControl implements Control {
	
	public static final String TABLE_END_MARKER = "%END%";
	
	private static String PAR_PROTOCOL = "protocol";
	private static String PAR_DEGREE = "degree";
	private static String PAR_PRINT = "printIndices";
	
	private int protocolID;
	private int degree;
	private boolean printIndices;
	
	private static Iterator<Node> sourcesIterator = null;
	private static List<Node> visiting = new LinkedList<Node>();
	
	public CAControl(String prefix) {
		protocolID = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
		degree = Configuration.getInt(prefix + "." + PAR_DEGREE);
		if (degree <= 0)
			throw new IllegalParameterException(prefix + "." + PAR_DEGREE, "only positive values allowed");
		printIndices = Configuration.contains(prefix + "." + PAR_PRINT);
	}

	@Override
	public boolean execute() {
		
		if (printIndices) print();
				
		if (sourcesIterator == null) {
			sourcesIterator = CAInitializer.getSources().iterator();
		}
		
		Iterator<Node> it = visiting.iterator();
		while (it.hasNext()) {
			Node n = it.next();
			CentralityApproximation ca = (CentralityApproximation) n.getProtocol(protocolID);
			if (ca.isDone(n)) it.remove();
		}
		
		if (visiting.size() == 0) {
			while (visiting.size() < degree && sourcesIterator.hasNext()) {
				Node s = sourcesIterator.next();
				CentralityApproximation ca = (CentralityApproximation) s.getProtocol(protocolID);
				ca.initAccumulation(s);
				visiting.add(s);
			}
		}
		int counter = 0;
		Set<Node> sources = CAInitializer.getSources();
		for (Node s : sources) {
			CentralityApproximation ca = (CentralityApproximation) s.getProtocol(protocolID);
			if (ca.isDone(s)) counter++;
		}
		if (counter == sources.size()) {
			print();
			System.err.println("All sources completed the accumulation, stopping the simulation");
			sourcesIterator = null;
			if (!visiting.isEmpty()) visiting = new LinkedList<Node>();
			return true;
		} else {
			System.err.println(counter + " out of " + sources.size() + " sources completed the accumulation");
			return false;
		}
	}
	
	private void print() {
		System.out.println("# Centrality indices at time " + CommonState.getIntTime());
		System.out.println("# Label Closeness Stress Betweenness");
		for (int i = 0; i < Network.size(); ++i) {
			MyNode node = (MyNode) Network.get(i);
			CentralityApproximation ca = (CentralityApproximation) node.getProtocol(protocolID);
			String s = "%s %.16f %d %.16f\n";
			System.out.printf(s, node.getLabel(), ca.getCC(), ca.getSC(), ca.getBC());
		}
		System.out.println(TABLE_END_MARKER);
	}

}
