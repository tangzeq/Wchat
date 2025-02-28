package tangzeqi.com.service;

import com.intellij.execution.TaskExecutor;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.VirtualFileImpl;
import com.intellij.psi.PsiManager;
import io.netty.bootstrap.Bootstrap;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ObjectUtils;
import tangzeqi.com.action.ShowChatAction;
import tangzeqi.com.panel.ChatPanel;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatService {
    public static boolean start;
    public static boolean connect;
    public static ChatPanel chat;
    public static String serverIp;
    public static String serverPort;
    public static String userName;
    public static String connectIp;
    public static String connectPort;
    private static volatile NettyServer server = new NettyServer();
    public static volatile ServerHandler serverHandler = new ServerHandler();
    private static volatile NettyCustomer customer = new NettyCustomer();
    public static volatile CustomerHandler customerHandler = new CustomerHandler();
    public static volatile Bootstrap customerBoot = new Bootstrap();
    public static volatile ThreadPoolExecutor executor = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() * 2,// 设置核心线程数
            Runtime.getRuntime().availableProcessors() * 2,// 设置最大线程数
            1000 * 60,// 设置线程活跃时间
            TimeUnit.SECONDS,// 设置线程活跃时间单位
            new ArrayBlockingQueue<>(1000)
    );

    /**
     * 启动本地服务
     */
    public static void start() {
        if (!start) {
            sysMessage("正在启动聊天室");
            executor.execute(() -> {
                try {
                    server.makeServer(serverIp, new AtomicInteger(Integer.parseInt(serverPort)), serverHandler);
                } catch (Throwable throwable) {
                    ChatService.start = true;
                    throwable.printStackTrace();
                    sysMessage("启动失败");
                    startStatus(false);
                }
            });
        } else {
            sysMessage("正在关闭聊天室");
            executor.execute(()->server.out());
        }
    }

    public static void startStatus(Boolean onOff) {
        if(ChatService.start == onOff) return;
        if(onOff) {
            chat.serverStatus(true, "关闭聊天室");
            sysMessage("本地聊天室已启动");
            ChatService.start = true;
        } else {
            chat.serverStatus(true, "启动聊天室");
            sysMessage("本地聊天室已关闭");
            ChatService.start = false;
        }
    }

    /**
     * 项目连接
     */
    public static void connect() {
        if (!connect) {
            sysMessage("正在加入聊天室");
            executor.execute(()-> {
                try {
                    customer.makerCustomer(connectIp,Integer.parseInt(connectPort),customerHandler);
                } catch (Throwable e) {
                    ChatService.connect = true;
                    sysMessage("连接失败");
                    connectStatus(false);
                    e.printStackTrace();
                }
            });
        } else {
            sysMessage("正在退出聊天室");
            executor.execute(()->customer.out());
        }
    }

    public static void connectStatus(Boolean onOff) {
        if(ChatService.connect == onOff) return;
        if(onOff) {
            chat.connectStatus(true, "退出聊天室");
            sysMessage(userName+"已加入聊天室");
            ChatService.connect = true;
        } else {
            chat.connectStatus(true, "进入聊天室");
            sysMessage(userName+"已退出聊天室");
            ChatService.connect = false;
        }
    }

    /**
     * 向用户列表框发送信息
     *
     * @param m 信息
     */
    public static void sysMessage(String m) {
        if (ObjectUtils.isNotEmpty(chat)) {
            chat.addSysMessage(m, "系统");
        }
    }

    /**
     * 向聊天框发送信息
     *
     * @param m    信息
     * @param root 用户
     */
    public static void chatMessage(String m, String root) {
        if (ObjectUtils.isNotEmpty(chat)) {
            chat.addMessage(m, root);
        }
    }

    public static void openFileLine(String file,int line) {
        VirtualFile virtualFile = ShowChatAction.project.getBaseDir().findFileByRelativePath(file);
        FileEditorManager manager = FileEditorManager.getInstance(ShowChatAction.project);
        manager.openFile(virtualFile,true);
        CaretModel caretModel = manager.getSelectedTextEditor().getCaretModel();
        caretModel.moveToLogicalPosition(new LogicalPosition(line,0));
        caretModel.moveCaretRelatively(0,-1,false,true,true);
        caretModel.moveCaretRelatively(1,0,false,true,true);
        caretModel.moveCaretRelatively(-1,0,false,true,true);
    }

    public static void shutDown() {
        server.shutDown();
        customer.shutDown();
    }

    public static void sendChat(String message) {
        chat.inputFieldPost(message);
    }
}
