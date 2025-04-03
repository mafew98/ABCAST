package totalbroadcast;

import java.util.Comparator;
import java.util.HashSet;
import java.util.concurrent.PriorityBlockingQueue;

public class SequencerQueue {

    private PriorityBlockingQueue<SequencedMessage> sequencedQueue = new PriorityBlockingQueue<SequencedMessage>(500, new SequenceComparator());
    
    public void addMessageToQueue(SequencedMessage sequencedMessage) {
        this.sequencedQueue.add(sequencedMessage);
    }

    public SequencedMessage peekSequenceQueue() {
        return sequencedQueue.peek();
    }

    public SequencedMessage pollSequenceQueue() {
        return sequencedQueue.poll();
    }

    public boolean isEmpty() {
        return this.sequencedQueue.isEmpty();
    }

    public HashSet<Message> sequencedSet() {
        HashSet<Message> hashSet = new HashSet<>();
        for (SequencedMessage sequencedMessage : sequencedQueue) {
            hashSet.add(sequencedMessage.getSequencedMessage());
        }
        return hashSet;
    }

}

class SequenceComparator implements Comparator<SequencedMessage> {
    @Override
    public int compare(SequencedMessage sm1, SequencedMessage sm2) {
        return Integer.compare(sm1.getSequenceNumber(), sm2.getSequenceNumber()); // Ascending order
    }
}
