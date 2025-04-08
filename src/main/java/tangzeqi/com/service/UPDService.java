package tangzeqi.com.service;

import com.alibaba.fastjson.JSON;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.ObjectUtils;
import tangzeqi.com.project.MyProject;
import tangzeqi.com.stroge.BaseMessage;
import tangzeqi.com.stroge.BaseUser;
import tangzeqi.com.stroge.TextMessage;
import tangzeqi.com.stroge.UPDMessage;
import tangzeqi.com.utils.Md5Utils;
import tangzeqi.com.utils.MessageUtils;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 局域网广播通讯，UPD
 */
public class UPDService {

    private volatile boolean doing = false;

    private volatile AtomicLong msgIndesx = new AtomicLong(0);

    private final String project;
    /**
     * 广播模式
     */
    private volatile DatagramSocket socket;

    private final DatagramPacket packet = new DatagramPacket("".getBytes(), "".getBytes().length, InetAddress.getByName("255.255.255.255"), 0);


    private volatile ConcurrentHashMap<String, UPDInetSocketAddress> addresses = new ConcurrentHashMap<>();

    public UPDService(String project) throws SocketException, UnknownHostException {
        this.project = project;
    }

    public void start() {
        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);
            scan();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public void scan() {
        if(doing) {
        } else {
            doing = true;
            //感知
            MyProject.cache(project).executor.execute(() -> {
                MyProject.cache(project).executor.execute(new Scaner(0,5000));
                MyProject.cache(project).executor.execute(new Scaner(5001,10000));
                MyProject.cache(project).executor.execute(new Scaner(10001,15000));
                MyProject.cache(project).executor.execute(new Scaner(15001,20000));
                MyProject.cache(project).executor.execute(new Scaner(20001,25000));
                MyProject.cache(project).executor.execute(new Scaner(25001,30000));
                MyProject.cache(project).executor.execute(new Scaner(30001,35000));
                MyProject.cache(project).executor.execute(new Scaner(35001,40000));
                MyProject.cache(project).executor.execute(new Scaner(40001,45000));
                MyProject.cache(project).executor.execute(new Scaner(45001,50000));
                MyProject.cache(project).executor.execute(new Scaner(50001,55000));
                MyProject.cache(project).executor.execute(new Scaner(55001,60000));
                MyProject.cache(project).executor.execute(new Scaner(60001,65535));
            });
            //感知、信息、保活
            MyProject.cache(project).executor.execute(() -> {
                byte[] receiveBuffer = new byte[1500];
                final DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                while (!socket.isClosed()) {
                    try {
                        socket.receive(receivePacket);
                        String receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength(),StandardCharsets.UTF_8);
                        BaseMessage message = MessageUtils.resolve(receivedData);
                        if (message.getMessage() instanceof UPDMessage) {
                            if (check(((UPDMessage) message.getMessage()).getToken())) {
                                if(!addresses.containsKey(receivePacket.getAddress() + ":" + receivePacket.getPort())) {
                                    MyProject.cache(project).sysMessage("发现::"+receivePacket.getAddress() + ":" + receivePacket.getPort());
                                }
                                addresses.put(receivePacket.getAddress() + ":" + receivePacket.getPort(), new UPDInetSocketAddress(receivePacket.getAddress(), receivePacket.getPort()));
                            }
                        } else if (message.getMessage() instanceof TextMessage) {
                            if(msgIndesx.get()<message.getId() && msgIndesx.compareAndSet(msgIndesx.get(), message.getId())) {
                                MyProject.cache(project).chatMessage(((TextMessage) message.getMessage()).getMessage(), message.getMessage().getName());
                            }
                        }
                    } catch (IOException e) {
                    } catch (Exception e) {
                    }
                }
            });
        }
    }

    public <T extends BaseUser> void send(T o) {
        byte[] message = new byte[0];
        message = JSON.toJSONString(BaseMessage.builder().id(System.nanoTime()).message(o).build()).getBytes(StandardCharsets.UTF_8);
        packet.setData(message);
        packet.setLength(message.length);
        long time = new Date().getTime();
        for (UPDInetSocketAddress address : addresses.values()) {
            if(address.outTime>=time) {
                packet.setSocketAddress(address);
                try {
                    socket.send(packet);
                } catch (IOException e) {
                }
            }
        }
    }

    private boolean check(String token) {
        if (ObjectUtils.isEmpty(token)) return false;
        return Md5Utils.getMD5(new Date().getTime() / 1000000 + "", StandardCharsets.UTF_8.name()).equalsIgnoreCase(token);
    }

    public void shutDowm() {
        socket.close();
        doing = false;
        addresses.clear();
    }

    private class UPDInetSocketAddress extends InetSocketAddress {
        public final long outTime = DateUtils.addMinutes(new Date(),1).getTime();

        public UPDInetSocketAddress(int port) {
            super(port);
        }

        public UPDInetSocketAddress(InetAddress addr, int port) {
            super(addr, port);
        }

        public UPDInetSocketAddress(String hostname, int port) {
            super(hostname, port);
        }
    }

    private class Scaner implements Runnable {
        private final DatagramPacket scanPacket;

        {
            try {
                scanPacket = new DatagramPacket("".getBytes(), "".getBytes().length, InetAddress.getByName("255.255.255.255"), 0);
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }

        int start;
        int end;

        public Scaner(int start, int end) {
            this.start = start;
            this.end = end;
        }
        @Override
        public void run() {
            while (!socket.isClosed()) {
                for (int  portD = (start+end)/2 ,portU = ((start+end)/2)+1; portU <= end && portD >=start && !socket.isClosed(); portD--,portU++) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                    }
                    try {
                        BaseMessage<BaseUser> build = BaseMessage.builder().message(UPDMessage.builder().token(Md5Utils.getMD5(new Date().getTime() / 1000000 + "", StandardCharsets.UTF_8.name())).build()).build();
                        String string = JSON.toJSONString(build);
                        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
                        scanPacket.setData(bytes);
                        scanPacket.setLength(bytes.length);
                        scanPacket.setPort(portU);
                        socket.send(scanPacket);
//                        MyProject.cache(project).config.updconnectStatus(true,"开始感知端口:"+portU);
//                        MyProject.cache(project).sysMessage("开始感知端口:"+portU);
                        scanPacket.setPort(portD);
                        socket.send(scanPacket);
//                        MyProject.cache(project).config.updconnectStatus(true,"开始感知端口:"+portD);
//                        MyProject.cache(project).sysMessage("开始感知端口:"+portD);
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

}
