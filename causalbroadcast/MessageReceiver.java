package causalbroadcast;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

import java.text.SimpleDateFormat;

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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String rawMessageContent;
            while (messageCount < MAX_MESSAGES && (rawMessageContent = reader.readLine()) != null) {
                Message messageReceived = new Message(rawMessageContent, nodeId);
                String timestamp = sdf.format(new Date());
                System.out.println(String.format("[%s]Message received: {%s}", timestamp, rawMessageContent));
                messageCount += 1;
                Thread.sleep(random.nextInt(5));
                messageBroker.addMessageToQueue(messageReceived);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } 
    }
}
