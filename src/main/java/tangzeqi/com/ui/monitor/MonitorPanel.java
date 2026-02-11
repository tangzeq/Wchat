package tangzeqi.com.ui.monitor;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import tangzeqi.com.tools.monitor.PieChartPanel;
import tangzeqi.com.ui.MyPanel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.lang.management.*;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.DecimalFormat;
import java.util.*;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MonitorPanel extends JPanel implements MyPanel {
    private final String project;
    private JPanel monitorPanel;
    private JPanel cpuPanel;
    private JLabel cpuUsageLabel;
    private JLabel projectCpuUsageLabel;
    private JPanel memoryPanel;
    private JLabel memoryUsageLabel;
    private JPanel diskPanel;
    private JLabel diskUsageLabel;
    private JLabel projectDiskUsageLabel;
    private JPanel networkPanel;
    private JLabel networkDownloadLabel;
    private JLabel networkUploadLabel;
    private JPanel systemInfoPanel;
    private JLabel osNameLabel;
    private JLabel osVersionLabel;
    private JLabel javaVersionLabel;
    private JLabel systemTimeLabel;
    private JLabel ipAddressLabel;
    private JLabel projectInfoLabel;

    // 饼图组件
    private PieChartPanel cpuPieChart;
    private PieChartPanel projectCpuPieChart;
    private PieChartPanel memoryPieChart;
    private PieChartPanel systemMemoryPieChart;
    private PieChartPanel diskPieChart;
    private PieChartPanel projectDiskPieChart;

    private ScheduledExecutorService monitorUpdater;
    private final DecimalFormat df = new DecimalFormat("#.00");

    // 网络流量统计
    private long previousTxBytes = 0;
    private long previousRxBytes = 0;
    private long previousTime = 0;


    public MonitorPanel(String project) {
        this.project = project;
        $$$setupUI$$$();
        startMonitorUpdater();
    }

    public MonitorPanel() {
        this.project = null;
        $$$setupUI$$$();
    }

    private void startMonitorUpdater() {
        monitorUpdater = Executors.newSingleThreadScheduledExecutor();
        monitorUpdater.scheduleAtFixedRate(() -> {
            updateCPUUsage();
            updateMemoryUsage();
            updateDiskUsage();
            updateNetworkUsage();
            updateSystemInfo();
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void updateCPUUsage() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            double systemCpuUsage = 0;

            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                com.sun.management.OperatingSystemMXBean sunOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
                systemCpuUsage = sunOsBean.getSystemCpuLoad() * 100;
            }

            // 项目CPU使用率（使用当前线程的CPU使用情况作为参考）
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            double projectCpuUsage = 0;
            if (threadBean.isCurrentThreadCpuTimeSupported()) {
                long currentThreadCpuTime = threadBean.getCurrentThreadCpuTime();
                long elapsedTime = 1000000000; // 1秒
                projectCpuUsage = (currentThreadCpuTime / (double) elapsedTime) * 100;
                // 限制在合理范围内
                if (projectCpuUsage > 100) projectCpuUsage = 100;
            }

            cpuUsageLabel.setText("系统: " + df.format(systemCpuUsage) + "%");
            projectCpuUsageLabel.setText("项目: " + df.format(projectCpuUsage) + "%");

            if (cpuPieChart != null) {
                cpuPieChart.setValue(systemCpuUsage);
            }

            if (projectCpuPieChart != null) {
                projectCpuPieChart.setValue(projectCpuUsage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMemoryUsage() {
        try {
            // JVM内存使用率
            Runtime runtime = Runtime.getRuntime();
            long totalJVMMemory = runtime.totalMemory() / (1024 * 1024);
            long freeJVMMemory = runtime.freeMemory() / (1024 * 1024);
            long usedJVMMemory = totalJVMMemory - freeJVMMemory;
            double jvmMemoryUsage = (double) usedJVMMemory / totalJVMMemory * 100;

            // 系统内存使用率
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            double systemMemoryUsage = 0;
            long totalSystemMemory = 0;
            long usedSystemMemory = 0;

            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                com.sun.management.OperatingSystemMXBean sunOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
                totalSystemMemory = sunOsBean.getTotalPhysicalMemorySize() / (1024 * 1024);
                long freeSystemMemory = sunOsBean.getFreePhysicalMemorySize() / (1024 * 1024);
                usedSystemMemory = totalSystemMemory - freeSystemMemory;
                systemMemoryUsage = (double) usedSystemMemory / totalSystemMemory * 100;
            }

            memoryUsageLabel.setText("JVM: " + df.format(jvmMemoryUsage) + "% (" + usedJVMMemory + "MB / " + totalJVMMemory + "MB)\n系统: " + df.format(systemMemoryUsage) + "% (" + usedSystemMemory + "MB / " + totalSystemMemory + "MB)");

            if (memoryPieChart != null) {
                memoryPieChart.setValue(jvmMemoryUsage);
            }

            if (systemMemoryPieChart != null) {
                systemMemoryPieChart.setValue(systemMemoryUsage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateDiskUsage() {
        try {
            // 系统磁盘使用率
            File[] roots = File.listRoots();
            long totalSystemSpace = 0;
            long freeSystemSpace = 0;

            for (File root : roots) {
                totalSystemSpace += root.getTotalSpace();
                freeSystemSpace += root.getFreeSpace();
            }

            long usedSystemSpace = totalSystemSpace - freeSystemSpace;
            double systemDiskUsage = (double) usedSystemSpace / totalSystemSpace * 100;

            // 项目磁盘使用率
            long projectSize = 0;
            long totalProjectSpace = 1024 * 1024 * 1024; // 假设项目最大空间为1GB
            double projectDiskUsage = 0;

            if (project != null) {
                File projectDir = new File(project);
                if (projectDir.exists() && projectDir.isDirectory()) {
                    projectSize = getDirectorySize(projectDir);
                    projectDiskUsage = (double) projectSize / totalProjectSpace * 100;
                    // 限制在合理范围内
                    if (projectDiskUsage > 100) projectDiskUsage = 100;
                }
            }

            diskUsageLabel.setText("系统: " + df.format(systemDiskUsage) + "% (" + formatBytes(usedSystemSpace) + " / " + formatBytes(totalSystemSpace) + ")");
            projectDiskUsageLabel.setText("项目: " + df.format(projectDiskUsage) + "% (" + formatBytes(projectSize) + " / " + formatBytes(totalProjectSpace) + ")");

            if (diskPieChart != null) {
                diskPieChart.setValue(systemDiskUsage);
            }

            if (projectDiskPieChart != null) {
                projectDiskPieChart.setValue(projectDiskUsage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long getDirectorySize(File directory) {
        long size = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    size += file.length();
                } else if (file.isDirectory()) {
                    size += getDirectorySize(file);
                }
            }
        }
        return size;
    }

    private void updateNetworkUsage() {
        try {
            long currentTime = System.currentTimeMillis();
            long txBytes = 0;
            long rxBytes = 0;

            // 获取所有网络接口的流量数据
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    continue;
                }

                // 尝试使用 com.sun.management.NetworkInterface 获取网络流量数据
                try {
                    // 使用反射获取网络流量数据，避免编译错误
                    Class<?> sunNetworkInterfaceClass = Class.forName("com.sun.management.NetworkInterface");
                    if (sunNetworkInterfaceClass.isAssignableFrom(networkInterface.getClass())) {
                        Method getTxBytesMethod = sunNetworkInterfaceClass.getMethod("getTxBytes");
                        Method getRxBytesMethod = sunNetworkInterfaceClass.getMethod("getRxBytes");
                        txBytes += (long) getTxBytesMethod.invoke(networkInterface);
                        rxBytes += (long) getRxBytesMethod.invoke(networkInterface);
                    }
                } catch (Exception e) {
                    // 如果反射失败，继续尝试其他接口
                }
            }

            // 计算网络速度
            if (previousTime > 0 && previousTxBytes > 0 && previousRxBytes > 0) {
                double timeDiff = (currentTime - previousTime) / 1000.0; // 转换为秒
                if (timeDiff > 0) {
                    double downloadSpeed = (rxBytes - previousRxBytes) / timeDiff / 1024.0; // 转换为 KB/s
                    double uploadSpeed = (txBytes - previousTxBytes) / timeDiff / 1024.0; // 转换为 KB/s

                    networkDownloadLabel.setText("下载: " + df.format(downloadSpeed) + " KB/s");
                    networkUploadLabel.setText("上传: " + df.format(uploadSpeed) + " KB/s");
                }
            }

            // 更新历史数据
            previousTxBytes = txBytes;
            previousRxBytes = rxBytes;
            previousTime = currentTime;
        } catch (Exception e) {
            e.printStackTrace();
            networkDownloadLabel.setText("下载: 0.00 KB/s");
            networkUploadLabel.setText("上传: 0.00 KB/s");
        }
    }

    private void updateSystemInfo() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

            osNameLabel.setText("操作系统: " + osBean.getName());
            osVersionLabel.setText("系统版本: " + osBean.getVersion());
            javaVersionLabel.setText("Java版本: " + runtimeBean.getVmVersion());

            // 更新系统时间
            Date now = new Date();
            systemTimeLabel.setText("系统时间: " + now.toString());

            // 更新IP地址
            StringBuilder ipAddresses = new StringBuilder();
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress.getHostAddress().contains(".")) {
                        ipAddresses.append(inetAddress.getHostAddress()).append("; ");
                    }
                }
            }
            ipAddressLabel.setText("IP地址: " + (ipAddresses.length() > 0 ? ipAddresses.substring(0, ipAddresses.length() - 2) : "未找到"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return df.format(bytes / 1024.0) + " KB";
        } else if (bytes < 1024 * 1024 * 1024) {
            return df.format(bytes / (1024.0 * 1024.0)) + " MB";
        } else {
            return df.format(bytes / (1024.0 * 1024.0 * 1024.0)) + " GB";
        }
    }

    private void createUIComponents() {
        // 自定义组件创建代码
    }

    @Override
    public JComponent getComponent(String project) {
        return new MonitorPanel(project).$$$getRootComponent$$$();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("MonitorPanel");
            frame.setContentPane(new MonitorPanel().$$$getRootComponent$$$());
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
        monitorPanel = new JPanel();
        monitorPanel.setLayout(new GridLayoutManager(3, 2, new Insets(10, 10, 10, 10), -1, -1));
        monitorPanel.setMinimumSize(new Dimension(800, 600));
        monitorPanel.setPreferredSize(new Dimension(1000, 700));
        monitorPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$("微软雅黑", Font.BOLD, 14, monitorPanel.getFont()), null));
        systemInfoPanel = new JPanel();
        systemInfoPanel.setLayout(new GridLayoutManager(6, 1, new Insets(5, 5, 5, 5), -1, -1));
        monitorPanel.add(systemInfoPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        systemInfoPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$("微软雅黑", Font.PLAIN, 12, systemInfoPanel.getFont()), null));
        osNameLabel = new JLabel();
        Font osNameLabelFont = this.$$$getFont$$$("微软雅黑", Font.PLAIN, 12, osNameLabel.getFont());
        if (osNameLabelFont != null) osNameLabel.setFont(osNameLabelFont);
        osNameLabel.setText("操作系统: ");
        systemInfoPanel.add(osNameLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        osVersionLabel = new JLabel();
        Font osVersionLabelFont = this.$$$getFont$$$("微软雅黑", Font.PLAIN, 12, osVersionLabel.getFont());
        if (osVersionLabelFont != null) osVersionLabel.setFont(osVersionLabelFont);
        osVersionLabel.setText("系统版本: ");
        systemInfoPanel.add(osVersionLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        javaVersionLabel = new JLabel();
        Font javaVersionLabelFont = this.$$$getFont$$$("微软雅黑", Font.PLAIN, 12, javaVersionLabel.getFont());
        if (javaVersionLabelFont != null) javaVersionLabel.setFont(javaVersionLabelFont);
        javaVersionLabel.setText("Java版本: ");
        systemInfoPanel.add(javaVersionLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        systemTimeLabel = new JLabel();
        Font systemTimeLabelFont = this.$$$getFont$$$("微软雅黑", Font.PLAIN, 12, systemTimeLabel.getFont());
        if (systemTimeLabelFont != null) systemTimeLabel.setFont(systemTimeLabelFont);
        systemTimeLabel.setText("系统时间: ");
        systemInfoPanel.add(systemTimeLabel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ipAddressLabel = new JLabel();
        Font ipAddressLabelFont = this.$$$getFont$$$("微软雅黑", Font.PLAIN, 12, ipAddressLabel.getFont());
        if (ipAddressLabelFont != null) ipAddressLabel.setFont(ipAddressLabelFont);
        ipAddressLabel.setText("IP地址: ");
        systemInfoPanel.add(ipAddressLabel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        projectInfoLabel = new JLabel();
        Font projectInfoLabelFont = this.$$$getFont$$$("微软雅黑", Font.PLAIN, 12, projectInfoLabel.getFont());
        if (projectInfoLabelFont != null) projectInfoLabel.setFont(projectInfoLabelFont);
        projectInfoLabel.setText("项目: 未选择");
        systemInfoPanel.add(projectInfoLabel, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        networkPanel = new JPanel();
        networkPanel.setLayout(new GridLayoutManager(2, 1, new Insets(5, 5, 5, 5), -1, -1));
        monitorPanel.add(networkPanel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        networkPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$("微软雅黑", Font.PLAIN, 12, networkPanel.getFont()), null));
        networkDownloadLabel = new JLabel();
        Font networkDownloadLabelFont = this.$$$getFont$$$("微软雅黑", Font.PLAIN, 14, networkDownloadLabel.getFont());
        if (networkDownloadLabelFont != null) networkDownloadLabel.setFont(networkDownloadLabelFont);
        networkDownloadLabel.setHorizontalAlignment(0);
        networkDownloadLabel.setText("下载: 0.00 KB/s");
        networkPanel.add(networkDownloadLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        networkUploadLabel = new JLabel();
        Font networkUploadLabelFont = this.$$$getFont$$$("微软雅黑", Font.PLAIN, 14, networkUploadLabel.getFont());
        if (networkUploadLabelFont != null) networkUploadLabel.setFont(networkUploadLabelFont);
        networkUploadLabel.setHorizontalAlignment(0);
        networkUploadLabel.setText("上传: 0.00 KB/s");
        networkPanel.add(networkUploadLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cpuPanel = new JPanel();
        cpuPanel.setLayout(new GridLayoutManager(2, 2, new Insets(5, 5, 5, 5), -1, -1));
        monitorPanel.add(cpuPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        cpuPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$("微软雅黑", Font.PLAIN, 12, cpuPanel.getFont()), null));
        cpuPieChart = new PieChartPanel();
        cpuPanel.add(cpuPieChart, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        projectCpuPieChart = new PieChartPanel();
        cpuPanel.add(projectCpuPieChart, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        cpuUsageLabel = new JLabel();
        Font cpuUsageLabelFont = this.$$$getFont$$$("微软雅黑", Font.BOLD, 14, cpuUsageLabel.getFont());
        if (cpuUsageLabelFont != null) cpuUsageLabel.setFont(cpuUsageLabelFont);
        cpuUsageLabel.setHorizontalAlignment(0);
        cpuUsageLabel.setText("系统: 0.00%");
        cpuPanel.add(cpuUsageLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        projectCpuUsageLabel = new JLabel();
        Font projectCpuUsageLabelFont = this.$$$getFont$$$("微软雅黑", Font.BOLD, 14, projectCpuUsageLabel.getFont());
        if (projectCpuUsageLabelFont != null) projectCpuUsageLabel.setFont(projectCpuUsageLabelFont);
        projectCpuUsageLabel.setHorizontalAlignment(0);
        projectCpuUsageLabel.setText("项目: 0.00%");
        cpuPanel.add(projectCpuUsageLabel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        memoryPanel = new JPanel();
        memoryPanel.setLayout(new GridLayoutManager(2, 2, new Insets(5, 5, 5, 5), -1, -1));
        monitorPanel.add(memoryPanel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        memoryPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$("微软雅黑", Font.PLAIN, 12, memoryPanel.getFont()), null));
        memoryPieChart = new PieChartPanel();
        memoryPanel.add(memoryPieChart, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        systemMemoryPieChart = new PieChartPanel();
        memoryPanel.add(systemMemoryPieChart, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        memoryUsageLabel = new JLabel();
        Font memoryUsageLabelFont = this.$$$getFont$$$("微软雅黑", Font.BOLD, 14, memoryUsageLabel.getFont());
        if (memoryUsageLabelFont != null) memoryUsageLabel.setFont(memoryUsageLabelFont);
        memoryUsageLabel.setHorizontalAlignment(0);
        memoryUsageLabel.setText("JVM: 0.00%\\n系统: 0.00%");
        memoryPanel.add(memoryUsageLabel, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        diskPanel = new JPanel();
        diskPanel.setLayout(new GridLayoutManager(2, 2, new Insets(5, 5, 5, 5), -1, -1));
        monitorPanel.add(diskPanel, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        diskPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$("微软雅黑", Font.PLAIN, 12, diskPanel.getFont()), null));
        diskPieChart = new PieChartPanel();
        diskPanel.add(diskPieChart, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        projectDiskPieChart = new PieChartPanel();
        diskPanel.add(projectDiskPieChart, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        diskUsageLabel = new JLabel();
        Font diskUsageLabelFont = this.$$$getFont$$$("微软雅黑", Font.BOLD, 14, diskUsageLabel.getFont());
        if (diskUsageLabelFont != null) diskUsageLabel.setFont(diskUsageLabelFont);
        diskUsageLabel.setHorizontalAlignment(0);
        diskUsageLabel.setText("系统: 0.00%");
        diskPanel.add(diskUsageLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        projectDiskUsageLabel = new JLabel();
        Font projectDiskUsageLabelFont = this.$$$getFont$$$("微软雅黑", Font.BOLD, 14, projectDiskUsageLabel.getFont());
        if (projectDiskUsageLabelFont != null) projectDiskUsageLabel.setFont(projectDiskUsageLabelFont);
        projectDiskUsageLabel.setHorizontalAlignment(0);
        projectDiskUsageLabel.setText("项目: 0.00%");
        diskPanel.add(projectDiskUsageLabel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return monitorPanel;
    }

}
