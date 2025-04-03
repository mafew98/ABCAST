package totalbroadcast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SequencedMessage {
    private int messageSequenceNo;
    private Message sequencedMessage;

    public SequencedMessage(int messageSequenceNo, Message sequencedMessage) {
        this.sequencedMessage = sequencedMessage;
        this.messageSequenceNo = messageSequenceNo;
    }

    public SequencedMessage(String rawMessage) {
        String[] messageParts = rawMessage.split("-", 2);
        this.messageSequenceNo = Integer.parseInt(messageParts[0]);
        this.sequencedMessage = new Message(messageParts[1]);
    }

    public int getSequenceNumber() {
        return this.messageSequenceNo;
    }

    public Message getSequencedMessage() {
        return this.sequencedMessage;
    }

    public static boolean isSequencedMessage(String message) {
        String regex = "(\\d+)\\-(.*)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);
        return matcher.matches();
    }

    public String toString() {
        return Integer.toString(this.messageSequenceNo) + Message.createRawMessage(sequencedMessage.getMessageContent(), sequencedMessage.getMessageClock());
    }

    public static String createSequencedRawMessage(Message message, int sequenceNo) {
        return (sequenceNo + "-" + Message.createRawMessage(message.getMessageContent(), message.getMessageClock()));
    }
}
