package tangzeqi.com.tools.broser.server;

import com.intellij.ui.jcef.JBCefApp;
import com.intellij.ui.jcef.JBCefClient;
import lombok.extern.slf4j.Slf4j;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLifeSpanHandlerAdapter;
import tangzeqi.com.tools.broser.Broser;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class MyJCEF extends Broser {
    // JCEF核心组件
    private MyJBCefBrowserBase jbCefBrowser;
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
            String safeUrl = initialUrl != null ? initialUrl : "https://www.cnblogs.com/tangzeqi";
            jbCefBrowser = new MyJBCefBrowserBase(safeUrl);
            jbCefClient = jbCefBrowser.getJBCefClient();
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
            log.error("JCEF初始化失败", e);
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
            jbCefBrowser.openDevtools();
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

}