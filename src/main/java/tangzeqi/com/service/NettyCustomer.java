package tangzeqi.com.service;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.Charset;
import java.util.Collection;

import static tangzeqi.com.service.ChatService.*;

/**
 * 功能描述：客户端
 * 作者：唐泽齐
 */
public class NettyCustomer {
    private NioEventLoopGroup message = new NioEventLoopGroup();
    private boolean open = false;

    public void makerCustomer(String inetHost, int port, CustomerHandler customerHandler) throws Throwable {
        customerHandler.makeonLine();
        if (open) {
            sysMessage("客户端更新连接至 " + inetHost + ":" + port);
            final Collection<ChannelHandlerContext> remotes = customerHandler.remotes();
            customerBoot.connect(inetHost, port);
            for (ChannelHandlerContext remote : remotes) remote.close();
            connectStatus(true);
            return;
        }
        try {
            open = true;
            if (customerBoot == null) customerBoot = new Bootstrap();
            ChannelFuture channelFuture = customerBoot
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
                    .connect(inetHost, port).sync();
            sysMessage("已接入聊天室"+" IP " + inetHost + ", 端口号 " + port);
            connectStatus(true);
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                message.shutdownGracefully().sync();
                connectStatus(false);
            } catch (InterruptedException e) {
                e.printStackTrace();
                connectStatus(false);
            }
        }
    }

     public void shutDown() {
        try {
            message.shutdownGracefully().sync();
            connectStatus(false);
        } catch (InterruptedException e) {
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
