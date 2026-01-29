//package tangzeqi.com.tools.broser;
//
//import javafx.application.Platform;
//import javafx.embed.swing.JFXPanel;
//import javafx.scene.Scene;
//import javafx.scene.web.WebEngine;
//import javafx.scene.web.WebHistory;
//import javafx.scene.web.WebView;
//import javafx.concurrent.Worker;
//import javafx.stage.Stage;
//import lombok.extern.slf4j.Slf4j;
//
//import javax.swing.*;
//import java.awt.*;
//import java.lang.reflect.Method;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.List;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.atomic.AtomicReference;
//import java.util.function.Consumer;
//
///**
// * Swing 嵌入 JavaFX WebView 的封装类
// * 修复开发者工具空白和视频播放IO异常问题
// */
//@Slf4j
//public class MyWebView extends Broser {
//
//    final private String project;
//    private JFXPanel jfxPanel;
//    private WebView webView;
//    private WebEngine webEngine;
//    private JSplitPane splitPane;
//    private JFXPanel devToolsPanel;
//    private WebView devToolsView;
//    private WebEngine devToolsEngine;
//    private volatile boolean isInitialized = false;
//    private volatile boolean devToolsCreated = false;
//
//    // 静态初始化：配置JavaFX媒体播放属性，解决线程创建失败问题
//    static {
//        // 增加媒体线程数限制
//        System.setProperty("javafx.media.threads", "8");
//        // 禁用媒体硬件加速（部分平台兼容问题）
//        System.setProperty("prism.forceGPU", "false");
//        // 启用WebKit媒体日志
//        System.setProperty("javafx.webkit.debug", "true");
//        // 允许跨域媒体加载
//        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
//    }
//
//    /**
//     * 构造方法
//     * @param project 项目标识
//     */
//    public MyWebView(String project) {
//        this.project = project;
//        setOpaque(true);
//        setLayout(new BorderLayout());
//        // 初始化JavaFX平台（带媒体配置）
//        initJavaFXPlatform();
//        initialize();
//    }
//
//    /**
//     * 初始化JavaFX平台（修复媒体线程问题）
//     */
//    private void initJavaFXPlatform() {
//        if (!Platform.isFxApplicationThread()) {
//            CountDownLatch latch = new CountDownLatch(1);
//            // 检查并启动JavaFX平台
//            if (!Platform.isFxApplicationThread()) {
//                try {
//                    Platform.startup(() -> {
//                        // 额外的媒体配置
//                        System.setProperty("webkit.media.player.useNative", "true");
//                        latch.countDown();
//                    });
//                } catch (Exception e) {
//                    log.error("JavaFX启动失败: {}", e.getMessage());
//                    latch.countDown();
//                }
//            } else {
//                latch.countDown();
//            }
//            try {
//                latch.await();
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                log.error("JavaFX初始化中断: {}", e.getMessage());
//            }
//        }
//    }
//
//    /**
//     * 初始化组件（修复核心逻辑）
//     */
//    private void initialize() {
//        // 初始化分割面板
//        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
//        splitPane.setResizeWeight(0.7);
//        splitPane.setOneTouchExpandable(true);
//        splitPane.setContinuousLayout(true);
//        add(splitPane, BorderLayout.CENTER);
//
//        // 主WebView面板
//        jfxPanel = new JFXPanel();
//        splitPane.setLeftComponent(jfxPanel);
//
//        // 开发者工具面板（初始隐藏）
//        devToolsPanel = new JFXPanel();
//        splitPane.setRightComponent(devToolsPanel);
//        splitPane.setDividerLocation(1.0);
//
//        // FX线程初始化WebView
//        Platform.runLater(() -> {
//            try {
//                webView = new WebView();
//                webEngine = webView.getEngine();
//
//                // 核心配置：修复媒体播放
//                webView.setCache(true);
//                webEngine.setJavaScriptEnabled(true);
//                webEngine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");
//
//                // 修复媒体播放错误监听
//                webEngine.getLoadWorker().exceptionProperty().addListener((obs, old, ex) -> {
//                    if (ex != null && ex.getMessage().contains("media")) {
//                        log.error("媒体加载异常: {}", ex.getMessage());
//                        // 自动重试一次
//                        retryMediaLoad();
//                    }
//                });
//
//                // 页面加载完成回调
//                webEngine.getLoadWorker().stateProperty().addListener((obs, old, newState) -> {
//                    if (newState == Worker.State.SUCCEEDED) {
//                        initMediaPlayback();
//                        isInitialized = true;
//                        log.info("页面加载完成: {}", webEngine.getLocation());
//                    } else if (newState == Worker.State.FAILED) {
//                        isInitialized = false;
//                        String error = webEngine.getLoadWorker().getException() != null ?
//                                webEngine.getLoadWorker().getException().getMessage() : "未知错误";
//                        log.error("页面加载失败: {}", error);
//                    }
//                });
//
//                // 设置主场景
//                Scene scene = new Scene(webView);
//                jfxPanel.setScene(scene);
//
//            } catch (Exception e) {
//                log.error("WebView初始化失败: {}", e.getMessage(), e);
//            }
//        });
//    }
//
//    /**
//     * 初始化媒体播放（修复核心）
//     */
//    private void initMediaPlayback() {
//        if (webEngine == null) return;
//
//        // 修复的媒体播放脚本
//        String mediaScript = """
//                // 修复视频跨域问题
//                document.addEventListener('DOMContentLoaded', function() {
//                    var videos = document.querySelectorAll('video');
//                    videos.forEach(function(video) {
//                        // 强制跨域设置
//                        video.crossOrigin = 'anonymous';
//                        // 修复Blob URL播放
//                        if (video.src.startsWith('blob:')) {
//                            var source = video.querySelector('source');
//                            if (source) {
//                                video.src = source.src;
//                            }
//                        }
//                        // 增加视频缓冲
//                        video.preload = 'metadata';
//                        video.controls = true;
//
//                        // 错误重试
//                        video.addEventListener('error', function() {
//                            console.error('视频错误:', this.src, this.error);
//                            // 重置并重新加载
//                            this.src = this.src;
//                            setTimeout(() => this.load(), 1000);
//                        });
//
//                        // 修复自动播放
//                        video.addEventListener('canplay', function() {
//                            if (this.hasAttribute('autoplay')) {
//                                this.muted = true;
//                                this.play().catch(e => console.warn('自动播放失败:', e));
//                            }
//                        });
//                    });
//                });
//
//                // 全局媒体错误监听
//                window.addEventListener('error', function(e) {
//                    if (e.target.tagName === 'VIDEO' || e.target.tagName === 'AUDIO') {
//                        console.error('媒体元素错误:', e.target.src);
//                    }
//                }, true);
//                """;
//
//        try {
//            webEngine.executeScript(mediaScript);
//        } catch (Exception e) {
//            log.error("媒体脚本执行失败: {}", e.getMessage(), e);
//        }
//    }
//
//    /**
//     * 重试媒体加载
//     */
//    private void retryMediaLoad() {
//        Platform.runLater(() -> {
//            if (isInitialized && webEngine != null) {
//                try {
//                    String retryScript = """
//                            var videos = document.querySelectorAll('video');
//                            videos.forEach(function(video) {
//                                if (video.networkState === video.NETWORK_NO_SOURCE) {
//                                    video.load();
//                                    console.log('重试加载视频:', video.src);
//                                }
//                            });
//                            """;
//                    webEngine.executeScript(retryScript);
//                } catch (Exception e) {
//                        log.error("媒体重试失败: {}", e.getMessage(), e);
//                    }
//            }
//        });
//    }
//
//
//
//    /**
//     * 加载URL（修复格式处理）
//     */
//    public void loadURL(String url) {
//        String processedUrl = processUrl(url);
//        if (processedUrl == null) return;
//
//        Platform.runLater(() -> {
//            if (webEngine != null) {
//                try {
//                    // 先停止当前加载
//                    webEngine.getLoadWorker().cancel();
//                    webEngine.load(processedUrl);
//                } catch (Exception e) {
//                    log.error("URL加载失败: {}", e.getMessage(), e);
//                }
//            }
//        });
//    }
//
//    /**
//     * 处理URL格式（修复）
//     */
//    private String processUrl(String url) {
//        if (url == null || url.trim().isEmpty()) return null;
//
//        String cleanUrl = url.trim();
//        // 补全协议
//        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://") &&
//                !cleanUrl.startsWith("file://") && !cleanUrl.startsWith("about:")) {
//            cleanUrl = "https://" + cleanUrl;
//        }
//
//        // 验证URL
//        try {
//            new URL(cleanUrl);
//        } catch (MalformedURLException e) {
//            log.error("URL格式错误: {}", cleanUrl);
//            return null;
//        }
//
//        return cleanUrl;
//    }
//
//    /**
//     * 修复的开发者工具（核心修复）
//     */
//    public void showDeveloperTools() {
//        if (!isInitialized || webView == null) {
//            log.error("WebView未初始化，无法打开开发者工具");
//            return;
//        }
//
//        Platform.runLater(() -> {
//            try {
//                // 方案1：使用JavaFX内置开发者工具（推荐）
//                if (tryNativeDeveloperTools()) {
//                    splitPane.setDividerLocation(0.7);
//                    return;
//                }
//
//                // 方案2：备用调试页面（确保有内容）
//                if (!devToolsCreated) {
//                    createFallbackDevTools();
//                    devToolsCreated = true;
//                }
//
//                // 显示开发者工具
//                splitPane.setDividerLocation(0.7);
//
//            } catch (Exception e) {
//                log.error("开发者工具打开失败: {}", e.getMessage(), e);
//            }
//        });
//    }
//
//    /**
//     * 尝试使用JavaFX内置开发者工具
//     */
//    private boolean tryNativeDeveloperTools() {
//        try {
//            // JavaFX 11+ 内置方法
//            Method showDevTools = WebView.class.getMethod("showDeveloperTools");
//            showDevTools.invoke(webView);
//            return true;
//        } catch (Exception e) {
//            // 兼容旧版本
//            try {
//                Method getDebugger = WebEngine.class.getDeclaredMethod("getDebugger");
//                getDebugger.setAccessible(true);
//                Object debugger = getDebugger.invoke(webEngine);
//                if (debugger != null) {
//                    // 启动独立调试窗口
//                    Stage devStage = new Stage();
//                    devStage.setTitle("开发者工具 - " + project);
//                    WebView devView = new WebView();
//                    Scene scene = new Scene(devView, 800, 600);
//                    devStage.setScene(scene);
//                    devStage.show();
//
//                    // 加载基础调试页面
//                    devView.getEngine().loadContent(getBasicDevToolsHtml());
//                    return true;
//                }
//            } catch (Exception ex) {
//                        log.error("内置开发者工具不支持: {}", ex.getMessage(), ex);
//                    }
//        }
//        return false;
//    }
//
//    /**
//     * 创建备用开发者工具（确保有内容）
//     */
//    private void createFallbackDevTools() {
//        devToolsView = new WebView();
//        devToolsEngine = devToolsView.getEngine();
//        devToolsEngine.setJavaScriptEnabled(true);
//        Scene devScene = new Scene(devToolsView);
//        devToolsPanel.setScene(devScene);
//
//        // 加载有内容的调试页面
//        devToolsEngine.loadContent(getBasicDevToolsHtml());
//
//        // 同步主页面控制台日志
//        String syncScript = """
//                // 同步主页面控制台
//                window.addEventListener('message', function(e) {
//                    var logDiv = document.getElementById('console-log');
//                    var div = document.createElement('div');
//                    div.className = 'log-entry';
//                    div.textContent = new Date().toLocaleTimeString() + ': ' + e.data;
//                    logDiv.appendChild(div);
//                    logDiv.scrollTop = logDiv.scrollHeight;
//                });
//                """;
//        devToolsEngine.executeScript(syncScript);
//
//        // 主页面日志转发
//        String forwardScript = """
//                // 重写控制台方法
//                var originalLog = console.log;
//                var originalError = console.error;
//                var originalWarn = console.warn;
//
//                console.log = function() {
//                    originalLog.apply(console, arguments);
//                    window.parent.postMessage(Array.from(arguments).join(' '), '*');
//                };
//
//                console.error = function() {
//                    originalError.apply(console, arguments);
//                    window.parent.postMessage('[ERROR] ' + Array.from(arguments).join(' '), '*');
//                };
//
//                console.warn = function() {
//                    originalWarn.apply(console, arguments);
//                    window.parent.postMessage('[WARN] ' + Array.from(arguments).join(' '), '*');
//                };
//                """;
//        webEngine.executeScript(forwardScript);
//    }
//
//    /**
//     * 基础开发者工具HTML（确保有内容）
//     */
//    private String getBasicDevToolsHtml() {
//        return """
//                <!DOCTYPE html>
//                <html style="height:100%; margin:0; padding:0;">
//                <head>
//                    <meta charset="UTF-8">
//                    <title>简易开发者工具</title>
//                    <style>
//                        body {
//                            background: #1e1e1e;
//                            color: #ffffff;
//                            font-family: Consolas, monospace;
//                            font-size: 12px;
//                            height: 100%;
//                            margin: 0;
//                            padding: 10px;
//                        }
//                        #console-log {
//                            height: calc(100% - 40px);
//                            overflow-y: auto;
//                            border: 1px solid #333;
//                            padding: 5px;
//                            white-space: pre-wrap;
//                        }
//                        .log-entry {
//                            margin: 2px 0;
//                            border-bottom: 1px solid #2d2d2d;
//                        }
//                        h3 {
//                            margin: 0 0 10px 0;
//                            color: #4ecdc4;
//                        }
//                    </style>
//                </head>
//                <body>
//                    <h3>开发者工具控制台</h3>
//                    <div id="console-log">
//                        <div class="log-entry">开发者工具已启动 - %s</div>
//                        <div class="log-entry">页面URL: %s</div>
//                    </div>
//                </body>
//                </html>
//                """.formatted(project, webEngine.getLocation());
//    }
//
//    // ========== 其他核心方法（保持兼容） ==========
//    public void retryLoadVideo(String videoUrl) {
//        if (videoUrl == null || videoUrl.trim().isEmpty()) return;
//
//        String safeUrl = videoUrl.replace("'", "\\'").replace("\"", "\\\"");
//        Platform.runLater(() -> {
//            if (isInitialized && webEngine != null) {
//                try {
//                    String script = String.format("""
//                            var videos = document.querySelectorAll('video[src*="%s"], video source[src*="%s"]');
//                            videos.forEach(function(video) {
//                                var parent = video.tagName === 'SOURCE' ? video.parentElement : video;
//                                parent.src = parent.src;
//                                parent.load();
//                                console.log('重试加载视频:', parent.src);
//                            });
//                            """, safeUrl, safeUrl);
//                    webEngine.executeScript(script);
//                } catch (Exception e) {
//                    log.error("视频重试失败: {}", e.getMessage(), e);
//                }
//            }
//        });
//    }
//
//    public Object executeScript(String script) {
//        if (script == null || script.trim().isEmpty()) return null;
//
//        AtomicReference<Object> result = new AtomicReference<>(null);
//        CountDownLatch latch = new CountDownLatch(1);
//
//        Platform.runLater(() -> {
//            try {
//                if (isInitialized && webEngine != null) {
//                    result.set(webEngine.executeScript(script));
//                }
//            } catch (Exception e) {
//                log.error("脚本执行失败: {}", e.getMessage(), e);
//            } finally {
//                latch.countDown();
//            }
//        });
//
//        try {
//            latch.await();
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//        return result.get();
//    }
//
//    public void refresh() {
//        Platform.runLater(() -> {
//            if (isInitialized && webEngine != null) {
//                try {
//                    webEngine.reload();
//                } catch (Exception e) {
//                    log.error("刷新失败: {}", e.getMessage(), e);
//                }
//            }
//        });
//    }
//
//    public void goBack() {
//        Platform.runLater(() -> {
//            if (!isInitialized || webEngine == null) return;
//
//            try {
//                WebHistory history = webEngine.getHistory();
//                List<WebHistory.Entry> entries = history.getEntries();
//                int current = history.getCurrentIndex();
//
//                if (current > 0 && current < entries.size()) {
//                    history.go(-1);
//                }
//            } catch (Exception e) {
//                log.error("后退失败: {}", e.getMessage(), e);
//            }
//        });
//    }
//
//    public void goForward() {
//        Platform.runLater(() -> {
//            if (!isInitialized || webEngine == null) return;
//
//            try {
//                WebHistory history = webEngine.getHistory();
//                List<WebHistory.Entry> entries = history.getEntries();
//                int current = history.getCurrentIndex();
//
//                if (current >= 0 && current < entries.size() - 1) {
//                    history.go(1);
//                }
//            } catch (Exception e) {
//                log.error("前进失败: {}", e.getMessage(), e);
//            }
//        });
//    }
//
//    public String getCurrentURL() {
//        if (Platform.isFxApplicationThread() && webEngine != null) {
//            return webEngine.getLocation();
//        }
//
//        AtomicReference<String> url = new AtomicReference<>(null);
//        CountDownLatch latch = new CountDownLatch(1);
//
//        Platform.runLater(() -> {
//            try {
//                url.set(webEngine != null ? webEngine.getLocation() : null);
//            } finally {
//                latch.countDown();
//            }
//        });
//
//        try {
//            latch.await();
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//        return url.get();
//    }
//
//    public void shutdown() {
//        isInitialized = false;
//        devToolsCreated = false;
//
//        Platform.runLater(() -> {
//            try {
//                if (webEngine != null) {
//                    webEngine.load("about:blank");
//                    webEngine.setJavaScriptEnabled(false);
//                }
//                if (devToolsEngine != null) {
//                    devToolsEngine.load("about:blank");
//                }
//                if (jfxPanel != null) jfxPanel.setScene(null);
//                if (devToolsPanel != null) devToolsPanel.setScene(null);
//            } catch (Exception e) {
//                log.error("资源释放失败: {}", e.getMessage(), e);
//            } finally {
//                webView = null;
//                webEngine = null;
//                devToolsView = null;
//                devToolsEngine = null;
//            }
//        });
//
//        removeAll();
//        revalidate();
//        repaint();
//    }
//
//    // ========== Getter方法 ==========
//    public boolean isInitialized() {
//        return isInitialized;
//    }
//
//    public void hideDeveloperTools() {
//        if (splitPane != null) {
//            splitPane.setDividerLocation(1.0);
//        }
//    }
//
//    @Override
//    public void setPreferredSize(Dimension preferredSize) {
//        super.setPreferredSize(preferredSize);
//        if (preferredSize == null) return;
//
//        Platform.runLater(() -> {
//            try {
//                if (webView != null) {
//                    webView.setPrefSize(preferredSize.getWidth(), preferredSize.getHeight());
//                }
//                if (devToolsView != null) {
//                    devToolsView.setPrefSize(preferredSize.getWidth() * 0.3, preferredSize.getHeight());
//                }
//            } catch (Exception e) {
//                    log.error("设置尺寸失败: {}", e.getMessage(), e);
//                }
//        });
//    }
//}