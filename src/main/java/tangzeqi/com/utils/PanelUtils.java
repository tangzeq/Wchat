package tangzeqi.com.utils;

import com.intellij.ui.components.JBTextField;
import org.apache.commons.lang3.ObjectUtils;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;

public class PanelUtils {
    public static GridBagConstraints resetGBC(@Nullable GridBagConstraints gbc) {
        if(ObjectUtils.isEmpty(gbc)) gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        return gbc;
    }

    public static DocumentListener textLimit(JBTextField text, int len) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (ObjectUtils.isNotEmpty(text.getText()) && text.getText().length() > len) {
                    SwingUtilities.invokeLater(() -> {
                        try {
                            text.setText(text.getText(0, len));
                            text.setSelectionStart(text.getText().length());
                            text.setSelectionEnd(text.getText().length());
                        } catch (Throwable ex) {
                            throw new RuntimeException(ex);
                        }
                    });
                    Toolkit.getDefaultToolkit().beep();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        };
    }
}
