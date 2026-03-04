package tangzeqi.com.utils;

import com.sun.management.OperatingSystemMXBean;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.*;



public class SystemUtils {
    private static final OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    // 用于存储上一次网络数据统计
    private static long lastBytesSent = 0;
    private static long lastBytesRecv = 0;
    private static long lastTimestamp = System.currentTimeMillis();
    
    // 静态初始化块，首次加载时获取初始网络数据
    static {
        // 首次加载时获取初始网络数据，避免第一次计算时速度为0
        Map<String, Long> initialStats = getNetworkStats();
        lastBytesSent = initialStats.getOrDefault("tx_bytes", 0L);
        lastBytesRecv = initialStats.getOrDefault("rx_bytes", 0L);
        lastTimestamp = System.currentTimeMillis();
    }

    /**
     * 获取当前操作系统CPU使用率
     * @return CPU使用率（百分比）
     */
    public static double getSystemCpuUsage() {
        return osBean.getSystemCpuLoad() * 100;
    }

    /**
     * 获取当前操作系统内存使用率
     * @return 内存使用率（百分比）
     */
    public static double getSystemMemoryUsage() {
        long totalMemory = osBean.getTotalPhysicalMemorySize();
        long freeMemory = osBean.getFreePhysicalMemorySize();
        long usedMemory = totalMemory - freeMemory;
        return (double) usedMemory / totalMemory * 100;
    }

    /**
     * 获取当前操作系统磁盘使用率
     * @return 磁盘使用率（百分比）
     */
    public static double getSystemDiskUsage() {
        File[] roots = File.listRoots();
        long totalSpace = 0;
        long freeSpace = 0;

        for (File root : roots) {
            totalSpace += root.getTotalSpace();
            freeSpace += root.getFreeSpace();
        }

        long usedSpace = totalSpace - freeSpace;
        return (double) usedSpace / totalSpace * 100;
    }

    /**
     * 获取当前操作系统总的上传网速
     * @return 上传网速（自动转换为合适的单位）
     */
    public static String getSystemUploadSpeed() {
        double bytesPerSecond = getUploadSpeedInBytes();
        return formatSpeed(bytesPerSecond);
    }

    /**
     * 获取当前操作系统总的下载网速
     * @return 下载网速（自动转换为合适的单位）
     */
    public static String getSystemDownloadSpeed() {
        double bytesPerSecond = getDownloadSpeedInBytes();
        return formatSpeed(bytesPerSecond);
    }

    /**
     * 获取上传网速（字节/秒）
     * @return 上传网速（字节/秒）
     */
    public static double getUploadSpeedInBytes() {
        try {
            Map<String, Long> networkStats = getNetworkStats();
            long currentBytesSent = networkStats.getOrDefault("tx_bytes", 0L);

            long currentTimestamp = System.currentTimeMillis();
            long timeDiff = currentTimestamp - lastTimestamp;
            long bytesDiff = currentBytesSent - lastBytesSent;

            lastBytesSent = currentBytesSent;
            lastTimestamp = currentTimestamp;

            return timeDiff > 0 ? (double) bytesDiff / timeDiff * 1000 : 0.0; // 字节/秒
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    /**
     * 获取下载网速（字节/秒）
     * @return 下载网速（字节/秒）
     */
    public static double getDownloadSpeedInBytes() {
        try {
            Map<String, Long> networkStats = getNetworkStats();
            long currentBytesRecv = networkStats.getOrDefault("rx_bytes", 0L);

            long currentTimestamp = System.currentTimeMillis();
            long timeDiff = currentTimestamp - lastTimestamp;
            long bytesDiff = currentBytesRecv - lastBytesRecv;

            lastBytesRecv = currentBytesRecv;
            lastTimestamp = currentTimestamp;

            return timeDiff > 0 ? (double) bytesDiff / timeDiff * 1000 : 0.0; // 字节/秒
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    /**
     * 获取网络统计数据
     * @return 包含网络统计数据的Map
     */
    private static Map<String, Long> getNetworkStats() {
        Map<String, Long> stats = new HashMap<>();
        try {
            String osName = System.getProperty("os.name").toLowerCase();

            if (osName.contains("win")) {
                // Windows系统，使用netstat -e获取网络统计数据
                Process process = Runtime.getRuntime().exec("netstat -e");
                // 获取系统默认编码，解决乱码问题
                String encoding = System.getProperty("sun.jnu.encoding");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), encoding));

                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    // 寻找包含"字节"或"Bytes"的行
                    if (line.contains("字节") || line.contains("Bytes")) {
                        // 按空格分割，过滤空字符串
                        String[] parts = line.split("\\s+");
                        List<String> validParts = new ArrayList<>();
                        for (String part : parts) {
                            if (!part.isEmpty()) {
                                validParts.add(part);
                            }
                        }

                        if (validParts.size() >= 3) {
                            try {
                                // 确保正确获取发送和接收字节数
                                // 格式: 字节 接收字节数 发送字节数
                                long recv = parseNetworkValue(validParts.get(1));
                                long sent = parseNetworkValue(validParts.get(2));
                                stats.put("tx_bytes", sent);
                                stats.put("rx_bytes", recv);
                            } catch (NumberFormatException ex) {
                                System.out.println("Failed to parse network values: " + ex.getMessage());
                            }
                        }
                        break;
                    }
                }
                reader.close();
            } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
                // Unix/Linux系统
                Process process = Runtime.getRuntime().exec("cat /proc/net/dev");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(":")) {
                        String[] parts = line.split(":");
                        if (parts.length >= 2) {
                            String[] values = parts[1].trim().split("\\s+");
                            if (values.length >= 2) {
                                long recv = Long.parseLong(values[0]);
                                long sent = Long.parseLong(values[8]);
                                stats.put("tx_bytes", stats.getOrDefault("tx_bytes", 0L) + sent);
                                stats.put("rx_bytes", stats.getOrDefault("rx_bytes", 0L) + recv);
                            }
                        }
                    }
                }
                reader.close();
            } else if (osName.contains("mac")) {
                // macOS系统
                Process process = Runtime.getRuntime().exec("netstat -b -i");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("<Link#") || line.trim().isEmpty()) {
                        continue;
                    }

                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 7) {
                        try {
                            long recv = Long.parseLong(parts[6]);
                            long sent = Long.parseLong(parts[7]);
                            stats.put("tx_bytes", stats.getOrDefault("tx_bytes", 0L) + sent);
                            stats.put("rx_bytes", stats.getOrDefault("rx_bytes", 0L) + recv);
                        } catch (NumberFormatException e) {
                            // 忽略无法解析的行
                        }
                    }
                }
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stats;
    }

    /**
     * 解析网络值
     * @param value 字符串形式的网络值
     * @return 解析后的长整型值
     */
    private static long parseNetworkValue(String value) {
        try {
            // 移除可能的千位分隔符
            value = value.replace(",", "");
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * 格式化网速，自动转换为合适的单位
     * @param bytesPerSecond 网速（字节/秒）
     * @return 格式化后的网速字符串
     */
    private static String formatSpeed(double bytesPerSecond) {
        if (bytesPerSecond < 1024) {
            return String.format("%.2f B/s", bytesPerSecond);
        } else if (bytesPerSecond < 1024 * 1024) {
            return String.format("%.2f KB/s", bytesPerSecond / 1024);
        } else if (bytesPerSecond < 1024 * 1024 * 1024) {
            return String.format("%.2f MB/s", bytesPerSecond / (1024 * 1024));
        } else {
            return String.format("%.2f GB/s", bytesPerSecond / (1024 * 1024 * 1024));
        }
    }

    public static void main(String[] args) {
        System.out.println("args = " + getSystemUploadSpeed());
        System.out.println("args = " + getSystemDownloadSpeed());
    }
}
