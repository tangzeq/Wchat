package tangzeqi.com.ui;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.concurrent.Worker;
import com.sun.webkit.WebPage;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Swing 嵌入 JavaFX WebView 的封装类
 * 包含URL加载、JS执行、开发者工具、历史记录切换等核心功能
 * 增加了视频播放支持
 */
public class MyWebView extends JPanel {

    final private String project;

    private JFXPanel jfxPanel;
    private WebView webView;
    private WebEngine webEngine;

    private JSplitPane splitPane;  //用于分割主视图和开发者工具
    private JFXPanel devToolsPanel;  //开发者工具面板
    private WebView devToolsView;  //开发者工具WebView
    private WebEngine devToolsEngine;  //开发者工具WebEngine
    private volatile boolean isInitialized = false;

    /**
     * 构造方法
     * @param project 项目标识（自定义，可用于业务区分）
     */
    public MyWebView(String project) {
        this.project = project;
        initJavaFXPlatform();
        initialize();
    }

    /**
     * 显式启动 JavaFX 平台
     */
    private void initJavaFXPlatform() {
        if (!Platform.isFxApplicationThread()) {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(() -> {
                latch.countDown();
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("JavaFX平台初始化被中断：" + e.getMessage());
            }
        }
    }

    /**
     * 初始化 Swing 和 JavaFX 组件
     */
    private void initialize() {
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.7);  // 设置主视图占70%宽度
        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);

        jfxPanel = new JFXPanel();
        splitPane.setLeftComponent(jfxPanel);

        devToolsPanel = new JFXPanel();
        splitPane.setRightComponent(devToolsPanel);
        splitPane.setDividerLocation(0.7);  // 初始分割位置
        Platform.runLater(() -> {
            try {
                webView = new WebView();
                webEngine = webView.getEngine();

                // 设置浏览器用户代理
                webEngine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36");

                // 添加媒体播放错误处理
                webEngine.onErrorProperty().addListener((observable, oldValue, newValue) -> {
                    System.err.println("页面加载错误: " + newValue);
                });

                // 启用媒体播放支持
                webView.getEngine().setJavaScriptEnabled(true);
                webView.setContextMenuEnabled(true);

                // 页面加载状态监听器
                webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == Worker.State.SUCCEEDED) {
                        showDeveloperTools();
                        isInitialized = true;
                        // 直接执行脚本，避免嵌套Platform.runLater
                        String script =
                                "window.addEventListener('error', function(e) {" +
                                        "    if (e.target.tagName === 'VIDEO') {" +
                                        "        console.error('视频加载错误:', e.target.src);" +
                                        "    }" +
                                        "}, true);" +
                                        "document.addEventListener('DOMContentLoaded', function() {" +
                                        "    var videos = document.getElementsByTagName('video');" +
                                        "    for (var i = 0; i < videos.length; i++) {" +
                                        "        videos[i].crossOrigin = 'anonymous';" +
                                        "        videos[i].preload = 'auto'; // 新增：设置视频预加载，提升播放启动速度" +
                                        "        // 可选：自动播放（需浏览器支持，部分浏览器禁用自动播放）" +
                                        "        if (videos[i].hasAttribute('autoplay')) {" +
                                        "            videos[i].play().catch(function(e) {" +
                                        "                console.warn('视频自动播放失败:', e.message);" +
                                        "            });" +
                                        "        }" +
                                        "    }" +
                                        "});";
                        webEngine.executeScript(script);
                    } else if (newValue == Worker.State.FAILED || newValue == Worker.State.CANCELLED) {
                        isInitialized = false;
                    }
                });

                Scene scene = new Scene(webView);
                jfxPanel.setScene(scene);
            } catch (Exception e) {
                System.err.println("初始化WebView失败：" + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * 处理视频播放错误
     * @param videoUrl 视频URL
     */
    private void handleVideoError(String videoUrl) {
        System.err.println("视频播放失败: " + videoUrl);
    }

    /**
     * 重试加载视频
     * @param videoUrl 视频URL
     */
    public void retryLoadVideo(String videoUrl) {
        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            return;
        }

        Platform.runLater(() -> {
            if (webEngine != null && isInitialized) {
                try {
                    String script = String.format(
                            "var videos = document.getElementsByTagName('video');" +
                                    "for (var i = 0; i < videos.length; i++) {" +
                                    "    if (videos[i].src.includes('%s')) {" +
                                    "        videos[i].load();" +
                                    "        break;" +
                                    "    }" +
                                    "}", videoUrl
                    );
                    webEngine.executeScript(script);
                } catch (Exception e) {
                    System.err.println("重试加载视频失败：" + e.getMessage());
                }
            }
        });
    }

    /**
     * 处理视频URL，确保使用正确的协议
     * @param videoUrl 原始视频URL
     * @return 处理后的URL
     */
    private String processVideoUrl(String videoUrl) {
        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            return videoUrl;
        }

        String processedUrl = videoUrl.trim();

        // 如果是blob URL，尝试转换为普通URL
        if (processedUrl.startsWith("blob:")) {
            System.err.println("检测到blob URL，可能无法正常播放: " + processedUrl);
        }

        return processedUrl;
    }

    /**
     * 加载指定URL
     * @param url 要加载的地址
     */
    public void loadURL(String url) {
        if (url == null || url.trim().isEmpty()) {
            return;
        }

        Platform.runLater(() -> {
            if (webEngine != null) {
                try {
                    String targetUrl = url.trim();
                    if (!targetUrl.startsWith("http://") &&
                            !targetUrl.startsWith("https://") &&
                            !targetUrl.startsWith("file://")) {
                        targetUrl = "https://" + targetUrl;
                    }
                    webEngine.load(targetUrl);
                } catch (Exception e) {
                    System.err.println("加载URL失败: " + e.getMessage());
                    handleVideoError(url);
                }
            }
        });
    }

    /**
     * 打开JavaFX WebView开发者工具
     */
    public void showDeveloperTools() {
        Platform.runLater(() -> {
            if (!isInitialized || webView == null || webEngine == null) {
                System.err.println("WebView未初始化");
                return;
            }

            try {
                if (devToolsView == null) {
                    devToolsView = new WebView();
                    devToolsEngine = devToolsView.getEngine();
                    Scene devToolsScene = new Scene(devToolsView);
                    devToolsPanel.setScene(devToolsScene);
                }

                // 获取WebPage实例并连接开发者工具
                Method getWebPageMethod = WebEngine.class.getDeclaredMethod("getWebPage");
                getWebPageMethod.setAccessible(true);
                WebPage webPage = (WebPage) getWebPageMethod.invoke(webEngine);

                // 获取调试器实例
                Method getDebuggerMethod = WebEngine.class.getDeclaredMethod("getDebugger");
                getDebuggerMethod.setAccessible(true);
                Object debugger = getDebuggerMethod.invoke(webEngine);

                if (debugger != null) {
                    // 启用调试器
                    Method setEnabledMethod = debugger.getClass().getMethod("setEnabled", boolean.class);
                    setEnabledMethod.setAccessible(true);
                    setEnabledMethod.invoke(debugger, true);

                    // 设置消息回调
                    Method setMessageCallbackMethod = debugger.getClass().getMethod("setMessageCallback", Callback.class);
                    setMessageCallbackMethod.setAccessible(true);
                    setMessageCallbackMethod.invoke(debugger, (Callback<String, Void>) message -> {
                        Platform.runLater(() -> {
                            try {
                                devToolsEngine.executeScript(
                                        "if (window.WebInspector) {" +
                                                "    WebInspector.dispatchMessageFromBackend(" +
                                                "        JSON.parse('" + message.replace("'", "\\'") + "')" +
                                                "    );" +
                                                "}"
                                );
                            } catch (Exception e) {
                                System.err.println("执行调试脚本失败: " + e.getMessage());
                            }
                        });
                        return null;
                    });

                    // 加载开发者工具界面
                    devToolsEngine.loadContent(
                            "<!DOCTYPE html>" +
                                    "<html>" +
                                    "<head>" +
                                    "    <meta charset=\"UTF-8\">" +
                                    "    <script src=\"https://cdnjs.cloudflare.com/ajax/libs/devtools/1.0.0/devtools.js\"></script>" +
                                    "    <style>" +
                                    "        body { margin: 0; padding: 0; }" +
                                    "        #container { width: 100%; height: 100vh; }" +
                                    "    </style>" +
                                    "</head>" +
                                    "<body>" +
                                    "    <div id=\"container\"></div>" +
                                    "    <script>" +
                                    "        var WebInspector = {" +
                                    "            dispatchMessageFromBackend: function(message) {" +
                                    "                console.log('Received message:', message);" +
                                    "                if (window.DevToolsAPI) {" +
                                    "                    DevToolsAPI.dispatchMessageFromBackend(message);" +
                                    "                }" +
                                    "            }" +
                                    "        };" +
                                    "        if (window.DevToolsAPI) {" +
                                    "            DevToolsAPI.showPanel('elements');" +
                                    "        }" +
                                    "    </script>" +
                                    "</body>" +
                                    "</html>"
                    );

                    // 发送初始化消息
                    Method sendMessageMethod = debugger.getClass().getMethod("sendMessage", String.class);
                    sendMessageMethod.setAccessible(true);
                    sendMessageMethod.invoke(debugger, "{\"id\":1,\"method\":\"Inspector.enable\"}");
                }

                // 显示分割面板
                splitPane.setDividerLocation(0.7);
            } catch (Exception e) {
                System.err.println("打开开发者工具失败：" + e.getMessage());
                e.printStackTrace();
            }
        });
    }


    /**
     * 开发者工具降级方案（原有逻辑，适配不支持showInspector的JavaFX版本）
     */
    private void showDeveloperToolsFallback() {
        // 此处复制你原有showDeveloperTools()方法中的逻辑
        try {
            Method getDebuggerMethod = WebEngine.class.getDeclaredMethod("getDebugger");
            getDebuggerMethod.setAccessible(true);
            Object debugger = getDebuggerMethod.invoke(webEngine);

            if (debugger != null) {
                Stage inspectorStage = new Stage();
                inspectorStage.setTitle("Developer Tools");
                WebView inspectorWebView = new WebView();
                WebEngine inspectorEngine = inspectorWebView.getEngine();

                Scene scene = new Scene(inspectorWebView, 800, 600);
                inspectorStage.setScene(scene);

                Method setEnabledMethod = debugger.getClass().getMethod("setEnabled", boolean.class);
                setEnabledMethod.setAccessible(true);
                setEnabledMethod.invoke(debugger, true);

                Method setMessageCallbackMethod = debugger.getClass().getMethod("setMessageCallback", Callback.class);
                setMessageCallbackMethod.setAccessible(true);
                setMessageCallbackMethod.invoke(debugger, (Callback<String, Void>) message -> {
                    try {
                        Method executeScriptMethod = WebEngine.class.getDeclaredMethod("executeScript", String.class);
                        executeScriptMethod.setAccessible(true);
                        executeScriptMethod.invoke(inspectorEngine,
                                "if (window.WebInspector) {" +
                                        "    WebInspector.dispatchMessageFromBackend(" +
                                        "        JSON.parse('" + message.replace("'", "\\'") + "')" +
                                        "    );" +
                                        "}"
                        );
                    } catch (Exception e) {
                        System.err.println("执行调试脚本失败: " + e.getMessage());
                    }
                    return null;
                });

                Method loadContentMethod = WebEngine.class.getDeclaredMethod("loadContent", String.class);
                loadContentMethod.setAccessible(true);
                loadContentMethod.invoke(inspectorEngine,
                        "<!DOCTYPE html>" +
                                "<html>" +
                                "<head>" +
                                "    <script>" +
                                "        var WebInspector = {" +
                                "            dispatchMessageFromBackend: function(message) {" +
                                "                console.log('Received message:', message);" +
                                "            }" +
                                "        };" +
                                "    </script>" +
                                "</head>" +
                                "<body>" +
                                "    <h1>Developer Tools (Fallback)</h1>" +
                                "    <div id='content'></div>" +
                                "</body>" +
                                "</html>"
                );

                Method sendMessageMethod = debugger.getClass().getMethod("sendMessage", String.class);
                sendMessageMethod.setAccessible(true);
                sendMessageMethod.invoke(debugger, "{\"id\":1,\"method\":\"Inspector.enable\"}");

                inspectorStage.show();
            }
        } catch (Exception e) {
            System.err.println("降级方案打开开发者工具失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 执行JavaScript脚本
     * @param script 要执行的JS脚本
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
                    System.err.println("执行JS脚本失败：" + e.getMessage());
                }
            }
        });
    }

    /**
     * 刷新当前页面
     */
    public void refresh() {
        Platform.runLater(() -> {
            if (isInitialized && webEngine != null) {
                try {
                    webEngine.reload();
                } catch (Exception e) {
                    System.err.println("刷新页面失败：" + e.getMessage());
                }
            }
        });
    }

    /**
     * 后退到上一个历史页面
     */
    /**
     * 后退到上一个历史页面（全JavaFX版本兼容，无报错）
     */
    public void goBack() {
        Platform.runLater(() -> {
            if (isInitialized && webEngine != null) {
                try {
                    // 获取历史记录对象（核心，确保非空）
                    WebHistory webHistory = webEngine.getHistory();
                    if (webHistory == null) {
                        System.err.println("历史记录对象为空，无法执行后退操作");
                        return;
                    }

                    // 低版本兼容写法：手动判断当前索引，实现后退（替代canGoBack() + goBack()）
                    int currentIndex = webHistory.getCurrentIndex();
                    // 只有当前索引>0时，才有上一个历史页面
                    if (currentIndex > 0) {
                        // go(-1)：表示向后退1步（核心API，所有JavaFX版本都支持）
                        webHistory.go(-1);
                        System.out.println("成功后退到上一个页面");
                    } else {
                        System.err.println("无历史记录可后退");
                    }
                } catch (Exception e) {
                    System.err.println("后退失败：" + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 前进到下一个历史页面
     */
    /**
     * 前进到下一个历史页面（全JavaFX版本兼容，无报错）
     */
    public void goForward() {
        Platform.runLater(() -> {
            if (isInitialized && webEngine != null) {
                try {
                    // 获取历史记录对象（核心，确保非空）
                    WebHistory webHistory = webEngine.getHistory();
                    if (webHistory == null) {
                        System.err.println("历史记录对象为空，无法执行前进操作");
                        return;
                    }

                    // 低版本兼容写法：手动判断索引边界，实现前进（替代canGoForward() + goForward()）
                    int currentIndex = webHistory.getCurrentIndex();
                    // 获取历史记录总数
                    int historyCount = webHistory.getEntries().size();
                    // 只有当前索引 < 历史记录总数-1 时，才有下一个历史页面
                    if (currentIndex < historyCount - 1) {
                        // go(1)：表示向前进1步（核心API，所有JavaFX版本都支持）
                        webHistory.go(1);
                        System.out.println("成功前进到下一个页面");
                    } else {
                        System.err.println("无历史记录可前进");
                    }
                } catch (Exception e) {
                    System.err.println("前进失败：" + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 同步获取当前页面URL
     * @return 当前页面URL，未加载时返回null
     */
    public String getCurrentURL() {
        if (Platform.isFxApplicationThread() && webEngine != null) {
            return webEngine.getLocation();
        }

        AtomicReference<String> resultRef = new AtomicReference<>(null);
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                if (webEngine != null) {
                    resultRef.set(webEngine.getLocation());
                }
            } finally {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("获取当前URL被中断：" + e.getMessage());
        }

        return resultRef.get();
    }

    /**
     * 异步获取当前页面URL
     * @param callback 结果回调函数
     */
    public void getCurrentURLAsync(Consumer<String> callback) {
        if (callback == null) {
            return;
        }
        Platform.runLater(() -> {
            try {
                String url = webEngine != null ? webEngine.getLocation() : null;
                callback.accept(url);
            } catch (Exception e) {
                System.err.println("异步获取URL失败：" + e.getMessage());
            }
        });
    }

    /**
     * 同步获取当前页面标题
     * @return 当前页面标题，未加载时返回null
     */
    public String getTitle() {
        if (Platform.isFxApplicationThread() && webEngine != null) {
            return webEngine.getTitle();
        }

        AtomicReference<String> resultRef = new AtomicReference<>(null);
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                if (webEngine != null) {
                    resultRef.set(webEngine.getTitle());
                }
            } finally {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("获取页面标题被中断：" + e.getMessage());
        }

        return resultRef.get();
    }

    /**
     * 异步获取当前页面标题
     * @param callback 结果回调函数
     */
    public void getTitleAsync(Consumer<String> callback) {
        if (callback == null) {
            return;
        }
        Platform.runLater(() -> {
            try {
                String title = webEngine != null ? webEngine.getTitle() : null;
                callback.accept(title);
            } catch (Exception e) {
                System.err.println("异步获取标题失败：" + e.getMessage());
            }
        });
    }

    /**
     * 设置组件首选尺寸
     * @param preferredSize 首选尺寸
     */
    @Override
    public void setPreferredSize(Dimension preferredSize) {
        super.setPreferredSize(preferredSize);
        if (preferredSize == null) {
            return;
        }
        Platform.runLater(() -> {
            try {
                if (webView != null) {
                    webView.setPrefWidth(preferredSize.getWidth());
                    webView.setPrefHeight(preferredSize.getHeight());
                }
            } catch (Exception e) {
                System.err.println("设置首选尺寸失败：" + e.getMessage());
            }
        });
    }

    /**
     * 释放资源
     */
    public void shutdown() {
        Platform.runLater(() -> {
            try {
                if (webEngine != null) {
                    webEngine.load("about:blank");
                }
                if (jfxPanel != null) {
                    jfxPanel.setScene(null);
                }
            } catch (Exception e) {
                System.err.println("释放资源失败：" + e.getMessage());
            } finally {
                webView = null;
                webEngine = null;
                isInitialized = false;
            }
        });
    }
}
