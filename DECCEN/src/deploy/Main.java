package deploy;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
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
		
		double totCC = 0.0;
		long totSC = 0;
		double totBC = 0.0;

		while (sc.hasNext()) {
			int i = sc.nextInt() - 1;
			totCC += CC[i] = sc.nextDouble();
			totSC += SC[i] = sc.nextLong();
			totBC += BC[i] = sc.nextDouble();
			if (CC[i] > maxCC) maxCC = CC[i];
			else if (CC[i] < minCC) minCC = CC[i];
			if (SC[i] > maxSC) maxSC = SC[i];
			else if (SC[i] < minSC) minSC = SC[i];
			if (BC[i] > maxBC) maxBC = BC[i];
			else if (BC[i] < minBC) minBC = BC[i];
		}
		
		double percentile = 0.9;
		
		double[] tmpcc = Arrays.copyOf(CC, n);
		long[] tmpsc = Arrays.copyOf(SC, n);
		double[] tmpbc = Arrays.copyOf(BC, n);
		
		Arrays.sort(tmpcc);
		Arrays.sort(tmpsc);
		Arrays.sort(tmpbc);
		
		
		double percentileCC = tmpcc[(int) (n * percentile)];
		long percentileSC = tmpsc[(int) (n * percentile)];
		double percentileBC = tmpbc[(int) (n * percentile)];
		
		tmpcc = tmpbc = null; tmpsc = null;
		
		sc.close();
		
		// Read data file with experiment results
		
		double[][] runCC = new double[n][runs];
		long[][] runSC = new long[n][runs];
		double[][] runBC = new double[n][runs];
		
		double[] totRunCC = new double[runs];
		long[] totRunSC = new long[runs];
		double[] totRunBC = new double[runs];
		
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
					totRunCC[c] += runCC[i][c] = line.nextDouble();
					totRunSC[c] += runSC[i][c] = line.nextLong();
					totRunBC[c] += runBC[i][c] = line.nextDouble();
				}
				line.close();
			}
		}
		
		IncrementalStats closenessError = new IncrementalStats();
		IncrementalStats betweennessError = new IncrementalStats();
		IncrementalStats stressError = new IncrementalStats();
		
		IncrementalStats closenessPercentileError = new IncrementalStats();
		IncrementalStats betweennessPercentileError = new IncrementalStats();
		IncrementalStats stressPercentileError = new IncrementalStats();
		
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
				/*
				 * NORMALIZED ERROR
				 * double scaledCC = (maxCC - CC[i]) / (maxCC - minCC);
				double scaledApproxCC = (runMaxCC - runCC[i][k]) / (runMaxCC - runMinCC);
				
				double scaledBC = (maxBC - BC[i]) / (maxBC - minBC);
				double scaledApproxBC = (runMaxBC - runBC[i][k]) / (runMaxBC - runMinBC);
				
				double scaledSC = (maxSC - SC[i]) / (double) (maxSC - minSC);
				double scaledApproxSC = (runMaxSC - runSC[i][k]) / (double) (runMaxSC - runMinSC);
				
				closenessError.add(Math.abs(scaledCC - scaledApproxCC));
				//closenessError.add(Math.abs(CC[i] - runCC[i][k]));
				betweennessError.add(Math.abs(scaledBC - scaledApproxBC));
				stressError.add(Math.abs(scaledSC - scaledApproxSC));*/
				
				/*
				 * EUCLIDEAN ERROR
				double ecc = (runCC[i][k] / totRunCC[k]) - (CC[i] / totCC);

				double esc = (runSC[i][k] / (double)totRunSC[k]) - (SC[i] / (double)totSC);

				double ebc = (runBC[i][k] / totRunBC[k]) - (BC[i] / totBC);
				
				closenessError.add(ecc*ecc);
				betweennessError.add(ebc*ebc);
				stressError.add(esc*esc);*/
				
				double ecc = Math.abs(runCC[i][k] - CC[i]) / CC[i];
				double esc = (runSC[i][k] == SC[i]) ? 0.0 : (Math.abs((double)(runSC[i][k] - SC[i])) / SC[i]);
				double ebc = (runBC[i][k] == BC[i]) ? 0.0 : (Math.abs(runBC[i][k] - BC[i]) / BC[i]);
				
				double factor = 1.0;
				
				closenessError.add(factor*ecc);
				betweennessError.add(factor*ebc);
				stressError.add(factor*esc);
				
				if (CC[i] >= percentileCC) closenessPercentileError.add(factor*ecc);
				if (BC[i] >= percentileBC) betweennessPercentileError.add(factor*ebc);
				if (SC[i] >= percentileSC) stressPercentileError.add(factor*esc);
								
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
					
					// note: as it happens, during the estimation several nodes that have different centrality get
					// the same estimate value, the following conditions do not include this case:
					boolean _invertedCC = (CC[i] > CC[j] && runCC[i][k] < runCC[j][k]) || (CC[i] < CC[j] && runCC[i][k] > runCC[j][k]);
					boolean _invertedBC = (BC[i] > BC[j] && runBC[i][k] < runBC[j][k]) || (BC[i] < BC[j] && runBC[i][k] > runBC[j][k]);
					boolean _invertedSC = (SC[i] > SC[j] && runSC[i][k] < runSC[j][k]) || (SC[i] < SC[j] && runSC[i][k] > runSC[j][k]);
					
					/*if (invertedCC != _invertedCC && CC[i] != 0.0) {
						++__c;
						System.out.println("+++CC[i]=" + CC[i] + " CC[j]=" + CC[j] + " app[i]=" + runCC[i][k] +" app[j]=" +  runCC[j][k]);
					}*/
					
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
			
			double invFactor = 1.0;
			
			double totPairs = (n*(n-1)) / 2.0;
			closenessInversionPerc.add(invFactor * (closenessInversion / totPairs));
			stressInversionPerc.add(invFactor * (stressInversion / totPairs));
			betweennessInversionPerc.add(invFactor * (betweennessInversion / totPairs));
		}
		
		//System.out.println("Aggregate data");
		//System.out.println("CCerr CCpercInv BCerr BCpercInv SCerr SCpercInv");
		//for (int k= 0; k < nbuckets; ++k) {
			double ccErr = closenessError.getAverage();
			double ccPercentileErr = closenessPercentileError.getAverage();
			double ccPercInv = closenessInversionPerc.getAverage();
			double bcErr = betweennessError.getAverage();
			double bcPercentileErr = betweennessPercentileError.getAverage();
			double bcPercInv = betweennessInversionPerc.getAverage();
			double scErr = stressError.getAverage();
			double scPercentileErr = stressPercentileError.getAverage();
			double scPercInv = stressInversionPerc.getAverage();
			/*
			double ccErr = Math.sqrt(closenessError.getSum() / (double) runs);
			double ccPercInv = closenessInversionPerc.getAverage();
			double bcErr = Math.sqrt(betweennessError.getSum() / (double) runs);
			double bcPercInv = betweennessInversionPerc.getAverage();
			double scErr = Math.sqrt(stressError.getSum() / (double) runs);
			double scPercInv = stressInversionPerc.getAverage();*/
			
			System.out.printf("%.9f  %.9f  %.9f  %.9f  %.9f  %.9f  %.9f  %.9f  %.9f\n", ccErr, ccPercentileErr,
					ccPercInv, bcErr, bcPercentileErr, bcPercInv, scErr, scPercentileErr, scPercInv);

			//System.out.printf("%4d %8f %8f %8f %8f %8f %8f %8f\n", k, (k+1)*step, ccErr, ccPercInv, bcErr, bcPercInv, scErr, scPercInv);
		//}
		//System.out.println("==================");
		
		
		
		
		sc.close();
	}

}
