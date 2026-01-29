package tangzeqi.com.tools.chat.server;


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
import tangzeqi.com.tools.chat.handler.ServerHandler;
import tangzeqi.com.project.MyProject;
import tangzeqi.com.utils.NetUtils;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 功能描述：服务端
 * 作者：唐泽齐
 */
public class NettyServer {
    private final String project;
    private volatile NioEventLoopGroup accpt = new NioEventLoopGroup();
    private volatile NioEventLoopGroup message = new NioEventLoopGroup();
    private volatile boolean open = false;

    private volatile Channel channel;
    private volatile ServerBootstrap server = new ServerBootstrap();

    public NettyServer(String project) {
        this.project = project;
    }

    public int makeServer(String host, AtomicInteger port, ServerHandler serverHandler) throws Throwable {
        if (!NetUtils.port(project, port.get())) return port.get();
        serverHandler.setServerCache(host + ":" + port);
        MyProject.cache(project).sysMessage("尝试创建聊天室 IP = " + host + ", 端口号 = " + port);
        serverHandler.host = host;
        serverHandler.port = port.get();
        serverHandler.makeonLine();
        try {
            if (open) {
                MyProject.cache(project).sysMessage("服务端更新绑定至 " + port);
                serverHandler.clear();
                channel = server.bind(port.get()).sync().channel();
                serverHandler.setServerCache(host + ":" + port);
                MyProject.cache(project).startStatus(true);
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
            MyProject.cache(project).sysMessage("当前服务 IP = " + host + ", 端口号 = " + port);
            MyProject.cache(project).startStatus(true);
            channel.closeFuture().sync();
            return port.get();
        } catch (Throwable e) {
            open = false;
            server = new ServerBootstrap();
            MyProject.cache(project).startStatus(false);
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
//            accpt.shutdownGracefully().sync();
//            message.shutdownGracefully().sync();
            MyProject.cache(project).startStatus(false);
        }
    }

    public void out() {
        try {
            channel.close();
            for (ChannelHandlerContext value : MyProject.cache(project).serverHandler.channles()) {
                value.close();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void shutDown() {
        try {
//            accpt.shutdownGracefully().sync();
//            message.shutdownGracefully().sync();
            accpt.shutdownGracefully();
            message.shutdownGracefully();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        try {
            NettyServer server = new NettyServer("");
            AtomicInteger port = new AtomicInteger(0);
            server.makeServer("192.168.0.158", port, new ServerHandler(""));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

}
