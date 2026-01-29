package tangzeqi.com.ui;

import tangzeqi.com.tools.chat.stroge.BaseUser;
import tangzeqi.com.tools.chat.stroge.TextMessage;

import javax.swing.*;
import java.awt.*;

class MessageCellRenderer extends JTextArea implements ListCellRenderer<BaseUser> {
    public MessageCellRenderer() {
        setLineWrap(true);
        setWrapStyleWord(true);
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends BaseUser> list, BaseUser user, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        if(user instanceof TextMessage) {
            String text = user.getName() + ": " + ((TextMessage) user).getMessage();
            setText(text);

            // 重置首选大小，让组件自然布局
            setPreferredSize(null);

            // 计算文本所需的高度
            FontMetrics metrics = getFontMetrics(getFont());
            int width = list.getWidth();
            if (width > 0) {
                // 考虑边距
                width -= 10; // 左右边距各5像素
                int lines = (int) Math.ceil(metrics.stringWidth(text) / (double) width);
                int height = metrics.getHeight() * lines + 10; // 上下边距各5像素
                setPreferredSize(new Dimension(width, height));
            }
        }

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        return this;
    }
}


