package centrality;

import java.util.Iterator;

public interface CycleBasedDispatcherSupport<T> {
	
	interface SendQueueEntry<T> {
		T getMessage();
		CycleBasedDispatcherSupport<T> getDestination();
	}
	
	void addToSendQueue(T message, CycleBasedDispatcherSupport<T> destination);
	
	boolean hasOutgoingMessages();
	
	void addToIncoming(T message);
	
	Iterator<T> getIncomingMessagesIterator();
	
	Iterator<SendQueueEntry<T>> getSendQueueIterator();

}
