package tangzeqi.com.listener;

import com.intellij.ide.AppLifecycleListener;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;
import tangzeqi.com.service.ChatService;

public class IDEcloseListener implements AppLifecycleListener, ApplicationComponent {
    @Override
    public void appWillBeClosed(boolean isRestart) {
        // 处理关闭事件
        System.out.println("Wchat is shutDowning...");
        Thread.currentThread().interrupt();
        ChatService.shutDown();
        System.out.println("Wchat is shutDowned");
    }

    @Override
    public void appStarting(@Nullable Project projectFromCommandLine) {
        System.out.println("Wchat is online !");
    }
}
