package tangzeqi.com.service;

import lombok.SneakyThrows;
import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import static tangzeqi.com.service.ChatService.*;

public class MqttService {
    // 公共 MQTT 代理的地址
    private static volatile String BROKER_URL = "tcp://broker.hivemq.com:1883";
    // 订阅和发布消息的主题
    private static volatile String TOPIC = "Wchat/" + mqttroom;
    // 客户端的唯一标识符
    private static volatile String CLIENT_ID = ChatService.userName + System.currentTimeMillis();
    private static volatile MqttClient client;

    private static volatile Boolean open = false;

    public static void start(String room, String name) {
        try {
            if (open) {
                TOPIC = "Wchat/" + room;
                CLIENT_ID = name + System.currentTimeMillis();
                client.subscribe(TOPIC);
                sysMessage("您已进入公网频道，请勿泄露机密信息！");
                sysMessage("您已进入公网频道，请勿泄露机密信息！");
                sysMessage("您已进入公网频道，请勿泄露机密信息！");
                mqttStatus(true);
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
                sysMessage("您已进入公网频道，请勿泄露机密信息！");
                sysMessage("您已进入公网频道，请勿泄露机密信息！");
                sysMessage("您已进入公网频道，请勿泄露机密信息！");
                mqttStatus(true);
                open = true;
                // 订阅指定主题
                client.subscribe(TOPIC);
                // 设置消息回调，处理接收到的消息
                client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        sysMessage("您已退出公网频道");
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        String str = new String(message.getPayload());
                        String[] split = str.split(" ");
                        ChatService.chatMessage(str.replace(split[0] + " ", ""), split[0]);
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
            mqtt = false;
        }
    }

    public static void message(String str) {
        try {
            str = String.format("%s %s", userName, str);
            if (ObjectUtils.isNotEmpty(client)) {
                client.publish(TOPIC, new MqttMessage(str.getBytes()));
            }
        } catch (Throwable e) {
            sysMessage("公网信息发送失败");
            e.printStackTrace();
        }
    }

    public static void shutDowm() {
        try {
            client.unsubscribe(TOPIC);
            client.disconnect();
            client = null;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void out() {
        try {
            client.unsubscribe(TOPIC);
            mqttStatus(false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}
