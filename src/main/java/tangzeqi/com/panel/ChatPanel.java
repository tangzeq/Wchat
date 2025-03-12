package tangzeqi.com.panel;

import com.intellij.ui.components.JBTextField;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.UIUtil;
import tangzeqi.com.service.ChatService;
import tangzeqi.com.service.MqttService;
import tangzeqi.com.stroge.TextMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static tangzeqi.com.utils.PanelUtils.textLimit;

public class ChatPanel extends JPanel {


    private final JTextArea messageArea;
    private final JBTextField inputField;
    private final JButton sendButton;

    private GridBagConstraints gbc = new GridBagConstraints();

    public ChatPanel() {
        super(new BorderLayout());
        inputField = new JBTextField();
        messageArea = new JTextArea();
        sendButton = new JButton("发送");
        ///////////////////聊天窗口////////////////////
        chatPanel();
        ChatService.chat = this;
    }


    private void chatPanel() {
        messageArea.setEditable(false);
        messageArea.setFont(UIUtil.getLabelFont());
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mouseClick(e, messageArea);
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
        JScrollPane scrollPane = new JScrollPane(messageArea);
        add(scrollPane, BorderLayout.CENTER);
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField.addActionListener(this::sendMessage);
        inputField.setEditable(true);
        inputField.getDocument().addDocumentListener(textLimit(inputField, 1000));
        sendButton.addActionListener(this::sendMessage);
        inputPanel.add(inputField,BorderLayout.CENTER);
        inputPanel.add(sendButton,BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);
        addMessage("欢迎来到聊天插件!", "系统");
    }

    private void mouseClick(MouseEvent e, JTextArea area) {
        int i = area.viewToModel(new Point(e.getX(), e.getY()));
        String regex = "\\[(.+?)\\]\\s(.+\\..+)\\:(\\d+)（点击跳转）\\n";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(area.getText());
        String file;
        int line;
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            if (start <= i && end >= i) {
                file = matcher.group(2);
                line = Integer.parseInt(matcher.group(3));
                ChatService.openFileLine(file, line);
                break;
            }
        }
    }


    private void sendMessage(ActionEvent e) {
        String message = inputField.getText().trim();
        if (!ChatService.connect && !ChatService.mqtt) {
            addMessage("未加入聊天室或未启用公网频道!", "系统");
        } else if (!message.isEmpty()) {
            inputField.setEnabled(false);
            sendButton.setEnabled(false);
            sendButton.setText("发送中...");
            if (ChatService.connect) {
                TextMessage textMessage = new TextMessage();
                textMessage.setMessage(message);
                textMessage.setName(ChatService.userName);
                ChatService.customerHandler.sendMessage(textMessage);
            }
            if (ChatService.mqtt) {
                MqttService.message(message);
            }
            inputField.setText("");
            sendButton.setText("发送");
            inputField.setEnabled(true);
            sendButton.setEnabled(true);
            inputField.requestFocusInWindow();
        }
    }

    public void addMessage(String message, String sender) {
        String formattedMessage = String.format("[%s] %s\n", sender, message);
        messageArea.append(formattedMessage);
        messageArea.setCaretPosition(messageArea.getDocument().getLength());
    }


    public void inputFieldPost(String str) {
        inputField.setText(str);
        inputField.postActionEvent();
        inputField.setText("");
    }

    public static Content content() {
        ChatPanel chatPanel = new ChatPanel();
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        return contentFactory.createContent(chatPanel, "chat", false);
    }

}
