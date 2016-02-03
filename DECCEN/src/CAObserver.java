import java.util.Deque;
import java.util.LinkedList;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;


public class CAObserver implements Control {
	
	public static String PAR_PROTOCOL = "protocol";
	public static String PAR_FRACTION = "fraction";
	
	private int protocolID;
	private double fraction;
	private Deque<Node> Q = new LinkedList<Node>();
	
	public CAObserver(String prefix) {
		protocolID = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
		fraction = Configuration.getDouble(prefix + "." + PAR_FRACTION, 1.0);
	}

	@Override
	public boolean execute() {
		/*Node source = Network.get(25);
		boolean done = true;
		int c = 0;
		for (int i = 0; i < Network.size(); ++i) {
			Node n = Network.get(i);
			CentralityApproximation ca = (CentralityApproximation) n.getProtocol(protocolID);
			boolean reported = ca.isDone(source); //dbfs.hasState(DecentralizedBFS.State.REPORTED);
			done &= reported;
			//if (!dbfs.hasState(DecentralizedBFS.State.CLOSED) && !Q.contains(n)) {
			if (!ca.isClosed(source) && !Q.contains(n)) {
				Q.addLast(n);
				//System.out.println(n + " --- " + (ca.isOpen(source) ? "OPEN" : "DONE"));
			}
			if (reported) {
				System.out.println(n + "REPORTED BACK");
				c++;
			}
		}
		
		System.out.println(c + " nodes reported, " + Q.size() + " visited");*/
		/*
		java.util.Iterator<Node> it = Q.iterator();
		while (it.hasNext())
			System.out.println(it.next());
		
		if (done) {
			System.out.println("All nodes reported");
			for (int i = 0; i < Network.size(); ++i) {
				Node n = Network.get(i);
				DecentralizedBFS dbfs = (DecentralizedBFS) n.getProtocol(protocolID);
				System.out.printf("%4s [%d] [ predecessors ", ((MyNode)n).getLabel(), dbfs.depth);
				for (Node pred : dbfs.getPredecessors()) System.out.printf("%s ", ((MyNode)pred).getLabel());
				System.out.printf("] sigma = %d, delta = %d\n", dbfs.sigma, dbfs.delta);
			}
		}*/
		
		System.out.println("## Approximated Betweenness Centrality indices at time " + CommonState.getIntTime() + " ####");
		for (int i = 0; i < Network.size(); ++i) {
			Node node = Network.get(i);
			CentralityApproximation ca = (CentralityApproximation) node.getProtocol(protocolID);
			//System.out.println(node + ": SC = " + sdp.getStressCentralityValue()
			//		+ ", CC = " + sdp.getClosenessCentralityValue()
			//		+ ", BC = " + sdp.getBetweennessCentralityValue());
			//String s = "%s: SC = %6d, CC = %12.10f, BC = %16.11f\n";
			String s = "%12s: SC = %6d, BC = %16.11f\n";
			System.out.printf(s, node.toString(), ca.getApproximatedStressCentralityValue(node, fraction),
					ca.getApproximatedBetweennessCentralityValue(node, fraction));
		}
		System.out.println("##############################################");
		return false;
		
	}

}
