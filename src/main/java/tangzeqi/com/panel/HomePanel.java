package tangzeqi.com.panel;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.UIUtil;
import org.apache.batik.ext.swing.JGridBagPanel;
import org.apache.commons.lang3.ObjectUtils;
import tangzeqi.com.service.ChatService;
import tangzeqi.com.utils.NetUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import static tangzeqi.com.utils.PanelUtils.resetGBC;
import static tangzeqi.com.utils.PanelUtils.textLimit;

public class HomePanel extends JPanel {
    public HomePanel() {

    }

    public static Content content() {
        HomePanel homePanel = new HomePanel();
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        return contentFactory.createContent(homePanel, "home", false);
    }
}
