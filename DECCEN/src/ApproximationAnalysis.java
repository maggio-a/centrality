import java.util.HashSet;
import java.util.Set;

import peersim.config.Configuration;
import peersim.config.IllegalParameterException;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.util.IncrementalStats;


public class ApproximationAnalysis implements Control {
	
	private static String PAR_PROTOCOL = "protocol";
	private static String PAR_NBUCKETS = "nbuckets";
	
	private int protocolID;
	private int nbuckets;
	
	public ApproximationAnalysis(String prefix) {
		protocolID = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
		nbuckets = Configuration.getInt(prefix + "." + PAR_NBUCKETS);
		if (nbuckets <= 0)
			throw new IllegalParameterException(prefix + "." + PAR_NBUCKETS, "only positive values allowed");
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean execute() {
		Set<Node> sources = CAInitializer.getSources();
		int networkSize = Network.size();
		
		if (sources.size() != networkSize)
			throw new IllegalStateException("Cannot perform error analysis without the exact centrality values");
		
		// Setting up source buckets
		
		Set<Node>[] buckets = new Set[nbuckets];
		for (int i = 0; i < nbuckets; ++i) {
			buckets[i] = new HashSet<Node>();
		}
		
		double step = 1.0 / nbuckets;
		
		for (Node s : sources) {
			double p = CommonState.r.nextDouble();
			for (int i = 0; i < nbuckets; ++i) {
				if (p <= (i+1)*step) buckets[i].add(s);
			}
		}
		
		int totalSC = 0;
		for (int i = 0; i < networkSize; ++i) {
			Node node = Network.get(i);
			CentralityApproximation ca = (CentralityApproximation) node.getProtocol(protocolID);
			totalSC += ca.getApproximatedStressCentralityValue(node);
		}
		
		// perform error analysis
		IncrementalStats[] errorSC = new IncrementalStats[nbuckets];
		int[] inversion = new int[nbuckets];
		
		for (int k = 0; k < nbuckets; ++k) {
			errorSC[k] = new IncrementalStats();
			inversion[k] = 0;
			
			int totalBucketSC = 0;
			for (Node node : buckets[k]) {
				CentralityApproximation ca = (CentralityApproximation) node.getProtocol(protocolID);
				totalBucketSC += ca.getApproximatedStressCentralityValue(node, buckets[k]);
			}
			
			// squared error
			for (int i = 0; i < networkSize; ++i) {
				Node node = Network.get(i);
				CentralityApproximation ca = (CentralityApproximation) node.getProtocol(protocolID);
				double normalizedSC = ca.getApproximatedStressCentralityValue(node) / (double) totalSC;
				double normalizedApproxSC = ca.getApproximatedStressCentralityValue(node, buckets[k]) / (double) totalBucketSC;
				double err = normalizedSC - normalizedApproxSC;
				errorSC[k].add(err*err);
			}
			
			// inversion number FIXME maybe weight these on the error
			for (int i = 0; i < networkSize; ++i) {
				Node n1 = Network.get(i);
				CentralityApproximation ca1 = (CentralityApproximation) n1.getProtocol(protocolID);
				int SC1 = ca1.getApproximatedStressCentralityValue(n1);
				int approxSC1 = ca1.getApproximatedStressCentralityValue(n1, buckets[k]);
				for (int j = 0; j < networkSize; ++j) {
					if (i == j) continue;
					Node n2 = Network.get(j);
					CentralityApproximation ca2 = (CentralityApproximation) n2.getProtocol(protocolID);
					int SC2 = ca2.getApproximatedStressCentralityValue(n2);
					int approxSC2 = ca2.getApproximatedStressCentralityValue(n2, buckets[k]);
					if ((SC1 > SC2 && approxSC1 <= approxSC2) || (SC1 < SC2 && approxSC1 >= approxSC2))
						inversion[k]++; //FIXME this should be weighted with the error
				}
			}
			// FIXME pretty print to copy seamlessly in LaTeX
			System.out.println("Bucket " + k + " (" + buckets[k].size() + " sources) avg SC err: " +
					errorSC[k].getAverage() + ", inversion number: " + inversion[k]);
		}
		
		
		return false;
	}

}
