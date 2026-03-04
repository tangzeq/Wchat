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

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    @Override
    public JComponent getComponent(String project) {
        return new ToolsPanel(project).toolsPanel;
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

}
