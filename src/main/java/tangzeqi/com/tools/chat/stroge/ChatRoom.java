package tangzeqi.com.tools.chat.stroge;

public class ChatRoom {

    private String ip;
    private int port;
    private String creator;

    public ChatRoom(String ip, int port, String creator) {
        this.ip = ip;
        this.port = port;
        this.creator = creator;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getCreator() {
        return creator;
    }

}
