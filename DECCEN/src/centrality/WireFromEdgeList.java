package centrality;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

import peersim.config.Configuration;
import peersim.core.Network;
import peersim.dynamics.WireGraph;
import peersim.graph.Graph;

public class WireFromEdgeList extends WireGraph {
	
	private static final String PAR_FILENAME = "filename";
	private static final String PAR_LABELS = "setLabels";
	
	private final String filename;
	private final boolean setLabels;

	public WireFromEdgeList(String prefix) {
		super(prefix);
		filename = Configuration.getString(prefix + "." + PAR_FILENAME);
		setLabels = Configuration.contains(prefix + "." + PAR_LABELS);
		
	}

	@Override
	public void wire(Graph g) {
		try (Scanner s = new Scanner(new FileReader(filename))) {
			int ln = 0;
			int n = Network.size();
			while (s.hasNextLine()) {
				ln++;
				String line = s.nextLine();
				if (line.startsWith("#") || line.startsWith("%"))
					continue;
				String[] id = line.split("\\s"); // whitespace delimiter
				if (id.length < 2)
					System.err.println(getClass().getName() + " Warning (line " + ln + "): missing edge endpoint, skipping");
				if (id.length > 2)
					System.err.println(getClass().getName() + " Warning (line " + ln + "): tokens ignored");
				int v1 = Integer.parseInt(id[0]) - 1;
				int v2 = Integer.parseInt(id[1]) - 1;
				if (v1 < 0 || v1 >= n || v2 < 0 || v2 >= n)
					System.err.println(getClass().getName() + " Warning (line " + ln + "): id out of range");
				else {
					g.setEdge(v1, v2);
					if (setLabels) {
						MyNode n1 = (MyNode) g.getNode(v1);
						MyNode n2 = (MyNode) g.getNode(v2);
						n1.setLabel(id[0]);
						n2.setLabel(id[1]);
					}
				}
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}
