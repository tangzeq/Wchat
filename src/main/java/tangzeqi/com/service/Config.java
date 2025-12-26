package tangzeqi.com.service;

public interface Config {
    void serverStatus(boolean b, String buttonTitle);

    void connectStatus(boolean b, String buttonTitle);

    void addSysMessage(String message, String root);

    void mqttStatus(boolean b, String buttonTitle);

    void updconnectStatus(boolean b, String buttonTitle);
}
