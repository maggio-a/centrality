package cycleDrivenSizeEstimation4;

import java.util.Random;

import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.NodeInitializer;

public class MyNodeInitializer implements NodeInitializer {

	private static final String PAR_PROT = "protocol";
	private static final String DEGREE_MIN = "degreeMin";
	private static final String DEGREE_MAX = "degreeMax";
	private static final Random generator = new Random(123);
	
	private int linkablePID ;
	private int degreeMin;
	private int degreeMax;

	public MyNodeInitializer(String prefix) {
		linkablePID = Configuration.getPid(prefix + "." + PAR_PROT);
		degreeMin = Configuration.getInt(prefix + "." + DEGREE_MIN);
		degreeMax = Configuration.getInt(prefix + "." + DEGREE_MAX);
	}
	
	@Override
	public void initialize(Node n) {
		int degree = generator.nextInt(degreeMax - degreeMin) + degreeMin;
		LinkableExample linkable = (LinkableExample) n.getProtocol(linkablePID);
		
		for (int j = 0; j < degree; j++) {
			Node other;
			LinkableExample otherLinkable;
			do {
				other = Network.get(generator.nextInt(Network.size()));
				otherLinkable = (LinkableExample) other.getProtocol(linkablePID);
			} while (n == null || !n.isUp() || otherLinkable.contains(n));
			
			linkable.addNeighbor(other);
			otherLinkable.addNeighbor(n);
		}
	}

}
