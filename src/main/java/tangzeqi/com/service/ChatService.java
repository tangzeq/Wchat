package tangzeqi.com.service;

import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import io.netty.bootstrap.Bootstrap;
import org.apache.commons.lang3.ObjectUtils;
import tangzeqi.com.listener.MyDocumentListener;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static tangzeqi.com.ui.UiConstantEnum.SYS;

public class ChatService {
    public final Project project;
    public boolean start;
    public boolean connect;
    public Chat chat;
    public Config config;
    public String serverIp;
    public String serverPort;
    public String userName;
    public String connectIp;
    public String connectPort;

    public volatile boolean mqtt;
    public volatile String mqttroom;
    public volatile ToolWindow toolWindow;

    private volatile NettyServer server;
    public volatile ServerHandler serverHandler;
    private volatile NettyCustomer customer;
    public volatile CustomerHandler customerHandler;
    public volatile MqttService mqttService;
    public volatile Bootstrap customerBoot = new Bootstrap();

    public volatile boolean upd = false;
    public volatile UPDService updService;
    public volatile MyDocumentListener synListener;


    public volatile ThreadPoolExecutor executor = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() * 2,// 设置核心线程数
            Runtime.getRuntime().availableProcessors() * 2,// 设置最大线程数
            1000 * 60,// 设置线程活跃时间
            TimeUnit.SECONDS,// 设置线程活跃时间单位
            new ArrayBlockingQueue<>(1000)
    );

    public ChatService(Project project) {
        this.project = project;
        server = new NettyServer(project.getName());
        serverHandler = new ServerHandler(project.getName());
        customer = new NettyCustomer(project.getName());
        customerHandler = new CustomerHandler(project.getName());
        mqttService = new MqttService(project.getName());
        try {
            updService = new UPDService(project.getName());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 启动本地服务
     */
    public void start() {
        if (!start) {
            sysMessage("正在启动聊天室");
            executor.execute(() -> {
                try {
                    server.makeServer(serverIp, new AtomicInteger(Integer.parseInt(serverPort)), serverHandler);
                } catch (Throwable throwable) {
                    start = true;
                    throwable.printStackTrace();
                    sysMessage("启动失败");
                    startStatus(false);
                }
            });
        } else {
            sysMessage("正在关闭聊天室");
            executor.execute(() -> server.out());
        }
    }

    public void startStatus(Boolean onOff) {
        if (start == onOff) return;
        if (onOff) {
            config.serverStatus(true, "关闭聊天室");
            sysMessage("本地聊天室已启动");
            start = true;
        } else {
            config.serverStatus(true, "启动聊天室");
            sysMessage("本地聊天室已关闭");
            start = false;
        }
    }

    /**
     * 项目连接
     */
    public void connect() {
        if (!connect) {
            sysMessage("正在加入聊天室");
            executor.execute(() -> {
                try {
                    customer.makerCustomer(connectIp, Integer.parseInt(connectPort), customerHandler);
                } catch (Throwable e) {
                    connect = true;
                    sysMessage("连接失败");
                    connectStatus(false);
                    throw new RuntimeException(e);
                }
            });
        } else {
            sysMessage("正在退出聊天室");
            executor.execute(() -> customer.out());
        }
    }

    public void connectStatus(Boolean onOff) {
        if (connect == onOff) return;
        if (onOff) {
            config.connectStatus(true, "退出聊天室");
            sysMessage(userName + "已加入聊天室");
            connect = true;
        } else {
            config.connectStatus(true, "进入聊天室");
            sysMessage(userName + "已退出聊天室");
            connect = false;
        }
    }

    /**
     * 向用户列表框发送信息
     *
     * @param m 信息
     */
    public void sysMessage(String m) {
        if (ObjectUtils.isNotEmpty(config)) {
            config.addSysMessage(m, SYS.getValue());
        }
    }

    /**
     * 向聊天框发送信息
     *
     * @param m    信息
     * @param root 用户
     */
    public void chatMessage(String m, String root) {
        if (ObjectUtils.isNotEmpty(chat)) {
            chat.addMessage(m, root);
        }
    }

    public void openFileLine(String file, int line) {
        VirtualFile virtualFile = project.getBaseDir().findFileByRelativePath(file);
        FileEditorManager manager = FileEditorManager.getInstance(project);
        manager.openFile(virtualFile, true);
        CaretModel caretModel = manager.getSelectedTextEditor().getCaretModel();
        caretModel.moveToLogicalPosition(new LogicalPosition(line, 0));
        caretModel.moveCaretRelatively(0, -1, false, true, true);
        caretModel.moveCaretRelatively(1, 0, false, true, true);
        caretModel.moveCaretRelatively(-1, 0, false, true, true);
    }

    public void shutDown() {
        System.out.println("Wchat is shutDowning...");
        executor.execute(() -> server.shutDown());
        executor.execute(() -> customer.shutDown());
        executor.execute(() -> mqttService.shutDowm());
        executor.execute(() -> updService.shutDowm());
        System.out.println("Wchat is shutDowned");
    }

    public void sendChat(String message) {
        chat.send(message);
    }

    public void mqttconnect() {
        if (!mqtt) {
            sysMessage("正在启用公网频道");
            executor.execute(() -> {
                try {
                    mqttService.start(mqttroom, userName);
                } catch (Throwable e) {
                    mqtt = true;
                    sysMessage("启用公网频道失败");
                    mqttStatus(false);
                    throw new RuntimeException(e);
                }
            });
        } else {
            sysMessage("正在关闭公网频道");
            executor.execute(() -> mqttService.out());
        }
    }

    public void mqttStatus(Boolean onOff) {
        if (mqtt == onOff) return;
        if (onOff) {
            sysMessage("关闭聊天室");
            sysMessage("启用公网频道");
            //关闭 聊天室
            server.out();
            customer.out();
            //禁用 启动聊天室 进入聊天室
            connect = false;
            config.connectStatus(false, "进入聊天室");
            start = false;
            config.serverStatus(false, "启动聊天室");
            config.mqttStatus(true, "关闭公网聊天");
        } else {
            //启用 启动聊天室 进入聊天室
            sysMessage("退出公网频道");
            sysMessage("开启聊天室");
            config.connectStatus(true, "进入聊天室");
            config.serverStatus(true, "启动聊天室");
            config.mqttStatus(true, "开启公网聊天");
        }
        mqtt = onOff;
    }

    public void updconnect() {
        if (!upd) {
            sysMessage("正在启用局域网广播模式");
            executor.execute(() -> {
                try {
                    updService.start();
                    upd = true;
                    config.updconnectStatus(true, "关闭局域网广播模式");
                    sysMessage("开始感知");
                } catch (Throwable e) {
                    upd = false;
                    sysMessage("启用公网局域网广播模式失败");
                    config.updconnectStatus(true, "启用局域网广播模式");
                    throw new RuntimeException(e);
                }
            });
        } else {
            sysMessage("正在关闭局域网广播模式");
            upd = false;
            config.updconnectStatus(true, "启用局域网广播模式");
            executor.execute(() -> updService.shutDowm());
        }
    }

    public void showContent(String name) {
        toolWindow.getContentManager().setSelectedContent(toolWindow.getContentManager().findContent(name));
    }
}
