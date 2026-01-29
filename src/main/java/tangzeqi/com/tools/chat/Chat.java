package tangzeqi.com.tools.chat;

public interface Chat {


    void addMessage(String message, String root);

    void send(String message);

    boolean isShowing();
}
