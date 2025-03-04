package tangzeqi.com.panel;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.*;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManagerEvent;
import com.intellij.ui.content.ContentManagerListener;
import com.intellij.util.IconUtil;
import com.intellij.util.ui.UIUtil;
import org.apache.batik.ext.swing.JGridBagPanel;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import tangzeqi.com.service.ChatService;
import tangzeqi.com.service.MqttService;
import tangzeqi.com.stroge.TextMessage;
import tangzeqi.com.utils.NetUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatPanel extends JPanel {

    private final JButton mqtt;
    private final JBTextField mqttroom;
    private final JBTextField serverIp;
    private final JBTextField serverPort;
    private final JButton button;
    private final JBTextField userName;
    private final JBTextField connectIp;
    private final JBTextField connectPort;
    private final JButton connect;

    private final JTextArea messageArea;
    private final JBTextField inputField;

    private final JTextArea userArea;

    private final GridBagConstraints gbc;

    public volatile Project project;

    public ChatPanel() {
        super(new GridBagLayout());
        String host = NetUtils.host();
        String port = String.valueOf(NetUtils.port());
        String topic = "88283";
        gbc = new GridBagConstraints();
        serverIp = new JBTextField(15);
        serverIp.setText(host);
        serverPort = new JBTextField(5);
        serverPort.setText(port);
        userName = new JBTextField(20);
        userName.setText("神秘用户");
        connectIp = new JBTextField(15);
        connectIp.setText(host);
        connectPort = new JBTextField(5);
        connectPort.setText(port);
        inputField = new JBTextField();
        messageArea = new JTextArea();
        userArea = new JTextArea();
        button = new JButton("创建聊天室");
        connect = new JButton("加入聊天室");
        mqttroom = new JBTextField(20);
        mqttroom.setText(topic);
        mqtt = new JButton("开启公网聊天");
        ///////////////////系统窗口////////////////////
        JGridBagPanel sys = sysPanel();
        resetGBC(gbc);
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 2.0;
        gbc.insets = new Insets(0, 5, 0, 5);
        gbc.fill = GridBagConstraints.BOTH;
        add(sys, gbc);
        ///////////////////聊天窗口////////////////////
        JGridBagPanel chat = chatPanel();
        resetGBC(gbc);
        gbc.gridy = 0;
        gbc.gridx = 1;
        gbc.weightx = 6.0;
        gbc.insets = new Insets(0, 5, 0, 5);
        gbc.fill = GridBagConstraints.BOTH;
        add(chat, gbc);
        /////////////////////配置窗口///////////////////////
        JGridBagPanel config = configPanel();
        resetGBC(gbc);
        gbc.gridy = 0;
        gbc.gridx = 2;
        gbc.weightx = 2.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(config, gbc);
        ChatService.chat = this;
        ChatService.sysMessage("检索到IP地址 " + host);
        ChatService.sysMessage("检索到可用端口 " + port);
        ChatService.sysMessage("预设公网频道号 " + topic);
    }

    private GridBagConstraints resetGBC(GridBagConstraints gbc) {
        if (ObjectUtils.isEmpty(gbc)) gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        return gbc;
    }

    private JGridBagPanel configPanel() {
        JGridBagPanel config = new JGridBagPanel();
        //MQTT频道编号
        mqttroom.setEditable(true);
        mqttroom.setToolTipText("公网频道编号");
        mqttroom.getDocument().addDocumentListener(limit(mqttroom, 20));
        resetGBC(gbc);
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 2.0;
        config.add(mqttroom, gbc);
        //MQTT启动
        mqtt.addActionListener(this::mqttStart);
        resetGBC(gbc);
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 2.0;
        config.add(mqtt, gbc);
        //系统行
        serverIp.setEditable(true);
        serverIp.setToolTipText("本地服务IP地址");
        serverIp.getDocument().addDocumentListener(limit(serverIp, 100));
        resetGBC(gbc);
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.weightx = 1.5;
        config.add(serverIp, gbc);
        serverPort.setEditable(true);
        serverPort.setToolTipText("本地服务端口号");
        serverPort.getDocument().addDocumentListener(limit(serverPort, 10));
        resetGBC(gbc);
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.gridy = 2;
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        config.add(serverPort, gbc);
        //系统启动
        button.addActionListener(this::serverStart);
        resetGBC(gbc);
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 2.0;
        config.add(button, gbc);
        //用户名称
        userName.setEditable(true);
        userName.setToolTipText("聊天室用户昵称");
        userName.getDocument().addDocumentListener(limit(userName, 100));
        resetGBC(gbc);
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 2.0;
        config.add(userName, gbc);
        //连接输入
        connectIp.setEditable(true);
        connectIp.setToolTipText("聊天室IP地址");
        connectIp.getDocument().addDocumentListener(limit(connectIp, 100));
        resetGBC(gbc);
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 1.5;
        config.add(connectIp, gbc);
        connectPort.setEditable(true);
        connectPort.setToolTipText("聊天室端口号");
        connectPort.getDocument().addDocumentListener(limit(connectPort, 10));
        resetGBC(gbc);
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.gridy = 5;
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        config.add(connectPort, gbc);
        //开始连接
        connect.addActionListener(this::connectStart);
        resetGBC(gbc);
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 2.0;
        config.add(connect, gbc);
        return config;
    }

    private void mqttStart(ActionEvent actionEvent) {
        String room = mqttroom.getText();
        if (ObjectUtils.isEmpty(room)) {
            ChatService.sysMessage("请填写公网频道编号！");
            return;
        }
        ChatService.mqttroom = room.trim();
        String name = userName.getText();
        if (ObjectUtils.isEmpty(name)) {
            ChatService.sysMessage("请填写聊天您的昵称！");
            return;
        }
        ChatService.userName = name.trim();
        mqttStatus(false, "接入公网中。。。");
        ChatService.mqttconnect();
    }

    private JGridBagPanel chatPanel() {
        JGridBagPanel chat = new JGridBagPanel();
        messageArea.setEditable(false);
        messageArea.setFont(UIUtil.getLabelFont());
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mouseClick(e, messageArea);
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
        resetGBC(gbc);
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weighty = 0.9;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.BOTH;
        chat.add(new JBScrollPane(messageArea), gbc);
        inputField.addActionListener(this::sendMessage);
        inputField.setEditable(true);
        inputField.getDocument().addDocumentListener(limit(inputField, 1000));
        resetGBC(gbc);
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weighty = 0.1;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        chat.add(inputField, gbc);
        addMessage("欢迎来到聊天插件!", "系统");
        return chat;
    }

    private void mouseClick(MouseEvent e, JTextArea area) {
        int i = area.viewToModel(new Point(e.getX(), e.getY()));
        String regex = "\\[(.+?)\\]\\s(.+\\..+)\\:(\\d+)（点击跳转）\\n";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(area.getText());
        String file;
        int line;
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            if (start <= i && end >= i) {
                file = matcher.group(2);
                line = Integer.parseInt(matcher.group(3));
                ChatService.openFileLine(file, line);
                break;
            }
        }
    }

    private JGridBagPanel sysPanel() {
        JGridBagPanel sys = new JGridBagPanel();
        userArea.setEditable(false);
        userArea.setFont(UIUtil.getLabelFont());
        userArea.setLineWrap(true);
        userArea.setWrapStyleWord(true);
        resetGBC(gbc);
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.BOTH;
        sys.add(new JBScrollPane(userArea), gbc);
        return sys;
    }

    private void sendMessage(ActionEvent e) {
        String message = inputField.getText().trim();
        if (!ChatService.connect && !ChatService.mqtt) {
            ChatService.sysMessage("未加入聊天室或未启用公网频道");
        } else if (!message.isEmpty()) {
            if (ChatService.connect) {
                TextMessage textMessage = new TextMessage();
                textMessage.setMessage(message);
                textMessage.setName(ChatService.userName);
                ChatService.customerHandler.sendMessage(textMessage);
            }
            if (ChatService.mqtt) {
                MqttService.message(message);
            }
            inputField.setText("");
        }
    }

    public void addMessage(String message, String sender) {
        String formattedMessage = String.format("[%s] %s\n", sender, message);
        messageArea.append(formattedMessage);
        messageArea.setCaretPosition(messageArea.getDocument().getLength());
    }

    public void addSysMessage(String message, String sender) {
        String formattedMessage = String.format("[%s] %s\n", sender, message);
        userArea.append(formattedMessage);
        userArea.setCaretPosition(userArea.getDocument().getLength());
    }


    //系统连接程序
    private void connectStart(ActionEvent actionEvent) {
        String name = userName.getText();
        if (ObjectUtils.isEmpty(name)) {
            ChatService.sysMessage("请填写聊天您的昵称！");
            return;
        }
        ChatService.userName = name.trim();
        String ip = connectIp.getText();
        if (ObjectUtils.isEmpty(ip)) {
            ChatService.sysMessage("请指定连接服务器IP！");
            return;
        }
        ChatService.connectIp = ip.trim();
        String port = connectPort.getText();
        if (ObjectUtils.isEmpty(port)) {
            ChatService.sysMessage("请指定连接服务器端口号！");
            return;
        }
        ChatService.connectPort = port.trim();
        connectStatus(false, "处理中。。。");
        ChatService.connect();
    }

    //系统启动程序
    private void serverStart(ActionEvent actionEvent) {
        String ip = serverIp.getText();
        if (ObjectUtils.isEmpty(ip)) {
            ChatService.sysMessage("请指定本地服务器IP！");
            return;
        }
        ChatService.serverIp = ip.trim();
        String port = serverPort.getText();
        if (ObjectUtils.isEmpty(port)) {
            ChatService.sysMessage("请指定本地服务器端口号！");
            return;
        }
        ChatService.serverPort = port.trim();
        serverStatus(false, "处理中。。。");
        ChatService.start();
    }

    public void serverStatus(Boolean cenClick, String text) {
        if (ObjectUtils.isNotEmpty(button)) {
            button.setEnabled(cenClick);
            button.setText(text);
        }
    }

    public void connectStatus(Boolean cenClick, String text) {
        if (ObjectUtils.isNotEmpty(connect)) {
            connect.setEnabled(cenClick);
            connect.setText(text);
        }
    }

    public void mqttStatus(Boolean cenClick, String text) {
        if (ObjectUtils.isNotEmpty(mqtt)) {
            mqtt.setEnabled(cenClick);
            mqtt.setText(text);
        }
    }

    private DocumentListener limit(JBTextField text, int len) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (ObjectUtils.isNotEmpty(text.getText()) && text.getText().length() > len) {
                    SwingUtilities.invokeLater(() -> {
                        try {
                            text.setText(text.getText(0, len));
                            text.setSelectionStart(text.getText().length());
                            text.setSelectionEnd(text.getText().length());
                        } catch (BadLocationException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
                    Toolkit.getDefaultToolkit().beep();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        };
    }

    public void inputFieldPost(String str) {
        inputField.setText(str);
        inputField.postActionEvent();
        inputField.setText("");
    }

    public static void register(@NotNull Project project) {
        ToolWindowManager window = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = window.getToolWindow("Chat");
        if (ObjectUtils.isEmpty(toolWindow)) {
            ChatPanel chatPanel = new ChatPanel();
            chatPanel.project = project;
            window.registerToolWindow(RegisterToolWindowTask.closable("Chat", IconUtil.getEditIcon(), ToolWindowAnchor.BOTTOM));
            toolWindow = window.getToolWindow("Chat");
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            panel.add(chatPanel, gbc);
            toolWindow.getComponent().add(panel);
        } else {
            toolWindow.show();
        }
    }
}
