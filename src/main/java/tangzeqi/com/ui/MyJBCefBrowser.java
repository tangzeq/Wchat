package tangzeqi.com.ui;

import com.intellij.ui.jcef.JBCefBrowser;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefKeyboardHandlerAdapter;
import org.cef.handler.CefLifeSpanHandlerAdapter;
import org.cef.handler.CefLoadHandler;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.misc.BoolRef;
import org.cef.network.CefRequest;
import tangzeqi.com.project.MyProject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class MyJBCefBrowser extends JBCefBrowser {

    final private String project;

    public MyJBCefBrowser(String project) {
        super();
        this.project  = project;
        initializeSupport();
    }

    private void initializeSupport() {
        getJBCefClient().addLifeSpanHandler(new CefLifeSpanHandlerAdapter() {
            @Override
            public boolean onBeforePopup(CefBrowser browser, CefFrame frame, String target_url, String target_frame_name) {
                browser.loadURL(target_url);
                return true;
            }
        }, getCefBrowser());

        getJBCefClient().addKeyboardHandler(new CefKeyboardHandlerAdapter() {
            @Override
            public boolean onKeyEvent(CefBrowser browser, CefKeyEvent  event) {
                if (event.windows_key_code == 81 && event.type == CefKeyEvent.EventType.KEYEVENT_RAWKEYDOWN) { // Q键
                    return true;
                }
                return false;
            }
        }, getCefBrowser());
    }
}
