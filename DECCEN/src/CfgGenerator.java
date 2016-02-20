import java.io.File;
import java.io.PrintStream;

public class CfgGenerator {

	private static String[] fractions = { "0.05", "0.10", "0.15", "0.20",
			"0.25", "0.30", "0.35", "0.40", "0.45", "0.50", "0.55", "0.60",
			"0.65", "0.70", "0.75", "0.80", "0.85", "0.90", "0.95", "1.0" };
	
	private static String[] suffixes = { "005", "010", "015", "020",
		"025", "030", "035", "040", "045", "050", "055", "060",
		"065", "070", "075", "080", "085", "090", "095", "100" };
	
	private static boolean correct = true;

	private static String name = "dolphins";
	private static int _netSize = 62;
	private static int _deg = 1500;

	public static void main(String[] args) throws Exception {
		
		PrintStream script = new PrintStream("approximation_" + name + ".cmd");
		PrintStream analysis = new PrintStream("analysis_" + name + ".cmd");
		
		String experimentPath = "experiments/" + name + "/";
		
		for (int i = 0; i < fractions.length; ++i) {
			
			String filename = "approximation_" + name + suffixes[i] + "_" + correct; 
			PrintStream ps = new PrintStream(new File(experimentPath + filename + ".cfg"));
			boolean exact = (i == fractions.length -1);
			
			
			// write out the cfg file
			ps.println("#SIMULATION");
			ps.println("");
			if (exact) ps.println("random.seed 1234567890");
			ps.println("simulation.cycles 10000000");
			ps.println("simulation.experiments " + (exact ? "1" : "10"));
			ps.println("network.size " + _netSize);
			ps.println("network.node MyNode");
			ps.println("");
			ps.println("");
			ps.println("");
			ps.println("# PROTOCOLS");
			ps.println("");
			ps.println("protocol.linkable LinkableImplementation");
			ps.println("");
			ps.println("protocol.approximation CentralityApproximation");
			ps.println("protocol.approximation.lnk linkable");
			ps.println("protocol.approximation.ignoreCorrectEstimate " + !correct);
			ps.println("");
			ps.println("");
			ps.println("");
			ps.println("# INITIALIZERS");
			ps.println("");
			ps.println("init.wire WireFromEdgeList");
			ps.println("init.wire.protocol linkable");
			ps.println("init.wire.undirected");
			ps.println("init.wire.pack");
			ps.println("init.wire.filename /data/" + name + "/" + name + ".out");
			ps.println("init.wire.setLabels");
			ps.println("");
			ps.println("# Initializes the values");
			ps.println("init.initializer ApproximationInitializer");
			ps.println("init.initializer.protocol approximation");
			ps.println("init.initializer.fraction "+ fractions[i]);
			ps.println("");
			ps.println("order.init wire initializer");
			ps.println("");
			ps.println("");
			ps.println("");
			ps.println("# CONTROLS");
			ps.println("");
			ps.println("# Regulates the number of simultaneous visits to control memory usage");
			ps.println("control.ac ApproximationControl");
			ps.println("control.ac.protocol approximation");
			ps.println("control.ac.degree " + _deg);
			ps.println("#control.acc.printIndices");
			ps.println("");
			ps.println("control.transport CycleBasedTransport");
			ps.println("control.transport.protocol approximation");
			ps.println("control.transport.printStatistics");
			ps.println("control.transport.terminateOnEmptyQueues");
			ps.println("");
			ps.println("control.observer CentralityObserver");
			ps.println("control.observer.protocol approximation");
			ps.println("control.observer.until 0");
			ps.println("control.observer.FINAL");
			ps.println("");
			
			ps.close();
			
			script.println("java -jar centrality.jar -s " + experimentPath + filename + ".cfg > "
					+ "results/" + name + "/" + filename + ".txt");
			
			String redirect = (i == 0) ? ">" : ">>";
			analysis.println("java -jar centrality.jar -a results/" + name + "/" + filename + ".txt "
					+ redirect + " results/" + name + "/analysis_" + filename + ".txt");
		}
		
		script.close();
		analysis.close();
		
	}

}
