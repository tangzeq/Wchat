package tangzeqi.com.listener;

import com.intellij.ide.AppLifecycleListener;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.util.IconUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.Nullable;
import tangzeqi.com.plugin.ChatPlugin;
import tangzeqi.com.service.ChatService;

public class IDEcloseListener implements AppLifecycleListener, ApplicationComponent {
    @Override
    public void appWillBeClosed(boolean isRestart) {
        // 处理关闭事件
        System.out.println("Wchat is shutDowning...");
        ChatService.shutDown();
        System.out.println("Wchat is shutDowned");
    }

    @Override
    public void appStarting(@Nullable Project project) {
        if(ObjectUtils.isNotEmpty(project)) {
            ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
            toolWindowManager.registerToolWindow(RegisterToolWindowTask.lazyAndClosable("Chat", ChatPlugin.getInstance(), IconUtil.getEditIcon()));
            ToolWindow chat = toolWindowManager.getToolWindow("Chat");
            if (ObjectUtils.isNotEmpty(chat)) {
                System.out.println("Wchat is loaded ! ");
                chat.show();
            }
        }
    }
}
