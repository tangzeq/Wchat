package tangzeqi.com.ui.mind;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import tangzeqi.com.tools.mind.MindService;
import tangzeqi.com.tools.mind.server.LightweightMindService;
import tangzeqi.com.tools.mind.server.MindProgressListener;
import tangzeqi.com.tools.mind.server.MindProgressUIListener;
import tangzeqi.com.ui.MyPanel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

public class MindPanel extends JPanel implements MyPanel {
    private final String project;

    private JPanel mindPanel;
    private JScrollPane mindOutputScroll;
    private JTextArea mindOutputArea;
    private JTextField mindInputField;
    private JButton mindTrainButton;
    private JButton mindChatButton;

    // 记忆库
    private MindService mind;

    public MindPanel(String project) {
        this.project = project;
        $$$setupUI$$$();
        initMind();
        setupScrollPanes();
        setupEventHandlers();
    }

    public MindPanel() {
        this.project = null;
        $$$setupUI$$$();
    }

    private void initMind() {
        this.mind = new LightweightMindService();
        // 为记忆库面板添加右键菜单
        setupMindPanelContextMenu();
    }

    private void setupScrollPanes() {
        // 设置记忆库输出滚动面板
        mindOutputScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mindOutputScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mindOutputScroll.setPreferredSize(null); // 移除首选大小，让其自动适应父容器

        // 确保文本区域能够随滚动面板一起调整大小
        setComponentSizePolicy(mindOutputArea);
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
                mindOutputArea.append("打开记忆库文件夹失败: " + ex.getMessage() + "\n");
            }
        });
        contextMenu.add(openFolderItem);

        // 为记忆库输出区域添加右键菜单
        mindOutputArea.setComponentPopupMenu(contextMenu);

        // 为整个记忆库面板添加右键菜单
        mindPanel.setComponentPopupMenu(contextMenu);
    }

    private void setupEventHandlers() {
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
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    @Override
    public JComponent getComponent(String project) {
        return new MindPanel(project).$$$getRootComponent$$$();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("MindPanel");
            frame.setContentPane(new MindPanel().mindPanel);
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
        mindPanel = new JPanel();
        mindPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        mindPanel.setMinimumSize(new Dimension(-1, -1));
        mindPanel.setPreferredSize(new Dimension(-1, -1));
        mindOutputScroll = new JScrollPane();
        mindPanel.add(mindOutputScroll, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        mindOutputArea = new JTextArea();
        mindOutputArea.setEditable(false);
        mindOutputArea.setLineWrap(true);
        mindOutputArea.setPreferredSize(new Dimension(800, 300));
        mindOutputArea.setWrapStyleWord(true);
        mindOutputScroll.setViewportView(mindOutputArea);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(5, 5, 5, 5), -1, -1));
        mindPanel.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mindInputField = new JTextField();
        mindInputField.setPreferredSize(new Dimension(500, 20));
        panel1.add(mindInputField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mindTrainButton = new JButton();
        mindTrainButton.setText("回忆");
        panel1.add(mindTrainButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mindChatButton = new JButton();
        mindChatButton.setText("记住");
        panel1.add(mindChatButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mindPanel;
    }


}
