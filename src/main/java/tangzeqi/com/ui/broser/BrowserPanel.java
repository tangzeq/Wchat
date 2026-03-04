package tangzeqi.com.ui.broser;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import tangzeqi.com.tools.broser.Broser;
import tangzeqi.com.tools.broser.server.MyJCEF;
import tangzeqi.com.ui.MyPanel;

import javax.swing.*;
import java.awt.*;

public class BrowserPanel extends JPanel implements MyPanel {
    private final String project;

    private JPanel browserPanel;
    private JPanel browserBar;
    private JTextField urlField;
    private JButton goButton;
    private JPanel browserContentPanel;

    // 浏览器
    private Broser browser;

    public BrowserPanel(String project) {
        this.project = project;
        initializeBrowser();
        setupEventHandlers();
    }

    public BrowserPanel() {
        this.project = null;
    }

    // 添加浏览器初始化方法
    private void initializeBrowser() {
        browser = new MyJCEF(project);
        // 添加浏览器主视图
        GridConstraints constraints = new GridConstraints(
                0, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null, 0, true
        );
        // 直接添加浏览器组件，而不是遍历获取组件
        for (Component component : browser.getComponents()) {
            if (component != null) {
                browserContentPanel.add(component,constraints);
            }
        }
    }

    private void setupEventHandlers() {
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
        SwingUtilities.invokeLater(()->{
            goButton.doClick();
        });
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


    private void createUIComponents() {
        browserContentPanel = new JPanel(new BorderLayout());
    }

    @Override
    public JComponent getComponent(String project) {
        return new BrowserPanel(project).browserPanel;
    }

    /****************************************标准的GUI 生成*****************************************************/
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("BrowserPanel");
            frame.setContentPane(new BrowserPanel().browserPanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        });
    }

}
