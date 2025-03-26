package causalbroadcast;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageBroadcaster implements Runnable {
    private VectorClock vectorClock;
    private HashMap<Integer, PrintWriter> outputStreams; // Hash between nodeID and the output socket stream
    private HashMap<Integer, Socket> connectionHash;
    private final int nodeId;
    private final int MAX_MESSAGES = 100; // Sets the maximum messages expected.
    private final Random random = new Random();

    // Constructor
    public MessageBroadcaster(VectorClock vectorClock, ConnectionContext connectionContext) throws IOException {
        this.vectorClock = vectorClock;
        this.nodeId = connectionContext.getNodeId();
        this.outputStreams = connectionContext.getOutputWriterHash();
        this.connectionHash = connectionContext.getConnectionHash();
    }

    /**
     * Main Broadcaster method. Does the following actions:
     * 1. Go through all the sockets and create an output stream.
     * 2. Iteratively increment the vector clock value and prepare a message.
     * 3. Send the output messages to each of the streams
     */
    @Override
    public void run() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            for (int i = 0; i < MAX_MESSAGES; i++) {
                synchronized (vectorClock) {
                    vectorClock.increment(nodeId);
                }
                String messageContent = "Message no." + (i + 1) + " from " + nodeId;
                String rawMessageString = Message.createRawMessage(messageContent, vectorClock);
                String timestamp = sdf.format(new Date());
                System.out.println(String.format("[%s]Broadcasting: " + rawMessageString, timestamp));
                for (Map.Entry<Integer, PrintWriter> entry : outputStreams.entrySet()) {
                    try {
                        timestamp = sdf.format(new Date());
                        System.out.println(String.format("[%s]Broadcasting Message to [%d]", timestamp, entry.getKey()));
                        entry.getValue().println(rawMessageString); // Using the printwriter object to write to the
                                                                    // outputstream
                    } catch (Exception e) {
                        System.err.println("Failed to send message to process " + entry.getKey());
                    }
                }
                Thread.sleep(10);
                // Random wait to introduce message differences
                Thread.sleep(random.nextInt(10));
            }
            // Flush all the outputstreams
            for (Map.Entry<Integer, PrintWriter> entry : outputStreams.entrySet()) {
                String timestamp = sdf.format(new Date());
                System.out.println(String.format("[%s]Flushing comm channel to [%d]", timestamp, entry.getKey()));
                entry.getValue().flush();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Properly handle thread interruptions
            e.printStackTrace();
        }
    }
}