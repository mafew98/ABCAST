package causalbroadcast;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.PriorityBlockingQueue;

public class MessageBroker {

    private HashMap<Integer, Socket> connectionHash;
    protected VectorClock vectorClock;
    protected PriorityBlockingQueue<Message> messageQueue; // Shared message queue
    private final int MAX_PROCESSES = 4;
    private ConnectionContext connectionContext;
    private ArrayList<Thread> receiverThreads = new ArrayList<>();
    private Thread broadcasterThread;
    private int DELIVERY_COUNT = 0;

    // Constructor
    public MessageBroker(ConnectionContext connectionContext) {
        this.connectionHash = connectionContext.getConnectionHash();
        this.connectionContext = connectionContext;
        this.vectorClock = new VectorClock(MAX_PROCESSES); // initializing the vector clock for this node
        // create a minimum 100 message wide queue to handle deferred delivery
        this.messageQueue = new PriorityBlockingQueue<>(100, new MessageComparator());
    }

    /**
     * Starts receiver threads that handle all messages from one node (tied to one
     * socket)
     */
    public void startReceivers() {
        for (Map.Entry<Integer, Socket> entry : connectionHash.entrySet()) {
            Thread receiver = new MessageReceiver(entry.getKey(), connectionContext, this);
            receiver.start();
            receiverThreads.add(receiver);
        }
    }

    /**
     * Starts the broadcaster thread that broadcasts messages to all other nodes.
     * 
     * @throws IOException
     */
    public void startBroadcaster() throws IOException {
        broadcasterThread = new Thread(new MessageBroadcaster(vectorClock, connectionContext));
        broadcasterThread.start();
    }

    /**
     * Utility process to adds messages received to the common priority queue. The
     * priority queue is blocking to ensure thread safety.
     * 
     * @param message
     */
    protected void addMessageToQueue(Message message) {
        messageQueue.add(message);
        processMessages();
    }

    /**
     * Method to go through all the messages in the queue and deliver the eligible
     * ones.
     */
    private void processMessages() {
        ArrayList<Message> remainingMessages = new ArrayList<>(); // Temporary list to hold undeliverable messages
        while (!messageQueue.isEmpty()) {
            Message topMessage = messageQueue.poll(); // Take the message from the queue
            if (isDeliverable(topMessage)) {
                deliverMessage(topMessage);
            } else {
                System.out.println("Unable to deliver "
                        + Message.createRawMessage(topMessage.getMessageContent(), topMessage.getMessageClock())
                        + ". Current Clock: " + this.vectorClock.toString());
                remainingMessages.add(topMessage); // Store undeliverable messages
            }
        }
        // Reinsert undeliverable messages back into the queue
        messageQueue.addAll(remainingMessages);
    }

    /**
     * Compares the message at the top of the queue with the current vector clock to
     * determine if it is deliverable.
     * Synchronized method since multiple threads may access this.
     * 
     * @param message
     * @return boolean
     */
    private synchronized boolean isDeliverable(Message message) {
        return vectorClock.canDeliver(message.getMessageClock(), message.getNodeId());
    }

    /**
     * Increments the vector clock to indicate delivery and delivers a message.
     * 
     * @param message
     */
    private synchronized void deliverMessage(Message message) {
        System.out.println(
                "Delivered: " + message.createRawMessage(message.getMessageContent(), message.getMessageClock()));
        vectorClock.merge(message.getMessageClock());
        DELIVERY_COUNT++;
    }

    /**
     * Method to enforce waiting for working reader/writer threads to finish.
     * 
     * @throws InterruptedException
     */
    public void waitForCompletion() throws InterruptedException {
        for (Thread receiver : receiverThreads) {
            receiver.join(); // Wait for each receiver to finish
        }
        if (broadcasterThread != null) {
            broadcasterThread.join(); // Wait for broadcaster to finish
        }
        while (!messageQueue.isEmpty()) { // Ensuring that surplus buffered messages are processed
            processMessages();
        }

        System.out.println("Total Number of Messages Received: " + DELIVERY_COUNT);
    }

    /**
     * Accessor for vectorClock (synchronized)
     * 
     * @return
     */
    public synchronized VectorClock getVectorClock() {
        return vectorClock;
    }
}