
import java.io.Serializable;

final class ChatMessage implements Serializable {
    private static final long serialVersionUID = 6898543889087L;

    // Here is where you should implement the chat message object.
    // Variables, Constructors, Methods, etc.

    private MessageType type;
    private String message;
    private String recipient;

    enum MessageType {
        CONNECTED,  // -1
        GENERAL,    // 0
        LOGOUT,     // 1
        DIRECT,     // 2
        LIST        // 3
    }

//    public ChatMessage() {
//        super();
//    }

    public ChatMessage (int type, String message) {
        if (type == -1)
            this.type = MessageType.CONNECTED;
        else if (type == 0)
            this.type = MessageType.GENERAL;
        else if (type == 1)
            this.type = MessageType.LOGOUT;
        else if (type == 3)
            this.type = MessageType.LIST;

        this.message = message;
        this.recipient = null;
    }

    public ChatMessage (int type, String recipient, String message) {
        if (type == 2)
            this.type = MessageType.DIRECT;

        this.message = message;
        this.recipient = recipient;
    }

    public String getMessage() {
        return message;
    }

    public MessageType getType() {
        return type;
    }

    public String getRecipient() {
        return recipient;
    }
}
