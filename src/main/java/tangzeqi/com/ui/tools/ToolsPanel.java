package tangzeqi.com.ui.tools;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.SourceType;
import tangzeqi.com.ui.MyPanel;
import tangzeqi.com.utils.CodeGenerationUtils;
import tangzeqi.com.utils.SQLUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class ToolsPanel extends JPanel implements MyPanel {
    private final String project;

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

    public ToolsPanel(String project) {
        this.project = project;
        $$$setupUI$$$();
        setupScrollPanes();
        setupEventHandlers();
    }

    public ToolsPanel() {
        this.project = null;
    }

    private void setupScrollPanes() {
        // 设置工具箱输入滚动面板
        inputScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        inputScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        inputScroll.setPreferredSize(null); // 移除首选大小，让其自动适应父容器

        // 设置工具箱输出滚动面板
        outputScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        outputScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        outputScroll.setPreferredSize(null); // 移除首选大小，让其自动适应父容器

        // 确保文本区域能够随滚动面板一起调整大小
        setComponentSizePolicy(inputArea);
        setComponentSizePolicy(outputArea);
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

    private void setupEventHandlers() {
        // 工具箱事件处理
        jsonFormatButton.addActionListener(e -> formatJSON());
        jsonToJavaButton.addActionListener(e -> convertJSONToJava());
        sqlToJavaButton.addActionListener(e -> convertSQLToJava());
        encryptButton.addActionListener(e -> encryptText());
        decryptButton.addActionListener(e -> decryptText());
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("ToolsPanel");
            frame.setContentPane(new ToolsPanel().toolsPanel);
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
        toolsPanel = new JPanel();
        toolsPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        toolsPanel.setMinimumSize(new Dimension(-1, -1));
        toolsPanel.setPreferredSize(new Dimension(-1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(5, 5, 5, 5), -1, -1));
        toolsPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        inputScroll = new JScrollPane();
        panel1.add(inputScroll, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(500, -1), null, 0, false));
        inputScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "输入", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        inputArea = new JTextArea();
        inputArea.setLineWrap(true);
        inputArea.setPreferredSize(new Dimension(400, 200));
        inputArea.setWrapStyleWord(true);
        inputScroll.setViewportView(inputArea);
        outputScroll = new JScrollPane();
        panel1.add(outputScroll, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(500, -1), null, 0, false));
        outputScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "输出", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setPreferredSize(new Dimension(400, 200));
        outputArea.setWrapStyleWord(true);
        outputScroll.setViewportView(outputArea);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        toolsPanel.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "工具列表", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        jsonFormatButton = new JButton();
        jsonFormatButton.setText("JSON格式化");
        panel2.add(jsonFormatButton);
        jsonToJavaButton = new JButton();
        jsonToJavaButton.setText("JSON转Java实体");
        panel2.add(jsonToJavaButton);
        sqlToJavaButton = new JButton();
        sqlToJavaButton.setText("SQL转Java实体");
        panel2.add(sqlToJavaButton);
        encryptButton = new JButton();
        encryptButton.setText("加密工具");
        panel2.add(encryptButton);
        decryptButton = new JButton();
        decryptButton.setText("解密工具");
        panel2.add(decryptButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return toolsPanel;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    @Override
    public JComponent getComponent(String project) {
        return new ToolsPanel(project).$$$getRootComponent$$$();
    }
}
