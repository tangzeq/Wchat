package tangzeqi.com.service;

public interface Chat {


    void addMessage(String message, String root);

    void send(String message);

    boolean isShowing();
}
