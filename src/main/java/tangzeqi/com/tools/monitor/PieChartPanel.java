package tangzeqi.com.tools.monitor;

import tangzeqi.com.ui.monitor.MonitorPanel;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

// 饼图面板类
public class PieChartPanel extends JPanel {
    private double value;
    private String label;
    private Color color;
    private final DecimalFormat df = new DecimalFormat("#.00");

    public PieChartPanel() {
        this.value = 0;
        this.label = "";
        this.color = Color.RED;
        setPreferredSize(new Dimension(100, 100));
    }

    public PieChartPanel(double value, String label, Color color) {
        this.value = value;
        this.label = label;
        this.color = color;
        setPreferredSize(new Dimension(100, 100));
    }

    public void setValue(double value) {
        this.value = value;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // 绘制背景
        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // 绘制饼图
        int diameter = Math.min(getWidth() - 20, getHeight() - 20);
        int x = (getWidth() - diameter) / 2;
        int y = (getHeight() - diameter) / 2;

        // 绘制未使用部分
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillArc(x, y, diameter, diameter, 0, 360);

        // 绘制使用部分
        g2d.setColor(color);
        int arcAngle = (int) (value * 3.6);
        g2d.fillArc(x, y, diameter, diameter, 0, arcAngle);

        // 绘制边框
        g2d.setColor(Color.GRAY);
        g2d.drawArc(x, y, diameter, diameter, 0, 360);

        // 绘制中心文字
        g2d.setColor(getForeground());
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 12));
        String text = df.format(value) + "%";
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (getWidth() - fm.stringWidth(text)) / 2;
        int textY = (getHeight() + fm.getAscent()) / 2 - 5;
        g2d.drawString(text, textX, textY);

        g2d.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        text = label;
        fm = g2d.getFontMetrics();
        textX = (getWidth() - fm.stringWidth(text)) / 2;
        textY = (getHeight() + fm.getAscent()) / 2 + 10;
        g2d.drawString(text, textX, textY);
    }
}
