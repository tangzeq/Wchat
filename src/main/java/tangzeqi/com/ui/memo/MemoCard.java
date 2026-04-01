package tangzeqi.com.ui.memo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class MemoCard {
    private JButton over;
    private JButton edit;
    private JButton delete;
    private JTextArea textArea;
    private JPanel buttons;
    private JLabel timeLabel;
    public JPanel main;

    public MemoCard() {
        createUIComponents();
    }
    public void text(String text, Color background, Color foreground) {
        this.textArea.setText(text);
        this.textArea.setBackground(background);
        this.main.setBackground(background);
        this.buttons.setBackground(background);
        this.textArea.setForeground(foreground);
        this.timeLabel.setForeground(foreground);
    }

    public void setTime(String time) {
        this.timeLabel.setText(time);
    }

    public void buttons(ActionListener over, ActionListener edit, ActionListener delete) {
        this.over.addActionListener(over);
        this.edit.addActionListener(edit);
        this.delete.addActionListener(delete);
    }

    public void setCompleted(boolean completed) {
        if (completed) {
            textArea.setFont(textArea.getFont().deriveFont(Font.ITALIC));
            textArea.setForeground(Color.GRAY);
            timeLabel.setForeground(Color.GRAY);
            over.setText("取消完成");
        } else {
            textArea.setFont(textArea.getFont().deriveFont(Font.PLAIN));
            over.setText("完成");
        }
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    public JPanel getUIComponents() {
        main.setVisible(true);
        return main;
    }
}
