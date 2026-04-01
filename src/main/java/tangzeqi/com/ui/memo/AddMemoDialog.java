package tangzeqi.com.ui.memo;

import com.michaelbaranov.microba.calendar.DatePicker;
import tangzeqi.com.tools.memo.model.MemoItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AddMemoDialog {
    private JPanel main;
    private JTextArea textContext;
    private DatePicker datePicker;
    private JSpinner hourSpinner;
    private JSpinner minuteSpinner;
    private JSpinner secondSpinner;
    private JRadioButton noRepeatRadio;
    private JRadioButton dailyRadio;
    private JRadioButton monthlyRadio;
    private JRadioButton yearlyRadio;
    private JButton saveButton;
    private JButton cancelButton;
    public JTextArea textView;
    public JPanel cardColor;
    public JPanel fontColor;
    public JPanel panel1;
    private MemoItem memo = new MemoItem();
    private ButtonGroup repeatButtonGroup;

    public AddMemoDialog() {
        SwingUtilities.invokeLater(() -> {
            // 初始化重复规则单选按钮组
            repeatButtonGroup = new ButtonGroup();
            repeatButtonGroup.add(noRepeatRadio);
            repeatButtonGroup.add(dailyRadio);
            repeatButtonGroup.add(monthlyRadio);
            repeatButtonGroup.add(yearlyRadio);

            for (Component component : cardColor.getComponents()) {
                component.addMouseListener(new MouseListener() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        textView.setBackground(e.getComponent().getBackground());
                        memo.setCardColor(textView.getBackground().toString());
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
            }
            for (Component component : fontColor.getComponents()) {
                component.addMouseListener(new MouseListener() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        textView.setForeground(e.getComponent().getBackground());
                        memo.setTextColor(textView.getForeground().toString());
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
            }
        });
    }

    public void save(ActionListener save) {
        SwingUtilities.invokeLater(() -> {
            saveButton.addActionListener(save);
        });
    }

    public MemoItem getMemo() {
        memo.setContent(textContext.getText());

        // 获取日期选择器的日期
        if (datePicker != null) {
            Date selectedDate = datePicker.getDate();
            if (selectedDate != null) {
                // 获取时分秒
                int hour = (Integer) hourSpinner.getValue();
                int minute = (Integer) minuteSpinner.getValue();
                int second = (Integer) secondSpinner.getValue();

                // 合并日期和时间
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(selectedDate);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, second);

                Date combinedDateTime = calendar.getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                memo.setReminderTime(sdf.format(combinedDateTime));
            } else {
                memo.setReminderTime("");
            }
        } else {
            memo.setReminderTime("");
        }

        // 获取选中的重复规则
        String repeatRule = "无";
        if (dailyRadio.isSelected()) {
            repeatRule = "日";
        } else if (monthlyRadio.isSelected()) {
            repeatRule = "月";
        } else if (yearlyRadio.isSelected()) {
            repeatRule = "年";
        }
        memo.setRepeatRule(repeatRule);

        memo.setTextColor(textView.getForeground().getRGB() + "");
        memo.setCardColor(textView.getBackground().getRGB() + "");
        return memo;
    }

    public void setMemo(MemoItem memo) {
        SwingUtilities.invokeLater(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            this.memo = memo;
            textView.setForeground(Color.decode(memo.getTextColor()));
            textView.setBackground(Color.decode(memo.getCardColor()));
            // 设置提醒时间
            if (datePicker != null && memo.getReminderTime() != null && !memo.getReminderTime().isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = sdf.parse(memo.getReminderTime());

                    // 设置日期
                    datePicker.setDate(date);

                    // 设置时分秒
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    hourSpinner.setValue(calendar.get(Calendar.HOUR_OF_DAY));
                    minuteSpinner.setValue(calendar.get(Calendar.MINUTE));
                    secondSpinner.setValue(calendar.get(Calendar.SECOND));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 设置重复规则
            String repeatRule = memo.getRepeatRule();
            if (repeatRule == null) {
                noRepeatRadio.setSelected(true);
            } else {
                switch (repeatRule) {
                    case "日":
                        dailyRadio.setSelected(true);
                        break;
                    case "月":
                        monthlyRadio.setSelected(true);
                        break;
                    case "年":
                        yearlyRadio.setSelected(true);
                        break;
                    default:
                        noRepeatRadio.setSelected(true);
                        break;
                }
            }
            textContext.setText(memo.getContent());
        });
    }

    public void cancel(ActionListener cancel) {
        cancelButton.addActionListener(cancel);
    }

    public JPanel getUIComponents() {
        main.setVisible(true);
        return main;
    }

    public JFrame getUIFrame() {
        JFrame frame = new JFrame("任务管理");
        frame.setContentPane(this.main);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        // 设置窗口在屏幕中央显示
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
        this.save(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
        this.cancel(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
        return frame;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        // 使用 IntelliJ 平台的 DatePicker
        datePicker = new DatePicker();
        // 初始化时分秒选择器
        // 小时选择器 (0-23)
        SpinnerNumberModel hourModel = new SpinnerNumberModel(0, 0, 23, 1);
        hourSpinner = new JSpinner(hourModel);
        try {
            JSpinner.NumberEditor hourEditor = new JSpinner.NumberEditor(hourSpinner, "00");
            hourSpinner.setEditor(hourEditor);
            // 确保文本框输入时也遵循限制
            JFormattedTextField hourTextField = hourEditor.getTextField();
            hourTextField.setFocusLostBehavior(JFormattedTextField.PERSIST);
            hourTextField.setInputVerifier(new InputVerifier() {
                @Override
                public boolean verify(JComponent input) {
                    JFormattedTextField textField = (JFormattedTextField) input;
                    try {
                        int value = Integer.parseInt(textField.getText());
                        return value >= 0 && value <= 23;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 分钟选择器 (0-59)
        SpinnerNumberModel minuteModel = new SpinnerNumberModel(0, 0, 59, 1);
        minuteSpinner = new JSpinner(minuteModel);
        try {
            JSpinner.NumberEditor minuteEditor = new JSpinner.NumberEditor(minuteSpinner, "00");
            minuteSpinner.setEditor(minuteEditor);
            // 确保文本框输入时也遵循限制
            JFormattedTextField minuteTextField = minuteEditor.getTextField();
            minuteTextField.setFocusLostBehavior(JFormattedTextField.PERSIST);
            minuteTextField.setInputVerifier(new InputVerifier() {
                @Override
                public boolean verify(JComponent input) {
                    JFormattedTextField textField = (JFormattedTextField) input;
                    try {
                        int value = Integer.parseInt(textField.getText());
                        return value >= 0 && value <= 59;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 秒选择器 (0-59)
        SpinnerNumberModel secondModel = new SpinnerNumberModel(0, 0, 59, 1);
        secondSpinner = new JSpinner(secondModel);
        try {
            JSpinner.NumberEditor secondEditor = new JSpinner.NumberEditor(secondSpinner, "00");
            secondSpinner.setEditor(secondEditor);
            // 确保文本框输入时也遵循限制
            JFormattedTextField secondTextField = secondEditor.getTextField();
            secondTextField.setFocusLostBehavior(JFormattedTextField.PERSIST);
            secondTextField.setInputVerifier(new InputVerifier() {
                @Override
                public boolean verify(JComponent input) {
                    JFormattedTextField textField = (JFormattedTextField) input;
                    try {
                        int value = Integer.parseInt(textField.getText());
                        return value >= 0 && value <= 59;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {
        JFrame frame = new AddMemoDialog().getUIFrame();
    }

}