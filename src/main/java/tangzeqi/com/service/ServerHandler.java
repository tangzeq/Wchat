package tangzeqi.com.service;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ObjectUtils;
import tangzeqi.com.stroge.BaseMessage;
import tangzeqi.com.stroge.NodeNet;
import tangzeqi.com.stroge.TextMessage;
import tangzeqi.com.utils.ChannelUtils;
import tangzeqi.com.utils.MessageUtils;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static tangzeqi.com.service.ChatService.*;
import static tangzeqi.com.utils.ChannelUtils.remoteHost;
import static tangzeqi.com.utils.ChannelUtils.remotePort;

/**
 * 功能描述：服务端信息处理
 * 作者：唐泽齐
 */
@ChannelHandler.Sharable
public class ServerHandler extends ChannelInboundHandlerAdapter {
    static volatile public Cache<Long, Object> messageCache = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofSeconds(60)).build();
    static volatile ConcurrentLinkedQueue<BaseMessage> message = new ConcurrentLinkedQueue<>();
    static volatile Map<String, ChannelHandlerContext> customerCache = new ConcurrentHashMap<>();
    static volatile Map<String, String> serverCache = new ConcurrentHashMap<String, String>();
    static volatile Map<String, String> customerHost = new ConcurrentHashMap<String, String>();
    static volatile int online = 0;
    static volatile public String host = "";
    static volatile public Integer port = -1;
    static volatile public boolean active = true;

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        customerCache.put(ctx.channel().id().toString(), ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        customerCache.remove(ctx.channel().id().toString(), ctx);
        String server = customerHost.remove(ctx.channel().id().toString());
        BaseMessage bm = BaseMessage.builder().id(ChannelUtils.makeId(ctx)).type(2).message(TextMessage.builder().message(server).build()).build();
        if (!customerHost.containsValue(server)) customerHandler.getQueueQueue().add(bm);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!ObjectUtils.isEmpty(msg) && !"\r\n".equals(msg)) {
            BaseMessage bm = MessageUtils.resolve(msg);
            if ("0".equals(bm.getType().toString())) {
                customerHost.put(ctx.channel().id().toString(), ((TextMessage) bm.getMessage()).getMessage());
            } else if (ObjectUtils.isEmpty(messageCache.getIfPresent(bm.getId()))) {
                message.add(bm);
            }
        }
    }
    @SneakyThrows
    public void makeonLine() {
        if (online == 0) {
            sysMessage("服务端 激活");
            online++;
            executor.execute(()-> {
                while (true) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    for (ChannelHandlerContext context : customerCache.values()) {
                        try {
                            context.writeAndFlush(Unpooled.copiedBuffer((System.getProperty("line.separator")).getBytes("UTF-8")));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            executor.execute(()-> {
                    while (true) {
                        try {
                            Thread.sleep(0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //信息
                        while (message.size() > 0) {
                            BaseMessage s = message.poll();
                            if (!ObjectUtils.isEmpty(s)) {
                                if (ObjectUtils.isEmpty(messageCache.getIfPresent(s.getId()))) {
                                    messageCache.put(s.getId(), 1);
                                    customerHandler.getQueueQueue().add(s);
                                    for (ChannelHandlerContext context : customerCache.values()) {
                                        while (!active) {
                                            try {
                                                Thread.sleep(50);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
//                                        sysMessage(ChannelUtils.localHost(context) + ":" + ChannelUtils.localPort(context) + "服务端 向" + ChannelUtils.remoteHost(context) + ":" + ChannelUtils.remotePort(context) + "发送信息, msg = " + s);
                                        executor.execute(()-> {
                                            try {
                                                context.writeAndFlush(Unpooled.copiedBuffer((JSON.toJSONString(s) + "" + System.getProperty("line.separator")).getBytes("UTF-8")));
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
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
}
