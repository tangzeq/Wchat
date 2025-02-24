package tangzeqi.com.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.util.IconUtil;
import org.apache.commons.lang3.ObjectUtils;
import tangzeqi.com.plugin.ChatPlugin;

public class ShowChatAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(e.getProject());
        ToolWindow chat = toolWindowManager.getToolWindow("Chat");
        if (ObjectUtils.isNotEmpty(chat)) chat.show();
        else
            toolWindowManager.registerToolWindow(RegisterToolWindowTask.lazyAndClosable("Chat", ChatPlugin.getInstance(), IconUtil.getEditIcon()));

    }
}
