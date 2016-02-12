package centrality;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collection;
import java.util.Scanner;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.OverlayGraph;
import peersim.graph.Graph;
import peersim.graph.NeighbourListGraph;

public class WireFromEdgeList implements Control {
	
	private static final String PAR_FILENAME = "filename";
	private static final String PAR_LINKABLE = "linkable";
	
	private final String filename;
	private final int linkableId;

	public WireFromEdgeList(String prefix) {
		filename = Configuration.getString(prefix + "." + PAR_FILENAME);
		linkableId = Configuration.getPid(prefix + "." + PAR_LINKABLE);
	}

	@Override
	public boolean execute() {
		assert Network.size() == 0 : Network.size();
		// build a network model from file
		NeighbourListGraph model = new NeighbourListGraph(false); // undirected
		try {
			Scanner s = new Scanner(new FileReader(filename));
			int ln = 0;
			while (s.hasNextLine()) {
				ln++;
				String line = s.nextLine();
				if (line.startsWith("#") || line.startsWith("%"))
					continue; // ignore comments
				String[] id = line.split("\\s"); // whitespace delimiter
				if (id.length < 2) {
					System.err.println(getClass().getName() + " Error (line " + ln + "): missing edge endpoint");
					System.exit(-1);
				}
				if (id.length > 2)
					System.err.println(getClass().getName() + " Warning (line " + ln + "): tokens ignored");
				// if a "node" was previously added, NeighbourListGraph.addNode() simply returns its id
				model.setEdge(model.addNode(id[0]), model.addNode(id[1]));
			}
			s.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		for (int i = 0; i < model.size(); i++) {
			Node n = (Node) Network.prototype.clone();
			Network.add(n);
		}
		// add edges to the linkable protocol according to the model's structure
		Graph g = new OverlayGraph(linkableId, false);
		for (int i = 0; i < model.size(); i++) {
			Collection<Integer> adj = model.getNeighbours(i);
			for (Integer j : adj) {
				if (!g.isEdge(i,j)) g.setEdge(i,j);
			}
			MyNode n = (MyNode) Network.get(i);
			LinkableImplementation lnk = (LinkableImplementation) n.getProtocol(linkableId);
			n.setLabel((String)model.getNode(i));
			lnk.pack();
		}
		
		Network.sort(new java.util.Comparator<Node>() {

			public int compare(Node o1, Node o2) {
				MyNode n1 = (MyNode) o1;
				MyNode n2 = (MyNode) o2;
				return Integer.parseInt(n1.getLabel()) - Integer.parseInt(n2.getLabel());
				//return n1.getLabel().compareTo(n2.getLabel());
			}
			
		});
		
		return false;
	}

}
