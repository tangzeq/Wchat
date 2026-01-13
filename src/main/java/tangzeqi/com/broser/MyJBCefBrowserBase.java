package tangzeqi.com.broser;

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
        if (this.myDevtoolsFrame != null) {
            this.myDevtoolsFrame.setVisible(true);
            this.myDevtoolsFrame.toFront();
        } else {
            Component comp = this.getComponent();
            Window ancestor = comp == null ? KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow() : SwingUtilities.getWindowAncestor(comp);
            if (ancestor != null) {
                Rectangle bounds = ancestor.getGraphicsConfiguration().getBounds();
                this.myDevtoolsFrame = new JDialog(ancestor);
                this.myDevtoolsFrame.setTitle("Wchat DevTools");
                this.myDevtoolsFrame.setDefaultCloseOperation(1);
                this.myDevtoolsFrame.setBounds(bounds.width / 4 + 100, bounds.height / 4 + 100, bounds.width / 2, bounds.height / 2);
                this.myDevtoolsFrame.setLayout(new BorderLayout());
                JBCefBrowser devTools = JBCefBrowser.createBuilder().setCefBrowser(this.myCefBrowser.getDevTools()).setClient(this.myCefClient).build();
                this.myDevtoolsFrame.add(devTools.getComponent(), "Center");
                Disposer.register(this, devTools);
                Disposer.register(this, new Disposable() {
                    public void dispose() {
                        MyJBCefBrowserBase.this.myDevtoolsFrame.dispose();
                    }
                });
                this.myDevtoolsFrame.setVisible(true);
            }
        }
    }
}
