
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;


public class BFSInitializer implements Control {
	
	private static String PAR_PROTOCOL = "protocol";
	
	private int protocolID;
	
	public BFSInitializer(String prefix) {
		protocolID = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
	}
	
	@Override
	public boolean execute() {
		int i = CommonState.r.nextInt(Network.size());
		Node n = Network.get(i);
		DecentralizedBFS dbfs = (DecentralizedBFS) n.getProtocol(protocolID);
		System.out.println("Strarting visit from " + n);
		dbfs.bfs(n, protocolID);
		return false;
	}

}
