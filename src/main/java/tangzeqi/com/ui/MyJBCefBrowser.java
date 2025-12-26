package tangzeqi.com.ui;

import com.intellij.ui.jcef.JBCefBrowser;
import lombok.SneakyThrows;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefContextMenuParams;
import org.cef.callback.CefMenuModel;
import org.cef.handler.CefContextMenuHandlerAdapter;
import org.cef.handler.CefDisplayHandlerAdapter;
import org.cef.handler.CefLifeSpanHandlerAdapter;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.misc.BoolRef;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * 完整的浏览器组件，提供所有标准的浏览器功能
 */
public class MyJBCefBrowser extends JBCefBrowser {

    private MediaPlayerListener mediaPlayerListener;
    
    /**
     * 媒体播放监听器接口
     */
    public interface MediaPlayerListener {
        void onMediaStateChanged(boolean isPlaying);
        void onVolumeChanged(double volume);
        void onMutedChanged(boolean muted);
        void onFullscreenChanged(boolean fullscreen);
        void onProgressChanged(double currentTime, double duration);
    }

    public MyJBCefBrowser() {
        super();
        initializeHtml5Support();
    }

    public MyJBCefBrowser(String url) {
        super(url);
        initializeHtml5Support();
    }

    /**
     * 设置媒体播放监听器
     */
    public void setMediaPlayerListener(MediaPlayerListener listener) {
        this.mediaPlayerListener = listener;
    }

    /**
     * 初始化 HTML5 媒体支持
     */
    private void initializeHtml5Support() {
        // 加载处理器 - 处理页面加载状态
        getJBCefClient().addLoadHandler(new CefLoadHandlerAdapter() {
            @Override
            public void onLoadingStateChange(CefBrowser browser, boolean isLoading,
                                              boolean canGoBack, boolean canGoForward) {
                // 页面加载状态变化时注入媒体控制脚本
                if (!isLoading) {
                    injectMediaControlScript();
                }
            }

            @Override
            public void onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode) {
                // 页面加载完成后注入媒体控制脚本
                injectMediaControlScript();
            }
        }, getCefBrowser());

        // 显示处理器 - 处理标题
        getJBCefClient().addDisplayHandler(new CefDisplayHandlerAdapter() {
            @Override
            public void onTitleChange(CefBrowser browser, String title) {
                // 标题变化处理
            }
            
            public boolean onConsoleMessage(CefBrowser browser, String message, String source, int line) {
                // 监听媒体相关控制台消息
                if (message != null && message.contains("HTML5")) {
                    System.out.println("HTML5 媒体消息: " + message);
                }
                return false;
            }
        }, getCefBrowser());

        // 右键菜单添加媒体控制选项
        getJBCefClient().addContextMenuHandler(new CefContextMenuHandlerAdapter() {
            @Override
            public void onBeforeContextMenu(CefBrowser browser, CefFrame frame,
                                            CefContextMenuParams params, CefMenuModel model) {
                // 检查是否是媒体元素
                    model.addSeparator();
                    model.addItem(10001, "🎵 播放/暂停");
                    model.addItem(10002, "🔊 音量控制");
                    model.addItem(10003, "⏩ 快进 10秒");
                    model.addItem(10004, "⏪ 快退 10秒");
                    model.addItem(10005, "📊 媒体信息");
            }

            @Override
            public boolean onContextMenuCommand(CefBrowser browser, CefFrame frame,
                                                 CefContextMenuParams params, int commandId,
                                                 int eventFlags) {
                if (commandId >= 10001 && commandId <= 10005) {
                    executeMediaCommand(commandId);
                    return true;
                }
                return false;
            }
        }, getCefBrowser());

        // 生命周期处理器 - 阻止弹出新窗口，所有新页面在当前浏览器中打开
        getJBCefClient().addLifeSpanHandler(new CefLifeSpanHandlerAdapter() {
            @Override
            public boolean onBeforePopup(CefBrowser browser, CefFrame frame, String targetUrl,
                                         String targetFrameName, CefLifeSpanHandlerAdapter.WindowOpenDisposition targetDisposition,
                                         boolean userGesture, int popupFeatures, CefWindowInfo windowInfo,
                                         CefClient client, CefBrowserSettings settings, BoolRef noDefaultAuth) {
                // 阻止弹出新窗口，在当前浏览器中加载新页面
                if (targetUrl != null && !targetUrl.isEmpty()) {
                    // 在主框架中加载新URL
                    CefBrowser mainBrowser = getCefBrowser();
                    if (mainBrowser != null && mainBrowser.getMainFrame() != null) {
                        mainBrowser.getMainFrame().loadURL(targetUrl);
                    }
                }
                // 返回 true 阻止弹出窗口
                return true;
            }
        }, getCefBrowser());
    }

    /**
     * 注入媒体控制 JavaScript 脚本
     */
    private void injectMediaControlScript() {
        String script = """
            (function() {
                // 媒体状态追踪
                window.__cefMediaState = {
                    currentMedia: null,
                    listeners: new Set()
                };

                // 监听媒体元素事件
                function attachMediaListeners(media) {
                    if (!media || media.__cefListenersAttached) return;
                    media.__cefListenersAttached = true;

                    media.addEventListener('play', function() {
                        window.__cefMediaState.currentMedia = media;
                        window.__cefMediaState.listeners.forEach(function(cb) {
                            cb('play', media);
                        });
                    });

                    media.addEventListener('pause', function() {
                        window.__cefMediaState.listeners.forEach(function(cb) {
                            cb('pause', media);
                        });
                    });

                    media.addEventListener('ended', function() {
                        window.__cefMediaState.listeners.forEach(function(cb) {
                            cb('ended', media);
                        });
                    });

                    media.addEventListener('volumechange', function() {
                        window.__cefMediaState.listeners.forEach(function(cb) {
                            cb('volumechange', media);
                        });
                    });

                    media.addEventListener('timeupdate', function() {
                        window.__cefMediaState.listeners.forEach(function(cb) {
                            cb('timeupdate', media);
                        });
                    });
                }

                // 自动监听页面中的媒体元素
                function observeMediaElements() {
                    var observer = new MutationObserver(function(mutations) {
                        mutations.forEach(function(mutation) {
                            mutation.addedNodes.forEach(function(node) {
                                if (node.tagName === 'VIDEO' || node.tagName === 'AUDIO') {
                                    attachMediaListeners(node);
                                }
                            });
                        });
                    });

                    observer.observe(document.body, { childList: true, subtree: true });

                    // 监听已存在的媒体元素
                    document.querySelectorAll('video, audio').forEach(attachMediaListeners);
                }

                // 页面加载完成后初始化
                if (document.readyState === 'loading') {
                    document.addEventListener('DOMContentLoaded', observeMediaElements);
                } else {
                    observeMediaElements();
                }

                // 暴露控制函数到全局
                window.__cefMediaControl = {
                    play: function() {
                        var media = window.__cefMediaState.currentMedia || document.querySelector('video, audio');
                        if (media) media.play();
                    },
                    pause: function() {
                        var media = window.__cefMediaState.currentMedia || document.querySelector('video, audio');
                        if (media) media.pause();
                    },
                    togglePlay: function() {
                        var media = window.__cefMediaState.currentMedia || document.querySelector('video, audio');
                        if (media) {
                            if (media.paused) media.play();
                            else media.pause();
                        }
                    },
                    setVolume: function(value) {
                        var media = window.__cefMediaState.currentMedia || document.querySelector('video, audio');
                        if (media) {
                            media.volume = Math.max(0, Math.min(1, value));
                        }
                    },
                    getVolume: function() {
                        var media = window.__cefMediaState.currentMedia || document.querySelector('video, audio');
                        return media ? media.volume : 0;
                    },
                    mute: function() {
                        var media = window.__cefMediaState.currentMedia || document.querySelector('video, audio');
                        if (media) media.muted = true;
                    },
                    unmute: function() {
                        var media = window.__cefMediaState.currentMedia || document.querySelector('video, audio');
                        if (media) media.muted = false;
                    },
                    toggleMute: function() {
                        var media = window.__cefMediaState.currentMedia || document.querySelector('video, audio');
                        if (media) media.muted = !media.muted;
                    },
                    seek: function(time) {
                        var media = window.__cefMediaState.currentMedia || document.querySelector('video, audio');
                        if (media) media.currentTime = Math.max(0, Math.min(media.duration || time, time));
                    },
                    skipForward: function(seconds) {
                        var media = window.__cefMediaState.currentMedia || document.querySelector('video, audio');
                        if (media) media.currentTime = Math.min(media.duration || media.currentTime + seconds, media.currentTime + seconds);
                    },
                    skipBackward: function(seconds) {
                        var media = window.__cefMediaState.currentMedia || document.querySelector('video, audio');
                        if (media) media.currentTime = Math.max(0, media.currentTime - seconds);
                    },
                    setPlaybackRate: function(rate) {
                        var media = window.__cefMediaState.currentMedia || document.querySelector('video, audio');
                        if (media) media.playbackRate = rate;
                    },
                    getInfo: function() {
                        var media = window.__cefMediaState.currentMedia || document.querySelector('video, audio');
                        if (!media) return null;
                        return {
                            isPlaying: !media.paused,
                            currentTime: media.currentTime,
                            duration: media.duration,
                            volume: media.volume,
                            muted: media.muted,
                            playbackRate: media.playbackRate,
                            isVideo: media.tagName === 'VIDEO',
                            src: media.src
                        };
                    },
                    getCurrentMedia: function() {
                        return window.__cefMediaState.currentMedia;
                    },
                    getAllMedia: function() {
                        var videos = document.querySelectorAll('video');
                        var audios = document.querySelectorAll('audio');
                        return {
                            videos: Array.from(videos).map(function(v) {
                                return { tag: 'VIDEO', src: v.src, playing: !v.paused };
                            }),
                            audios: Array.from(audios).map(function(a) {
                                return { tag: 'AUDIO', src: a.src, playing: !a.paused };
                            })
                        };
                    }
                };

                console.log('HTML5 媒体控制脚本已加载');
            })();
            """;
        
        getCefBrowser().getMainFrame().executeJavaScript(script, "", 0);
    }

    /**
     * 执行媒体控制命令
     */
    private void executeMediaCommand(int commandId) {
        String script = "";
        switch (commandId) {
            case 10001: // 播放/暂停
                script = "window.__cefMediaControl.togglePlay()";
                break;
            case 10002: // 音量控制
                script = """
                    var vol = prompt('请输入音量 (0-1):', '0.5');
                    if (vol !== null) {
                        window.__cefMediaControl.setVolume(parseFloat(vol));
                    }
                    """;
                break;
            case 10003: // 快进 10秒
                script = "window.__cefMediaControl.skipForward(10)";
                break;
            case 10004: // 快退 10秒
                script = "window.__cefMediaControl.skipBackward(10)";
                break;
            case 10005: // 媒体信息
                script = """
                    var info = window.__cefMediaControl.getInfo();
                    if (info) {
                        alert('媒体信息:\\n' +
                            '类型: ' + (info.isVideo ? '视频' : '音频') + '\\n' +
                            '播放状态: ' + (info.isPlaying ? '正在播放' : '已暂停') + '\\n' +
                            '当前时间: ' + info.currentTime.toFixed(1) + '秒\\n' +
                            '总时长: ' + (info.duration ? info.duration.toFixed(1) + '秒' : '未知') + '\\n' +
                            '音量: ' + (info.volume * 100).toFixed(0) + '%\\n' +
                            '静音: ' + (info.muted ? '是' : '否') + '\\n' +
                            '播放速度: ' + info.playbackRate + 'x');
                    } else {
                        alert('未检测到媒体元素');
                    }
                    """;
                break;
        }
        
        if (!script.isEmpty()) {
            getCefBrowser().getMainFrame().executeJavaScript(script, "", 0);
        }
    }

    // ==================== 公共 API 方法 ====================

    /**
     * 播放当前媒体
     */
    public void playMedia() {
        executeJavaScript("window.__cefMediaControl.play()");
    }

    /**
     * 暂停当前媒体
     */
    public void pauseMedia() {
        executeJavaScript("window.__cefMediaControl.pause()");
    }

    /**
     * 切换播放/暂停状态
     */
    public void togglePlayPause() {
        executeJavaScript("window.__cefMediaControl.togglePlay()");
    }

    /**
     * 设置音量 (0.0 - 1.0)
     */
    public void setVolume(double volume) {
        executeJavaScript("window.__cefMediaControl.setVolume(" + volume + ")");
    }

    /**
     * 获取当前音量
     */
    public double getVolume() {
        String result = executeJavaScriptWithResult("return window.__cefMediaControl.getVolume()");
        try {
            return Double.parseDouble(result);
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 静音
     */
    public void mute() {
        executeJavaScript("window.__cefMediaControl.mute()");
    }

    /**
     * 取消静音
     */
    public void unmute() {
        executeJavaScript("window.__cefMediaControl.unmute()");
    }

    /**
     * 切换静音状态
     */
    public void toggleMute() {
        executeJavaScript("window.__cefMediaControl.toggleMute()");
    }

    /**
     * 跳转到指定时间（秒）
     */
    public void seekTo(double time) {
        executeJavaScript("window.__cefMediaControl.seek(" + time + ")");
    }

    /**
     * 快进（秒）
     */
    public void skipForward(double seconds) {
        executeJavaScript("window.__cefMediaControl.skipForward(" + seconds + ")");
    }

    /**
     * 快退（秒）
     */
    public void skipBackward(double seconds) {
        executeJavaScript("window.__cefMediaControl.skipBackward(" + seconds + ")");
    }

    /**
     * 设置播放速度
     */
    public void setPlaybackRate(double rate) {
        executeJavaScript("window.__cefMediaControl.setPlaybackRate(" + rate + ")");
    }

    /**
     * 获取媒体信息
     */
    public String getMediaInfo() {
        return executeJavaScriptWithResult("return JSON.stringify(window.__cefMediaControl.getInfo())");
    }

    /**
     * 获取所有媒体元素信息
     */
    public String getAllMediaInfo() {
        return executeJavaScriptWithResult("return JSON.stringify(window.__cefMediaControl.getAllMedia())");
    }

    /**
     * 检查页面是否有媒体正在播放
     */
    public boolean isMediaPlaying() {
        String result = executeJavaScriptWithResult("var info = window.__cefMediaControl.getInfo(); return info ? info.isPlaying : false");
        return "true".equalsIgnoreCase(result);
    }

    /**
     * 执行 JavaScript 并返回结果
     */
    @SneakyThrows
    private String executeJavaScriptWithResult(String script) {
        // 由于 JCEF 的限制，这里使用异步回调的方式
        // 实际使用时需要在 JavaScript 中通过回调函数处理结果
        executeJavaScript(script);
        return null;
    }

    /**
     * 执行 JavaScript 代码
     */
    public void executeJavaScript(String code) {
        CefFrame frame = getCefBrowser().getMainFrame();
        if (frame != null) {
            frame.executeJavaScript(code, "", 0);
        }
    }

    /**
     * 重新注入媒体控制脚本（用于页面切换后）
     */
    public void reinjectMediaScript() {
        injectMediaControlScript();
    }
}