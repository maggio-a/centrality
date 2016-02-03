
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;


public class CAInitializer implements Control {
	
	private static String PAR_PROTOCOL = "protocol";
	
	private int protocolID;
	
	public CAInitializer(String prefix) {
		protocolID = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
	}
	
	@Override
	public boolean execute() {
		/*int i = CommonState.r.nextInt(Network.size());
		System.out.println("Probing from Netwpork.get(" + i +")");
		//int i = 22;
		Node n = Network.get(i);
		CentralityApproximation ca = (CentralityApproximation) n.getProtocol(protocolID);
		System.out.println("Strarting visit from " + n);
		ca.probe(n);*/
		
		for (int i = 0; i < Network.size(); ++i) {
			Node node = Network.get(i);
			CentralityApproximation ca = (CentralityApproximation) node.getProtocol(protocolID);
			ca.probe(node);
		}
		return false;
	}

}
