package tangzeqi.com.tools.chat.panel;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.lang3.ObjectUtils;
import tangzeqi.com.project.MyProject;
import tangzeqi.com.tools.chat.Config;
import tangzeqi.com.utils.NetUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import static tangzeqi.com.utils.PanelUtils.resetGBC;
import static tangzeqi.com.utils.PanelUtils.textLimit;

public class ConfigPanel extends JPanel implements Config {
    private final String project;
    private final JButton mqtt;
    private final JBTextField mqttroom;
    private final JBTextField serverIp;
    private final JBTextField serverPort;
    private final JButton button;
    private final JBTextField userName;
    private final JBTextField connectIp;
    private final JBTextField connectPort;
    private final JButton connect;

    private final JTextArea userArea;

    private GridBagConstraints gbc = new GridBagConstraints();

    private final JButton updconnect;

    public ConfigPanel(String project) {
        super(new BorderLayout());
        this.project = project;
        String host = NetUtils.host();
        String port = String.valueOf(NetUtils.port());
        String topic = "88283";
        serverIp = new JBTextField();
        serverIp.setText(host);
        serverPort = new JBTextField();
        serverPort.setText(port);
        userName = new JBTextField();
        userName.setText("神秘用户");
        connectIp = new JBTextField();
        connectIp.setText(host);
        connectPort = new JBTextField();
        connectPort.setText(port);
        button = new JButton("创建聊天室");
        connect = new JButton("加入聊天室");
        mqttroom = new JBTextField();
        mqttroom.setText(topic);
        mqtt = new JButton("开启公网聊天");
        userArea = new JTextArea();
        updconnect = new JButton("启用局域网广播");
        ///////////////////系统窗口////////////////////
        sysPanel();
        /////////////////////配置窗口///////////////////////
        configPanel();
        MyProject.cache(project).config = this;
        MyProject.cache(project).sysMessage("检索到IP地址 " + host);
        MyProject.cache(project).sysMessage("检索到可用端口 " + port);
        MyProject.cache(project).sysMessage("预设公网频道号 " + topic);
    }

    private void configPanel() {
        int width = 300;
        int height = 30;
        JPanel config = new JPanel(new GridBagLayout());
//        //MQTT频道编号
//        mqttroom.setEditable(true);
//        mqttroom.setToolTipText("公网频道编号");
//        mqttroom.getDocument().addDocumentListener(textLimit(mqttroom, 20));
//        initSize(mqttroom, width, height);
//        gbc = resetGBC(gbc);
//        gbc.gridy = 0;
//        gbc.gridx = 0;
//        config.add(mqttroom, gbc);
//        //MQTT启动
//        mqtt.addActionListener(this::mqttStart);
//        initSize(mqtt, width, height);
//        gbc = resetGBC(gbc);
//        gbc.gridy = 1;
//        gbc.gridx = 0;
//        config.add(mqtt, gbc);
        //系统行
        JPanel server = new JPanel(new BorderLayout());
        initSize(server, width, height);
        serverIp.setEditable(true);
        serverIp.setToolTipText("本地服务IP地址");
        serverIp.getDocument().addDocumentListener(textLimit(serverIp, 100));
        server.add(serverIp, BorderLayout.CENTER);
        serverPort.setEditable(true);
        serverPort.setToolTipText("本地服务端口号");
        serverPort.getDocument().addDocumentListener(textLimit(serverPort, 10));
        server.add(serverPort, BorderLayout.EAST);
        gbc = resetGBC(gbc);
        gbc.gridy = 2;
        gbc.gridx = 0;
        config.add(server, gbc);
        //系统启动
        button.addActionListener(this::serverStart);
        initSize(button, width, height);
        gbc = resetGBC(gbc);
        gbc.gridy = 3;
        gbc.gridx = 0;
        config.add(button, gbc);
        //用户名称
        userName.setEditable(true);
        userName.setToolTipText("聊天室用户昵称");
        userName.getDocument().addDocumentListener(textLimit(userName, 100));
        userName.addActionListener(this::userNameListener);
        initSize(userName, width, height);
        gbc = resetGBC(gbc);
        gbc.gridy = 4;
        gbc.gridx = 0;
        config.add(userName, gbc);
        //连接输入
        JPanel nect = new JPanel(new BorderLayout());
        initSize(nect, width, height);
        connectIp.setEditable(true);
        connectIp.setToolTipText("聊天室IP地址");
        connectIp.getDocument().addDocumentListener(textLimit(connectIp, 100));
        nect.add(connectIp, BorderLayout.CENTER);
        connectPort.setEditable(true);
        connectPort.setToolTipText("聊天室端口号");
        connectPort.getDocument().addDocumentListener(textLimit(connectPort, 10));
        nect.add(connectPort, BorderLayout.EAST);
        gbc = resetGBC(gbc);
        gbc.gridy = 5;
        gbc.gridx = 0;
        config.add(nect, gbc);
        //开始连接
        connect.addActionListener(this::connectStart);
        initSize(connect, width, height);
        gbc = resetGBC(gbc);
        gbc.gridy = 6;
        gbc.gridx = 0;
        config.add(connect, gbc);
//        //局域网广播
//        updconnect.addActionListener(this::updconnectStart);
//        initSize(updconnect, width, height);
//        gbc = resetGBC(gbc);
//        gbc.gridy = 7;
//        gbc.gridx = 0;
//        config.add(updconnect, gbc);

        config.setPreferredSize(new Dimension(width, gbc.gridy * height));
        config.setMinimumSize(new Dimension(width, gbc.gridy * height));
        add(config, BorderLayout.WEST);
    }

    private void userNameListener(ActionEvent actionEvent) {
        String name = userName.getText();
        if (ObjectUtils.isNotEmpty(name) && ObjectUtils.isNotEmpty(name.trim())) {
            MyProject.cache(project).userName = name.trim();
        }
    }

    private void updconnectStart(ActionEvent actionEvent) {
        String name = userName.getText();
        if (ObjectUtils.isEmpty(name)) {
            MyProject.cache(project).sysMessage("请填写聊天您的昵称！");
            userName.requestFocusInWindow();
            return;
        }
        MyProject.cache(project).userName = name.trim();
        updconnectStatus(false, "处理中。。。");
        MyProject.cache(project).updconnect();
    }

    @Override
    public void updconnectStatus(boolean cenClick, String text) {
        if (ObjectUtils.isNotEmpty(updconnect)) {
            updconnect.setEnabled(cenClick);
            updconnect.setText(text);
        }
    }


    private void mqttStart(ActionEvent actionEvent) {
        String room = mqttroom.getText();
        if (ObjectUtils.isEmpty(room)) {
            MyProject.cache(project).sysMessage("请填写公网频道编号！");
            return;
        }
        MyProject.cache(project).mqttroom = room.trim();
        String name = userName.getText();
        if (ObjectUtils.isEmpty(name)) {
            MyProject.cache(project).sysMessage("请填写聊天您的昵称！");
            return;
        }
        MyProject.cache(project).userName = name.trim();
        mqttStatus(false, "接入公网中。。。");
        MyProject.cache(project).mqttconnect();
    }

    private void sysPanel() {
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
        add(new JBScrollPane(userArea), BorderLayout.CENTER);
    }

    @Override
    public void addSysMessage(String message, String sender) {
        String formattedMessage = String.format("[%s] %s\n", sender, message);
        userArea.append(formattedMessage);
        userArea.setCaretPosition(userArea.getDocument().getLength());
    }


    //系统连接程序
    private void connectStart(ActionEvent actionEvent) {
        String name = userName.getText();
        if (ObjectUtils.isEmpty(name)) {
            MyProject.cache(project).sysMessage("请填写聊天您的昵称！");
            userName.requestFocusInWindow();
            return;
        }
        MyProject.cache(project).userName = name.trim();
        String ip = connectIp.getText();
        if (ObjectUtils.isEmpty(ip)) {
            MyProject.cache(project).sysMessage("请指定连接服务器IP！");
            connectIp.requestFocusInWindow();
            return;
        }
        MyProject.cache(project).connectIp = ip.trim();
        String port = connectPort.getText();
        if (ObjectUtils.isEmpty(port)) {
            MyProject.cache(project).sysMessage("请指定连接服务器端口号！");
            connectPort.requestFocusInWindow();
            return;
        }
        MyProject.cache(project).connectPort = port.trim();
        connectStatus(false, "处理中。。。");
        MyProject.cache(project).connect();
    }

    //系统启动程序
    private void serverStart(ActionEvent actionEvent) {
        String ip = serverIp.getText();
        if (ObjectUtils.isEmpty(ip)) {
            MyProject.cache(project).sysMessage("请指定本地服务器IP！");
            serverIp.requestFocusInWindow();
            return;
        }
        MyProject.cache(project).serverIp = ip.trim();
        String port = serverPort.getText();
        if (ObjectUtils.isEmpty(port)) {
            MyProject.cache(project).sysMessage("请指定本地服务器端口号！");
            serverPort.requestFocusInWindow();
            return;
        }
        MyProject.cache(project).serverPort = port.trim();
        serverStatus(false, "处理中。。。");
        MyProject.cache(project).start();
    }

    @Override
    public void serverStatus(boolean cenClick, String text) {
        if (ObjectUtils.isNotEmpty(button)) {
            button.setEnabled(cenClick);
            button.setText(text);
        }
    }

    @Override
    public void connectStatus(boolean cenClick, String text) {
        if (ObjectUtils.isNotEmpty(connect)) {
            connect.setEnabled(cenClick);
            connect.setText(text);
        }
    }

    @Override
    public void mqttStatus(boolean cenClick, String text) {
        if (ObjectUtils.isNotEmpty(mqtt)) {
            mqtt.setEnabled(cenClick);
            mqtt.setText(text);
        }
    }

    public static Content content(String project) {
        ConfigPanel configPanel = new ConfigPanel(project);
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
        panel.add(configPanel, gbc);
        ContentFactory contentFactory = ContentFactory.getInstance();
        return contentFactory.createContent(panel, "config", false);
    }

    private void initSize(Component c, Integer width, Integer height) {
        if (!ObjectUtils.isEmpty(c)) {
            width = ObjectUtils.isEmpty(width) ? c.getPreferredSize().width : width;
            height = ObjectUtils.isEmpty(height) ? c.getPreferredSize().height : height;
            c.setPreferredSize(new Dimension(width, height));
            c.setMinimumSize(new Dimension(width, height));
            c.setMaximumSize(new Dimension(width, height));
        }
    }
}
