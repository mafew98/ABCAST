package causalbroadcast;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Random;

public class MessageReceiver extends Thread {
    private BufferedReader reader;
    private Integer nodeId;
    private MessageBroker messageBroker;
    private int messageCount = 0;
    private static final int MAX_MESSAGES = 100;  // Sets the maximum messages expected.
    private ConnectionContext connectionContext;
    private final Random random = new Random();

    /**
     * Constructor method
     * 
     * @param nodeId
     * @param connectionContext
     * @param messageBroker
     */
    public MessageReceiver(int nodeId, ConnectionContext connectionContext, MessageBroker messageBroker) {
        this.nodeId = nodeId; // receiver Node ID
        this.messageBroker = messageBroker; // parent object to delegate message queue access
        this.connectionContext = connectionContext;
        this.reader = connectionContext.getInputReaderHash().get(nodeId);
    }

    /**
     * Method to receive a message and trigger it's handling and processing.
     * Ends all input only to the socket to ensure that the socket remains up but no
     * messages are received.
     */
    @Override
    public void run() {
        try {
            String rawMessageContent;
            while (messageCount < MAX_MESSAGES && (rawMessageContent = reader.readLine()) != null) {
                Message messageReceived = new Message(rawMessageContent, nodeId);
                messageCount += 1;
                Thread.sleep(random.nextInt(5));
                messageBroker.addMessageToQueue(messageReceived);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                connectionContext.getConnectionHash().get(nodeId).shutdownInput(); // Stopping all input only to the
                                                                                   // socket.
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
