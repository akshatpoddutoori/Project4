import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

final class ChatMessage implements Serializable {
    private static final long serialVersionUID = 6898543889087L;

    // Here is where you should implement the chat message object.
    // Variables, Constructors, Methods, etc.

    private String type;
    private String message;

    public ChatMessage() {
        super();
    }

    public ChatMessage (int type, String message) {
        if (type == 0) {
            this.type = "general";
        } else if (type == 1) {
            this.type = "logout";
        }
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
