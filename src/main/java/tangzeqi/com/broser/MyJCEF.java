package tangzeqi.com.broser;

import com.intellij.ui.jcef.JBCefApp;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefClient;
import lombok.extern.slf4j.Slf4j;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefContextMenuParams;
import org.cef.callback.CefMenuModel;
import org.cef.handler.*;
import org.cef.network.CefRequest;

import javax.swing.*;
import java.awt.*;
import java.awt.AWTEvent;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class MyJCEF extends Broser {
    // JCEF核心组件
    private JBCefBrowser jbCefBrowser;
    private Component browserComponent;
    private JBCefClient jbCefClient;
    private final String project;

    // 状态标记
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);
    private String pendingUrl = null;

    /**
     * 构造函数：创建JCEF浏览器
     */
    public MyJCEF(String project, String initialUrl) {
        setLayout(new BorderLayout());
        this.project = project;
        setOpaque(true);
        setBackground(Color.WHITE);

        log.info("创建MyJCEF实例，项目: {}, 初始URL: {}", project, initialUrl);

        try {
            initJcef(initialUrl);
        } catch (Exception e) {
            log.error("JCEF初始化失败", e);
            handleInitializationError(e);
        }
    }

    public MyJCEF(String project) {
        this(project, "https://www.cnblogs.com/tangzeqi");
    }

    /**
     * 初始化JCEF核心
     */
    private void initJcef(String initialUrl) {
        try {
            if (!JBCefApp.isSupported()) {
                showErrorPanel("JCEF不支持", "当前环境不支持JCEF");
                return;
            }

            String safeUrl = initialUrl != null ? initialUrl : "https://www.baidu.com";
            jbCefBrowser = new JBCefBrowser(safeUrl);
            jbCefClient = jbCefBrowser.getJBCefClient();

            // 添加全局弹窗阻止
            String blockPopupsScript = """
                window.open = function() { return null; };
                window.alert = function() { return null; };
                window.confirm = function() { return null; };
                window.prompt = function() { return null; };
            """;

            jbCefBrowser.getCefBrowser().executeJavaScript(blockPopupsScript, safeUrl, 0);

            // 添加弹窗处理
            jbCefClient.addLifeSpanHandler(new CefLifeSpanHandlerAdapter() {
                @Override
                public boolean onBeforePopup(CefBrowser browser, CefFrame frame, String target_url, String target_frame_name) {
                    if (target_url != null) {
                        browser.loadURL(target_url);
                    }
                    return true;
                }
            }, jbCefBrowser.getCefBrowser());

            // 添加请求处理
            jbCefClient.addRequestHandler(new CefRequestHandlerAdapter() {
                @Override
                public boolean onBeforeBrowse(CefBrowser browser, CefFrame frame, CefRequest request, boolean user_gesture, boolean is_redirect) {
                    String url = request.getURL();
                    log.info("开始浏览: {}", url);

                    handleSpecialSites(url);
                    return false;
                }
            }, jbCefBrowser.getCefBrowser());

            // 添加上下文菜单处理
            jbCefClient.addContextMenuHandler(new CefContextMenuHandler() {
                @Override
                public void onBeforeContextMenu(CefBrowser browser, CefFrame frame, CefContextMenuParams params, CefMenuModel model) {
                    // 保留默认菜单，同时添加自定义菜单项
                    // 添加自定义菜单项
                    model.addSeparator();
                    model.addItem(1, "刷新");
                    model.addItem(2, "后退");
                    model.addItem(3, "前进");
                    model.addSeparator();
                    model.addItem(4, "开发者工具");
                    model.addSeparator();
                    model.addItem(5, "复制链接地址");
                }

                @Override
                public boolean onContextMenuCommand(CefBrowser browser, CefFrame frame, CefContextMenuParams params, int command_id, int event_flags) {
                    switch (command_id) {
                        case 1:
                            browser.reload();
                            return true;
                        case 2:
                            browser.goBack();
                            return true;
                        case 3:
                            browser.goForward();
                            return true;
                        case 4:
                            openDevTools();
                            return true;
                        case 5:
                            String url = params.getSourceUrl();
                            if (url != null && !url.isEmpty()) {
                                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                                        new StringSelection(url), null);
                            }
                            return true;
                    }
                    return false;
                }

                @Override
                public void onContextMenuDismissed(CefBrowser browser, CefFrame frame) {
                    // 菜单被关闭时的处理
                }
            }, jbCefBrowser.getCefBrowser());

            // 添加加载处理
            jbCefClient.addLoadHandler(new CefLoadHandlerAdapter() {
                @Override
                public void onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode) {
                    isInitialized.set(true);
                    log.info("页面加载完成");

                    enableMediaFeatures(browser);

                    if (pendingUrl != null) {
                        String urlToLoad = pendingUrl;
                        pendingUrl = null;
                        browser.loadURL(urlToLoad);
                    }
                }
            }, jbCefBrowser.getCefBrowser());

            // 获取浏览器UI组件
            browserComponent = jbCefBrowser.getComponent();
            if (browserComponent != null) {
                log.info("浏览器组件类型: {}", browserComponent.getClass().getName());
                
                // 创建一个自定义的JPanel包装器
                JPanel wrapperPanel = new JPanel(new BorderLayout());
                wrapperPanel.setOpaque(false);
                wrapperPanel.add(browserComponent, BorderLayout.CENTER);
                
                // 添加到主面板
                add(wrapperPanel, BorderLayout.CENTER);
                revalidate();
                repaint();
                
                // 使用AWT事件监听来捕获所有鼠标事件
                Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
                    if (event instanceof MouseEvent) {
                        MouseEvent mouseEvent = (MouseEvent) event;
                        // 检查是否是右键点击，并且事件源是我们的包装面板或其子组件
                        if (mouseEvent.getButton() == MouseEvent.BUTTON3 && 
                            SwingUtilities.isDescendingFrom(mouseEvent.getComponent(), wrapperPanel)) {
                            log.info("AWT事件监听器捕获到右键点击");
                            SwingUtilities.invokeLater(() -> {
                                showCustomContextMenu(mouseEvent);
                            });
                        }
                    }
                }, AWTEvent.MOUSE_EVENT_MASK);
                
                log.info("包装面板已添加，AWT事件监听器已注册，右键菜单应该可以正常工作");
            } else {
                showErrorPanel("浏览器组件创建失败", "无法获取JCEF浏览器组件");
            }
        } catch (Exception e) {
            handleInitializationError(e);
        }
    }

    /**
     * 显示自定义上下文菜单
     */
    private void showCustomContextMenu(MouseEvent e) {
        JPopupMenu popupMenu = new JPopupMenu();
        
        // 添加菜单项
        JMenuItem refreshItem = new JMenuItem("刷新");
        refreshItem.addActionListener(evt -> {
            if (jbCefBrowser != null) {
                jbCefBrowser.getCefBrowser().reload();
            }
        });
        popupMenu.add(refreshItem);

        JMenuItem backItem = new JMenuItem("后退");
        backItem.addActionListener(evt -> {
            if (jbCefBrowser != null) {
                jbCefBrowser.getCefBrowser().goBack();
            }
        });
        popupMenu.add(backItem);

        JMenuItem forwardItem = new JMenuItem("前进");
        forwardItem.addActionListener(evt -> {
            if (jbCefBrowser != null) {
                jbCefBrowser.getCefBrowser().goForward();
            }
        });
        popupMenu.add(forwardItem);

        popupMenu.addSeparator();

        JMenuItem devToolsItem = new JMenuItem("开发者工具");
        devToolsItem.addActionListener(evt -> {
            openDevTools();
        });
        popupMenu.add(devToolsItem);

        popupMenu.addSeparator();

        JMenuItem copyUrlItem = new JMenuItem("复制链接地址");
        copyUrlItem.addActionListener(evt -> {
            if (jbCefBrowser != null) {
                String url = jbCefBrowser.getCefBrowser().getURL();
                if (url != null && !url.isEmpty()) {
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                            new StringSelection(url), null);
                }
            }
        });
        popupMenu.add(copyUrlItem);

        // 显示菜单
        popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * 处理特殊网站
     */
    private void handleSpecialSites(String url) {
        if (url.contains("bilibili.com")) {
            String bilibiliScript = """
                // B站视频播放修复
                (function() {
                    console.log('B站视频播放修复脚本执行');
                    
                    // 定期检查播放器
                    const checkPlayer = setInterval(function() {
                        const player = document.querySelector('.bilibili-player video');
                        if (player) {
                            console.log('找到B站播放器');
                            player.muted = true;
                            player.autoplay = true;
                            player.playsinline = true;
                            player.crossOrigin = 'anonymous';
                            player.preload = 'metadata';
                            player.controls = true;
                            
                            // 尝试播放
                            player.play().catch(function(e) {
                                console.warn('B站播放器启动失败:', e);
                                setTimeout(function() {
                                    player.play().catch(console.error);
                                }, 1000);
                            });
                        }
                    }, 2000);
                    
                    // 清理定时器
                    setTimeout(function() {
                        clearInterval(checkPlayer);
                    }, 30000);
                })();
            """;
            jbCefBrowser.getCefBrowser().executeJavaScript(bilibiliScript, url, 0);
        }
        else if (url.contains("douyin.com")) {
            String douyinScript = """
                // 抖音视频播放修复
                (function() {
                    console.log('抖音视频播放修复脚本执行');
                    
                    // 定期检查播放器
                    const checkPlayer = setInterval(function() {
                        const player = document.querySelector('video');
                        if (player) {
                            console.log('找到抖音播放器');
                            player.muted = true;
                            player.autoplay = true;
                            player.playsinline = true;
                            player.crossOrigin = 'anonymous';
                            player.preload = 'metadata';
                            player.controls = true;
                            
                            // 尝试播放
                            player.play().catch(function(e) {
                                console.warn('抖音播放器启动失败:', e);
                                setTimeout(function() {
                                    player.play().catch(console.error);
                                }, 1000);
                            });
                        }
                    }, 2000);
                    
                    // 清理定时器
                    setTimeout(function() {
                        clearInterval(checkPlayer);
                    }, 30000);
                })();
            """;
            jbCefBrowser.getCefBrowser().executeJavaScript(douyinScript, url, 0);
        }
    }

    /**
     * 启用媒体功能
     */
    private void enableMediaFeatures(CefBrowser browser) {
        String enableMediaScript = """
            // 媒体播放修复脚本
            (function() {
                console.log('媒体播放修复脚本执行');
                
                // 修复视频跨域问题
                function processVideo(video) {
                    if (video.dataset.processed) return;
                    video.dataset.processed = 'true';
                    console.log('处理视频:', video.src);
                    
                    // 强制跨域设置
                    video.crossOrigin = 'anonymous';
                    // 修复Blob URL播放
                    if (video.src.startsWith('blob:')) {
                        var source = video.querySelector('source');
                        if (source) {
                            video.src = source.src;
                            console.log('修复Blob URL:', video.src);
                        }
                    }
                    // 增加视频缓冲
                    video.preload = 'metadata';
                    video.controls = true;
                    
                    // 错误重试
                    video.addEventListener('error', function() {
                        console.error('视频错误:', this.src, this.error);
                        // 重置并重新加载
                        this.src = this.src;
                        setTimeout(() => this.load(), 1000);
                    });
                    
                    // 修复自动播放
                    video.addEventListener('canplay', function() {
                        if (this.hasAttribute('autoplay')) {
                            this.muted = true;
                            this.play().catch(e => console.warn('自动播放失败:', e));
                        }
                    });
                    
                    // 尝试播放
                    video.play().catch(function(e) {
                        console.warn('视频播放失败:', e);
                    });
                }
                
                function processAllVideos() {
                    var videos = document.querySelectorAll('video');
                    console.log('找到视频数量:', videos.length);
                    videos.forEach(processVideo);
                }
                
                // 立即处理现有视频
                processAllVideos();
                
                // 定期检查新视频
                setInterval(processAllVideos, 2000);
                
                // 监听DOM变化
                const observer = new MutationObserver(function(mutations) {
                    mutations.forEach(function(mutation) {
                        if (mutation.addedNodes.length > 0) {
                            processAllVideos();
                        }
                    });
                });
                
                observer.observe(document.body, {
                    childList: true,
                    subtree: true
                });
                
                console.log('媒体播放修复脚本初始化完成');
            })();
        """;

        try {
            browser.executeJavaScript(enableMediaScript, browser.getURL(), 0);
            log.info("媒体播放脚本执行成功");
        } catch (Throwable e) {
            log.error("执行媒体播放脚本失败", e);
        }
    }

    /**
     * 处理初始化错误
     */
    private void handleInitializationError(Exception e) {
        log.error("JCEF初始化失败，尝试恢复", e);

        try {
            if (jbCefBrowser != null) {
                jbCefBrowser.dispose();
            }
        } catch (Exception cleanupError) {
            log.error("清理资源时发生错误", cleanupError);
        }

        SwingUtilities.invokeLater(() -> {
            showErrorPanel("JCEF初始化失败", "正在尝试重新初始化...\n错误信息: " + e.getMessage());
            Timer timer = new Timer(2000, evt -> {
                try {
                    initJcef(pendingUrl);
                } catch (Exception retryError) {
                    log.error("重试初始化失败", retryError);
                    showErrorPanel("初始化失败", "无法初始化JCEF组件，请检查环境配置");
                }
            });
            timer.setRepeats(false);
            timer.start();
        });
    }

    /**
     * 显示错误面板
     */
    private void showErrorPanel(String title, String message) {
        JPanel errorPanel = new JPanel(new BorderLayout());
        errorPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("<html><h2>" + title + "</h2></html>");
        titleLabel.setForeground(Color.RED);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel msgLabel = new JLabel("<html><pre>" + message + "</pre></html>");
        msgLabel.setHorizontalAlignment(SwingConstants.CENTER);

        errorPanel.add(titleLabel, BorderLayout.NORTH);
        errorPanel.add(msgLabel, BorderLayout.CENTER);

        add(errorPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    // ===================== 对外提供的核心方法 =====================
    public void loadURL(String url) {
        String processedUrl = processUrl(url);
        if (processedUrl == null) return;

        log.info("请求加载URL: {}", processedUrl);

        if (!isInitialized.get()) {
            log.info("JCEF尚未初始化，保存URL待初始化后加载: {}", processedUrl);
            pendingUrl = processedUrl;
            if (jbCefBrowser == null) {
                log.info("JCEF浏览器实例为空，重新初始化");
                initJcef(processedUrl);
            }
            return;
        }

        if (jbCefBrowser != null) {
            try {
                log.info("开始加载URL: {}", processedUrl);
                jbCefBrowser.loadURL(processedUrl);
            } catch (Exception e) {
                log.error("加载URL失败: {}", processedUrl, e);
                showErrorPanel("加载URL失败", "URL: " + processedUrl + "\n错误: " + e.getMessage());
            }
        } else {
            log.error("JCEF浏览器实例为空，无法加载URL");
            showErrorPanel("浏览器未初始化", "无法加载URL: " + processedUrl);
        }
    }

    /**
     * 处理URL格式（修复）
     */
    private String processUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            log.warn("空URL，跳过加载");
            return null;
        }

        String cleanUrl = url.trim();
        // 补全协议
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://") &&
                !cleanUrl.startsWith("file://") && !cleanUrl.startsWith("about:")) {
            cleanUrl = "https://" + cleanUrl;
        }

        // 验证URL
        try {
            new java.net.URL(cleanUrl);
        } catch (java.net.MalformedURLException e) {
            log.error("URL格式错误: {}", cleanUrl);
            showErrorPanel("URL格式错误", "URL: " + cleanUrl + "\n错误: " + e.getMessage());
            return null;
        }

        return cleanUrl;
    }

    public void refresh() {
        log.info("请求刷新当前页面");
        if (jbCefBrowser != null && isInitialized.get()) {
            try {
                jbCefBrowser.getCefBrowser().reload();
                log.info("刷新页面成功");
            } catch (Exception e) {
                log.error("刷新页面失败", e);
            }
        } else {
            log.error("浏览器未初始化，无法刷新页面");
        }
    }

    public void goBack() {
        log.info("请求后退");
        if (jbCefBrowser != null && isInitialized.get()) {
            try {
                jbCefBrowser.getCefBrowser().goBack();
                log.info("后退成功");
            } catch (Exception e) {
                log.error("后退失败", e);
            }
        } else {
            log.error("浏览器未初始化，无法后退");
        }
    }

    public void goForward() {
        log.info("请求前进");
        if (jbCefBrowser != null && isInitialized.get()) {
            try {
                jbCefBrowser.getCefBrowser().goForward();
                log.info("前进成功");
            } catch (Exception e) {
                log.error("前进失败", e);
            }
        } else {
            log.error("浏览器未初始化，无法前进");
        }
    }

    public String getCurrentURL() {
        if (jbCefBrowser != null && isInitialized.get()) {
            try {
                return jbCefBrowser.getCefBrowser().getURL();
            } catch (Exception e) {
                log.error("获取URL失败", e);
                return "";
            }
        }
        return "";
    }

    public Object executeScript(String script) {
        log.info("请求执行JS代码");
        if (!isInitialized.get()) {
            log.error("浏览器未初始化，无法执行JS代码");
            return null;
        }

        if (jbCefBrowser != null) {
            try {
                log.info("执行JS代码");
                jbCefBrowser.getCefBrowser().executeJavaScript(script, jbCefBrowser.getCefBrowser().getURL(), 0);
                log.info("JS代码执行成功");
            } catch (Exception e) {
                log.error("执行JS代码失败", e);
            }
        } else {
            log.error("浏览器实例为空，无法执行JS代码");
        }
        return null;
    }

    public void openDevTools() {
        log.info("请求打开开发者工具");
        if (jbCefBrowser != null && isInitialized.get()) {
            try {
                log.info("打开开发者工具");
                jbCefBrowser.openDevtools();
                log.info("开发者工具打开成功");
                
                // 尝试修改开发者工具窗口标题，使用定时器定期检查
                Timer timer = new Timer(500, null);
                int[] attempts = {0};
                final int MAX_ATTEMPTS = 10;
                
                timer.addActionListener(evt -> {
                    if (attempts[0] >= MAX_ATTEMPTS) {
                        ((Timer) evt.getSource()).stop();
                        return;
                    }
                    
                    attempts[0]++;
                    log.info("尝试修改开发者工具窗口标题，第 {} 次", attempts[0]);
                    
                    Window[] windows = Window.getWindows();
                    for (Window window : windows) {
                        if (window instanceof Frame) {
                            Frame frame = (Frame) window;
                            if (frame.getTitle().equals("JCEF DevTools")) {
                                log.info("找到开发者工具窗口，修改标题为 Wchat DevTools");
                                frame.setTitle("Wchat DevTools");
                                ((Timer) evt.getSource()).stop();
                                break;
                            }
                        }
                    }
                });
                
                timer.setRepeats(true);
                timer.start();
            } catch (Exception e) {
                log.error("打开开发者工具失败", e);
                showErrorPanel("打开开发者工具失败", e.getMessage());
            }
        } else {
            log.error("浏览器未初始化，无法打开开发者工具");
            showErrorPanel("浏览器未初始化", "无法打开开发者工具");
        }
    }

    public void dispose() {
        SwingUtilities.invokeLater(() -> {
            try {
                if (jbCefBrowser != null) {
                    if (jbCefClient != null) {
                        jbCefClient.dispose();
                    }
                    jbCefBrowser.dispose();
                }
                removeAll();
                isInitialized.set(false);
            } catch (Exception e) {
                log.error("释放资源时发生错误", e);
            }
        });
    }

    public JBCefBrowser getJbCefBrowser() {
        return jbCefBrowser;
    }

}