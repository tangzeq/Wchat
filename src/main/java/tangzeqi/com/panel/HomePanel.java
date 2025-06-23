package tangzeqi.com.panel;

import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

import javax.swing.*;

public class HomePanel extends JPanel {
    private final String project;

    public HomePanel(String project) {
        this.project = project;
    }

    public static Content content(String project) {
        HomePanel homePanel = new HomePanel(project);
        ContentFactory contentFactory = ContentFactory.getInstance();
        return contentFactory.createContent(homePanel, "home", false);
    }
}
