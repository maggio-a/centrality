package centrality;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;


public class CentralityObserver implements Control {
	
	public static final String PAR_PROTOCOL = "protocol";
	public static final String TABLE_END_MARKER = "%END%";
	
	private int protocolID;
	
	public CentralityObserver(String prefix) {
		protocolID = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
	}

	@Override
	public boolean execute() {
		System.out.println("# Centrality indices at time " + CommonState.getIntTime());
		System.out.println("# Label Closeness Stress Betweenness");
		for (int i = 0; i < Network.size(); ++i) {
			MyNode node = (MyNode) Network.get(i);
			SynchronousCentralityProtocol centrality = (SynchronousCentralityProtocol) node.getProtocol(protocolID);
			System.out.printf("%s %.16f %d %.16f\n",
					node.getLabel(), centrality.getCC(), centrality.getSC(), centrality.getBC());
		}
		System.out.println(TABLE_END_MARKER);
		return false;
	}


}
