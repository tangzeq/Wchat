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
        initMind();
        setupScrollPanes();
        setupEventHandlers();
    }

    public MindPanel() {
        this.project = null;
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
        return new MindPanel(project).mindPanel;
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


}
