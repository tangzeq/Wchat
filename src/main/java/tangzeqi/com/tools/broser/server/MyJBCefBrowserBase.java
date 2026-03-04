package tangzeqi.com.tools.broser.server;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.jcef.JBCefBrowser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class MyJBCefBrowserBase extends JBCefBrowser {
    private @Nullable JDialog myDevtoolsFrame;

    public MyJBCefBrowserBase(@NotNull String url) {
        super(url);
    }

    @Override
    public void openDevtools() {
        // 直接使用父类的 openDevtools 方法
        super.openDevtools();
        // 查找并修改 DevTools 窗口的标题
        Window[] windows = Window.getWindows();
        for (Window window : windows) {
            if (window instanceof JDialog) {
                JDialog dialog = (JDialog) window;
                if ("JCEF DevTools".equals(dialog.getTitle())) {
                    dialog.setTitle("Wchat DevTools");
                    break;
                }
            }
        }
    }
}
