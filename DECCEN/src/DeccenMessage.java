
import java.util.HashMap;
import java.util.Map;

import peersim.core.Node;


public class DeccenMessage {
	
	public enum Type { NOSP, REPORT }
	
	public enum Attachment {
		SENDER, SOURCE, DESTINATION, WEIGHT
	}
	
	private Map<Attachment, Object> attachments;
	public final Type type;
	
	private DeccenMessage(Type t) {
		type = t;
		attachments = new HashMap<Attachment, Object>(8);
	}
	
	public static DeccenMessage createNOSPMessage(Node sender, Node source, int weight) {
		DeccenMessage m = new DeccenMessage(Type.NOSP);
		m.attachments.put(Attachment.SENDER, sender);
		m.attachments.put(Attachment.SOURCE, source);
		m.attachments.put(Attachment.WEIGHT, weight);
		return m;
	}
	
	public static DeccenMessage createReportMessage(Node sender, Node source, Node destination, int weight) {
		DeccenMessage m = new DeccenMessage(Type.REPORT);
		m.attachments.put(Attachment.SENDER, sender);
		m.attachments.put(Attachment.SOURCE, source);
		m.attachments.put(Attachment.DESTINATION, destination);
		m.attachments.put(Attachment.WEIGHT, weight);
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
