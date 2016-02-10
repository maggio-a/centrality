import java.util.HashSet;
import java.util.Set;

import peersim.Simulator;
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
	
	private static IncrementalStats[] aggregateClosenessError = null;
	private static IncrementalStats[] aggregateBetweennessError = null;
	private static IncrementalStats[] aggregateStressError = null;
	
	private static int[] aggregateClosenessInversion = null;
	private static int[] aggregateBetweennessInversion = null;
	private static int[] aggregateStressInversion = null;
	
	private static boolean multiRun = false;
	private static int nexp = 0;
	
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
		
		if ((nexp == 1) && Configuration.getInt(Simulator.PAR_EXPS,1) > 1) {
			aggregateClosenessError = new IncrementalStats[nbuckets];
			aggregateBetweennessError = new IncrementalStats[nbuckets];
			aggregateStressError = new IncrementalStats[nbuckets];
			
			aggregateClosenessInversion = new int[nbuckets];
			aggregateBetweennessInversion = new int[nbuckets];
			aggregateStressInversion = new int[nbuckets];
			
			multiRun = true;
		}
		
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
		
		// analysis is performed on scaled values (all indices sum to 1)
		
		double[] CC = new double[networkSize];
		double[] BC = new double[networkSize];
		int[] SC = new int[networkSize];
		
		double totalCC = 0.0;
		double totalBC = 0.0;
		int totalSC = 0;
		for (int i = 0; i < networkSize; ++i) {
			Node node = Network.get(i);
			CentralityApproximation ca = (CentralityApproximation) node.getProtocol(protocolID);
			CC[i] = ca.getApproximatedCC(node);
			BC[i] = ca.getApproximatedBC(node);
			SC[i] = ca.getApproximatedSC(node);
			totalCC += CC[i];
			totalBC += BC[i];
			totalSC += SC[i];
		}
		
		// perform error analysis
		IncrementalStats[] closenessError = new IncrementalStats[nbuckets];
		IncrementalStats[] betweennessError = new IncrementalStats[nbuckets];
		IncrementalStats[] stressError = new IncrementalStats[nbuckets];
		
		int[] closenessInversion = new int[nbuckets];
		int[] betweennessInversion = new int[nbuckets];
		int[] stressInversion = new int[nbuckets];
		
		double[] bucketCC = new double[networkSize];
		double[] bucketBC = new double[networkSize];
		int[] bucketSC = new int[networkSize];
		
		System.out.println("Bucket Fraction CCerr CCpercInv BCerr BCpercInv SCerr SCpercInv");
		
		for (int k = 0; k < nbuckets; ++k) {
			closenessError[k] = new IncrementalStats();
			betweennessError[k] = new IncrementalStats();
			stressError[k] = new IncrementalStats();
			
			closenessInversion[k] = 0;
			betweennessInversion[k] = 0;
			stressInversion[k] = 0;
			
			double bucketTotalCC = 0.0;
			double bucketTotalBC = 0.0;
			int bucketTotalSC = 0;
			
			for (int i = 0; i < networkSize; ++i) {
				Node node = Network.get(i);
				CentralityApproximation ca = (CentralityApproximation) node.getProtocol(protocolID);
				bucketCC[i] = ca.getApproximatedCC(node, buckets[k]);
				bucketBC[i] = ca.getApproximatedBC(node, buckets[k]);
				bucketSC[i] = ca.getApproximatedSC(node, buckets[k]);
				bucketTotalCC += bucketCC[i];
				bucketTotalBC += bucketBC[i];
				bucketTotalSC += bucketSC[i];
			}
			
			for (int i = 0; i < networkSize; ++i) {
				double scaledCC = CC[i] / totalCC;
				double scaledApproxCC = bucketCC[i] / bucketTotalCC;
				
				double scaledBC = BC[i] / totalBC;
				double scaledApproxBC = bucketBC[i] / bucketTotalBC;
				
				double scaledSC = SC[i] / (double) totalSC;
				double scaledApproxSC = bucketSC[i] / (double) bucketTotalSC;
				
				closenessError[k].add(Math.abs(scaledCC - scaledApproxCC));
				betweennessError[k].add(Math.abs(scaledBC - scaledApproxBC));
				stressError[k].add(Math.abs(scaledSC - scaledApproxSC));
				
				if (multiRun) {
					aggregateClosenessError[k].add(Math.abs(scaledCC - scaledApproxCC));
					aggregateBetweennessError[k].add(Math.abs(scaledBC - scaledApproxBC));
					aggregateStressError[k].add(Math.abs(scaledSC - scaledApproxSC));
				}
			}
			
			//TODO need to review the conditions
			for (int i = 0; i < networkSize; ++i) {
				for (int j = i+1; j < networkSize; ++j) {
					boolean invertedCC = !((CC[i] > CC[j] && bucketCC[i] > bucketCC[j])
							|| (CC[i] < CC[j] && bucketCC[i] < bucketCC[j]) 
							|| (CC[i] == CC[j] && bucketCC[i] == bucketCC[j]));
					
					boolean invertedBC = !((BC[i] > BC[j] && bucketBC[i] > bucketBC[j])
							|| (BC[i] < BC[j] && bucketBC[i] < bucketBC[j]) 
							|| (BC[i] == BC[j] && bucketBC[i] == bucketBC[j]));
					
					boolean invertedSC = !((SC[i] > SC[j] && bucketSC[i] > bucketSC[j])
							|| (SC[i] < SC[j] && bucketSC[i] < bucketSC[j]) 
							|| (SC[i] == SC[j] && bucketSC[i] == bucketSC[j]));
					
					if (invertedCC) {
						closenessInversion[k]++;
						if (multiRun) aggregateClosenessInversion[k]++;
					}
					if (invertedBC) {
						betweennessInversion[k]++;
						if (multiRun) aggregateBetweennessInversion[k]++;
					}
					if (invertedSC) {
						stressInversion[k]++;
						if (multiRun) aggregateStressInversion[k]++;
					}
				}
			}
			
			double ccErr = closenessError[k].getAverage();
			double ccPercInv = 100.0 * closenessInversion[k] / (double) (networkSize*(networkSize-1));
			double bcErr = betweennessError[k].getAverage();
			double bcPercInv = 100.0 * betweennessInversion[k] / (double) (networkSize*(networkSize-1));
			double scErr = stressError[k].getAverage();
			double scPercInv = 100.0 * stressInversion[k] / (double) (networkSize*(networkSize-1));
			System.out.printf("%4d %8f %8f %8f %8f %8f %8f %8f\n", k, (k+1)*step, ccErr, ccPercInv, bcErr, bcPercInv, scErr, scPercInv);
			
			
		}
		
		if (multiRun) {
			System.out.println("== Aggregate data over " + ++nexp + " experiments ==");
			System.out.println("Bucket Fraction CCerr CCpercInv BCerr BCpercInv SCerr SCpercInv");
			for (int k= 0; k < nbuckets; ++k) {
				double ccErr = aggregateClosenessError[k].getAverage();
				double ccPercInv = 100.0 * aggregateClosenessInversion[k] / (double) (networkSize*(networkSize-1));
				double bcErr = aggregateBetweennessError[k].getAverage();
				double bcPercInv = 100.0 * aggregateBetweennessInversion[k] / (double) (networkSize*(networkSize-1));
				double scErr = aggregateStressError[k].getAverage();
				double scPercInv = 100.0 * aggregateStressInversion[k] / (double) (networkSize*(networkSize-1));
				System.out.printf("%4d %8f %8f %8f %8f %8f %8f %8f\n", k, (k+1)*step, ccErr, ccPercInv, bcErr, bcPercInv, scErr, scPercInv);
			}
			System.out.println("==================");
		}
		
		
		return false;
	}

}
