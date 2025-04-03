package totalbroadcast;

import java.util.HashSet;
import java.util.concurrent.PriorityBlockingQueue;

public class MessageQueue {
    protected PriorityBlockingQueue<Message> messageQueue; // Shared message queue

    public MessageQueue(int initCapacity) {
        this.messageQueue = new PriorityBlockingQueue<>(initCapacity, new MessageComparator());
    }

    /**
     * Utility process to adds messages received to the common priority queue. The
     * priority queue is blocking to ensure thread safety.
     * 
     * @param message
     */
    public void addMessageToQueue(Message message) {
        messageQueue.add(message);
    }

    public Message pollMessageQueue() {
        return messageQueue.poll();
    }
    
    public Message peekMessageQueue() {
        return messageQueue.peek();
    }
    
    public boolean isMessageQueueEmpty() {
        return messageQueue.isEmpty();
    }

    /**
     * Here recreating the Queue is more efficient since removing a specific element from the queue is an O(n) operation.
     * @param elements
     */
    public void removeAllSetElements(HashSet<Message> elements) {
        for (Message message: elements) {
            this.messageQueue.remove(message);
        }
    }
}
