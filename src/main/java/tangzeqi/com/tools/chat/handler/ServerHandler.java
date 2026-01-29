package tangzeqi.com.tools.chat.handler;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ObjectUtils;
import tangzeqi.com.project.MyProject;
import tangzeqi.com.tools.chat.stroge.BaseMessage;
import tangzeqi.com.tools.chat.stroge.NodeNet;
import tangzeqi.com.tools.chat.stroge.TextMessage;
import tangzeqi.com.utils.ChannelUtils;
import tangzeqi.com.utils.MessageUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static tangzeqi.com.utils.ChannelUtils.remoteHost;
import static tangzeqi.com.utils.ChannelUtils.remotePort;

/**
 * 功能描述：服务端信息处理
 * 作者：唐泽齐
 */
@ChannelHandler.Sharable
public class ServerHandler extends ChannelInboundHandlerAdapter {
    private final String project;
    volatile public Cache<Long, Object> messageCache = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofSeconds(60)).build();
    volatile ConcurrentLinkedQueue<BaseMessage> message = new ConcurrentLinkedQueue<>();
    volatile Map<String, ChannelHandlerContext> customerCache = new ConcurrentHashMap<>();
    volatile Map<String, String> serverCache = new ConcurrentHashMap<String, String>();
    volatile Map<String, String> customerHost = new ConcurrentHashMap<String, String>();
    volatile int online = 0;
    volatile public String host = "";
    volatile public Integer port = -1;
    volatile public boolean active = true;

    public ServerHandler(String project) {
        this.project = project;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        customerCache.put(ctx.channel().id().toString(), ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        customerCache.remove(ctx.channel().id().toString(), ctx);
        String server = customerHost.remove(ctx.channel().id().toString());
        BaseMessage bm = BaseMessage.builder().id(ChannelUtils.makeId(ctx)).type(2).message(TextMessage.builder().message(server).build()).build();
        if (!customerHost.containsValue(server)) MyProject.cache(project).customerHandler.getQueueQueue().add(bm);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!ObjectUtils.isEmpty(msg) && !"\r\n".equals(msg)) {
            BaseMessage bm = MessageUtils.resolve(msg);
            if ("0".equals(bm.getType().toString())) {
                customerHost.put(ctx.channel().id().toString(), ((TextMessage) bm.getMessage()).getMessage());
            } else if (ObjectUtils.isEmpty(messageCache.getIfPresent(bm.getId()))) {
                bm.setChanleId(ctx.channel().id().toString());
                send(bm);
            }
        }
    }

    @SneakyThrows
    public void makeonLine() {
        if (online == 0) {
            MyProject.cache(project).sysMessage("服务端 激活");
            online++;
            MyProject.cache(project).executor.execute(() -> {
                while (true) {
                    try {
                        Thread.sleep(50);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                    for (ChannelHandlerContext context : customerCache.values()) {
                        try {
                            context.writeAndFlush(Unpooled.copiedBuffer((System.getProperty("line.separator")).getBytes("UTF-8")));
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
            MyProject.cache(project).executor.execute(() -> {
                while (true) {
                    try {
                        Thread.sleep(0);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                    //信息
                    while (message.size() > 0) {
                        BaseMessage s = message.remove();
                        if (!ObjectUtils.isEmpty(s)) {
                            if (ObjectUtils.isEmpty(messageCache.getIfPresent(s.getId()))) {
                                messageCache.put(s.getId(), 1);
                                MyProject.cache(project).customerHandler.getQueueQueue().add(s);
                                for (ChannelHandlerContext context : customerCache.values()) {
                                    while (!active) {
                                        try {
                                            Thread.sleep(50);
                                        } catch (Throwable e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
//                                        sysMessage(ChannelUtils.localHost(context) + ":" + ChannelUtils.localPort(context) + "服务端 向" + ChannelUtils.remoteHost(context) + ":" + ChannelUtils.remotePort(context) + "发送信息, msg = " + s);
                                    MyProject.cache(project).executor.execute(() -> {
                                        try {
                                            context.writeAndFlush(Unpooled.copiedBuffer((JSON.toJSONString(s) + "" + System.getProperty("line.separator")).getBytes("UTF-8")));
                                        } catch (Throwable e) {
                                            throw new RuntimeException(e);
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    public Integer getOnline() {
        return online - 1;
    }

    public Map<String, String> getServerCache() {
        return serverCache;
    }

    public Map<String, String> setServerCache(String s) {
        serverCache.put(s.hashCode() + "", s);
        return serverCache;
    }

    public Map<String, String> delServerCache(String s) {
        try {
            Integer.valueOf(s);
            serverCache.remove(s);
        } catch (Throwable e) {
            serverCache.remove(s.hashCode() + "", s);
        }
        return serverCache;
    }

    public Map<String, String> getCustomerHost() {
        return customerHost;
    }
//
//    public Map<String, String> delCustomerHost(String s) {
//        try {
//            Integer.valueOf(s);
//            customerHost.remove(s);
//        } catch (Throwable e) {
//            customerHost.remove(s.hashCode()+"");
//    throw new RuntimeException(e);
//        }
//        return customerHost;
//    }

    public ConcurrentLinkedQueue<BaseMessage> getMessage() {
        return message;
    }

    public List<NodeNet> getChannles() {
        List<NodeNet> channles = new ArrayList<>();
        for (Map.Entry<String, ChannelHandlerContext> entry : customerCache.entrySet()) {
            channles.add(NodeNet.builder().type(entry.getKey()).host(remoteHost(entry.getValue())).port(remotePort(entry.getValue())).build());
        }
        return channles;
    }

    public Collection<ChannelHandlerContext> channles() {
        return customerCache.values();
    }

    public List<NodeNet> getNodes() {
        List<NodeNet> nodes = new ArrayList<>();
        for (Map.Entry<String, String> entry : serverCache.entrySet()) {
            nodes.add(NodeNet.builder().type(entry.getKey()).host((entry.getValue().split(":"))[0]).port(Integer.valueOf((entry.getValue().split(":"))[1])).build());
        }
        return nodes;
    }

    public void clear() {
        customerCache.values().forEach(c -> c.close());
        serverCache.clear();
        customerHost.clear();
    }

    synchronized public void send(BaseMessage message) {
        for (ChannelHandlerContext context : customerCache.values()) {
            while (!active) {
                try {
                    Thread.sleep(0);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                context.writeAndFlush(Unpooled.copiedBuffer((JSON.toJSONString(message) + System.getProperty("line.separator")).getBytes(StandardCharsets.UTF_8)));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }


}
