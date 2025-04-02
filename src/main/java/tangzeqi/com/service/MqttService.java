package tangzeqi.com.service;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import tangzeqi.com.project.MyProject;

public class MqttService {
    private final String project;
    // 公共 MQTT 代理的地址
    private volatile String BROKER_URL = "tcp://broker.hivemq.com:1883";
    // 订阅和发布消息的主题
    private volatile String TOPIC = "";
    // 客户端的唯一标识符
    private volatile String CLIENT_ID = "";
    private volatile MqttClient client;

    private volatile Boolean open = false;

    public MqttService(String project) {
        this.project = project;
    }

    public void start(String room, String name) {
        TOPIC = "Wchat/" + MyProject.cache(project).mqttroom;
        CLIENT_ID = MyProject.cache(project).userName + System.currentTimeMillis();
        try {
            if (open) {
                TOPIC = "Wchat/" + room;
                CLIENT_ID = name + System.currentTimeMillis();
                client.subscribe(TOPIC);
                MyProject.cache(project).sysMessage("您已进入公网频道，请勿泄露机密信息！");
                MyProject.cache(project).sysMessage("您已进入公网频道，请勿泄露机密信息！");
                MyProject.cache(project).sysMessage("您已进入公网频道，请勿泄露机密信息！");
                MyProject.cache(project).mqttStatus(true);
            } else {
                TOPIC = "Wchat/" + room;
                CLIENT_ID = name + System.currentTimeMillis();
                // 创建 MQTT 客户端实例，指定代理地址、客户端 ID 和持久化方式
                client = new MqttClient(BROKER_URL, CLIENT_ID, new MemoryPersistence());
                // 配置连接选项
                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setCleanSession(true);
                // 连接到 MQTT 代理
                client.connect(connOpts);
                MyProject.cache(project).sysMessage("您已进入公网频道，请勿泄露机密信息！");
                MyProject.cache(project).sysMessage("您已进入公网频道，请勿泄露机密信息！");
                MyProject.cache(project).sysMessage("您已进入公网频道，请勿泄露机密信息！");
                MyProject.cache(project).mqttStatus(true);
                open = true;
                // 订阅指定主题
                client.subscribe(TOPIC);
                // 设置消息回调，处理接收到的消息
                client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        MyProject.cache(project).sysMessage("您已退出公网频道");
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        String str = new String(message.getPayload());
                        String[] split = str.split(" ");
                        MyProject.cache(project).chatMessage(str.replace(split[0] + " ", ""), split[0]);
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        System.out.println("Message delivered");
                    }
                });
            }
        } catch (Throwable e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
            open = false;
            MyProject.cache(project).mqtt = false;
        }
    }

    public void message(String str) {
        try {
            str = String.format("%s %s", MyProject.cache(project).userName, str);
            if (ObjectUtils.isNotEmpty(client)) {
                client.publish(TOPIC, new MqttMessage(str.getBytes()));
            }
        } catch (Throwable e) {
            MyProject.cache(project).sysMessage("公网信息发送失败");
            e.printStackTrace();
        }
    }

    public void shutDowm() {
        try {
            client.unsubscribe(TOPIC);
            client.disconnect();
            client = null;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void out() {
        try {
            client.unsubscribe(TOPIC);
            MyProject.cache(project).mqttStatus(false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}
