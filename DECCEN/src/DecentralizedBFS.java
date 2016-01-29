import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.Linkable;
import peersim.core.Node;


public class DecentralizedBFS implements CDProtocol {
	
	private static final String PAR_LINKABLE = "lnk";
	
	private int linkableProtocolID;
	
	//private Map<Node, List<Node>> predecessors;
	private List<Node> predecessors;
	
	public int state;
	int depth;
	int sigma;
	int delta;
	
	private static final int CLOSED   = 0;
	private static final int OPEN     = 1;
	private static final int VISITING = 2;
	private static final int FRINGE   = 3;
	private static final int INTERNAL = 4;
	public static final int REPORTED = 5;
	
	public DecentralizedBFS(String prefix) {
		linkableProtocolID = Configuration.getPid(prefix + "." + PAR_LINKABLE);
		state = CLOSED;
		depth = -1;
		sigma = 0;
		delta = 0;
	}
	
	@Override
	public Object clone() {
		DecentralizedBFS o = null;
		try {
			o = (DecentralizedBFS) super.clone();
		} catch (CloneNotSupportedException e) { }
		
		//o.predecessors = new HashMap<Node, List<Node>>();
		o.predecessors = new LinkedList<Node>();
		
		return o;
	}

	@Override
	public void nextCycle(Node self, int protocolID) {
		Linkable lnk = (Linkable) self.getProtocol(linkableProtocolID);
		
		if (state == VISITING) {
			boolean fringe = true;
			assert depth >= 0 : "negative depth";
			for (int i = 0; i < lnk.degree(); ++i) {
				Node n = lnk.getNeighbor(i);
				if (!predecessors.contains(n)) {
					DecentralizedBFS other = (DecentralizedBFS) n.getProtocol(protocolID);
					fringe &= other.visit(n, self, depth+1);
				}
			}
			if (fringe) {
				state = FRINGE;
			} else {
				state = INTERNAL;
			}
		} else if (state == OPEN) {
			state = VISITING;
		} else if (state == FRINGE) {
			// can feedback to the predecessors
			for (Node n : predecessors) {
				DecentralizedBFS pred = (DecentralizedBFS) n.getProtocol(protocolID);
				pred.report(delta, sigma);
			}
			state = REPORTED;
		} else if (state == INTERNAL) {
			// check if all the neighbors that are not predecessors have completed the feedback
			// if so, feedback to the predecessors as well
			boolean canReport = true;
			for (int i = 0; i < lnk.degree(); ++i) {
				Node n = lnk.getNeighbor(i);
				if (!predecessors.contains(n)) {
					DecentralizedBFS other = (DecentralizedBFS) n.getProtocol(protocolID);
					canReport &= other.hasState(REPORTED);
				}
			}
			if (canReport) {
				if (!predecessors.contains(self)) {
					for (Node n : predecessors) {
						DecentralizedBFS pred = (DecentralizedBFS) n.getProtocol(protocolID);
						pred.report(delta, sigma);
					}
				} else {
					// self stared the visit, so there is noone to report to
				}
				state = REPORTED;
			}
		}
	}
	
	public void bfs(Node self, int pid) {
		LinkedList<Node> pl = new LinkedList<Node>();
		pl.add(self);
		predecessors = pl;
		depth = 0;
		sigma = 1;
		//predecessors.put(self, pl);
		state = VISITING;
		
	}
	
	public List<Node> getPredecessors() {
		return this.predecessors;
	}
	
	public boolean hasState(int s) {
		return this.state == s;
	}
	
	private boolean visit(Node self, Node visitor, int d) {
		System.out.println(visitor + " trying to visit " + self + " with depth " + d);

		//assert (state != VISITING) || (state == VISITING && d > depth) : "visit state invalid";
		if (state == CLOSED || ((state == OPEN || state == VISITING) && depth == d)) {
			System.out.println(visitor + " visiting " + self);

			sigma++;
			state = OPEN;
			depth = d;
			predecessors.add(visitor);
			return false;
		} else {
			return true;
		}
	}
	
	private void report(int d_w, int s_w) {
		delta += (int) (this.sigma * (1 + (d_w / (double) s_w)));
	}
	
}
