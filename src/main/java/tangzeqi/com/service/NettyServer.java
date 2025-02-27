package tangzeqi.com.service;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.SneakyThrows;
import tangzeqi.com.utils.NetUtils;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

import static tangzeqi.com.service.ChatService.*;

/**
 * 功能描述：服务端
 * 作者：唐泽齐
 */
public class NettyServer {
    private volatile NioEventLoopGroup accpt = new NioEventLoopGroup();
    private volatile NioEventLoopGroup message = new NioEventLoopGroup();
    private volatile boolean open = false;

    private volatile Channel channel;
    private volatile ServerBootstrap server = new ServerBootstrap();

    public int makeServer(String host, AtomicInteger port, ServerHandler serverHandler) throws Throwable {
        if (!NetUtils.port(port.get())) return port.get();
        serverHandler.setServerCache(host + ":" + port);
        sysMessage("尝试创建聊天室 IP = " + host + ", 端口号 = " + port);
        serverHandler.host = host;
        serverHandler.port = port.get();
        serverHandler.makeonLine();
        try {
            if (open) {
                sysMessage("服务端更新绑定至 " + port);
                serverHandler.clear();
                channel = server.bind(port.get()).sync().channel();
                serverHandler.setServerCache(host + ":" + port);
                startStatus(true);
                channel.closeFuture().sync();
                return port.get();
            }
            open = true;
            channel = server
                    .group(accpt, message)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new LineBasedFrameDecoder(Integer.MAX_VALUE));
                            ch.pipeline().addLast(new StringDecoder(Charset.forName("UTF-8")));
                            ch.pipeline().addLast(new StringEncoder(Charset.forName("UTF-8")));
                            ch.pipeline().addLast(serverHandler);
                        }
                    })
                    .bind(port.get()).sync().channel();
            port.set(((InetSocketAddress) channel.localAddress()).getPort());
            serverHandler.port = port.get();
            sysMessage("当前服务 IP = " + host + ", 端口号 = " + port);
            startStatus(true);
            channel.closeFuture().sync();
            return port.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        } finally {
//            accpt.shutdownGracefully().sync();
//            message.shutdownGracefully().sync();
            startStatus(false);
        }
        return port.get();
    }

    public void out() {
        channel.close();
        for (ChannelHandlerContext value : serverHandler.customerCache.values()) {
            value.close();
        }
    }

    public void shutDown() {
        try {
            accpt.shutdownGracefully().sync();
            message.shutdownGracefully().sync();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            NettyServer server = new NettyServer();
            AtomicInteger port = new AtomicInteger(0);
            server.makeServer("192.168.0.158", port, new ServerHandler());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

}
