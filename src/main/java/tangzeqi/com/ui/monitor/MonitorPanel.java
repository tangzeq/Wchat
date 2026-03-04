package tangzeqi.com.ui.monitor;

import tangzeqi.com.tools.monitor.Monitor;
import tangzeqi.com.tools.monitor.server.MonitorService;
import tangzeqi.com.ui.MyPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static tangzeqi.com.tools.monitor.stroge.MonitorItemEnum.*;
import static tangzeqi.com.utils.NetUtils.formatSpeed;

public class MonitorPanel extends JPanel implements MyPanel {
    private final String project;
    private JPanel monitorPanel;
    private JLabel projectInfoLabel;
    private JLabel refreshIntervalLabel;
    private JButton refreshButton;
    private JLabel statusLabel;
    private JLabel environmentInfoLabel;
    private JPanel topControlPanel;
    private JPanel metricsPanel;
    private JPanel systemMetricsPanel;
    private JCheckBox systemCpuLabel;
    private JCheckBox systemMemoryLabel;
    private JCheckBox systemDiskLabel;
    private JCheckBox systemUploadLabel;
    private JCheckBox systemDownloadLabel;
    private JPanel trendPanel;
    private JPanel trendChart;
    private JPanel alertPanel;
    private JLabel alertLabel;

    private ScheduledExecutorService monitorUpdater;
    private final DecimalFormat df = new DecimalFormat("#.00");
    private Monitor monitorService;

    // 告警记录
    private List<String> alerts = new ArrayList<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("MonitorPanel");
            frame.setContentPane(new MonitorPanel().monitorPanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        });
    }

    public MonitorPanel(String project) {
        this.project = project;
        this.monitorService = new MonitorService(project);
        SwingUtilities.invokeLater(() -> {
            // 初始化顶部标签数据
            updateTopLabels();
            //刷新按钮监听
            refreshButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateMetrics();
                }
            });

            //开始定时刷新
            monitorUpdater = Executors.newSingleThreadScheduledExecutor();
            monitorUpdater.scheduleAtFixedRate(this::updateMetrics, 0, 1, TimeUnit.SECONDS);
        });
    }

    public MonitorPanel() {
        this.project = null;

    }

    private void updateTopLabels() {
        // 更新项目信息
        if (project != null && !project.isEmpty()) {
            projectInfoLabel.setText("当前工程: " + project);
        } else {
            projectInfoLabel.setText("当前工程: [未选择]");
        }
        
        // 更新环境信息
        String osName = System.getProperty("os.name");
        String ip = getLocalIP();
        environmentInfoLabel.setText("系统: " + osName + " | IP: " + ip);
    }

    private String getLocalIP() {
        try {
            java.net.InetAddress localHost = java.net.InetAddress.getLocalHost();
            return localHost.getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    private void updateMetrics() {
        if (monitorService == null) {
            return;
        }
        try {
            //更新数据
            monitorService.flushAll();
            // 更新CPU
            Double cpu = monitorService.getLastData(SYS_CPU.name());
            systemCpuLabel.setText(getStatusIcon(cpu) + "CPU: " + df.format(cpu) + "%");

            // 更新内存
            double memory = monitorService.getLastData(SYS_MEM.name());
            systemMemoryLabel.setText(getStatusIcon(memory) + "内存: " + df.format(memory) + "%");

            // 更新磁盘
            double disk = monitorService.getLastData(SYS_DISK.name());
            systemDiskLabel.setText(getStatusIcon(disk) + "磁盘: " + df.format(disk) + "%");

            // 更新网络上传
            double upload = monitorService.getLastData(SYS_UP_SPEED.name());
            systemUploadLabel.setText("📤上传: " + formatSpeed(upload));

            // 更新网络下载
            double download = monitorService.getLastData(SYS_DOWN_SPEED.name());
            systemDownloadLabel.setText("📥下载: " + formatSpeed(download));

            // 更新状态
            updateStatus(cpu, memory, disk);

            // 更新告警
            checkAlerts(cpu, memory, disk);

            // 更新趋势图表
            updateTrendChart();

        } catch (Exception e) {
            statusLabel.setText("状态: 🔴 异常");
            alertLabel.setText("监控服务异常: " + e.getMessage());
        }
    }

    /**
     * 获取状态图标
     *
     * @param value 当前值
     * @return
     */
    private String getStatusIcon(double value) {
        if (value > 90) {
            return "🔴";
        } else if (value > 60) {
            return "🟡";
        } else {
            return "🟢";
        }
    }

    private void updateStatus(double cpu, double memory, double disk) {
        if (cpu > 90 || memory > 90 || disk > 90) {
            statusLabel.setText("状态: 🔴 警告");
        } else if (cpu > 70 || memory > 70 || disk > 70) {
            statusLabel.setText("状态: 🟡 注意");
        } else {
            statusLabel.setText("状态: 🟢 正常");
        }
    }

    private void checkAlerts(double cpu, double memory, double disk) {
        alerts.clear();

        if (cpu > 90) {
            alerts.add("CPU使用率过高: " + df.format(cpu) + "%");
        }
        if (memory > 90) {
            alerts.add("内存使用率过高: " + df.format(memory) + "%");
        }
        if (disk > 90) {
            alerts.add("磁盘使用率过高: " + df.format(disk) + "%");
        }

        if (alerts.isEmpty()) {
            alertLabel.setText("无告警");
        } else {
            StringBuilder alertText = new StringBuilder();
            for (String alert : alerts) {
                alertText.append(alert).append("\n");
            }
            alertLabel.setText(alertText.toString());
        }
    }

    private void updateTrendChart() {
        trendChart.revalidate();
        trendChart.repaint();
    }

    private void drawChart(Graphics g) {
        if (g == null) {
            return;
        }

        int width = trendChart.getWidth();
        int height = trendChart.getHeight();

        if (width <= 0 || height <= 0) {
            return;
        }

        // 绘制背景
        g.setColor(getBackground());
        g.fillRect(0, 0, width, height);

        // 绘制坐标轴
        g.setColor(Color.GRAY);
        g.drawLine(0, 0, 0, height); // Y轴
        g.drawLine(0, height, width, height); // X轴

        // 绘制网格线
        g.setColor(new Color(220, 220, 220, 100));
        drawGrid(g, width, height);

        // 绘制趋势线
        if (monitorService != null) {
            if (systemCpuLabel.isSelected()) {
                List<Double> cpuData = monitorService.getOldData(SYS_CPU.getName());
                drawTrendLine(g, cpuData, SYS_CPU.getColor(), width, height, 100);
            }
            if (systemMemoryLabel.isSelected()) {
                List<Double> memoryData = monitorService.getOldData(SYS_MEM.getName());
                drawTrendLine(g, memoryData, SYS_MEM.getColor(), width, height, 100);
            }
            if (systemDiskLabel.isSelected()) {
                List<Double> diskData = monitorService.getOldData(SYS_DISK.getName());
                drawTrendLine(g, diskData, SYS_DISK.getColor(), width, height, 100);
            }
            if (systemUploadLabel.isSelected()) {
                List<Double> uploadData = monitorService.getOldData(SYS_UP_SPEED.getName());
                // 计算最大值进行归一化
                double maxUpload = getMaxValue(uploadData);
                drawTrendLine(g, uploadData, SYS_UP_SPEED.getColor(), width, height, maxUpload > 0 ? maxUpload : 100);
            }
            if (systemDownloadLabel.isSelected()) {
                List<Double> downloadData = monitorService.getOldData(SYS_DOWN_SPEED.getName());
                // 计算最大值进行归一化
                double maxDownload = getMaxValue(downloadData);
                drawTrendLine(g, downloadData, SYS_DOWN_SPEED.getColor(), width, height, maxDownload > 0 ? maxDownload : 100);
            }
            drawLegend(g,width, height);
        } else {
        }
    }

    private void drawGrid(Graphics g, int width, int height) {
        g.setColor(new Color(220, 220, 220, 100));

        // 水平网格线
        for (int i = 0; i <= 5; i++) {
            int y = height * i / 5;
            g.drawLine(0, y, width, y);
        }

        // 垂直网格线
        for (int i = 0; i <= 6; i++) {
            int x = width * i / 6;
            g.drawLine(x, 0, x, height);
        }
    }

    private void drawTrendLine(Graphics g, List<Double> data, Color color, int width, int height, double maxValue) {
        if (data == null || data.size() < 2) return;

        g.setColor(color);
        int dataSize = data.size();
        int chartWidth = width;
        int chartHeight = height;

        // 使用二次贝塞尔曲线绘制平滑曲线
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(2));

        for (int i = 1; i < dataSize; i++) {
            double value1 = data.get(i - 1);
            double value2 = data.get(i);

            int x1 = chartWidth * (i - 1) / (dataSize - 1);
            int y1 = height - (int) (chartHeight * value1 / maxValue);

            int x2 = chartWidth * i / (dataSize - 1);
            int y2 = height - (int) (chartHeight * value2 / maxValue);

            // 计算控制点，使用相邻点的中点
            int controlX = (x1 + x2) / 2;
            int controlY = (y1 + y2) / 2;

            // 绘制二次贝塞尔曲线
            g2d.drawLine(x1, y1, controlX, controlY);
            g2d.drawLine(controlX, controlY, x2, y2);
        }
    }

    private void drawLegend(Graphics g, int width, int height) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("微软雅黑", Font.PLAIN, 11));

        int legendX = width - 200;
        int legendY = 30;

        if (systemCpuLabel.isSelected()) {
            g.setColor(SYS_CPU.getColor());
            g.fillRect(legendX, legendY, 10, 10);
            g.setColor(Color.BLACK);
            g.drawString("CPU", legendX + 15, legendY + 9);
            legendY += 20;
        }

        if (systemMemoryLabel.isSelected()) {
            g.setColor(SYS_MEM.getColor());
            g.fillRect(legendX, legendY, 10, 10);
            g.setColor(Color.BLACK);
            g.drawString("内存", legendX + 15, legendY + 9);
            legendY += 20;
        }

        if (systemDiskLabel.isSelected()) {
            g.setColor(SYS_DISK.getColor());
            g.fillRect(legendX, legendY, 10, 10);
            g.setColor(Color.BLACK);
            g.drawString("磁盘", legendX + 15, legendY + 9);
            legendY += 20;
        }
        if (systemUploadLabel.isSelected()) {
            g.setColor(SYS_UP_SPEED.getColor());
            g.fillRect(legendX, legendY, 10, 10);
            g.setColor(Color.BLACK);
            g.drawString("上传", legendX + 15, legendY + 9);
            legendY += 20;
        }
        if (systemDownloadLabel.isSelected()) {
            g.setColor(SYS_DOWN_SPEED.getColor());
            g.fillRect(legendX, legendY, 10, 10);
            g.setColor(Color.BLACK);
            g.drawString("下载", legendX + 15, legendY + 9);
            legendY += 20;
        }
    }

    private double getMaxValue(List<Double> data) {
        if (data == null || data.isEmpty()) {
            return 0.0;
        }
        double max = data.get(0);
        for (Double value : data) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    @Override
    public JComponent getComponent(String project) {
        return new MonitorPanel(project).monitorPanel;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        // 创建新的trendChart
        trendChart = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawChart(g);
            }
        };
        // 设置布局和尺寸
        trendChart.setLayout(new BorderLayout());
        trendChart.setPreferredSize(new Dimension(800, 300));
        trendChart.setMinimumSize(new Dimension(400, 200));
        trendChart.setBounds(0, 0, 800, 300);
    }
}
