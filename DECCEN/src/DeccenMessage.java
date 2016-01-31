
import java.util.HashMap;
import java.util.Map;

import peersim.core.Node;


public class DeccenMessage implements Message {
	
	public enum Type { NOSP, REPORT }
	
	public enum Attachment {
		SENDER, SOURCE, DESTINATION, SP_COUNT, SP_LENGTH
	}
	
	private Map<Attachment, Object> attachments;
	public final Type type;
	
	private DeccenMessage(Type t) {
		type = t;
		attachments = new HashMap<Attachment, Object>(8);
	}
	
	public static DeccenMessage createNOSPMessage(Node sender, Node source, int count, int length) {
		DeccenMessage m = new DeccenMessage(Type.NOSP);
		m.attachments.put(Attachment.SENDER, sender);
		m.attachments.put(Attachment.SOURCE, source);
		m.attachments.put(Attachment.SP_COUNT, count);
		m.attachments.put(Attachment.SP_LENGTH, length);
		return m;
	}
	
	public static DeccenMessage createReportMessage(Node sender, Node source, Node destination, int count, int length) {
		DeccenMessage m = new DeccenMessage(Type.REPORT);
		m.attachments.put(Attachment.SENDER, sender);
		m.attachments.put(Attachment.SOURCE, source);
		m.attachments.put(Attachment.DESTINATION, destination);
		m.attachments.put(Attachment.SP_COUNT, count);
		m.attachments.put(Attachment.SP_LENGTH, length);
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
