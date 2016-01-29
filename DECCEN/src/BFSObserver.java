import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;


public class BFSObserver implements Control {
	
	public static String PAR_PROTOCOL = "protocol";
	
	private int protocolID;
	
	public BFSObserver(String prefix) {
		protocolID = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
	}

	@Override
	public boolean execute() {
		boolean done = true;
		int c = 0;
		for (int i = 0; i < Network.size(); ++i) {
			Node n = Network.get(i);
			DecentralizedBFS dbfs = (DecentralizedBFS) n.getProtocol(protocolID);
			boolean reported = dbfs.hasState(DecentralizedBFS.REPORTED);
			done &= reported;
			if (reported) c++;
		}
		
		System.out.println(c + " nodes reported");
		
		if (done) {
			System.out.println("All nodes reported");
			for (int i = 0; i < Network.size(); ++i) {
				Node n = Network.get(i);
				DecentralizedBFS dbfs = (DecentralizedBFS) n.getProtocol(protocolID);
				System.out.println(n + "[" + dbfs.depth + "]" + " predecessors: " + dbfs.getPredecessors());
			}
		}
		
		return done;
	}

}
