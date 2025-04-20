package tangzeqi.com.utils;

import lombok.SneakyThrows;
import org.apache.commons.collections.MapUtils;
import tangzeqi.com.project.MyProject;

import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 功能描述：网络工具
 * 作者：唐泽齐
 */
public class NetUtils {

    @SneakyThrows
    public static String host() {
        return InetAddress.getLocalHost().getHostAddress();
    }

    @SneakyThrows
    public static List<String> hosts() {
        List<String> hosts = new ArrayList<>();
        try {
            // 获取所有网络接口
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isUp()) {
                    // 获取该网络接口的所有地址
                    networkInterface.getInterfaceAddresses().forEach(interfaceAddress -> {
                        InetAddress broadcast = interfaceAddress.getBroadcast();
                        InetAddress address = interfaceAddress.getAddress();
                        // 只处理 IPv4 地址
                        if (broadcast != null && address instanceof Inet4Address && !broadcast.isAnyLocalAddress() && !broadcast.isLoopbackAddress() && !broadcast.isLinkLocalAddress()) {
                            hosts.add(address.getHostAddress());
                        }
                    });
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return hosts;
    }

    public static Map<String,String> broadcast() {
        Map<String,String> map = new HashMap<>();
        map.put("255.255.255.255",mac());
        AtomicReference<Map<String,String>> broadcasts = new AtomicReference<>(map);
        try {
            // 获取所有网络接口
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isUp()) {
                    // 获取该网络接口的所有地址
                    networkInterface.getInterfaceAddresses().forEach(interfaceAddress -> {
                        InetAddress broadcast = interfaceAddress.getBroadcast();
                        InetAddress address = interfaceAddress.getAddress();
                        // 只处理 IPv4 地址
                        if (broadcast != null && address instanceof Inet4Address && !broadcast.isAnyLocalAddress() && !broadcast.isLoopbackAddress() && !broadcast.isLinkLocalAddress()) {
                            broadcasts.get().put(broadcast.getHostAddress(),mac());
                        }
                    });
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return broadcasts.get();
    }

    public static int port() {
        int port = 0;
        ServerSocket socket;
//        ChatService.sysMessage("自动检索可用端口");
//        for (; port <= 65535; port++) {
//            try {
//                ServerSocket socket = new ServerSocket(port);
//                socket.close();
//                ChatService.sysMessage("端口 " + port + " 可用");
//                break;
//            } catch (IOException ignored) {
//                ChatService.sysMessage("端口 " + port + " 已被占用");
//            }
//        }
        try {
            socket = new ServerSocket(port);
            port = socket.getLocalPort();
            socket.close();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return port;
    }

    public static Boolean port(String project,int port) {
        MyProject.cache(project).sysMessage("检查端口是否可用");
        try {
            ServerSocket socket = new ServerSocket(port);
            socket.close();
            MyProject.cache(project).sysMessage("端口 " + port + " 可用");
        } catch (Throwable ignored) {
            MyProject.cache(project).sysMessage("端口 " + port + " 已被占用");
            return false;
        }
        return true;
    }

    public static String mac() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = network.getHardwareAddress();
            if (mac != null) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < mac.length; i++) {
                    sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                }
                return sb.toString();
            } else {
                throw new RuntimeException("未找到物理地址");
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static String mac(InetAddress ip) {
        try {
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
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
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {
        port();
    }

}
