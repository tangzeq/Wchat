package tangzeqi.com.ui.chat;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.notification.*;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import tangzeqi.com.project.MyProject;
import tangzeqi.com.tools.chat.Chat;
import tangzeqi.com.tools.chat.Config;
import tangzeqi.com.tools.chat.handler.MessageCellRenderer;
import tangzeqi.com.tools.chat.stroge.BaseUser;
import tangzeqi.com.tools.chat.stroge.ChatRoom;
import tangzeqi.com.tools.chat.stroge.TextMessage;
import tangzeqi.com.ui.MyPanel;
import tangzeqi.com.utils.NetUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static tangzeqi.com.tools.chat.enums.UiConstantEnum.SYS;

public class ChatRoomPanel extends JPanel implements Config, Chat, MyPanel {
    private final String project;

    private JPanel chatRoomPanel;
    private JPanel leftPanel;
    private JPanel createChatPanel;
    private JTextField ipField;
    private JTextField portField;
    private JButton createChatButton;
    private JButton joinChatButton;
    private JPanel personalInfoPanel;
    private JTextField nicknameField;
    private JPanel centerPanel;
    private JScrollPane chatMessageScroll;
    private JList<BaseUser> chatMessageList;
    private JPanel inputPanel;
    private JTextField messageField;
    private JButton sendButton;
    private JScrollPane chatRoomScroll;
    private JList<String> chatRoomList;

    // 聊天消息列表
    private Vector<BaseUser> chatMessages = new Vector<>();
    private ScheduledExecutorService chatMessagesListUpdater;
    private static final int MAX_MESSAGES = 1000;
    // 聊天室列表
    private Vector<ChatRoom> chatRooms = new Vector<>();
    private ScheduledExecutorService chatRoomListUpdater;
    // 当前聊天室
    private ChatRoom currentChatRoom;


    public ChatRoomPanel(String project) {
        this.project = project;
        $$$setupUI$$$();
        initializeConfig();
        setupEventHandlers();
        startChatRoomListUpdater();
        MyProject.cache(project).chat = this;
        MyProject.cache(project).config = this;
    }

    public ChatRoomPanel() {
        this.project = null;
        $$$setupUI$$$();
    }

    private void initializeConfig() {
        String host = NetUtils.host();
        String port = String.valueOf(NetUtils.port());
        ipField.setText(host);
        portField.setText(port);
        // 设置聊天消息列表的渲染器和属性
        chatMessageList.setCellRenderer(new MessageCellRenderer());
        chatMessageList.setVisibleRowCount(-1); // 允许显示任意行数
        chatMessageList.setPrototypeCellValue(null); // 清除原型值
        chatMessageList.setLayoutOrientation(JList.VERTICAL);
        // 使用自定义的布局管理器来处理不同高度的单元格
        chatMessageList.setLayout(new BoxLayout(chatMessageList, BoxLayout.Y_AXIS));

        // 设置所有滚动面板的滚动条策略和大小策略
        setupScrollPanes();
    }

    private void setupScrollPanes() {
        // 设置聊天消息滚动面板
        chatMessageScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatMessageScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        chatMessageScroll.setPreferredSize(null); // 移除首选大小，让其自动适应父容器

        // 设置聊天室列表滚动面板
        chatRoomScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatRoomScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        chatRoomScroll.setPreferredSize(null); // 移除首选大小，让其自动适应父容器

        // 确保列表能够随滚动面板一起调整大小
        setComponentSizePolicy(chatMessageList);
        setComponentSizePolicy(chatRoomList);
    }

    /**
     * 设置组件的大小策略，使其能够随父容器一起调整大小
     */
    private void setComponentSizePolicy(JComponent component) {
        component.setPreferredSize(null); // 移除首选大小
        component.setMaximumSize(null); // 移除最大大小
        component.setMinimumSize(null); // 移除最小大小
        // 对于列表，确保其能够显示多行
        if (component instanceof JList) {
            JList<?> list = (JList<?>) component;
            list.setVisibleRowCount(-1); // 允许显示任意行数
        }
    }

    private void setupEventHandlers() {
        // 聊天室事件处理
        createChatButton.addActionListener(e -> {
            String ip = ipField.getText();
            String port = portField.getText();
            String nickName = nicknameField.getText();
            if (nickName.trim().isEmpty()) {
                addChatMessage(SYS.getValue(), "昵称不能为空！");
                nicknameField.requestFocusInWindow();
            } else if (ip.trim().isEmpty()) {
                addChatMessage(SYS.getValue(), "IP不能为空！");
                ipField.requestFocusInWindow();
            } else if (port.trim().isEmpty()) {
                addChatMessage(SYS.getValue(), "端口不能为空！");
                portField.requestFocusInWindow();
            } else {
                createChatRoom(ip, Integer.parseInt(port));
            }
        });

        joinChatButton.addActionListener(e -> {
            String ip = ipField.getText();
            String port = portField.getText();
            String nickName = nicknameField.getText();
            if (nickName.trim().isEmpty()) {
                addChatMessage(SYS.getValue(), "昵称不能为空！");
                nicknameField.requestFocusInWindow();
            } else if (ip.trim().isEmpty()) {
                addChatMessage(SYS.getValue(), "IP不能为空！");
                ipField.requestFocusInWindow();
            } else if (port.trim().isEmpty()) {
                addChatMessage(SYS.getValue(), "端口不能为空！");
                portField.requestFocusInWindow();
            } else {
                joinChatRoom(ip, Integer.parseInt(port));
            }
        });

        nicknameField.addActionListener(e -> {
            String name = nicknameField.getText();
            if (ObjectUtils.isNotEmpty(name) && ObjectUtils.isNotEmpty(name.trim())) {
                MyProject.cache(project).userName = name.trim();
            }
        });

        sendButton.addActionListener(e -> {
            String message = messageField.getText();
            if (!message.isEmpty()) {
                sendMessage(message);
                messageField.setText("");
            }
        });

        messageField.addActionListener(e -> {
            String message = messageField.getText();
            if (!message.isEmpty()) {
                sendMessage(message);
                messageField.setText("");
            }
        });

        // 聊天消息区域点击事件
        chatMessageList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int i = chatMessageList.locationToIndex(e.getPoint());
                    TextMessage message = (TextMessage) chatMessages.get(i);
                    String regex = "([^:]+):(\\d+)（点击跳转）";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(message.getMessage());
                    String file;
                    int line;
                    while (matcher.find()) {
                        int start = matcher.start();
                        int end = matcher.end();
                        file = matcher.group(1);
                        line = Integer.parseInt(matcher.group(2));
                        MyProject.cache(project).openFileLine(file, line);
                        break;
                    }
                }
            }
        });

        // 聊天室列表双击事件
        chatRoomList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = chatRoomList.locationToIndex(e.getPoint());
                    ChatRoom selected = chatRooms.get(index);
                    joinChatRoom(selected.getIp(), selected.getPort());
                }
            }
        });
    }

    // 聊天室相关方法
    private void createChatRoom(String ip, int port) {
        MyProject.cache(project).serverIp = ip;
        MyProject.cache(project).serverPort = port + "";
        MyProject.cache(project).userName = nicknameField.getText();
        MyProject.cache(project).start();
        if (!MyProject.cache(project).start) {
            ChatRoom room = new ChatRoom(ip, port, nicknameField.getText());
            chatRooms.add(room);
            updateChatRoomList();
            currentChatRoom = room;
        } else {
            chatRooms.removeIf(room -> room.getIp().equals(ip) && room.getPort() == port);
            updateChatRoomList();
        }
        addChatMessage(SYS.getValue(), "正在刷新聊天室列表");
    }

    private void joinChatRoom(String ip, int port) {
        for (ChatRoom room : chatRooms) {
            if (room.getIp().equals(ip) && room.getPort() == port) {
                MyProject.cache(project).connectIp = ip;
                MyProject.cache(project).connectPort = port + "";
                MyProject.cache(project).userName = nicknameField.getText();
                MyProject.cache(project).connect();
                currentChatRoom = room;
                return;
            }
        }
        addChatMessage(SYS.getValue(), "未找到该聊天室");
    }

    private void sendMessage(String message) {
        if (currentChatRoom != null) {
            if (!MyProject.cache(project).connect && !MyProject.cache(project).mqtt && !MyProject.cache(project).upd) {
                addMessage("未加入聊天室或未启用公网频道!", SYS.getValue());
            } else if (!message.isEmpty()) {
                sendButton.setEnabled(false);
                sendButton.setText("发送中...");
                if (MyProject.cache(project).connect) {
                    TextMessage textMessage = new TextMessage();
                    textMessage.setMessage(message);
                    textMessage.setName(MyProject.cache(project).userName);
                    MyProject.cache(project).customerHandler.send(textMessage);
                }
                if (MyProject.cache(project).mqtt) {
                    MyProject.cache(project).mqttService.message(message);
                }
                if (MyProject.cache(project).upd) {
                    TextMessage textMessage = new TextMessage();
                    textMessage.setMessage(message);
                    textMessage.setName(MyProject.cache(project).userName);
                    MyProject.cache(project).updService.send(textMessage);
                }
                sendButton.setText("发送");
                sendButton.setEnabled(true);
            }
        } else {
            addChatMessage(SYS.getValue(), "请先加入聊天室");
        }
    }

    private void addChatMessage(String root, String message) {
        TextMessage textMessage = new TextMessage();
        textMessage.setName(root);
        textMessage.setMessage(message);
        addChatMessage(textMessage);
        onMessage(message);
    }

    private void addChatMessage(BaseUser message) {
        if (chatMessages.size() >= MAX_MESSAGES) {
            chatMessages.remove(0);
        }
        chatMessages.add(message);
        updateChatMessagesList();
    }

    private void updateChatRoomList() {
        Vector<String> displayList = new Vector<>();
        for (ChatRoom room : chatRooms) {
            displayList.add(room.getIp() + ":" + room.getPort() + " - " + room.getCreator());
        }
        chatRoomList.setListData(displayList);
    }

    private void updateChatMessagesList() {
        // 更新列表数据
        chatMessageList.setListData(chatMessages);
        // 强制重新计算布局
        SwingUtilities.invokeLater(() -> {
            chatMessageList.revalidate();
            chatMessageList.repaint();
            chatMessageList.ensureIndexIsVisible(chatMessages.size() - 1);
        });
    }

    public void onMessage(String message) {
        if (ObjectUtils.isNotEmpty(MyProject.cache(project).chat) && !MyProject.cache(project).chat.isShowing()) {
            ApplicationManager.getApplication().invokeLater(() -> {
                // 创建通知组（确保类型为 BALLOON）
                NotificationGroup group = NotificationGroup.create(
                        "Wchat Notifications",
                        NotificationDisplayType.BALLOON, // 关键：使用 BALLOON 类型
                        true,
                        "",
                        "",
                        PluginId.findId()
                );
                // 创建并显示通知
                Notification notification = group.createNotification(
                        "收到新的消息",
                        message,
                        NotificationType.INFORMATION
                );
                notification.addAction(new AnAction("查看详情") {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                        MyProject.cache(project).toolWindow.show();
                        MyProject.cache(project).showContent("chat");
                        chatMessageScroll.requestFocusInWindow();
                        notification.expire();
                    }
                });
                Notifications.Bus.notify(notification, MyProject.cache(project).project);
            });
        }
    }

    private void startChatRoomListUpdater() {
        chatRoomListUpdater = Executors.newSingleThreadScheduledExecutor();
        chatRoomListUpdater.scheduleAtFixedRate(() -> {
            // TODO: 实现扫描网络中的聊天室
            // 这里可以添加网络扫描逻辑
            SwingUtilities.invokeLater(this::updateChatRoomList);
        }, 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void addMessage(String message, String root) {
        this.addChatMessage(root, message);
    }

    @Override
    public void send(String message) {
        this.sendMessage(message);
    }

    @Override
    public boolean isShowing() {
        return chatRoomPanel.isShowing();
    }

    @Override
    public void serverStatus(boolean b, String buttonTitle) {
        createChatButton.setEnabled(b);
        createChatButton.setText(buttonTitle);
    }

    @Override
    public void connectStatus(boolean b, String buttonTitle) {
        joinChatButton.setEnabled(b);
        joinChatButton.setText(buttonTitle);
    }

    @Override
    public void addSysMessage(String message, String root) {
        addChatMessage(root, message);
    }

    @Override
    public void mqttStatus(boolean b, String buttonTitle) {

    }

    @Override
    public void updconnectStatus(boolean b, String buttonTitle) {

    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    @Override
    public JComponent getComponent(String project) {
        return new ChatRoomPanel(project).$$$getRootComponent$$$();
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("ChatRoomPanel");
            frame.setContentPane(new ChatRoomPanel().chatRoomPanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        });
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        chatRoomPanel = new JPanel();
        chatRoomPanel.setLayout(new GridLayoutManager(1, 2, new Insets(10, 10, 10, 10), 10, 10));
        chatRoomPanel.setMinimumSize(new Dimension(800, 500));
        chatRoomPanel.setPreferredSize(new Dimension(1000, 700));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), 5, 5));
        chatRoomPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        chatMessageScroll = new JScrollPane();
        panel1.add(chatMessageScroll, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        chatMessageScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "聊天信息", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        chatMessageList = new JList();
        chatMessageScroll.setViewportView(chatMessageList);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(5, 5, 5, 5), 5, 5));
        panel1.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "发送消息", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        messageField = new JTextField();
        messageField.setPreferredSize(new Dimension(500, 30));
        panel2.add(messageField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sendButton = new JButton();
        sendButton.setPreferredSize(new Dimension(100, 30));
        sendButton.setText("发送");
        panel2.add(sendButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), 5, 10));
        chatRoomPanel.add(panel3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        chatRoomScroll = new JScrollPane();
        panel3.add(chatRoomScroll, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        chatRoomScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "聊天室列表", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        chatRoomList = new JList();
        chatRoomScroll.setViewportView(chatRoomList);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(3, 2, new Insets(8, 8, 8, 8), 10, 8));
        panel3.add(panel4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "聊天室管理", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label1 = new JLabel();
        label1.setText("IP：");
        panel4.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ipField = new JTextField();
        ipField.setPreferredSize(new Dimension(150, 25));
        ipField.setText("127.0.0.1");
        panel4.add(ipField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("端口：");
        panel4.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        portField = new JTextField();
        portField.setPreferredSize(new Dimension(150, 25));
        portField.setText("8080");
        panel4.add(portField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        createChatButton = new JButton();
        createChatButton.setPreferredSize(new Dimension(100, 30));
        createChatButton.setText("创建聊天室");
        panel4.add(createChatButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 3, false));
        joinChatButton = new JButton();
        joinChatButton.setPreferredSize(new Dimension(100, 30));
        joinChatButton.setText("加入聊天室");
        panel4.add(joinChatButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 2, new Insets(8, 8, 8, 8), 10, 5));
        panel3.add(panel5, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel5.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "个人信息", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label3 = new JLabel();
        label3.setText("昵称：");
        panel5.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nicknameField = new JTextField();
        nicknameField.setPreferredSize(new Dimension(150, 25));
        nicknameField.setText("用户");
        panel5.add(nicknameField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return chatRoomPanel;
    }

}
