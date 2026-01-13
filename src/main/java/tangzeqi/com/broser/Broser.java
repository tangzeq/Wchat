package tangzeqi.com.broser;

import com.intellij.ui.jcef.JBCefBrowser;

import javax.swing.*;

public abstract class Broser extends JPanel {
    public abstract void loadURL(String url);

}
