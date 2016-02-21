package deploy;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import centrality.CentralityObserver;
import peersim.Simulator;
import peersim.util.IncrementalStats;

public class Main {
	
	public static void main(String[] args) throws FileNotFoundException {
		if (args.length < 1) {
			usage();
			return;
		}
		
		switch(args[0]) {
		case "-s":
			if (args.length < 2) usage();
			else Simulator.main(new String[]{args[1]});
			break;
		case "-a":
			if (args.length < 4) usage();
			else analysis(args[1], args[2], Integer.parseInt(args[3]));
			break;
		default:
			usage();
		}
	}
	
	private static void usage() {
		System.err.println("To run a simulation invoke with option \"-s cfg\"");
		System.err.println("To run error analysis invoke with option \"-a indices data runs\"");
	}

	public static void analysis(String indicesFile, String dataFile, int runs) throws FileNotFoundException {
		
		//TODO normalize indices
		
		// Read data file with exact centrality values
		Scanner sc = new Scanner(new File(indicesFile));
		
		int n = sc.nextInt();
		
		double[] CC = new double[n];
		long[] SC = new long[n];
		double[] BC = new double[n];
		
		double maxCC = Double.NEGATIVE_INFINITY, minCC = Double.POSITIVE_INFINITY;
		long maxSC = Long.MIN_VALUE, minSC = Long.MAX_VALUE;
		double maxBC = Double.NEGATIVE_INFINITY, minBC = Double.POSITIVE_INFINITY;

		while (sc.hasNext()) {
			int i = sc.nextInt() - 1;
			CC[i] = sc.nextDouble();
			SC[i] = sc.nextLong();
			BC[i] = sc.nextDouble();
			if (CC[i] > maxCC) maxCC = CC[i];
			else if (CC[i] < minCC) minCC = CC[i];
			if (SC[i] > maxSC) maxSC = SC[i];
			else if (SC[i] < minSC) minSC = SC[i];
			if (BC[i] > maxBC) maxBC = BC[i];
			else if (BC[i] < minBC) minBC = BC[i];
		}
		
		sc.close();
		
		// Read data file with experiment results
		
		double[][] runCC = new double[n][runs];
		long[][] runSC = new long[n][runs];
		double[][] runBC = new double[n][runs];
		
		sc = new Scanner(new File(dataFile));
		int c = 0;
		while (sc.hasNextLine()) {
			String s = sc.nextLine();
			if (s.startsWith(CentralityObserver.TABLE_END_MARKER)) {
				c++;
			} else if (s.startsWith("#")){
				continue;
			} else {
				Scanner line = new Scanner(s);
				if (line.hasNext()) {
					int i = line.nextInt() - 1;
					runCC[i][c] = line.nextDouble();
					runSC[i][c] = line.nextLong();
					runBC[i][c] = line.nextDouble();
				}
				line.close();
			}
		}
		
		IncrementalStats closenessError = new IncrementalStats();
		IncrementalStats betweennessError = new IncrementalStats();
		IncrementalStats stressError = new IncrementalStats();
		
		IncrementalStats closenessInversionPerc = new IncrementalStats();
		IncrementalStats betweennessInversionPerc = new IncrementalStats();
		IncrementalStats stressInversionPerc = new IncrementalStats();
		
		for (int k = 0; k < runs; ++k) {
			
			double runMaxCC = Double.NEGATIVE_INFINITY, runMinCC = Double.POSITIVE_INFINITY;
			long runMaxSC = Long.MIN_VALUE, runMinSC = Long.MAX_VALUE;
			double runMaxBC = Double.NEGATIVE_INFINITY, runMinBC = Double.POSITIVE_INFINITY;
			
			for (int i = 0; i < n; ++i) {
				if (runCC[i][k] > runMaxCC) runMaxCC = runCC[i][k];
				else if (runCC[i][k] < runMinCC) runMinCC = runCC[i][k];
				if (runSC[i][k] > runMaxSC) runMaxSC = runSC[i][k];
				else if (runSC[i][k] < runMinSC) runMinSC = runSC[i][k];
				if (runBC[i][k] > runMaxBC) runMaxBC = runBC[i][k];
				else if (runBC[i][k] < runMinBC) runMinBC = runBC[i][k];

			}
			
			for (int i = 0; i < n; ++i) {
				double scaledCC = (maxCC - CC[i]) / (maxCC - minCC);
				double scaledApproxCC = (runMaxCC - runCC[i][k]) / (runMaxCC - runMinCC);
				
				double scaledBC = (maxBC - BC[i]) / (maxBC - minBC);
				double scaledApproxBC = (runMaxBC - runBC[i][k]) / (runMaxBC - runMinBC);
				
				double scaledSC = (maxSC - SC[i]) / (double) (maxSC - minSC);
				double scaledApproxSC = (runMaxSC - runSC[i][k]) / (double) (runMaxSC - runMinSC);
				
				closenessError.add(Math.abs(scaledCC - scaledApproxCC));
				//closenessError.add(Math.abs(CC[i] - runCC[i][k]));
				betweennessError.add(Math.abs(scaledBC - scaledApproxBC));
				stressError.add(Math.abs(scaledSC - scaledApproxSC));
				
			}
			
			long closenessInversion = 0;
			long betweennessInversion = 0;
			long stressInversion = 0;
			
			//TODO need to review the conditions
			for (int i = 0; i < n; ++i) {
				for (int j = i+1; j < n; ++j) {
					boolean invertedCC = !((CC[i] > CC[j] && runCC[i][k] > runCC[j][k])
							|| (CC[i] < CC[j] && runCC[i][k] < runCC[j][k]) 
							|| (CC[i] == CC[j] && runCC[i][k] == runCC[j][k]));
					
					boolean invertedBC = !((BC[i] > BC[j] && runBC[i][k] > runBC[j][k])
							|| (BC[i] < BC[j] && runBC[i][k] < runBC[j][k]) 
							|| (BC[i] == BC[j] && runBC[i][k] == runBC[j][k]));
					
					boolean invertedSC = !((SC[i] > SC[j] && runSC[i][k] > runSC[j][k])
							|| (SC[i] < SC[j] && runSC[i][k] < runSC[j][k]) 
							|| (SC[i] == SC[j] && runSC[i][k] == runSC[j][k]));
					
					if (invertedCC) {
						closenessInversion++;
					}
					if (invertedBC) {
						betweennessInversion++;
					}
					if (invertedSC) {
						stressInversion++;
					}
				}
			}
			
			double totPairs = (n*(n-1)) / 2.0;
			closenessInversionPerc.add(100.0 * (closenessInversion / totPairs));
			stressInversionPerc.add(100.0 * (stressInversion / totPairs));
			betweennessInversionPerc.add(100.0 * (betweennessInversion / totPairs));
		}
		
		//System.out.println("Aggregate data");
		//System.out.println("CCerr CCpercInv BCerr BCpercInv SCerr SCpercInv");
		//for (int k= 0; k < nbuckets; ++k) {
			double ccErr = closenessError.getAverage();
			double ccPercInv = closenessInversionPerc.getAverage();
			double bcErr = betweennessError.getAverage();
			double bcPercInv = betweennessInversionPerc.getAverage();
			double scErr = stressError.getAverage();
			double scPercInv = stressInversionPerc.getAverage();
			System.out.printf("%16f %16f %16f %16f %16f %16f\n", ccErr, ccPercInv, bcErr, bcPercInv, scErr, scPercInv);

			//System.out.printf("%4d %8f %8f %8f %8f %8f %8f %8f\n", k, (k+1)*step, ccErr, ccPercInv, bcErr, bcPercInv, scErr, scPercInv);
		//}
		//System.out.println("==================");
		
		
		
		
		sc.close();
	}

}
