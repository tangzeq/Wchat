package tangzeqi.com.service;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ObjectUtils;
import tangzeqi.com.project.MyProject;

import java.nio.charset.Charset;
import java.util.Collection;

import static tangzeqi.com.service.ChatService.*;

/**
 * 功能描述：客户端
 * 作者：唐泽齐
 */
public class NettyCustomer {
    private final String project;

    private volatile NioEventLoopGroup message = new NioEventLoopGroup();
    private volatile Channel channel;
    private volatile boolean open = false;

    public NettyCustomer(String project) {
        this.project = project;
    }

    public void makerCustomer(String inetHost, int port, CustomerHandler customerHandler) throws Throwable {
        customerHandler.makeonLine();
        try {
            if (open) {
                final Collection<ChannelHandlerContext> remotes = customerHandler.remotes();
                for (ChannelHandlerContext remote : remotes) remote.close();
                MyProject.cache(project).sysMessage("客户端更新连接至 " + inetHost + ":" + port);
                MyProject.cache(project).connectStatus(true);
                channel = MyProject.cache(project).customerBoot.connect(inetHost, port).channel();
                channel.closeFuture().sync();
                return;
            }
            open = true;
            if (ObjectUtils.isEmpty(MyProject.cache(project).customerBoot)) MyProject.cache(project).customerBoot = new Bootstrap();
            channel = MyProject.cache(project).customerBoot
                    .group(message)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new LineBasedFrameDecoder(Integer.MAX_VALUE));
                            ch.pipeline().addLast(new StringDecoder(Charset.forName("UTF-8")));
                            ch.pipeline().addLast(new StringEncoder(Charset.forName("UTF-8")));
                            ch.pipeline().addLast(customerHandler);
                        }
                    })
                    .connect(inetHost, port).sync().channel();
            MyProject.cache(project).sysMessage("已接入聊天室" + " IP " + inetHost + ", 端口号 " + port);
            MyProject.cache(project).connectStatus(true);
            channel.closeFuture().sync();
        } catch (Throwable e) {
            open = false;
            MyProject.cache(project).customerBoot = new Bootstrap();
            MyProject.cache(project).connectStatus(false);
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
//            message.shutdownGracefully().sync();
            MyProject.cache(project).connectStatus(false);
        }
    }

    public void out() {
        try {
            final Collection<ChannelHandlerContext> remotes = MyProject.cache(project).customerHandler.remotes();
            for (ChannelHandlerContext remote : remotes) {
                remote.close();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void shutDown() {
        try {
//            message.shutdownGracefully().sync();
            message.shutdownGracefully();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        try {
            NettyCustomer customer = new NettyCustomer("123");
            customer.makerCustomer("localhost", 8888, new CustomerHandler("123"));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }


}
