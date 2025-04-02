package tangzeqi.com.utils;

import lombok.SneakyThrows;
import tangzeqi.com.project.MyProject;
import tangzeqi.com.service.ChatService;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

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
        Enumeration<NetworkInterface> interfaceList = NetworkInterface.getNetworkInterfaces();
        while (interfaceList.hasMoreElements()) {
            NetworkInterface iface = interfaceList.nextElement();
            Enumeration<InetAddress> addrList = iface.getInetAddresses();
            while (addrList.hasMoreElements()) {
                InetAddress address = addrList.nextElement();
                if (!address.isLinkLocalAddress() && !address.isLoopbackAddress() && address.isSiteLocalAddress()) {
                    hosts.add(address.getHostAddress());
                }
            }
        }
        return hosts;
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return port;
    }

    public static Boolean port(String project,int port) {
        MyProject.cache(project).sysMessage("检查端口是否可用");
        try {
            ServerSocket socket = new ServerSocket(port);
            socket.close();
            MyProject.cache(project).sysMessage("端口 " + port + " 可用");
        } catch (IOException ignored) {
            MyProject.cache(project).sysMessage("端口 " + port + " 已被占用");
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        port();
    }

}
