package tangzeqi.com.ui;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ObjectUtils;
import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.SourceType;
import tangzeqi.com.utils.CodeGenerationUtils;
import tangzeqi.com.utils.SQLUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Wchat {
    private final String project;
    private JPanel wchat;
    private JPanel buttons;
    private JButton jsonButton;
    private JButton jsonToJavaButton;
    private JButton sqlToJavaButton;
    private JTextArea entryArea;
    private JTextArea outArea;
    private JPanel entryPanel;
    private JScrollPane entryScroll;
    private JPanel outPanel;
    private JScrollPane outScroll;

    public Wchat(String project) {
        this.project = project;
        jsonButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jsonButton.setEnabled(false);
                String text = entryArea.getText();
                if (ObjectUtils.isNotEmpty(text)) text = text.trim();
                if (ObjectUtils.isNotEmpty(text)) {
                    JsonObject jsonObject = new GsonBuilder().create().fromJson(text, JsonObject.class);
                    // 格式化输出JSON
                    String formattedJson = new GsonBuilder().setPrettyPrinting().create().toJson(jsonObject);
                    outArea.setText(formattedJson);
                    wchat.revalidate();
                    wchat.repaint();
                    outArea.requestFocusInWindow();
                }
                jsonButton.setEnabled(true);
            }
        });
        jsonToJavaButton.addActionListener(new ActionListener() {
            @SneakyThrows
            @Override
            public void actionPerformed(ActionEvent e) {
                jsonToJavaButton.setEnabled(false);
                String text = entryArea.getText();
                if (ObjectUtils.isNotEmpty(text)) text = text.trim();
                if (ObjectUtils.isNotEmpty(text)) {
                    String code = CodeGenerationUtils.jsonToCode(null, text);
                    outArea.setText(code);
                    wchat.revalidate();
                    wchat.repaint();
                    outArea.requestFocusInWindow();
                }
                jsonToJavaButton.setEnabled(true);
            }
        });
        sqlToJavaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sqlToJavaButton.setEnabled(false);
                String text = entryArea.getText();
                if (ObjectUtils.isNotEmpty(text)) text = text.trim();
                if (ObjectUtils.isNotEmpty(text)) {
                    String json = SQLUtils.selectToJson(text);
                    String code = CodeGenerationUtils.jsonToCode(new DefaultGenerationConfig() {
                        @Override
                        public boolean isGenerateBuilders() { // 配置是否生成builder类
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
                        public SourceType getSourceType() { // 配置源类型
                            return SourceType.JSON;
                        }
                    }, json);
                    outArea.setText(code);
                    wchat.revalidate();
                    wchat.repaint();
                    outArea.requestFocusInWindow();
                }
                sqlToJavaButton.setEnabled(true);
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Wchat");
        frame.setContentPane(new Wchat("chat").wchat);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        wchat = new JPanel();
        wchat.setLayout(new GridLayoutManager(4, 6, new Insets(0, 0, 0, 0), -1, -1));
        wchat.setMaximumSize(new Dimension(-1, -1));
        wchat.setMinimumSize(new Dimension(-1, -1));
        wchat.setPreferredSize(new Dimension(-1, -1));
        buttons = new JPanel();
        buttons.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        buttons.setEnabled(true);
        wchat.add(buttons, new GridConstraints(0, 0, 2, 6, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 30), new Dimension(-1, 30), new Dimension(-1, 30), 0, false));
        jsonButton = new JButton();
        jsonButton.setText("JSON格式化");
        buttons.add(jsonButton);
        jsonToJavaButton = new JButton();
        jsonToJavaButton.setText("JSON转JAVA实体");
        buttons.add(jsonToJavaButton);
        sqlToJavaButton = new JButton();
        sqlToJavaButton.setText("SQL查询转Java实体");
        buttons.add(sqlToJavaButton);
        entryPanel = new JPanel();
        entryPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        wchat.add(entryPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        entryScroll = new JScrollPane();
        entryPanel.add(entryScroll, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        entryArea = new JTextArea();
        entryScroll.setViewportView(entryArea);
        final Spacer spacer1 = new Spacer();
        wchat.add(spacer1, new GridConstraints(2, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        wchat.add(spacer2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        outPanel = new JPanel();
        outPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        wchat.add(outPanel, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        outScroll = new JScrollPane();
        outPanel.add(outScroll, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        outArea = new JTextArea();
        outScroll.setViewportView(outArea);
        final Spacer spacer3 = new Spacer();
        wchat.add(spacer3, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return wchat;
    }

}
