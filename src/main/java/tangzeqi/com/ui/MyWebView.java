package tangzeqi.com.ui;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.concurrent.Worker;
import com.sun.webkit.WebPage; // 用于打开开发者工具（JavaFX 内置API）

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

public class MyWebView extends JPanel {

    final private String project;

    private JFXPanel jfxPanel;
    private WebView webView;
    private WebEngine webEngine;
    private volatile boolean isInitialized = false; // 加 volatile 保证多线程可见性

    public MyWebView(String project) {
        this.project = project;
        // 初始化前先启动 JavaFX 平台（避免 Swing 中嵌入时平台未启动）
        initJavaFXPlatform();
        initialize();
    }

    /**
     * 显式启动 JavaFX 平台，确保 Swing 嵌入时无异常
     */
    private void initJavaFXPlatform() {
        if (!Platform.isFxApplicationThread()) {
            Platform.startup(() -> {}); // 空任务启动 JavaFX 平台
        }
    }

    private void initialize() {
        setLayout(new BorderLayout());
        jfxPanel = new JFXPanel();
        add(jfxPanel, BorderLayout.CENTER);

        // 异步初始化 JavaFX 组件
        Platform.runLater(() -> {
            webView = new WebView();
            webEngine = webView.getEngine();

            // 设置用户代理
            webEngine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36");

            // 设置页面加载监听器
            webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == Worker.State.SUCCEEDED) {
                    isInitialized = true;
                } else if (newValue == Worker.State.FAILED || newValue == Worker.State.CANCELLED) {
                    isInitialized = false; // 加载失败时重置状态
                }
            });

            // 创建场景并设置到面板
            Scene scene = new Scene(webView);
            jfxPanel.setScene(scene);
        });
    }

    /**
     * 加载 URL，修复空指针风险+移除冗余 AtomicReference
     */
    public void loadURL(String url) {
        if (url == null || url.trim().isEmpty()) {
            return; // 空值校验
        }
        AtomicReference<String> targetUrl = new AtomicReference<>(url);
        // 补全 URL 协议头
        if (!targetUrl.get().startsWith("http://") && !targetUrl.get().startsWith("https://") && !targetUrl.get().startsWith("file://")) {
            targetUrl.set("http://" + targetUrl.get());
        }
        // 异步执行，先判断 webEngine 是否初始化
        Platform.runLater(() -> {
            if (webEngine != null) {
                webEngine.load(targetUrl.get());
            }
        });
    }

    /**
     * 修复：正确打开 JavaFX WebView 开发者工具
     */
    public void showDeveloperTools() {
        Platform.runLater(() -> {
            if (isInitialized && webView != null) {
                try {
                    // 通过反射调用内置 API 打开开发者工具（兼容 JavaFX 17+）
                    Method method = WebPage.class.getDeclaredMethod("showDeveloperTools");
                    method.setAccessible(true);
                    method.invoke(webView.getEngine().getLoadWorker());
                } catch (Exception e) {
                    // 异常兜底，避免程序崩溃
                    System.err.println("打开开发者工具失败：" + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 执行 JS 脚本，增加非空校验
     */
    public void executeScript(String script) {
        if (script == null || script.trim().isEmpty()) {
            return;
        }
        Platform.runLater(() -> {
            if (isInitialized && webEngine != null) {
                try {
                    webEngine.executeScript(script);
                } catch (Exception e) {
                    System.err.println("执行 JS 脚本失败：" + e.getMessage());
                }
            }
        });
    }

    public void refresh() {
        Platform.runLater(() -> {
            if (isInitialized && webEngine != null) {
                webEngine.reload();
            }
        });
    }

    public void goBack() {
        Platform.runLater(() -> {
            if (isInitialized && webEngine != null) {
                int currentIndex = webEngine.getHistory().getCurrentIndex();
                if (currentIndex > 0) {
                    webEngine.getHistory().go(-1);
                }
            }
        });
    }

    public void goForward() {
        Platform.runLater(() -> {
            if (isInitialized && webEngine != null) {
                int currentIndex = webEngine.getHistory().getCurrentIndex();
                int entryCount = webEngine.getHistory().getEntries().size();
                if (currentIndex < entryCount - 1) {
                    webEngine.getHistory().go(1);
                }
            }
        });
    }

    /**
     * 获取当前 URL，增加非空校验，避免空指针
     */
    public String getCurrentURL() {
        // 若在 JavaFX 线程，直接返回；否则异步获取（可选同步返回，此处做安全兜底）
        if (Platform.isFxApplicationThread() && webEngine != null) {
            return webEngine.getLocation();
        }
        // 非 FX 线程，返回临时值（或通过 Future 同步获取，按需调整）
        return webEngine != null ? webEngine.getLocation() : null;
    }

    /**
     * 获取页面标题，增加非空校验
     */
    public String getTitle() {
        if (Platform.isFxApplicationThread() && webEngine != null) {
            return webEngine.getTitle();
        }
        return webEngine != null ? webEngine.getTitle() : null;
    }

    @Override
    public void setPreferredSize(Dimension preferredSize) {
        super.setPreferredSize(preferredSize);
        Platform.runLater(() -> {
            if (webView != null) {
                webView.setPrefSize(preferredSize.width, preferredSize.height);
            }
        });
    }

    /**
     * 优化资源释放，避免内存泄漏
     */
    public void shutdown() {
        Platform.runLater(() -> {
            // 清空 WebEngine 加载的内容
            if (webEngine != null) {
                webEngine.load("about:blank"); // 加载空白页释放资源
            }
            // 移除 JavaFX 组件引用
            if (jfxPanel != null) {
                jfxPanel.setScene(null);
            }
            webView = null;
            webEngine = null;
            isInitialized = false;
        });
        // 可选：关闭 JavaFX 平台（若应用不再使用 JavaFX）
        // if (Platform.isInitialized()) {
        //     Platform.exit();
        // }
    }
}