package tangzeqi.com.ui;

import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.SourceType;
import tangzeqi.com.tools.broser.Broser;
import tangzeqi.com.tools.broser.server.MyJCEF;
import tangzeqi.com.tools.mind.server.MindProgressListener;
import tangzeqi.com.tools.mind.server.MindProgressUIListener;
import tangzeqi.com.tools.mind.MindService;
import tangzeqi.com.tools.mind.server.LightweightMindService;
import tangzeqi.com.project.MyProject;
import tangzeqi.com.tools.chat.Chat;
import tangzeqi.com.tools.chat.Config;
import tangzeqi.com.tools.chat.stroge.BaseUser;
import tangzeqi.com.tools.chat.stroge.TextMessage;
import tangzeqi.com.utils.CodeGenerationUtils;
import tangzeqi.com.utils.NetUtils;
import tangzeqi.com.utils.SQLUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.swing.SwingUtilities;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static tangzeqi.com.ui.UiConstantEnum.SYS;

public class Wchat extends JPanel implements Config, Chat {
    private final String project;

    // 主容器
    private JPanel wchat;
    private JTabbedPane mainTabs;

    // 聊天室标签页
    private JPanel chatPanel;
    private JPanel leftPanel;
    private JPanel createChatPanel;
    private JTextField ipField;
    private JTextField portField;
    private JButton createChatButton;
    private JPanel joinChatPanel;
    private JTextField joinIpField;
    private JTextField joinPortField;
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

    // 文件夹标签页
    private JPanel folderPanel;
    private JPanel folderPathPanel;
    private JTextField folderPathField;
    private JButton browseButton;
    private JScrollPane fileListScroll;
    private JList<String> fileList;
    private JPanel fileInfoPanel;
    private JScrollPane fileContentScroll;
    private JTextArea fileContentArea;

    // 浏览器标签页

    private JPanel browserPanel;
    private JPanel browserBar;
    private JTextField urlField;
    private JButton goButton;
    private JPanel browserContentPanel;

    // 工具箱标签页
    private JPanel toolsPanel;
    private JPanel toolsWorkArea;
    private JScrollPane inputScroll;
    private JTextArea inputArea;
    private JScrollPane outputScroll;
    private JTextArea outputArea;
    private JPanel toolsButtons;
    private JButton jsonFormatButton;
    private JButton jsonToJavaButton;
    private JButton sqlToJavaButton;
    private JButton encryptButton;
    private JButton decryptButton;

    // 记忆库标签页
    private MindService mind;
    private JPanel mindPanel;
    private JScrollPane mindOutputScroll;
    private JTextArea mindOutputArea;
    private JPanel mindInputPanel;
    private JTextField mindInputField;
    private JButton mindTrainButton;
    private JButton mindChatButton;


    // 浏览器
    private Broser browser;
    // 聊天消息列表
    private Vector<BaseUser> chatMessages = new Vector<>();
    private ScheduledExecutorService chatMessagesListUpdater;
    private static final int MAX_MESSAGES = 1000;
    // 聊天室列表
    private Vector<ChatRoom> chatRooms = new Vector<>();
    private ScheduledExecutorService chatRoomListUpdater;
    // 当前聊天室
    private ChatRoom currentChatRoom;

    public Wchat(String project) {
        this.project = project;
        $$$setupUI$$$();
        SwingUtilities.invokeLater(() -> {
            initChatRoom();
            initializeBrowser();
            initMind();
        });
        MyProject.cache(project).chat = this;
        MyProject.cache(project).config = this;
    }

    private void initChatRoom() {
        initializeConfig();
        setupEventHandlers();
        startChatRoomListUpdater();
    }

    private void initMind() {
        this.mind = new LightweightMindService();
        // 为记忆库面板添加右键菜单
        setupMindPanelContextMenu();
    }

    private void initializeConfig() {
        String host = NetUtils.host();
        String port = String.valueOf(NetUtils.port());
        ipField.setText(host);
        portField.setText(port);
        joinIpField.setText(host);
        joinPortField.setText(port);
        folderPathField.setText(System.getProperty("user.home"));
        browseButton.doClick();
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

        // 设置文件列表滚动面板
        fileListScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        fileListScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        fileListScroll.setPreferredSize(null); // 移除首选大小，让其自动适应父容器

        // 设置文件内容滚动面板
        fileContentScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        fileContentScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        fileContentScroll.setPreferredSize(null); // 移除首选大小，让其自动适应父容器

        // 设置工具箱输入滚动面板
        inputScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        inputScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        inputScroll.setPreferredSize(null); // 移除首选大小，让其自动适应父容器

        // 设置工具箱输出滚动面板
        outputScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        outputScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        outputScroll.setPreferredSize(null); // 移除首选大小，让其自动适应父容器

        // 设置记忆库输出滚动面板
        mindOutputScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mindOutputScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mindOutputScroll.setPreferredSize(null); // 移除首选大小，让其自动适应父容器

        // 确保文本区域能够随滚动面板一起调整大小
        setComponentSizePolicy(inputArea);
        setComponentSizePolicy(outputArea);
        setComponentSizePolicy(fileContentArea);
        setComponentSizePolicy(mindOutputArea);

        // 确保列表能够随滚动面板一起调整大小
        setComponentSizePolicy(chatMessageList);
        setComponentSizePolicy(chatRoomList);
        setComponentSizePolicy(fileList);
    }

    /**
     * 设置组件的大小策略，使其能够随父容器一起调整大小
     */
    private void setComponentSizePolicy(JComponent component) {
        component.setPreferredSize(null); // 移除首选大小
        component.setMaximumSize(null); // 移除最大大小
        component.setMinimumSize(null); // 移除最小大小
        // 对于文本区域，确保其能够自动换行
        if (component instanceof JTextArea) {
            JTextArea textArea = (JTextArea) component;
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
        }
        // 对于列表，确保其能够显示多行
        if (component instanceof JList) {
            JList<?> list = (JList<?>) component;
            list.setVisibleRowCount(-1); // 允许显示任意行数
        }
    }

    private void setupMindPanelContextMenu() {
        // 创建右键菜单
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem openFolderItem = new JMenuItem("打开记忆库");
        openFolderItem.addActionListener(e -> {
            try {
                // 获取记忆库存储目录路径
                String mindFolderPath = System.getProperty("user.home") + "/.mind-idea-plugin";
                File mindFolder = new File(mindFolderPath);

                // 确保目录存在
                if (!mindFolder.exists()) {
                    mindFolder.mkdirs();
                }

                // 打开文件夹
                Desktop.getDesktop().open(mindFolder);
            } catch (Exception ex) {
                addChatMessage(SYS.getValue(), "打开记忆库文件夹失败: " + ex.getMessage());
            }
        });
        contextMenu.add(openFolderItem);

        // 为记忆库输出区域添加右键菜单
        mindOutputArea.setComponentPopupMenu(contextMenu);

        // 为记忆库输入面板添加右键菜单
        mindInputPanel.setComponentPopupMenu(contextMenu);

        // 为整个记忆库面板添加右键菜单
        mindPanel.setComponentPopupMenu(contextMenu);
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
            String ip = joinIpField.getText();
            String port = joinPortField.getText();
            String nickName = nicknameField.getText();
            if (nickName.trim().isEmpty()) {
                addChatMessage(SYS.getValue(), "昵称不能为空！");
                nicknameField.requestFocusInWindow();
            } else if (ip.trim().isEmpty()) {
                addChatMessage(SYS.getValue(), "IP不能为空！");
                joinIpField.requestFocusInWindow();
            } else if (port.trim().isEmpty()) {
                addChatMessage(SYS.getValue(), "端口不能为空！");
                joinPortField.requestFocusInWindow();
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

        // 聊天消息区域点击事件
        chatMessageList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                if (e.getClickCount() == 2) {
                    //实现双击事件处理
//                    JOptionPane.showMessageDialog(wchat, "双击了聊天消息");
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

        // 记忆库事件处理
        mindTrainButton.addActionListener(e -> {
            String input = mindInputField.getText();
            if (!input.isEmpty()) {
                // 创建进度监听器
                MindProgressListener listener = new MindProgressUIListener(mindOutputArea);
                new Thread(() -> {
                    mindTrainButton.setEnabled(false);
                    mindChatButton.setEnabled(false);
                    mind.get(input, listener);
                    SwingUtilities.invokeLater(() -> {
                        mindInputField.setText("");
                        mindOutputArea.setCaretPosition(mindOutputArea.getDocument().getLength());
                        mindTrainButton.setEnabled(true);
                        mindChatButton.setEnabled(true);
                    });
                }).start();
            }
        });

        mindChatButton.addActionListener(e -> {
            String input = mindInputField.getText();
            if (!input.isEmpty()) {
                input = input.trim();
                mindTrainButton.setEnabled(false);
                mindChatButton.setEnabled(false);
                MindProgressListener listener = new MindProgressUIListener(mindOutputArea);
                mind.set(input, listener);
                SwingUtilities.invokeLater(() -> {
                    mindInputField.setText("");
                    mindOutputArea.setCaretPosition(mindOutputArea.getDocument().getLength());
                    mindTrainButton.setEnabled(true);
                    mindChatButton.setEnabled(true);
                });
            }
        });

        // 为记忆库输入框添加回车键监听事件
        mindInputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    mindTrainButton.doClick();
                }
            }
        });

        // 文件夹事件处理
        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(wchat) == JFileChooser.APPROVE_OPTION) {
                File selected = chooser.getSelectedFile();
                folderPathField.setText(selected.getAbsolutePath());
                updateFileList(selected.getAbsolutePath());
            }
        });

        fileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = fileList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    String selected = fileList.getModel().getElementAt(index);
                    String fileName = selected.substring(selected.indexOf(' ') + 1); // 去掉图标

                    // 处理返回上级目录
                    if (selected.startsWith("🔙")) {
                        File currentDir = new File(folderPathField.getText());
                        File parent = currentDir.getParentFile();
                        if (parent != null) {
                            folderPathField.setText(parent.getAbsolutePath());
                            updateFileList(parent.getAbsolutePath());
                        }
                        return;
                    }

                    // 处理普通文件/文件夹
                    File file = new File(folderPathField.getText(), fileName);

                    if (file.isDirectory()) {
                        // 单击时显示文件夹内容
                        displayFolderContent(file);

                        // 双击时进入文件夹
                        if (e.getClickCount() == 2) {
                            folderPathField.setText(file.getAbsolutePath());
                            updateFileList(file.getAbsolutePath());
                        }
                    } else {
                        if (e.getClickCount() == 2) {
                            // 双击文件时打开文件
                            try {
                                Desktop.getDesktop().open(file);
                            } catch (IOException ex) {
                                fileContentArea.setText("无法打开文件：" + ex.getMessage());
                            }
                        } else {
                            // 检查文件类型
                            String fileExtension = fileName.toLowerCase();
                            boolean isTextFile = fileExtension.endsWith(".txt") ||
                                    fileExtension.endsWith(".log") ||
                                    fileExtension.endsWith(".md") ||
                                    fileExtension.endsWith(".java") ||
                                    fileExtension.endsWith(".xml") ||
                                    fileExtension.endsWith(".json") ||
                                    fileExtension.endsWith(".properties") ||
                                    fileExtension.endsWith(".yml") ||
                                    fileExtension.endsWith(".yaml") ||
                                    fileExtension.endsWith(".html") ||
                                    fileExtension.endsWith(".css") ||
                                    fileExtension.endsWith(".js") ||
                                    fileExtension.endsWith(".py") ||
                                    fileExtension.endsWith(".sql");

                            if (!isTextFile) {
                                // 显示二进制文件信息
                                String fileInfo = String.format(
                                        "文件类型：二进制文件\n" +
                                                "文件大小：%,d 字节\n" +
                                                "最后修改：%s\n" +
                                                "文件路径：%s\n" +
                                                "\n这是一个非文本文件，无法直接显示内容。\n" +
                                                "双击可以使用系统默认程序打开该文件。",
                                        file.length(),
                                        new Date(file.lastModified()).toString(),
                                        file.getAbsolutePath()
                                );
                                fileContentArea.setText(fileInfo);
                                return;
                            }

                            // 读取文本文件内容
                            new SwingWorker<String, Void>() {
                                @Override
                                protected String doInBackground() throws Exception {
                                    StringBuilder content = new StringBuilder();
                                    try {
                                        // 检测文件编码
                                        String encoding = FileEncodingDetector.detectEncoding(file);

                                        // 使用检测到的编码读取文件
                                        try (BufferedReader reader = new BufferedReader(
                                                new InputStreamReader(new FileInputStream(file), encoding))) {
                                            char[] buffer = new char[8192];
                                            int charsRead;
                                            while ((charsRead = reader.read(buffer)) != -1) {
                                                content.append(buffer, 0, charsRead);
                                                if (content.length() > 1024 * 1024) {
                                                    return "文件内容过大，无法完整显示";
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        return "读取文件时出错：" + e.getMessage();
                                    }
                                    return content.toString();
                                }

                                @Override
                                protected void done() {
                                    try {
                                        String content = get();
                                        fileContentArea.setText(content);
                                        // 滚动到顶部
                                        fileContentArea.setCaretPosition(0);
                                    } catch (Exception ex) {
                                        fileContentArea.setText("读取文件时出错：" + ex.getMessage());
                                    }
                                }
                            }.execute();
                        }
                    }
                }
            }
        });

        // 工具箱事件处理
        jsonFormatButton.addActionListener(e -> formatJSON());
        jsonToJavaButton.addActionListener(e -> convertJSONToJava());
        sqlToJavaButton.addActionListener(e -> convertSQLToJava());
        encryptButton.addActionListener(e -> encryptText());
        decryptButton.addActionListener(e -> decryptText());
    }

    // 添加浏览器初始化方法
    private void initializeBrowser() {
//        browser = new MyWebView(project);
        browser = new MyJCEF(project);
        // 添加浏览器主视图
        // 直接添加浏览器组件，而不是遍历获取组件
        for (Component component : browser.getComponents()) {
            browserContentPanel.add(component, BorderLayout.CENTER);
        }
        // URL加载事件处理
        urlField.addActionListener(e -> {
            String url = urlField.getText();
            if (!url.trim().isEmpty()) {
                loadURL(url);
            }
        });

        goButton.addActionListener(e -> {
            String url = urlField.getText();
            if (!url.trim().isEmpty()) {
                loadURL(url);
            }
        });

        // 加载初始URL
        loadURL(urlField.getText());
    }


    // 修改loadURL方法
    private void loadURL(String url) {
        if (browser != null) {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }
            browser.loadURL(url);
        }
    }

    // 添加颜色转换工具方法
    private String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
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
                NotificationGroup group =
                        NotificationGroup.create(
                                "Wchat Notifications",
                                NotificationDisplayType.BALLOON,  // 关键：使用 BALLOON 类型
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

    // 文件夹相关方法
    private void updateFileList(String path) {
        File dir = new File(path);
        File[] files = dir.listFiles();
        Vector<String> fileListVector = new Vector<>();

        // 如果不是根目录，添加返回上级目录选项
        File parent = dir.getParentFile();
        if (parent != null) {
            fileListVector.add("🔙 .. (返回上级目录)");
        }

        if (files != null) {
            // 先添加目录
            Arrays.stream(files)
                    .filter(File::isDirectory)
                    .sorted(Comparator.comparing(File::getName))
                    .forEach(file -> fileListVector.add("📁 " + file.getName()));

            // 再添加文件
            Arrays.stream(files)
                    .filter(File::isFile)
                    .sorted(Comparator.comparing(File::getName))
                    .forEach(file -> {
                        String icon = getFileIcon(file);
                        fileListVector.add(icon + " " + file.getName());
                    });
        }

        fileList.setListData(fileListVector);
    }

    // 根据文件类型返回对应的图标
    private String getFileIcon(File file) {
        String name = file.getName().toLowerCase();

        // 系统和可执行文件
        if (name.endsWith(".exe") || name.endsWith(".msi") || name.endsWith(".deb") ||
                name.endsWith(".rpm") || name.endsWith(".dmg") || name.endsWith(".pkg") ||
                name.endsWith(".app") || name.endsWith(".run") || name.endsWith(".bin") ||
                name.endsWith(".command") || name.endsWith(".bat") || name.endsWith(".cmd") ||
                name.endsWith(".com") || name.endsWith(".scr") || name.endsWith(".msc")) return "💻";

        // 文档类
        if (name.endsWith(".txt") || name.endsWith(".log") || name.endsWith(".md") ||
                name.endsWith(".readme") || name.endsWith(".rtf")) return "📄";
        if (name.endsWith(".pdf")) return "📕";
        if (name.endsWith(".doc") || name.endsWith(".docx") || name.endsWith(".dot") ||
                name.endsWith(".dotx")) return "📘";
        if (name.endsWith(".xls") || name.endsWith(".xlsx") || name.endsWith(".csv") ||
                name.endsWith(".xlsb") || name.endsWith(".xlsm")) return "📗";
        if (name.endsWith(".ppt") || name.endsWith(".pptx") || name.endsWith(".pps") ||
                name.endsWith(".ppsx")) return "📙";
        if (name.endsWith(".odt") || name.endsWith(".ods") || name.endsWith(".odp") ||
                name.endsWith(".odg") || name.endsWith(".odf")) return "📝";

        // 代码类
        if (name.endsWith(".java") || name.endsWith(".class") || name.endsWith(".jar") ||
                name.endsWith(".war") || name.endsWith(".ear") || name.endsWith(".jsp")) return "☕";
        if (name.endsWith(".py") || name.endsWith(".pyc") || name.endsWith(".pyd") ||
                name.endsWith(".pyw") || name.endsWith(".pyz")) return "🐍";
        if (name.endsWith(".js") || name.endsWith(".jsx") || name.endsWith(".ts") ||
                name.endsWith(".tsx") || name.endsWith(".json") || name.endsWith(".json5")) return "🌐";
        if (name.endsWith(".c") || name.endsWith(".cpp") || name.endsWith(".h") ||
                name.endsWith(".hpp") || name.endsWith(".cc") || name.endsWith(".cxx")) return "⚙️";
        if (name.endsWith(".cs") || name.endsWith(".vb") || name.endsWith(".fs") ||
                name.endsWith(".fsx")) return "🔷";
        if (name.endsWith(".php") || name.endsWith(".phtml") || name.endsWith(".php3") ||
                name.endsWith(".php4") || name.endsWith(".php5") || name.endsWith(".php7")) return "🐘";
        if (name.endsWith(".rb") || name.endsWith(".rbw") || name.endsWith(".rake")) return "💎";
        if (name.endsWith(".go")) return "🐹";
        if (name.endsWith(".rs")) return "🦀";
        if (name.endsWith(".swift")) return "🦉";
        if (name.endsWith(".kt") || name.endsWith(".kts")) return "🎯";
        if (name.endsWith(".scala") || name.endsWith(".sc")) return "🔮";
        if (name.endsWith(".pl") || name.endsWith(".pm") || name.endsWith(".t") ||
                name.endsWith(".pod")) return "🐪";
        if (name.endsWith(".sh") || name.endsWith(".bash") || name.endsWith(".zsh") ||
                name.endsWith(".fish") || name.endsWith(".csh")) return "🐚";
        if (name.endsWith(".r") || name.endsWith(".R")) return "📊";
        if (name.endsWith(".m") || name.endsWith(".matlab")) return "📐";
        if (name.endsWith(".lua")) return "🌙";
        if (name.endsWith(".dart")) return "🎯";
        if (name.endsWith(".elm")) return "🌳";
        if (name.endsWith(".hs") || name.endsWith(".lhs")) return "λ";

        // 网页和标记语言
        if (name.endsWith(".html") || name.endsWith(".htm") || name.endsWith(".xhtml") ||
                name.endsWith(".shtml") || name.endsWith(".dhtml")) return "🌐";
        if (name.endsWith(".css") || name.endsWith(".scss") || name.endsWith(".sass") ||
                name.endsWith(".less") || name.endsWith(".styl")) return "🎨";
        if (name.endsWith(".xml") || name.endsWith(".xsl") || name.endsWith(".xslt") ||
                name.endsWith(".xsd") || name.endsWith(".svg")) return "📜";
        if (name.endsWith(".vue") || name.endsWith(".svelte")) return "🖼️";

        // 配置文件
        if (name.endsWith(".ini") || name.endsWith(".conf") || name.endsWith(".config") ||
                name.endsWith(".cfg") || name.endsWith(".toml")) return "⚙️";
        if (name.endsWith(".yml") || name.endsWith(".yaml")) return "📋";
        if (name.endsWith(".env") || name.endsWith(".dotenv")) return "🌍";
        if (name.endsWith(".properties") || name.endsWith(".props")) return "📝";

        // 压缩文件
        if (name.endsWith(".zip") || name.endsWith(".zipx") || name.endsWith(".rar") ||
                name.endsWith(".7z") || name.endsWith(".ace") || name.endsWith(".arj") ||
                name.endsWith(".bz2") || name.endsWith(".cab") || name.endsWith(".gz") ||
                name.endsWith(".gzip") || name.endsWith(".lha") || name.endsWith(".lzh") ||
                name.endsWith(".lzma") || name.endsWith(".pak") || name.endsWith(".sit") ||
                name.endsWith(".sitx") || name.endsWith(".tar") || name.endsWith(".tgz") ||
                name.endsWith(".xz") || name.endsWith(".z") || name.endsWith(".zoo")) return "📦";

        // 图片类
        if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") ||
                name.endsWith(".gif") || name.endsWith(".bmp") || name.endsWith(".webp") ||
                name.endsWith(".ico") || name.endsWith(".tiff") || name.endsWith(".tif") ||
                name.endsWith(".psd") || name.endsWith(".ai") || name.endsWith(".eps") ||
                name.endsWith(".svg") || name.endsWith(".raw") || name.endsWith(".cr2") ||
                name.endsWith(".nef") || name.endsWith(".orf") || name.endsWith(".sr2")) return "🖼️";

        // 音频类
        if (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".flac") ||
                name.endsWith(".aac") || name.endsWith(".ogg") || name.endsWith(".wma") ||
                name.endsWith(".m4a") || name.endsWith(".m4p") || name.endsWith(".m4b") ||
                name.endsWith(".m4r") || name.endsWith(".opus") || name.endsWith(".aiff") ||
                name.endsWith(".au") || name.endsWith(".ra") || name.endsWith(".3gp") ||
                name.endsWith(".amr") || name.endsWith(".ac3") || name.endsWith(".dts")) return "🎵";

        // 视频类
        if (name.endsWith(".mp4") || name.endsWith(".avi") || name.endsWith(".mkv") ||
                name.endsWith(".mov") || name.endsWith(".wmv") || name.endsWith(".flv") ||
                name.endsWith(".webm") || name.endsWith(".m4v") || name.endsWith(".3gp") ||
                name.endsWith(".3g2") || name.endsWith(".asf") || name.endsWith(".rm") ||
                name.endsWith(".rmvb") || name.endsWith(".vob") || name.endsWith(".ts") ||
                name.endsWith(".mts") || name.endsWith(".m2ts") || name.endsWith(".divx") ||
                name.endsWith(".xvid") || name.endsWith(".f4v") || name.endsWith(".f4p") ||
                name.endsWith(".f4a") || name.endsWith(".f4b")) return "🎬";

        // 数据库
        if (name.endsWith(".sql") || name.endsWith(".db") || name.endsWith(".sqlite") ||
                name.endsWith(".sqlite3") || name.endsWith(".db3") || name.endsWith(".mdb") ||
                name.endsWith(".accdb") || name.endsWith(".dbf") || name.endsWith(".odb") ||
                name.endsWith(".frm") || name.endsWith(".myd") || name.endsWith(".myi")) return "🗄️";

        // 字体文件
        if (name.endsWith(".ttf") || name.endsWith(".otf") || name.endsWith(".woff") ||
                name.endsWith(".woff2") || name.endsWith(".eot") || name.endsWith(".fon") ||
                name.endsWith(".pfb") || name.endsWith(".pfm")) return "🔤";

        // 移动应用
        if (name.endsWith(".apk") || name.endsWith(".aab")) return "📱";
        if (name.endsWith(".ipa") || name.endsWith(".pxl")) return "🍎";
        if (name.endsWith(".xap") || name.endsWith(".appx")) return "🪟";
        if (name.endsWith(".bar")) return "📱";

        // 电子书
        if (name.endsWith(".epub") || name.endsWith(".mobi") || name.endsWith(".azw") ||
                name.endsWith(".azw3") || name.endsWith(".fb2") || name.endsWith(".lit")) return "📚";

        // 字处理
        if (name.endsWith(".pages")) return "📝";
        if (name.endsWith(".numbers")) return "📊";
        if (name.endsWith(".key") || name.endsWith(".keynote")) return "🎭";

        // 3D模型
        if (name.endsWith(".obj") || name.endsWith(".fbx") || name.endsWith(".dae") ||
                name.endsWith(".3ds") || name.endsWith(".blend") || name.endsWith(".max") ||
                name.endsWith(".ma") || name.endsWith(".mb")) return "🎮";

        // 虚拟化
        if (name.endsWith(".vmdk") || name.endsWith(".vdi") || name.endsWith(".vhd") ||
                name.endsWith(".hdd") || name.endsWith(".qcow2") || name.endsWith(".ova") ||
                name.endsWith(".ovf")) return "💾";

        // 其他特殊类型
        if (name.endsWith(".torrent")) return "🔗";
        if (name.endsWith(".key") || name.endsWith(".pem") || name.endsWith(".crt") ||
                name.endsWith(".cer") || name.endsWith(".p12") || name.endsWith(".pfx")) return "🔐";
        if (name.endsWith(".iso") || name.endsWith(".img") || name.endsWith(".dmg") ||
                name.endsWith(".toast") || name.endsWith(".vcd")) return "💿";
        if (name.endsWith(".dll") || name.endsWith(".so") || name.endsWith(".dylib")) return "🔧";
        if (name.endsWith(".sys") || name.endsWith(".drv")) return "⚙️";

        // 隐藏文件
        if (name.startsWith(".")) return "🔒";

        return "📎"; // 默认文件图标
    }

    private void displayFolderContent(File folder) {
        File[] files = folder.listFiles();
        if (files == null) {
            fileContentArea.setText("无法访问此文件夹");
            return;
        }

        StringBuilder content = new StringBuilder();
        content.append("文件夹内容：\n\n");

        // 添加文件夹
        Arrays.stream(files)
                .filter(File::isDirectory)
                .sorted(Comparator.comparing(File::getName))
                .forEach(file -> {
                    content.append("📁 ").append(file.getName())
                            .append(" (").append(file.length()).append(" 字节)\n");
                });

        content.append("\n");

        // 添加文件
        Arrays.stream(files)
                .filter(File::isFile)
                .sorted(Comparator.comparing(File::getName))
                .forEach(file -> {
                    content.append(getFileIcon(file) + " ").append(file.getName())
                            .append(" (").append(file.length()).append(" 字节)\n");
                });

        fileContentArea.setText(content.toString());
    }

    // 工具箱相关方法
    private void formatJSON() {
        String input = inputArea.getText();
        try {
            String formatted = CodeGenerationUtils.jsonFormat(input);
            outputArea.setText(formatted);
        } catch (Exception e) {
            outputArea.setText("错误: " + e.getMessage());
        }
    }

    private void convertJSONToJava() {
        String input = inputArea.getText();
        try {
            String code = CodeGenerationUtils.jsonToCode(null, input);
            outputArea.setText(code);
        } catch (Exception e) {
            outputArea.setText("错误: " + e.getMessage());
        }
    }

    private void convertSQLToJava() {
        String input = inputArea.getText();
        try {
            String json = SQLUtils.selectToJson(input);
            String code = CodeGenerationUtils.jsonToCode(new DefaultGenerationConfig() {
                @Override
                public boolean isGenerateBuilders() {
                    return true;
                }

                @Override
                public boolean isIncludeHashcodeAndEquals() {
                    return false;
                }

                @Override
                public boolean isIncludeToString() {
                    return false;
                }

                @Override
                public boolean isInitializeCollections() {
                    return false;
                }

                @Override
                public boolean isIncludeAllPropertiesConstructor() {
                    return false;
                }

                @Override
                public boolean isIncludeAdditionalProperties() {
                    return false;
                }

                @Override
                public boolean isIncludeGetters() {
                    return false;
                }

                @Override
                public boolean isIncludeSetters() {
                    return false;
                }

                @Override
                public boolean isIncludeGeneratedAnnotation() {
                    return false;
                }

                @Override
                public SourceType getSourceType() {
                    return SourceType.JSON;
                }
            }, json);
            outputArea.setText(code);
        } catch (Exception e) {
            outputArea.setText("错误: " + e.getMessage());
        }
    }

    private void encryptText() {
        String input = inputArea.getText();
        try {
            // TODO: 实现加密逻辑
            outputArea.setText("加密结果:\n" + input);
        } catch (Exception e) {
            outputArea.setText("错误: " + e.getMessage());
        }
    }

    private void decryptText() {
        String input = inputArea.getText();
        try {
            // TODO: 实现解密逻辑
            outputArea.setText("解密结果:\n" + input);
        } catch (Exception e) {
            outputArea.setText("错误: " + e.getMessage());
        }
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
        return wchat.isShowing();
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Wchat");
            frame.setContentPane(new Wchat("chat").wchat);
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
        createUIComponents();
        wchat = new JPanel();
        wchat.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        wchat.setMinimumSize(new Dimension(-1, -1));
        wchat.setPreferredSize(new Dimension(-1, -1));
        mainTabs = new JTabbedPane();
        wchat.add(mainTabs, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        chatPanel = new JPanel();
        chatPanel.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        mainTabs.addTab("聊天室", chatPanel);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setEnabled(true);
        chatPanel.add(panel1, new GridConstraints(0, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(100, -1), null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(3, 2, new Insets(1, 1, 1, 1), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "创建聊天室", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label1 = new JLabel();
        label1.setText("IP：");
        panel2.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ipField = new JTextField();
        ipField.setPreferredSize(new Dimension(120, 20));
        ipField.setText("127.0.0.1");
        panel2.add(ipField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("端口：");
        panel2.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        portField = new JTextField();
        portField.setPreferredSize(new Dimension(120, 20));
        portField.setText("8080");
        panel2.add(portField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        createChatButton = new JButton();
        createChatButton.setText("创建聊天室");
        panel2.add(createChatButton, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(3, 2, new Insets(1, 1, 1, 1), -1, -1));
        panel1.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "加入聊天室", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label3 = new JLabel();
        label3.setText("IP：");
        panel3.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        joinIpField = new JTextField();
        joinIpField.setPreferredSize(new Dimension(120, 20));
        joinIpField.setText("127.0.0.1");
        panel3.add(joinIpField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("端口：");
        panel3.add(label4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        joinPortField = new JTextField();
        joinPortField.setPreferredSize(new Dimension(120, 20));
        joinPortField.setText("8080");
        panel3.add(joinPortField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        joinChatButton = new JButton();
        joinChatButton.setText("加入聊天室");
        panel3.add(joinChatButton, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 2, new Insets(1, 1, 1, 1), -1, -1));
        panel1.add(panel4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "个人信息", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label5 = new JLabel();
        label5.setText("昵称：");
        panel4.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nicknameField = new JTextField();
        nicknameField.setPreferredSize(new Dimension(120, 20));
        nicknameField.setText("用户");
        panel4.add(nicknameField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(2, 1, new Insets(5, 5, 5, 5), -1, -1));
        chatPanel.add(panel5, new GridConstraints(0, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(600, -1), null, 0, false));
        chatMessageScroll = new JScrollPane();
        panel5.add(chatMessageScroll, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        chatMessageScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "聊天信息", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        chatMessageList = new JList();
        chatMessageScroll.setViewportView(chatMessageList);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 2, new Insets(5, 5, 5, 5), -1, -1));
        panel5.add(panel6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel6.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "发送消息", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        messageField = new JTextField();
        messageField.setPreferredSize(new Dimension(400, 20));
        panel6.add(messageField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sendButton = new JButton();
        sendButton.setText("发送");
        panel6.add(sendButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        chatRoomScroll = new JScrollPane();
        chatPanel.add(chatRoomScroll, new GridConstraints(0, 2, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(200, -1), null, 0, false));
        chatRoomScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "聊天室列表", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        chatRoomList = new JList();
        chatRoomScroll.setViewportView(chatRoomList);
        folderPanel = new JPanel();
        folderPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainTabs.addTab("文件夹", folderPanel);
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(1, 2, new Insets(5, 5, 5, 5), -1, -1));
        folderPanel.add(panel7, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel7.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "路径", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        folderPathField = new JTextField();
        folderPathField.setEditable(false);
        folderPathField.setMargin(new Insets(2, 6, 2, 6));
        folderPathField.setPreferredSize(new Dimension(500, 20));
        panel7.add(folderPathField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        browseButton = new JButton();
        browseButton.setText("浏览");
        panel7.add(browseButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fileListScroll = new JScrollPane();
        folderPanel.add(fileListScroll, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(200, -1), null, 0, false));
        fileListScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "文件列表", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        fileList = new JList();
        fileListScroll.setViewportView(fileList);
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        folderPanel.add(panel8, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(800, -1), null, 0, false));
        panel8.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "文件内容", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        fileContentScroll = new JScrollPane();
        panel8.add(fileContentScroll, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        fileContentArea = new JTextArea();
        fileContentArea.setEditable(false);
        fileContentArea.setLineWrap(true);
        fileContentArea.setPreferredSize(new Dimension(600, 300));
        fileContentArea.setWrapStyleWord(true);
        fileContentScroll.setViewportView(fileContentArea);
        browserPanel = new JPanel();
        browserPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainTabs.addTab("浏览器", browserPanel);
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        browserPanel.add(panel9, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        urlField = new JTextField();
        urlField.setPreferredSize(new Dimension(600, 25));
        urlField.setText("https://www.cnblogs.com/tangzeqi");
        panel9.add(urlField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        goButton = new JButton();
        goButton.setPreferredSize(new Dimension(60, 25));
        goButton.setText("访问");
        panel9.add(goButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        browserPanel.add(browserContentPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        toolsPanel = new JPanel();
        toolsPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainTabs.addTab("工具箱", toolsPanel);
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(1, 2, new Insets(5, 5, 5, 5), -1, -1));
        toolsPanel.add(panel10, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        inputScroll = new JScrollPane();
        panel10.add(inputScroll, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(500, -1), null, 0, false));
        inputScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "输入", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        inputArea = new JTextArea();
        inputArea.setLineWrap(true);
        inputArea.setPreferredSize(new Dimension(400, 200));
        inputArea.setWrapStyleWord(true);
        inputScroll.setViewportView(inputArea);
        outputScroll = new JScrollPane();
        panel10.add(outputScroll, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(500, -1), null, 0, false));
        outputScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "输出", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setPreferredSize(new Dimension(400, 200));
        outputArea.setWrapStyleWord(true);
        outputScroll.setViewportView(outputArea);
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        toolsPanel.add(panel11, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel11.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "工具列表", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        jsonFormatButton = new JButton();
        jsonFormatButton.setText("JSON格式化");
        panel11.add(jsonFormatButton);
        jsonToJavaButton = new JButton();
        jsonToJavaButton.setText("JSON转Java实体");
        panel11.add(jsonToJavaButton);
        sqlToJavaButton = new JButton();
        sqlToJavaButton.setText("SQL转Java实体");
        panel11.add(sqlToJavaButton);
        encryptButton = new JButton();
        encryptButton.setText("加密工具");
        panel11.add(encryptButton);
        decryptButton = new JButton();
        decryptButton.setText("解密工具");
        panel11.add(decryptButton);
        mindPanel = new JPanel();
        mindPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainTabs.addTab("记忆库", mindPanel);
        mindOutputScroll = new JScrollPane();
        mindPanel.add(mindOutputScroll, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        mindOutputScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "记忆", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        mindOutputArea = new JTextArea();
        mindOutputArea.setEditable(false);
        mindOutputArea.setLineWrap(true);
        mindOutputArea.setPreferredSize(new Dimension(800, 300));
        mindOutputArea.setWrapStyleWord(true);
        mindOutputScroll.setViewportView(mindOutputArea);
        final JPanel panel12 = new JPanel();
        panel12.setLayout(new GridLayoutManager(1, 3, new Insets(5, 5, 5, 5), -1, -1));
        mindPanel.add(panel12, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mindInputField = new JTextField();
        mindInputField.setPreferredSize(new Dimension(500, 20));
        panel12.add(mindInputField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mindTrainButton = new JButton();
        mindTrainButton.setText("回忆");
        panel12.add(mindTrainButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mindChatButton = new JButton();
        mindChatButton.setText("记住");
        panel12.add(mindChatButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return wchat;
    }

    private void createUIComponents() {
        browserContentPanel = new JPanel(new BorderLayout());
    }
}
