package tangzeqi.com.utils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import tangzeqi.com.project.MyProject;

import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 功能描述：网络工具（兼容JVM IPv4参数修复版）
 * 作者：唐泽齐
 */
@Slf4j
public class NetUtils {

    @SneakyThrows
    public static String host() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Throwable e) {
            // 兼容处理：获取本地IP失败时返回回环地址
            return "127.0.0.1";
        }
    }

    @SneakyThrows
    public static List<String> hosts() {
        List<String> hosts = new ArrayList<>();

        // 兼容JVM强制IPv4参数：先判断参数，使用简化逻辑
        String preferIPv4 = System.getProperty("java.net.preferIPv4Stack");
        if ("true".equalsIgnoreCase(preferIPv4)) {
            // 简化逻辑：避免枚举网络接口导致的空指针
            String localHost = host();
            if (!hosts.contains(localHost)) {
                hosts.add(localHost);
            }
            if (!hosts.contains("127.0.0.1")) {
                hosts.add("127.0.0.1");
            }
            return hosts;
        }

        try {
            // 获取所有网络接口
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            // 空校验：interfaces可能为null
            if (interfaces == null) {
                hosts.add("127.0.0.1");
                return hosts;
            }

            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                // 空校验+状态校验：过滤无效接口
                if (networkInterface == null || !networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual()) {
                    continue;
                }

                // 获取接口地址列表，增加空校验
                List<InterfaceAddress> interfaceAddresses = networkInterface.getInterfaceAddresses();
                if (interfaceAddresses == null || interfaceAddresses.isEmpty()) {
                    continue;
                }

                // 遍历地址，核心null校验
                interfaceAddresses.forEach(interfaceAddress -> {
                    // 关键修复：interfaceAddress可能为null
                    if (interfaceAddress == null) {
                        return;
                    }

                    InetAddress broadcast = null;
                    try {
                        // 防护：getBroadcast()可能抛出异常或返回null
                        broadcast = interfaceAddress.getBroadcast();
                    } catch (Throwable e) {
                        return;
                    }

                    InetAddress address = interfaceAddress.getAddress();
                    // 只处理 IPv4 地址，增加完整的null和类型校验
                    if (broadcast != null && address instanceof Inet4Address
                            && !broadcast.isAnyLocalAddress()
                            && !broadcast.isLoopbackAddress()
                            && !broadcast.isLinkLocalAddress()) {

                        String ip = address.getHostAddress();
                        // 避免重复添加
                        if (ip != null && !ip.isEmpty() && !hosts.contains(ip)) {
                            hosts.add(ip);
                        }
                    }
                });
            }

            // 兜底：确保至少有回环地址
            if (hosts.isEmpty()) {
                hosts.add("127.0.0.1");
            }
        } catch (Throwable e) {
            // 异常处理：不再直接抛出，返回兜底地址
            // 使用 sysMessage 替代 System.err.println，避免编码问题
            // 由于这里没有 project 上下文，暂时保留错误信息但不输出
            hosts.clear();
            hosts.add("127.0.0.1");
            // 注释掉抛出异常，避免工具窗口崩溃
            // throw new RuntimeException(e);
        }
        return hosts;
    }

    public static Map<String, String> broadcast() {
        Map<String, String> map = new HashMap<>();
        map.put("255.255.255.255", mac());
        AtomicReference<Map<String, String>> broadcasts = new AtomicReference<>(map);

        // 兼容JVM强制IPv4参数
        String preferIPv4 = System.getProperty("java.net.preferIPv4Stack");
        if ("true".equalsIgnoreCase(preferIPv4)) {
            return broadcasts.get();
        }

        try {
            // 获取所有网络接口
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces == null) {
                return broadcasts.get();
            }

            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface == null || !networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual()) {
                    continue;
                }

                List<InterfaceAddress> interfaceAddresses = networkInterface.getInterfaceAddresses();
                if (interfaceAddresses == null || interfaceAddresses.isEmpty()) {
                    continue;
                }

                interfaceAddresses.forEach(interfaceAddress -> {
                    if (interfaceAddress == null) {
                        return;
                    }

                    InetAddress broadcast = null;
                    try {
                        broadcast = interfaceAddress.getBroadcast();
                    } catch (Throwable e) {
                        return;
                    }

                    InetAddress address = interfaceAddress.getAddress();
                    // 只处理 IPv4 地址
                    if (broadcast != null && address instanceof Inet4Address
                            && !broadcast.isAnyLocalAddress()
                            && !broadcast.isLoopbackAddress()
                            && !broadcast.isLinkLocalAddress()) {

                        String broadcastIp = broadcast.getHostAddress();
                        if (broadcastIp != null && !broadcastIp.isEmpty()) {
                            broadcasts.get().put(broadcastIp, mac());
                        }
                    }
                });
            }
        } catch (Throwable e) {
            // 异常处理：返回已有数据，不抛出异常
            log.error("获取broadcast失败: {}", e.getMessage());
            // throw new RuntimeException(e);
        }
        return broadcasts.get();
    }

    public static int port() {
        int port = 0;
        ServerSocket socket;
        try {
            socket = new ServerSocket(port);
            port = socket.getLocalPort();
            socket.close();
        } catch (Throwable e) {
            // 端口获取失败时，使用默认端口
            log.error("获取端口失败，使用默认端口8080: {}", e.getMessage());
            port = 8080;
            // throw new RuntimeException(e);
        }
        return port;
    }

    public static Boolean port(String project, int port) {
        // 空校验：避免project为null导致NPE
        if (project == null || project.trim().isEmpty()) {
            log.error("project参数不能为空");
            return false;
        }

        MyProject.cache(project).sysMessage("检查端口是否可用");
        try {
            ServerSocket socket = new ServerSocket(port);
            socket.close();
            MyProject.cache(project).sysMessage("端口 " + port + " 可用");
            return true;
        } catch (Throwable ignored) {
            MyProject.cache(project).sysMessage("端口 " + port + " 已被占用");
            return false;
        }
    }

    public static String mac() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            // 空校验
            if (network == null) {
                return "00-00-00-00-00-00";
            }

            byte[] mac = network.getHardwareAddress();
            if (mac != null) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < mac.length; i++) {
                    sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                }
                return sb.toString();
            } else {
                // 未找到物理地址时返回默认值，不抛出异常
                return "00-00-00-00-00-00";
            }
        } catch (Throwable e) {
            // 异常处理：返回默认MAC，避免崩溃
            log.error("获取MAC地址失败: {}", e.getMessage());
            return "00-00-00-00-00-00";
            // throw new RuntimeException(e);
        }
    }

    public static String mac(InetAddress ip) {
        // 空校验
        if (ip == null) {
            return mac();
        }

        try {
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            if (network == null) {
                return mac();
            }

            byte[] mac = network.getHardwareAddress();
            if (mac != null) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < mac.length; i++) {
                    sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                }
                return sb.toString();
            } else {
                return mac();
            }
        } catch (Throwable e) {
            log.error("获取指定IP的MAC地址失败: {}", e.getMessage());
            return mac();
            // throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {
        // 测试方法，使用Slf4j日志输出
        log.info("本地IP: {}", host());
        log.info("所有IP列表: {}", hosts());
        log.info("广播地址: {}", broadcast());
        log.info("可用端口: {}", port());
        log.info("MAC地址: {}", mac());
    }

}