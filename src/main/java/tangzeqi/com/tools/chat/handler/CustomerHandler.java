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
import tangzeqi.com.tools.chat.server.ChatService;
import tangzeqi.com.listener.MyDocumentListener;
import tangzeqi.com.project.MyProject;
import tangzeqi.com.tools.chat.stroge.*;
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
public class CustomerHandler extends ChannelInboundHandlerAdapter {
    private final String project;
    private volatile Cache<Long, Object> messageCache = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofSeconds(60)).build();
    private volatile ConcurrentLinkedQueue<BaseMessage> queue = new ConcurrentLinkedQueue<>();
    private volatile Map<String, ChannelHandlerContext> remoteCache = new ConcurrentHashMap<>();
    private volatile int online = 0;

    public CustomerHandler(String project) {
        this.project = project;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String hostString = remoteHost(ctx);
        int portString = remotePort(ctx);
        remoteCache.put(ctx.channel().id().toString(), ctx);
        MyProject.cache(project).serverHandler.setServerCache(hostString + ":" + portString);
        BaseMessage bm = BaseMessage.builder().id(ChannelUtils.makeId(ctx)).type(1).message(MapMessage.builder().message(MyProject.cache(project).serverHandler.getServerCache()).build()).build();
        queue.add(bm);
        BaseMessage cbm = BaseMessage.builder().id(ChannelUtils.makeId(ctx)).type(0).message(TextMessage.builder().message(MyProject.cache(project).serverHandler.host + ":" + MyProject.cache(project).serverHandler.port).build()).build();
        ctx.writeAndFlush(Unpooled.copiedBuffer((JSON.toJSONString(cbm) + "" + System.getProperty("line.separator")).getBytes("UTF-8")));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (!ctx.channel().isActive()) {
            rest(ctx);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        while (queue.size() > 0) {
            BaseMessage s = queue.remove();
            if (!ObjectUtils.isEmpty(s) && remoteCache.size() > 0) {
                if (ObjectUtils.isEmpty(s.getId())) {
                    s.setId(ChannelUtils.makeId(remoteCache.values().stream().findFirst().get()));
                }
                if (digestion(s)) continue;
                //推送信息到本地服务
                if (ObjectUtils.isEmpty(MyProject.cache(project).serverHandler.messageCache.getIfPresent(s.getId())))
                    MyProject.cache(project).serverHandler.getMessage().add(s);
                //推送信息到外部服务
                if (!ObjectUtils.isEmpty(messageCache.getIfPresent(s.getId()))) continue;
                for (ChannelHandlerContext context : remoteCache.values()) {
                    while (!MyProject.cache(project).serverHandler.active) {
                        Thread.sleep(50);
                    }
//                    sysMessage(ChannelUtils.localHost(context) + ":" + ChannelUtils.localPort(context) + "客户端 向" + ChannelUtils.remoteHost(context) + ":" + ChannelUtils.remotePort(context) + "发送信息, msg = " + s);
                    MyProject.cache(project).executor.execute(new Runnable() {
                        @SneakyThrows
                        @Override
                        public void run() {
                            context.writeAndFlush(Unpooled.copiedBuffer((JSON.toJSONString(s) + "" + System.getProperty("line.separator")).getBytes("UTF-8")));
                        }
                    });
                }
            }
        }
        if (!ObjectUtils.isEmpty(msg) && !"\r\n".equals(msg)) {
            BaseMessage bm = MessageUtils.resolve(msg);
            messageCache.put(bm.getId(), 1);

//            MessageStorage.add(bm);
            if (!digestion(bm)) queue.add(bm);
        }

    }

    private boolean digestion(BaseMessage bm) {
        boolean rest = false;
        switch (bm.getType().intValue()) {
            case 1: {
                final Integer old = MyProject.cache(project).serverHandler.getServerCache().size();
                MapMessage mapMessage = (MapMessage) bm.getMessage();
                MyProject.cache(project).serverHandler.getServerCache().putAll(mapMessage.getMessage());
                final Integer now = MyProject.cache(project).serverHandler.getServerCache().size();
//                System.out.println("ServerCache = " + serverHandler.getServerCache().values());
                if (old.compareTo(now) != 0) {
                    BaseMessage nbm = BaseMessage.builder().message(MapMessage.builder().message(MyProject.cache(project).serverHandler.getServerCache()).build()).type(1).build();
                    queue.add(nbm);
                    rest = true;
                }
                break;
            }
            case 2: {
                final Integer old = MyProject.cache(project).serverHandler.getServerCache().size();
                TextMessage textMessage = (TextMessage) bm.getMessage();
                if (MyProject.cache(project).serverHandler.getCustomerHost().containsValue(textMessage.getMessage())) {
                    BaseMessage nbm = BaseMessage.builder().message(textMessage).type(1).build();
                    queue.add(nbm);
                    rest = true;
                    break;
                }
                MyProject.cache(project).serverHandler.delServerCache(textMessage.getMessage());
                final Integer now = MyProject.cache(project).serverHandler.getServerCache().size();
//                System.out.println("ServerCache = " + serverHandler.getServerCache().values());
                if (old.compareTo(now) != 0) {
                    BaseMessage nbm = BaseMessage.builder().message(textMessage).type(2).build();
                    queue.add(nbm);
                    rest = true;
                }
                break;
            }
            case 5:
                shunt(bm.getMessage());
                rest = true;
                break;
        }
        return rest;
    }

    private void shunt(BaseUser mes) {
        //聊天信息
        if (mes instanceof TextMessage) {
            MyProject.cache(project).chatMessage(((TextMessage) mes).getMessage(), mes.getName());
        }
        //协同编辑
        if (mes instanceof SynergyMessage) {
            ChatService cache = MyProject.cache(((SynergyMessage) mes).getProject());
            if (cache != null
                    && project.equalsIgnoreCase(((SynergyMessage) mes).getProject())
            ) {
                MyDocumentListener.syne(project, ((SynergyMessage) mes));
            }
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        if (!ctx.channel().isActive()) {
            rest(ctx);
        }
    }

    @SneakyThrows
    private void rest(ChannelHandlerContext ctx) {
        MyProject.cache(project).serverHandler.active = false;
        Thread.sleep(1000);
        String hostString = remoteHost(ctx);
        int portString = remotePort(ctx);
//        sysMessage(ctx.channel().id() + "从" + hostString + ":" + portString + "断开");
//        sysMessage(  "从" + hostString + ":" + portString + "断开");
        remoteCache.remove(ctx.channel().id().toString(), ctx);
        boolean single = true;
        for (ChannelHandlerContext context : remoteCache.values()) {
            if (remoteHost(context).equals(hostString) && remotePort(context).compareTo(Integer.valueOf(portString)) == 0)
                single = false;
        }
        if (single) MyProject.cache(project).serverHandler.delServerCache(hostString + ":" + portString);
        restremote();
        BaseMessage bm = BaseMessage.builder().type(2).message(TextMessage.builder().message(hostString + ":" + portString).build()).build();
        if (single) queue.add(bm);
        MyProject.cache(project).serverHandler.active = true;
    }

    synchronized public void send(BaseUser b) {
        BaseMessage message = BaseMessage.builder().id(System.nanoTime()).type(5).message(b).build();
        for (ChannelHandlerContext context : remoteCache.values()) {
            while (!MyProject.cache(project).serverHandler.active) {
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


    public ConcurrentLinkedQueue<BaseMessage> getQueueQueue() {
        return queue;
    }

    /**
     * 外部重连
     */
    public void restremote() {
        if (remoteCache.size() <= 0) {
            boolean connect = false;
            for (Object context : MyProject.cache(project).serverHandler.getServerCache().values()) {
                String[] split = context.toString().split(":");
                if (split[0].equals(MyProject.cache(project).serverHandler.host) && Integer.valueOf(split[1]).compareTo(Integer.valueOf(MyProject.cache(project).serverHandler.port)) == 0)
                    continue;
                MyProject.cache(project).customerBoot.connect(split[0], Integer.parseInt(split[1]));
                connect = true;
                MyProject.cache(project).sysMessage("远程" + split[0] + ":" + Integer.parseInt(split[1]) + "连接");
                break;
            }
            //自连接
            if (!connect) {
                MyProject.cache(project).customerBoot.connect(MyProject.cache(project).serverHandler.host, MyProject.cache(project).serverHandler.port);
            }
        }
    }

    public void makeonLine() {
        if (online == 0) {
            MyProject.cache(project).sysMessage("客户端 激活");
            online++;
            MyProject.cache(project).executor.execute(new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(50);
                            for (ChannelHandlerContext context : remoteCache.values()) {
                                context.writeAndFlush(Unpooled.copiedBuffer((System.getProperty("line.separator")).getBytes("UTF-8")));
                            }
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        }
    }

    public List<NodeNet> getRemotes() {
        List<NodeNet> remotes = new ArrayList<>();
        for (Map.Entry<String, ChannelHandlerContext> entry : remoteCache.entrySet()) {
            remotes.add(NodeNet.builder().type(entry.getKey()).host(remoteHost(entry.getValue())).port(remotePort(entry.getValue())).build());
        }
        return remotes;
    }

    public Collection<ChannelHandlerContext> remotes() {
        Collection<ChannelHandlerContext> set = remoteCache.values();
        return set;
    }

}
