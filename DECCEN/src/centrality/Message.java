package centrality;

import java.util.HashMap;
import java.util.Map;

import peersim.core.Node;


public class Message {
	
	public enum Type { NOSP, REPORT, BFS_PROBE, BFS_REPORT }
	
	public enum Attachment {
		SENDER, SOURCE, DESTINATION, SP_COUNT, SP_LENGTH, STRESS_CONTRIBUTION, BETWEENNESS_CONTRIBUTION
	}
	
	private Map<Attachment, Object> attachments;
	public final Type type;
	
	private Message(Type t) {
		type = t;
		attachments = new HashMap<Attachment, Object>(8);
	}
	
	public static Message createNOSPMessage(Node sender, Node source, int count, int length) {
		Message m = new Message(Type.NOSP);
		m.attachments.put(Attachment.SENDER, sender);
		m.attachments.put(Attachment.SOURCE, source);
		m.attachments.put(Attachment.SP_COUNT, count);
		m.attachments.put(Attachment.SP_LENGTH, length);
		return m;
	}
	
	public static Message createReportMessage(Node sender, Node source, Node destination, int count, int length) {
		Message m = new Message(Type.REPORT);
		m.attachments.put(Attachment.SENDER, sender);
		m.attachments.put(Attachment.SOURCE, source);
		m.attachments.put(Attachment.DESTINATION, destination);
		m.attachments.put(Attachment.SP_COUNT, count);
		m.attachments.put(Attachment.SP_LENGTH, length);
		return m;
	}
	
	public static Message createProbeMessage(Node sender, Node source, int count, int length) {
		Message m = new Message(Type.BFS_PROBE);
		m.attachments.put(Attachment.SENDER, sender);
		m.attachments.put(Attachment.SOURCE, source);
		m.attachments.put(Attachment.SP_COUNT, count);
		m.attachments.put(Attachment.SP_LENGTH, length);
		return m;
	}
	
	public static Message createSampleReportMessage(Node sender, Node source, double bc, long sc, int count) {
		Message m = new Message(Type.BFS_REPORT);
		m.attachments.put(Attachment.SENDER, sender);
		m.attachments.put(Attachment.SOURCE, source);
		m.attachments.put(Attachment.BETWEENNESS_CONTRIBUTION, bc);
		m.attachments.put(Attachment.STRESS_CONTRIBUTION, sc);
		m.attachments.put(Attachment.SP_COUNT, count);
		return m;
	}
	
	public <T> T get(Attachment a, Class<T> c) {
		return c.cast(attachments.get(a));
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "[type=" + type + "][attachments=" + attachments + "]";
	}

}
