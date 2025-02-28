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

import java.nio.charset.Charset;
import java.util.Collection;

import static tangzeqi.com.service.ChatService.*;

/**
 * 功能描述：客户端
 * 作者：唐泽齐
 */
public class NettyCustomer {
    private volatile NioEventLoopGroup message = new NioEventLoopGroup();
    private volatile Channel channel;
    private volatile boolean open = false;

    public void makerCustomer(String inetHost, int port, CustomerHandler customerHandler) throws Throwable {
        customerHandler.makeonLine();
        try {
            if (open) {
                final Collection<ChannelHandlerContext> remotes = customerHandler.remotes();
                for (ChannelHandlerContext remote : remotes) remote.close();
                sysMessage("客户端更新连接至 " + inetHost + ":" + port);
                connectStatus(true);
                channel = customerBoot.connect(inetHost, port).channel();
                channel.closeFuture().sync();
                return;
            }
            open = true;
            if(ObjectUtils.isEmpty(customerBoot)) customerBoot = new Bootstrap();
            channel = customerBoot
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
            sysMessage("已接入聊天室"+" IP " + inetHost + ", 端口号 " + port);
            connectStatus(true);
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        } finally {
//            message.shutdownGracefully().sync();
            connectStatus(false);
        }
    }

     public void out() {
        final Collection<ChannelHandlerContext> remotes = customerHandler.remotes();
        for (ChannelHandlerContext remote : remotes){
            remote.close();
        }
    }

    public void shutDown() {
        try {
//            message.shutdownGracefully().sync();
            message.shutdownGracefully();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            NettyCustomer customer = new NettyCustomer();
            customer.makerCustomer("localhost", 8888, new CustomerHandler());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }


}
