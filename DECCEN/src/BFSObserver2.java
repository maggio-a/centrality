import java.util.Deque;
import java.util.LinkedList;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;


public class BFSObserver2 implements Control {
	
	public static String PAR_PROTOCOL = "protocol";
	
	private int protocolID;
	private Deque<Node> Q = new LinkedList<Node>();
	
	public BFSObserver2(String prefix) {
		protocolID = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
	}

	@Override
	public boolean execute() {
		boolean done = true;
		int c = 0;
		for (int i = 0; i < Network.size(); ++i) {
			Node n = Network.get(i);
			DecentralizedBFS2 dbfs = (DecentralizedBFS2) n.getProtocol(protocolID);
			boolean reported = dbfs.hasState(DecentralizedBFS2.State.REPORTED);
			done &= reported;
			if (!dbfs.hasState(DecentralizedBFS2.State.CLOSED) && !Q.contains(n)) {
				Q.addLast(n);
				System.out.println(n + " --- " + dbfs.state);
			}
			if (reported) {
				System.out.println(n + "REPORTED BACK");
				c++;
			}
		}
		
		System.out.println(c + " nodes reported, " + Q.size() + " are open");
		
		java.util.Iterator<Node> it = Q.iterator();
		while (it.hasNext())
			System.out.println(it.next());
		
		if (done) {
			System.out.println("All nodes reported");
			for (int i = 0; i < Network.size(); ++i) {
				Node n = Network.get(i);
				DecentralizedBFS2 dbfs = (DecentralizedBFS2) n.getProtocol(protocolID);
				System.out.printf("%4s [%d] [ predecessors ", ((MyNode)n).getLabel(), dbfs.depth);
				for (Node pred : dbfs.getPredecessors()) System.out.printf("%s ", ((MyNode)pred).getLabel());
				System.out.printf("] sigma = %d, delta = %d\n", dbfs.sigma, dbfs.delta);
			}
		}
		
		return done;
	}

}
